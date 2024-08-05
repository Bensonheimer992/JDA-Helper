package de.bensonheimer.Command;

import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.List;

public interface ICommand {
    void handle(ICommandContext ctx);
    String getName();
    String getDescription();
    String getHelp();
    List<OptionData> getOptions();
}
