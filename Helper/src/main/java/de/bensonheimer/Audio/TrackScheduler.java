package de.bensonheimer.Audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lombok.Getter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {
    @Getter
    public final AudioPlayer player;
    @Getter
    public final BlockingQueue<AudioTrack> queue;
    private Runnable disconnectCallback;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }


    public void nextTrack() {
        this.player.startTrack(this.queue.poll(), false);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            nextTrack();
        }

        if (player.getPlayingTrack() == null && queue.isEmpty()) {
            if (disconnectCallback != null) {
                disconnectCallback.run();
            }
        }
    }

    public void queue(AudioTrack track) {
        if (!this.player.startTrack(track, true)) {
            this.queue.offer(track);
        }
    }

    public boolean hasNextTrack() {
        return !queue.isEmpty();
    }

    public void setDisconnectCallback(Runnable callback) {
        this.disconnectCallback = callback;
    }
}
