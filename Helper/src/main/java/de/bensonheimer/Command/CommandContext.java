package de.bensonheimer.Command;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class CommandContext implements ICommandContext {
    private final SlashCommandInteractionEvent event;

    public CommandContext(SlashCommandInteractionEvent event) {
        this.event = event;
    }

    @Override
    public Guild getGuild() {
        return this.getEvent().getGuild();
    }

    @Override
    public SlashCommandInteractionEvent getEvent() {
        return this.event;
    }
}