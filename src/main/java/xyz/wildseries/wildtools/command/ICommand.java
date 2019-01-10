package xyz.wildseries.wildtools.command;

import org.bukkit.command.CommandSender;
import xyz.wildseries.wildtools.WildToolsPlugin;

import java.util.List;

public interface ICommand {

    String getLabel();

    String getUsage();

    String getPermission();

    String getDescription();

    int getMinArgs();

    int getMaxArgs();

    void run(WildToolsPlugin plugin, CommandSender sender, String[] args);

    List<String> tabComplete(WildToolsPlugin plugin, CommandSender sender, String[] args);

}
