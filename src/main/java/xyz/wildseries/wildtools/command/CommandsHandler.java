package xyz.wildseries.wildtools.command;

import xyz.wildseries.wildtools.Locale;
import xyz.wildseries.wildtools.WildToolsPlugin;
import xyz.wildseries.wildtools.command.commands.CommandGive;
import xyz.wildseries.wildtools.command.commands.CommandInfo;
import xyz.wildseries.wildtools.command.commands.CommandReload;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import xyz.wildseries.wildtools.command.commands.CommandSettings;

import java.util.ArrayList;
import java.util.List;

public final class CommandsHandler implements CommandExecutor, TabCompleter {

    private WildToolsPlugin plugin;
    private List<ICommand> cmds;

    public CommandsHandler(WildToolsPlugin plugin){
        this.plugin = plugin;
        cmds = new ArrayList<>();
        cmds.add(new CommandGive());
        cmds.add(new CommandInfo());
        cmds.add(new CommandReload());
        cmds.add(new CommandSettings());
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if(args.length > 0){
            for(ICommand cmd : cmds) {
                if (cmd.getLabel().equalsIgnoreCase(args[0])){
                    if(cmd.getPermission() != null && !sender.hasPermission(cmd.getPermission())){
                        Locale.NO_PERMISSION.send(sender);
                        return false;
                    }
                    if(args.length < cmd.getMinArgs() || args.length > cmd.getMaxArgs()){
                        Locale.COMMAND_USAGE.send(sender, cmd.getUsage());
                        return false;
                    }
                    cmd.run(plugin, sender, args);
                    return true;
                }
            }
        }

        //Checking that the player has permission to use at least one of the commands.
        for(ICommand subCommand : cmds){
            if(sender.hasPermission(subCommand.getPermission())){
                //Player has permission
                Locale.HELP_COMMAND_HEADER.send(sender);

                for(ICommand cmd : cmds) {
                    if(sender.hasPermission(subCommand.getPermission()))
                        Locale.HELP_COMMAND_LINE.send(sender, cmd.getUsage(), cmd.getDescription());
                }

                Locale.HELP_COMMAND_FOOTER.send(sender);
                return false;
            }
        }

        Locale.NO_PERMISSION.send(sender);

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if(args.length > 0){
            for(ICommand cmd : cmds) {
                if (cmd.getLabel().equalsIgnoreCase(args[0])){
                    if(cmd.getPermission() != null && !sender.hasPermission(cmd.getPermission())){
                        return new ArrayList<>();
                    }
                    return cmd.tabComplete(plugin, sender, args);
                }
            }
        }

        List<String> list = new ArrayList<>();

        for(ICommand cmd : cmds)
            if(cmd.getPermission() == null || sender.hasPermission(cmd.getPermission()))
                if(cmd.getLabel().startsWith(args[0]))
                    list.add(cmd.getLabel());

        return list;
    }

}
