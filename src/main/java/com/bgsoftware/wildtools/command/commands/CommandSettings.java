package com.bgsoftware.wildtools.command.commands;

import com.bgsoftware.wildtools.command.ICommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.bgsoftware.wildtools.WildToolsPlugin;

import java.util.List;

public class CommandSettings implements ICommand {

    @Override
    public String getLabel() {
        return "settings";
    }

    @Override
    public String getUsage() {
        return "tools settings";
    }

    @Override
    public String getPermission() {
        return "wildtools.settings";
    }

    @Override
    public String getDescription() {
        return "Open settings editor.";
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
        if(!(sender instanceof Player)){
            sender.sendMessage(ChatColor.RED + "Only players can perform this command.");
            return;
        }

        Player player = (Player) sender;

        player.openInventory(plugin.getEditor().getSettingsEditor());
    }

    @Override
    public List<String> tabComplete(WildToolsPlugin plugin, CommandSender sender, String[] args) {
        return null;
    }
}
