package com.bgsoftware.wildtools.command.commands;

import com.bgsoftware.wildtools.command.ICommand;
import com.bgsoftware.wildtools.Locale;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.handlers.DataHandler;

import com.bgsoftware.wildtools.handlers.ProvidersHandler;
import com.bgsoftware.wildtools.utils.Executor;
import org.bukkit.command.CommandSender;
import com.bgsoftware.wildtools.handlers.ToolsHandler;

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
        Executor.async(() -> {
            WildToolsPlugin.log("******** RELOAD START ********");
            ToolsHandler.reload();
            DataHandler.reload();
            plugin.getEditor().reloadConfiguration();
            Locale.reload();
            ProvidersHandler.reload();
            plugin.getEvents().loadNotifiedForTools();
            WildToolsPlugin.log("******** RELOAD DONE ********");
            Locale.RELOAD_SUCCESS.send(sender);
        });
    }

    @Override
    public List<String> tabComplete(WildToolsPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

}
