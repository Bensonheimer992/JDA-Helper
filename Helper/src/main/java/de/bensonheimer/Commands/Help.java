package de.bensonheimer.Commands;

import de.bensonheimer.Command.ICommand;
import de.bensonheimer.Command.ICommandContext;
import de.bensonheimer.JDAHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import de.bensonheimer.Command.*;

import java.util.List;

public class Help implements ICommand {
    private final de.bensonheimer.Command.CommandManager manager;

    public Help(de.bensonheimer.Command.CommandManager commandManager) {
        this.manager = commandManager;
    }

    @Override
    public void handle(ICommandContext ctx) {
        StringBuilder response = new StringBuilder("\n");
        for (ICommand command : manager.getCommands()) {
            response.append(manager.getCommandAsMention(command))
                    .append(" - ")
                    .append(command.getHelp())
                    .append("\n");
        }
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(JDAHelper.EmbedColor);
        if (ctx.getEvent().getUserLocale().equals(DiscordLocale.GERMAN)) {
            embedBuilder.setTitle("**Verfügbare Befehle:**");
        } else if (ctx.getEvent().getUserLocale().equals(DiscordLocale.ENGLISH_US) || ctx.getEvent().getUserLocale().equals(DiscordLocale.ENGLISH_UK)) {
            embedBuilder.setTitle("**Available Commands:**");
        }
        embedBuilder.setDescription(response.toString());

        ctx.getEvent().replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Zeige die Help Liste an";
    }

    @Override
    public String getHelp() {
        return "Zeigt diese Liste an";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }
}
