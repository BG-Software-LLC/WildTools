package com.bgsoftware.wildtools.command.commands;

import com.bgsoftware.wildtools.Locale;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import com.bgsoftware.wildtools.command.ICommand;
import com.bgsoftware.wildtools.utils.items.ItemUtils;
import com.bgsoftware.wildtools.utils.items.ToolItemStack;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandGive implements ICommand {

    @Override
    public String getLabel() {
        return "give";
    }

    @Override
    public String getUsage() {
        return "tools give <player-name> <tool-name> [amount] [uses]";
    }

    @Override
    public String getPermission() {
        return "wildtools.give";
    }

    @Override
    public String getDescription() {
        return "Gives a tool to a specific player.";
    }

    @Override
    public int getMinArgs() {
        return 3;
    }

    @Override
    public int getMaxArgs() {
        return 5;
    }

    @Override
    public void run(WildToolsPlugin plugin, CommandSender sender, String[] args) {
        Tool tool = plugin.getToolsManager().getTool(args[2]);

        if (tool == null) {
            Locale.INVALID_TOOL.send(sender, args[2]);
            return;
        }

        Player player = Bukkit.getPlayer(args[1]);

        if (player == null) {
            Locale.INVALID_PLAYER.send(sender, args[1]);
            return;
        }

        int uses = -1;
        int amount = 1;

        if (args.length >= 4) {
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException error) {
                Locale.INVALID_NUMBER.send(sender, args[3]);
                return;
            }
        }

        if (args.length == 5) {
            try {
                uses = Integer.parseInt(args[4]);
            } catch (NumberFormatException error) {
                Locale.INVALID_NUMBER.send(sender, args[4]);
                return;
            }
        }

        for (int i = 0; i < amount; i++) {
            ToolItemStack toolItem = ToolItemStack.of(tool.getFormattedItemStack(uses > -1 ? uses : tool.getDefaultUses()));

            if (uses > -1)
                toolItem.setUses(uses);

            ItemUtils.addItem(toolItem.getItem(), player.getInventory(), player.getLocation(), null);
        }

        Locale.GIVE_TOOL_SUCCESS.send(sender, amount, tool.getName(), player.getName());
    }

    @Override
    public List<String> tabComplete(WildToolsPlugin plugin, CommandSender sender, String[] args) {
        if (!sender.hasPermission(getPermission()))
            return new ArrayList<>();

        if (args.length == 3) {
            List<String> list = new ArrayList<>();
            for (Tool tool : plugin.getToolsManager().getTools())
                if (tool.getName().startsWith(args[2]))
                    list.add(tool.getName());
            return list;
        }

        if (args.length >= 4) {
            return new ArrayList<>();
        }

        return null;
    }

}
