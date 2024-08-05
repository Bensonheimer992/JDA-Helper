package de.bensonheimer.Audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import de.bensonheimer.Command.ICommandContext;
import de.bensonheimer.JDAHelper;
import de.bensonheimer.Utils.Localization;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

public class MusicManager {
    private static final int MIN_VOLUME = 0;
    private static final int MAX_VOLUME = 100;

    public void play(ICommandContext commandContext, String trackUrl) {
        SlashCommandInteractionEvent event = commandContext.getEvent();
        Member member = event.getMember();
        Member self = event.getGuild().getSelfMember();
        GuildVoiceState memberVoiceState = member.getVoiceState();
        GuildVoiceState selfVoiceState = self.getVoiceState();

        if (memberVoiceState == null || !memberVoiceState.inAudioChannel()) {
            event.getHook().sendMessage(Localization.getInstance().getLocalizedMessage(event, "notInVoiceChannel")).setEphemeral(true).queue();
            return;
        }

        if (selfVoiceState == null || !selfVoiceState.inAudioChannel()) {
            event.getGuild().getAudioManager().openAudioConnection(memberVoiceState.getChannel());
        } else if (selfVoiceState.getChannel() != memberVoiceState.getChannel()) {
            event.getHook().sendMessage(Localization.getInstance().getLocalizedMessage(event, "notInSameVoiceChannel")).setEphemeral(true).queue();
            return;
        }

        String link = trackUrl.trim();

        if (!isUrl(link)) {
            link = "ytsearch: " + link;
        }

        PlayerManager playerManager = PlayerManager.getInstance();
        playerManager.play(commandContext.getGuild(), link).thenAccept(track -> {
            AudioTrackInfo info = track.getInfo();

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(JDAHelper.EmbedColor);
            embedBuilder.setTitle(info.title, info.uri);
            embedBuilder.addField(Localization.getInstance().getLocalizedMessage(event, "trackAuthor"), info.author, true);
            embedBuilder.addField(Localization.getInstance().getLocalizedMessage(event, "trackDuration"), formatTime(info.length), true);

            if (info.uri.contains("youtube.com")) {
                embedBuilder.setImage("https://img.youtube.com/vi/" + extractYouTubeVideoId(info.uri) + "/maxresdefault.jpg");
                embedBuilder.addField("Provider", "<:youtube:1264203893693222952> | Youtube", true);
            } else if (info.uri.contains("spotify.com")) {
                embedBuilder.setImage(info.artworkUrl);
                embedBuilder.addField("Provider", "<:spotify:1264203805491204206> | Spotify", true);
            }

            event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
        }).exceptionally(throwable -> {
            JDAHelper.getLogger().error(throwable.getLocalizedMessage());
            return null;
        });
    }

    public void stop(ICommandContext commandContext) {
        SlashCommandInteractionEvent event = commandContext.getEvent();
        event.deferReply().queue();
        Member member = event.getMember();
        Member self = event.getGuild().getSelfMember();
        GuildVoiceState memberVoiceState = member.getVoiceState();
        GuildVoiceState selfVoiceState = self.getVoiceState();

        if (memberVoiceState == null || !memberVoiceState.inAudioChannel()) {
            event.getHook().sendMessage(Localization.getInstance().getLocalizedMessage(event, "notInVoiceChannel")).setEphemeral(true).queue();
            return;
        }

        if (selfVoiceState == null || !selfVoiceState.inAudioChannel()) {
            event.getHook().sendMessage(Localization.getInstance().getLocalizedMessage(event, "botNotInVoiceChannel")).setEphemeral(true).queue();
            return;
        }

        if (selfVoiceState.getChannel() != memberVoiceState.getChannel()) {
            event.getHook().sendMessage(Localization.getInstance().getLocalizedMessage(event, "notInSameVoiceChannel")).setEphemeral(true).queue();
            return;
        }

        final GuildMusicManager guildMusicManager = PlayerManager.getInstance().getGuildMusicManager(event.getGuild());
        guildMusicManager.scheduler.player.stopTrack();
        guildMusicManager.scheduler.queue.clear();
        event.getGuild().getAudioManager().closeAudioConnection();
        event.getHook().sendMessage(Localization.getInstance().getLocalizedMessage(event, "stopPlayingMusic")).setEphemeral(true).queue();
    }

    public void volume(ICommandContext commandContext, Integer volume) {
        SlashCommandInteractionEvent event = commandContext.getEvent();

        if (volume == null) {
            event.getHook().sendMessage(Localization.getInstance().getLocalizedMessage(event, "invalidVolume")).setEphemeral(true).queue();
            return;
        }

        if (volume < MIN_VOLUME || volume > MAX_VOLUME) {
            event.getHook().sendMessage(Localization.getInstance().getLocalizedMessage(event, "volumeOutOfRange")).setEphemeral(true).queue();
            return;
        }

        Member member = event.getMember();
        Member self = event.getGuild().getSelfMember();
        GuildVoiceState memberVoiceState = member.getVoiceState();
        GuildVoiceState selfVoiceState = self.getVoiceState();

        if (memberVoiceState == null || !memberVoiceState.inAudioChannel()) {
            event.getHook().sendMessage(Localization.getInstance().getLocalizedMessage(event, "notInVoiceChannel")).setEphemeral(true).queue();
            return;
        }

        if (selfVoiceState == null || !selfVoiceState.inAudioChannel()) {
            event.getHook().sendMessage(Localization.getInstance().getLocalizedMessage(event, "botNotInVoiceChannel")).setEphemeral(true).queue();
            return;
        }

        if (selfVoiceState.getChannel() != memberVoiceState.getChannel()) {
            event.getHook().sendMessage(Localization.getInstance().getLocalizedMessage(event, "notInSameVoiceChannel")).setEphemeral(true).queue();
            return;
        }

        final GuildMusicManager guildMusicManager = PlayerManager.getInstance().getGuildMusicManager(event.getGuild());
        guildMusicManager.player.setVolume(volume);
        event.reply(String.format(Localization.getInstance().getLocalizedMessage(event, "volumeSet"), volume)).setEphemeral(true).queue();
    }

