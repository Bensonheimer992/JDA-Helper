package de.bensonheimer.Command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.internal.utils.Checks;

public class DefaultCommandContext implements ICommandContext {
    private final SlashCommandInteractionEvent event;

    public DefaultCommandContext(SlashCommandInteractionEvent event) {
        Checks.notNull(event, "event");
        this.event = event;
    }

    @Override
    public SlashCommandInteractionEvent getEvent() {
        return this.event;
    }
}