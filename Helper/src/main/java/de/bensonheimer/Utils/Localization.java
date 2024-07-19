package de.bensonheimer.Utils;

import de.bensonheimer.Audio.PlayerManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.util.HashMap;
import java.util.Map;

public class Localization {
    private static Localization INSTANCE;

    public static Localization getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Localization();
        }
        return INSTANCE;
    }

    private static final Map<String, Map<DiscordLocale, String>> messages = new HashMap<>();

    static {
        initializeMessages();
    }

    private static void initializeMessages() {
        Map<DiscordLocale, String> notInVoiceChannel = new HashMap<>();
        notInVoiceChannel.put(DiscordLocale.GERMAN, "Du bist in keinem Voice Channel");
        notInVoiceChannel.put(DiscordLocale.ENGLISH_US, "You are not in a voice channel");
        messages.put("notInVoiceChannel", notInVoiceChannel);

        Map<DiscordLocale, String> botNotInVoiceChannel = new HashMap<>();
        botNotInVoiceChannel.put(DiscordLocale.GERMAN, "Ich bin derzeit nicht in einem Voice Channel");
        botNotInVoiceChannel.put(DiscordLocale.ENGLISH_US, "I am currently not in a voice channel");
        messages.put("botNotInVoiceChannel", botNotInVoiceChannel);

        Map<DiscordLocale, String> notInSameVoiceChannel = new HashMap<>();
        notInSameVoiceChannel.put(DiscordLocale.GERMAN, "Du musst im gleichen Voice Channel sein wie ich");
        notInSameVoiceChannel.put(DiscordLocale.ENGLISH_US, "You must be in the same voice channel as me");
        messages.put("notInSameVoiceChannel", notInSameVoiceChannel);

        Map<DiscordLocale, String> stopPlayingMusic = new HashMap<>();
        stopPlayingMusic.put(DiscordLocale.GERMAN, "Ich spiele nun keine Musik mehr");
        stopPlayingMusic.put(DiscordLocale.ENGLISH_US, "I am no longer playing music");
        messages.put("stopPlayingMusic", stopPlayingMusic);

        Map<DiscordLocale, String> invalidVolume = new HashMap<>();
        invalidVolume.put(DiscordLocale.GERMAN, "Bitte gib eine gültige Lautstärke an");
        invalidVolume.put(DiscordLocale.ENGLISH_US, "Please provide a valid volume");
        messages.put("invalidVolume", invalidVolume);

        Map<DiscordLocale, String> volumeOutOfRange = new HashMap<>();
        volumeOutOfRange.put(DiscordLocale.GERMAN, "Die Lautstärke muss zwischen 1 und 100 liegen");
        volumeOutOfRange.put(DiscordLocale.ENGLISH_US, "The volume must be between 1 and 100");
        messages.put("volumeOutOfRange", volumeOutOfRange);

        Map<DiscordLocale, String> volumeSet = new HashMap<>();
        volumeSet.put(DiscordLocale.GERMAN, "Lautstärke auf %d%% gesetzt");
        volumeSet.put(DiscordLocale.ENGLISH_US, "Volume set to %d%%");
        messages.put("volumeSet", volumeSet);

        Map<DiscordLocale, String> skippedTrack = new HashMap<>();
        skippedTrack.put(DiscordLocale.GERMAN, "Der aktuelle Track wurde übersprungen");
        skippedTrack.put(DiscordLocale.ENGLISH_US, "Skipped the current track");
        messages.put("skippedTrack", skippedTrack);

        Map<DiscordLocale, String> noTrackPlaying = new HashMap<>();
        noTrackPlaying.put(DiscordLocale.GERMAN, "Es wird derzeit kein Track abgespielt");
        noTrackPlaying.put(DiscordLocale.ENGLISH_US, "No track is currently playing");
        messages.put("noTrackPlaying", noTrackPlaying);

        Map<DiscordLocale, String> nowPlayingTitle = new HashMap<>();
        nowPlayingTitle.put(DiscordLocale.GERMAN, "Aktuell wird abgespielt");
        nowPlayingTitle.put(DiscordLocale.ENGLISH_US, "Now Playing");
        messages.put("nowPlayingTitle", nowPlayingTitle);

        Map<DiscordLocale, String> trackTitle = new HashMap<>();
        trackTitle.put(DiscordLocale.GERMAN, "Titel");
        trackTitle.put(DiscordLocale.ENGLISH_US, "Title");
        messages.put("trackTitle", trackTitle);

        Map<DiscordLocale, String> trackAuthor = new HashMap<>();
        trackAuthor.put(DiscordLocale.GERMAN, "Autor");
        trackAuthor.put(DiscordLocale.ENGLISH_US, "Author");
        messages.put("trackAuthor", trackAuthor);

        Map<DiscordLocale, String> trackDuration = new HashMap<>();
        trackDuration.put(DiscordLocale.GERMAN, "Dauer");
        trackDuration.put(DiscordLocale.ENGLISH_US, "Duration");
        messages.put("trackDuration", trackDuration);

        Map<DiscordLocale, String> trackPosition = new HashMap<>();
        trackPosition.put(DiscordLocale.GERMAN, "Aktuelle Position");
        trackPosition.put(DiscordLocale.ENGLISH_US, "Current Position");
        messages.put("trackPosition", trackPosition);
    }

    public String getLocalizedMessage(SlashCommandInteractionEvent event, String messageKey) {
        DiscordLocale locale = event.getUserLocale();
        if (locale != DiscordLocale.GERMAN) {
            locale = DiscordLocale.ENGLISH_US; // Default to English for any non-German locale
        }
        return messages.get(messageKey).getOrDefault(locale, messages.get(messageKey).get(DiscordLocale.ENGLISH_US));
    }
}