    public void skip(ICommandContext commandContext) {
        SlashCommandInteractionEvent event = commandContext.getEvent();
        Member member = event.getMember();
        Member self = event.getGuild().getSelfMember();
        GuildVoiceState memberVoiceState = member.getVoiceState();
        GuildVoiceState selfVoiceState = self.getVoiceState();

        if (memberVoiceState == null || !memberVoiceState.inAudioChannel()) {
            event.getHook().sendMessage(Localization.getInstance().getLocalizedMessage(event, "notInVoiceChannel")).setEphemeral(true).queue();
            return;
        }

        if (selfVoiceState == null || !selfVoiceState.inAudioChannel()) {
            event.getHook().sendMessage(Localization.getInstance().getLocalizedMessage(event, "botNotInVoiceChannel")).setEphemeral(true).queue();
            return;
        }

        if (selfVoiceState.getChannel() != memberVoiceState.getChannel()) {
            event.getHook().sendMessage(Localization.getInstance().getLocalizedMessage(event, "notInSameVoiceChannel")).setEphemeral(true).queue();
            return;
        }

        final GuildMusicManager guildMusicManager = PlayerManager.getInstance().getGuildMusicManager(event.getGuild());
        guildMusicManager.scheduler.nextTrack();
        event.reply(Localization.getInstance().getLocalizedMessage(event, "skippedTrack")).setEphemeral(true).queue();
    }

    public void nowPlaying(ICommandContext commandContext) {
        SlashCommandInteractionEvent event = commandContext.getEvent();

        final GuildMusicManager guildMusicManager = PlayerManager.getInstance().getGuildMusicManager(event.getGuild());
        final AudioTrack track = guildMusicManager.player.getPlayingTrack();

        if (track == null) {
            event.getHook().sendMessage(Localization.getInstance().getLocalizedMessage(event, "noTrackPlaying")).setEphemeral(true).queue();
            return;
        }

        AudioTrackInfo info = track.getInfo();
        long currentPosition = track.getPosition();
        long totalDuration = track.getDuration();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(JDAHelper.EmbedColor);
        embed.setTitle(Localization.getInstance().getLocalizedMessage(event, "nowPlayingTitle"));
        embed.addField(Localization.getInstance().getLocalizedMessage(event, "trackTitle"), info.title, false);
        embed.addField(Localization.getInstance().getLocalizedMessage(event, "trackAuthor"), info.author, false);
        embed.addField(Localization.getInstance().getLocalizedMessage(event, "trackPosition"), formatTime(currentPosition), true);
        embed.addField(Localization.getInstance().getLocalizedMessage(event, "trackDuration"), formatTime(totalDuration), true);

        int totalBlocks = 20;
        int filledBlocks = (int) ((double) currentPosition / totalDuration * totalBlocks);
        StringBuilder progressBar = new StringBuilder();
        for (int i = 0; i < totalBlocks; i++) {
            if (i < filledBlocks) {
                progressBar.append("█");
            } else {
                progressBar.append("░");
            }
        }
        embed.addField("Progress", progressBar.toString(), false);

        if (info.uri != null && !info.uri.isEmpty()) {
            embed.addField("URL", info.uri, false);
        }
        if (info.uri.contains("youtube.com") || info.uri.contains("youtu.be")) {
            embed.setThumbnail("https://img.youtube.com/vi/" + extractYouTubeVideoId(info.uri) + "/maxresdefault.jpg");
        }

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    private String formatTime(long timeInMillis) {
        long hours = timeInMillis / TimeUnit.HOURS.toMillis(1);
        long minutes = (timeInMillis % TimeUnit.HOURS.toMillis(1)) / TimeUnit.MINUTES.toMillis(1);
        long seconds = (timeInMillis % TimeUnit.MINUTES.toMillis(1)) / TimeUnit.SECONDS.toMillis(1);

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    private boolean isUrl(String url) {
        try {
            new URI(url);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public String extractYouTubeVideoId(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }

        String videoId = null;

        if (url.contains("youtu.be/")) {
            videoId = url.substring(url.lastIndexOf("/") + 1);
        } else if (url.contains("youtube.com/watch?v=")) {
            videoId = url.split("v=")[1];
            int ampersandPosition = videoId.indexOf('&');
            if (ampersandPosition != -1) {
                videoId = videoId.substring(0, ampersandPosition);
            }
        }

        return videoId;
    }
}