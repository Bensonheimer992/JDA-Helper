package de.bensonheimer.Audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import de.bensonheimer.JDAHelper;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.AndroidTestsuite;
import dev.lavalink.youtube.clients.Music;
import dev.lavalink.youtube.clients.Web;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PlayerManager {
    private static PlayerManager INSTANCE;
    private final Map<Long, GuildMusicManager> guildMusicManagers;
    private final AudioPlayerManager audioPlayerManager;
    private final YoutubeAudioSourceManager youtubeAudioSourceManager;
    private final ScheduledExecutorService scheduler;
    private final Map<Long, ScheduledFuture<?>> disconnectTasks;

    public PlayerManager() {
        this.guildMusicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();
        this.youtubeAudioSourceManager = new YoutubeAudioSourceManager(true, new Music(), new Web(), new AndroidTestsuite());
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.disconnectTasks = new HashMap<>();

        audioPlayerManager.registerSourceManager(youtubeAudioSourceManager);
        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
    }

    public static PlayerManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PlayerManager();
        }
        return INSTANCE;
    }

    public GuildMusicManager getGuildMusicManager(Guild guild) {
        return this.guildMusicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager);
            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
            guildMusicManager.scheduler.setDisconnectCallback(() -> scheduleDisconnect(guild));
            return guildMusicManager;
        });
    }

    public void scheduleDisconnect(Guild guild) {
        cancelDisconnectTask(guild);
        ScheduledFuture<?> task = scheduler.schedule(() -> {
            GuildMusicManager musicManager = getGuildMusicManager(guild);
            if (musicManager.scheduler.queue.isEmpty() && musicManager.player.getPlayingTrack() == null) {
                guild.getAudioManager().closeAudioConnection();
            }
        }, 2, TimeUnit.SECONDS);
        disconnectTasks.put(guild.getIdLong(), task);
    }

    public void cancelDisconnectTask(Guild guild) {
        ScheduledFuture<?> task = disconnectTasks.remove(guild.getIdLong());
        if (task != null) {
            task.cancel(false);
        }
    }

    public void play(Guild guild, SlashCommandInteractionEvent event, String trackUrl) {
        final GuildMusicManager guildMusicManager = this.getGuildMusicManager(guild);

        this.audioPlayerManager.loadItemOrdered(guildMusicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                guildMusicManager.scheduler.queue(audioTrack);
                cancelDisconnectTask(guild);

                EmbedBuilder embedBuilder = new EmbedBuilder();
                if (audioTrack.getInfo().uri.startsWith("https://www.youtube.com")) {
                    sendEmbed(embedBuilder, audioTrack.getInfo().title, audioTrack.getInfo().uri, "https://img.youtube.com/vi/" + extractYouTubeVideoId(audioTrack.getInfo().uri) + "/maxresdefault.jpg", audioTrack.getInfo().author);
                } else {
                    sendEmbed(embedBuilder, audioTrack.getInfo().title, audioTrack.getInfo().uri, audioTrack.getInfo().artworkUrl, audioTrack.getInfo().author);
                }

                event.getHook().sendMessageEmbeds(embedBuilder.build()).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                final List<AudioTrack> tracks = audioPlaylist.getTracks();
                if (trackUrl.startsWith("ytsearch:")) {
                    if (!tracks.isEmpty()) {
                        AudioTrack firstTrack = tracks.get(0);
                        guildMusicManager.scheduler.queue(firstTrack);
                        cancelDisconnectTask(guild);

                        EmbedBuilder embedBuilder = new EmbedBuilder();
                        sendEmbed(embedBuilder, firstTrack.getInfo().title, firstTrack.getInfo().uri, "https://img.youtube.com/vi/" + extractYouTubeVideoId(firstTrack.getInfo().uri) + "/maxresdefault.jpg", firstTrack.getInfo().author);

                        event.getHook().sendMessageEmbeds(embedBuilder.build()).queue();
                    }
                } else {
                    AudioTrack firstTrack = audioPlaylist.getTracks().get(0);
                    AudioTrackInfo trackInfo = firstTrack.getInfo();

                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setColor(JDAHelper.EmbedColor);
                    sendEmbed(embedBuilder, audioPlaylist.getName(), trackInfo.uri, "https://img.youtube.com/vi/" + extractYouTubeVideoId(trackInfo.uri) + "/maxresdefault.jpg", "Playlist mit " + audioPlaylist.getTracks().size() + " Tracks geladen");

                    event.getHook().sendMessageEmbeds(embedBuilder.build()).queue();

                    for (final AudioTrack track : tracks) {
                        guildMusicManager.scheduler.queue(track);
                    }
                    cancelDisconnectTask(guild);
                }
            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException e) {

            }
        });
    }

    public void sendEmbed(EmbedBuilder embedBuilder, String title, String url, String img, String footer) {
        embedBuilder.setColor(JDAHelper.EmbedColor);
        embedBuilder.setTitle(title, url);
        embedBuilder.setImage(img);
        embedBuilder.setFooter(footer);
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
