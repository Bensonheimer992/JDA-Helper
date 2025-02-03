package de.bensonheimer.Audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.Android;
import dev.lavalink.youtube.clients.Music;
import dev.lavalink.youtube.clients.Web;
import net.dv8tion.jda.api.entities.Guild;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

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
        this.youtubeAudioSourceManager = new YoutubeAudioSourceManager(true, new Music(), new Web(), new Android());
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

    public CompletableFuture<AudioTrack> play(Guild guild, String trackUrl) {
        final GuildMusicManager guildMusicManager = this.getGuildMusicManager(guild);
        CompletableFuture<AudioTrack> future = new CompletableFuture<>();

        this.audioPlayerManager.loadItemOrdered(guildMusicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                guildMusicManager.scheduler.queue(audioTrack);
                cancelDisconnectTask(guild);
                future.complete(audioTrack);
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                final List<AudioTrack> tracks = audioPlaylist.getTracks();
                if (!tracks.isEmpty()) {
                    AudioTrack firstTrack = tracks.get(0);
                    if (trackUrl.startsWith("ytsearch:")) {
                        guildMusicManager.scheduler.queue(firstTrack);
                        future.complete(firstTrack);
                    } else {
                        for (final AudioTrack track : tracks) {
                            guildMusicManager.scheduler.queue(track);
                        }
                        future.complete(firstTrack);
                    }
                    cancelDisconnectTask(guild);
                } else {
                    future.complete(null);
                }
            }

            @Override
            public void noMatches() {
                future.complete(null);
            }

            @Override
            public void loadFailed(FriendlyException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }
}
