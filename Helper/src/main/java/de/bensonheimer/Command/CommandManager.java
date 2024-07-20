package de.bensonheimer.Command;

import de.bensonheimer.Commands.Help;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class CommandManager extends ListenerAdapter {
    @Getter
    private final List<ICommand> commands = new ArrayList<>();
    private final Map<String, String> commandIds = new HashMap<>();

    public CommandManager() {
        addCommand(new Help(this));
    }

    public void addCommand(ICommand command) {
        boolean nameFound = this.commands.stream().anyMatch((it) -> it.getName().equalsIgnoreCase(command.getName()));

        if (nameFound) {
            throw new IllegalArgumentException("Ein Befehl mit diesem Namen existiert bereits");
        }

        commands.add(command);
    }

    @Nullable
    public ICommand getCommand(String search) {
        String searchLower = search.toLowerCase();
        for (ICommand cmd : this.commands) {
            if (cmd.getName().equals(searchLower)) {
                return cmd;
            }
        }
        return null;
    }

    private void handleSlashCommand(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        ICommand command = this.getCommand(commandName);

        if (command != null) {
            CommandContext ctx = new CommandContext(event);
            command.handle(ctx);
        } else {
            event.reply("Ich kann diesen Befehl gerade nicht verarbeiten :(").setEphemeral(true).queue();
        }
    }

    public void registerCommands(JDA jda) {
        List<CommandData> commandData = commands.stream()
                .map(cmd -> Commands.slash(cmd.getName(), cmd.getDescription())
                        .addOptions(cmd.getOptions()))
                .collect(Collectors.toList());
        jda.updateCommands().addCommands(commandData).queue(
                success -> {
                    for (Command command : success) {
                        commandIds.put(command.getName(), command.getId());
                    }
                }
        );
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        handleSlashCommand(event);
    }

    public String getHelp(ICommand command) {
        return command.getHelp();
    }

    public String getCommandAsMention(ICommand command) {
        return "</" + command.getName() + ":" + commandIds.get(command.getName()) + ">";
    }
}