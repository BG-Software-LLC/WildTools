package com.bgsoftware.wildtools.command;

import com.bgsoftware.wildtools.command.commands.CommandGive;
import com.bgsoftware.wildtools.Locale;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.command.commands.CommandInfo;
import com.bgsoftware.wildtools.command.commands.CommandList;
import com.bgsoftware.wildtools.command.commands.CommandReload;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import com.bgsoftware.wildtools.command.commands.CommandSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandsHandler implements CommandExecutor, TabCompleter {

    private final WildToolsPlugin plugin;
    private final List<ICommand> commands;

    public CommandsHandler(WildToolsPlugin plugin){
        this.plugin = plugin;
        commands = new ArrayList<>();
        commands.add(new CommandGive());
        commands.add(new CommandInfo());
        commands.add(new CommandList());
        commands.add(new CommandReload());
        commands.add(new CommandSettings());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            for (ICommand cmd : this.commands) {
                if (cmd.getLabel().equalsIgnoreCase(args[0])) {
                    if (cmd.getPermission() != null && !sender.hasPermission(cmd.getPermission())) {
                        Locale.NO_PERMISSION.send(sender);
                        return false;
                    }

                    if (args.length < cmd.getMinArgs() || args.length > cmd.getMaxArgs()) {
                        Locale.COMMAND_USAGE.send(sender, cmd.getUsage());
                        return false;
                    }

                    cmd.run(this.plugin, sender, args);
                    return true;
                }
            }
        }

        //Checking that the player has permission to use at least one of the commands.
        for (ICommand subCommand : this.commands) {
            if (sender.hasPermission(subCommand.getPermission())) {
                //Player has permission
                Locale.HELP_COMMAND_HEADER.send(sender);

                for (ICommand cmd : this.commands) {
                    if (sender.hasPermission(subCommand.getPermission())) {
                        Locale.HELP_COMMAND_LINE.send(sender, cmd.getUsage(), cmd.getDescription());
                    }
                }

                Locale.HELP_COMMAND_FOOTER.send(sender);
                return false;
            }
        }

        Locale.NO_PERMISSION.send(sender);
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            for (ICommand cmd : this.commands) {
                if (cmd.getLabel().equalsIgnoreCase(args[0])) {
                    if (cmd.getPermission() != null && !sender.hasPermission(cmd.getPermission())) {
                        return Collections.emptyList();
                    }

                    return cmd.tabComplete(this.plugin, sender, args);
                }
            }
        }

        List<String> list = new ArrayList<>();

        for (ICommand cmd : this.commands) {
            if (cmd.getPermission() == null || sender.hasPermission(cmd.getPermission())) {
                if (cmd.getLabel().startsWith(args[0])) {
                    list.add(cmd.getLabel());
                }
            }
        }

        return list;
    }

}
