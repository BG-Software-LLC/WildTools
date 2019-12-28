package com.bgsoftware.wildtools.command.commands;

import com.bgsoftware.wildtools.Locale;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.api.objects.tools.CannonTool;
import com.bgsoftware.wildtools.api.objects.tools.CuboidTool;
import com.bgsoftware.wildtools.api.objects.tools.HarvesterTool;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import com.bgsoftware.wildtools.command.ICommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class CommandList implements ICommand {

    @Override
    public String getLabel() {
        return "list";
    }

    @Override
    public String getUsage() {
        return "tools list";
    }

    @Override
    public String getPermission() {
        return "wildtools.list";
    }

    @Override
    public String getDescription() {
        return "Get all the tools.";
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
        List<Tool> toolsList = plugin.getToolsManager().getTools().stream()
                .sorted(Comparator.comparingInt(o -> o.getToolMode().ordinal())).collect(Collectors.toList());

        ToolMode currentMode = ToolMode.BUILDER;

        Locale.TOOL_LIST_HEADER.send(sender);

        StringBuilder toolsListString = new StringBuilder();

        for(Tool tool : toolsList){
            if(tool.getToolMode() != currentMode){
                if(!toolsListString.toString().isEmpty())
                    Locale.TOOL_LIST_TYPE.send(sender, currentMode, toolsListString.toString().substring(1));
                toolsListString = new StringBuilder();
                currentMode = tool.getToolMode();
            }

            toolsListString.append("\n").append(Locale.TOOL_LIST_LINE.getMessage(tool.getName()));
        }

        Locale.TOOL_LIST_FOOTER.send(sender);
    }

    @Override
    public List<String> tabComplete(WildToolsPlugin plugin, CommandSender sender, String[] args) {
        if(!sender.hasPermission(getPermission()))
            return new ArrayList<>();

        if (args.length == 2) {
            List<String> list = new ArrayList<>();
            for (Tool tool : plugin.getToolsManager().getTools())
                if(tool.getName().startsWith(args[1]))
                    list.add(tool.getName());
            return list;
        }

        if (args.length >= 2) {
            return new ArrayList<>();
        }

        return null;
    }

    private String buildStringArray(Set<String> arr){
        List<String> formattedList = new ArrayList<>();

        if(arr.size() == 0)
            return "None";

        arr.forEach(str -> formattedList.add(getFormattedName(str)));

        return String.join(", ", formattedList);
    }

    private String getFormattedName(String oldName){
        StringBuilder name = new StringBuilder();
        String[] split = oldName.split("_");

        for(int i = 0; i < split.length; i++) {
            name.append(split[i].substring(0, 1).toUpperCase()).append(split[i].substring(1).toLowerCase());
            if(i != split.length - 1)
                name.append(" ");
        }

        return name.toString();
    }

}
