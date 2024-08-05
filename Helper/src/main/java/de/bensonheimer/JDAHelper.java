package de.bensonheimer;

import de.bensonheimer.Audio.MusicManager;
import de.bensonheimer.Command.ICommand;
import de.bensonheimer.Command.CommandManager;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static spark.Spark.port;

public class JDAHelper {
    @Getter
    static Logger logger = LoggerFactory.getLogger(JDAHelper.class);;
    @Getter
    private static final MusicManager musicManager = new MusicManager();
    private static final CommandManager commandManager = new CommandManager();
    private static final List<ListenerAdapter> pendingListeners = new ArrayList<>();
    private static JDA bot;
    public static Color EmbedColor;
    private static final String TRANSCRIPT_FOLDER = "transcript";

    public static void createBot(String token, OnlineStatus status, Activity.ActivityType activityType, String activityString, Color embedColor) {
        EmbedColor = embedColor;
        JDABuilder builder = JDABuilder.createDefault(token);
        builder.setStatus(status);
        if (activityType.equals(Activity.ActivityType.PLAYING)) {
            builder.setActivity(Activity.playing(activityString));
        } else if (activityType.equals(Activity.ActivityType.LISTENING)) {
            builder.setActivity(Activity.listening(activityString));
        } else if (activityType.equals(Activity.ActivityType.WATCHING)) {
            builder.setActivity(Activity.watching(activityString));
        }

        builder.setChunkingFilter(ChunkingFilter.ALL);
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.DIRECT_MESSAGE_TYPING, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.MESSAGE_CONTENT);
        EnumSet<CacheFlag> enumSet = EnumSet.of(CacheFlag.ONLINE_STATUS, CacheFlag.CLIENT_STATUS, CacheFlag.EMOJI, CacheFlag.VOICE_STATE);
        builder.enableCache(enumSet);

        bot = builder.build();
        commandManager.registerCommands(bot);
        bot.addEventListener(commandManager);

        for (ListenerAdapter listener : pendingListeners) {
            bot.addEventListener(listener);
        }
        pendingListeners.clear();
    }

    public static void createBot(String token, OnlineStatus status, Activity.ActivityType activityType, String activityString, String streamUrl, Color embedColor) {
        EmbedColor = embedColor;
        JDABuilder builder = JDABuilder.createDefault(token);
        builder.setStatus(status);
        if (activityType.equals(Activity.ActivityType.PLAYING)) {
            builder.setActivity(Activity.playing(activityString));
        } else if (activityType.equals(Activity.ActivityType.LISTENING)) {
            builder.setActivity(Activity.listening(activityString));
        } else if (activityType.equals(Activity.ActivityType.WATCHING)) {
            builder.setActivity(Activity.watching(activityString));
        } else if (activityType.equals(Activity.ActivityType.STREAMING)) {
            builder.setActivity(Activity.streaming(activityString, streamUrl));
        }

        builder.setChunkingFilter(ChunkingFilter.ALL);
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.DIRECT_MESSAGE_TYPING, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.MESSAGE_CONTENT);
        EnumSet<CacheFlag> enumSet = EnumSet.of(CacheFlag.ONLINE_STATUS, CacheFlag.CLIENT_STATUS, CacheFlag.EMOJI, CacheFlag.VOICE_STATE);
        builder.enableCache(enumSet);

        bot = builder.build();
        commandManager.registerCommands(bot);
        bot.addEventListener(commandManager);

        for (ListenerAdapter listener : pendingListeners) {
            bot.addEventListener(listener);
        }
        pendingListeners.clear();
    }

    public static void addCommand(ICommand command) {
        commandManager.addCommand(command);
    }

    public static void registerEvent(ListenerAdapter listenerAdapter) {
        if (bot != null) {
            bot.addEventListener(listenerAdapter);
        } else {
            pendingListeners.add(listenerAdapter);
        }
    }

    public static Guild guildFromID(String id) throws InterruptedException {
        Guild guild = bot.awaitReady().getGuildById(id);
        return guild;
    }

//    public static void useTicketSystem(Guild guild, Integer categoryId, Integer transcriptServerPort) {
//        port(transcriptServerPort);
//        createTranscriptDirectory();
//    }

//        private static void createTranscriptDirectory() {
//        Path transcriptPath = Path.of(TRANSCRIPT_FOLDER);
//
//        try {
//            if (!Files.exists(transcriptPath)) {
//                Files.createDirectory(transcriptPath);
//                logger.info("Creating Transcript Folder");
//            }
//        } catch (IOException e) {
//            logger.error(e.getLocalizedMessage());
//        }
//    }
}