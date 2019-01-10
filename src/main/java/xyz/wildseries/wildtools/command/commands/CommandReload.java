package xyz.wildseries.wildtools.command.commands;

import xyz.wildseries.wildtools.Locale;
import xyz.wildseries.wildtools.WildToolsPlugin;
import xyz.wildseries.wildtools.command.ICommand;
import xyz.wildseries.wildtools.handlers.DataHandler;

import org.bukkit.command.CommandSender;
import xyz.wildseries.wildtools.handlers.ToolsHandler;

import java.util.ArrayList;
import java.util.List;

public final class CommandReload implements ICommand {

    @Override
    public String getLabel() {
        return "reload";
    }

    @Override
    public String getUsage() {
        return "tools reload";
    }

    @Override
    public String getPermission() {
        return "wildtools.reload";
    }

    @Override
    public String getDescription() {
        return "Reload the settings and the language files.";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 1;
    }

    @Override
    public void run(WildToolsPlugin plugin, CommandSender sender, String[] args) {
        new Thread(() -> {
            WildToolsPlugin.log("******** RELOAD START ********");
            ToolsHandler.reload();
            DataHandler.reload();
            plugin.getEditor().reloadConfiguration();
            Locale.reload();
            WildToolsPlugin.log("******** RELOAD DONE ********");
            Locale.RELOAD_SUCCESS.send(sender);
        }).start();
    }

    @Override
    public List<String> tabComplete(WildToolsPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

}
