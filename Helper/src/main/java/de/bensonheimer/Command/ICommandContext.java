package de.bensonheimer.Command;

import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

public interface ICommandContext {
    default Guild getGuild() {
        return this.getEvent().getGuild();
    }

    default boolean isFromGuild() {
        return this.getEvent().isFromGuild();
    }

    SlashCommandInteractionEvent getEvent();

    default MessageChannelUnion getChannel() {
        return this.getEvent().getChannel();
    }

    default SlashCommandInteraction getInteraction() {
        return this.getEvent().getInteraction();
    }

    default User getUser() {
        return this.getEvent().getUser();
    }

    default Member getMember() {
        return this.getEvent().getMember();
    }

    default JDA getJDA() {
        return this.getEvent().getJDA();
    }

    default ShardManager getShardManager() {
        return this.getJDA().getShardManager();
    }

    default Member getSelfMember() {
        return this.getGuild().getSelfMember();
    }
}