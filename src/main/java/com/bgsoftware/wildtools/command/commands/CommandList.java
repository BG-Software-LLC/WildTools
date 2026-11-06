package com.bgsoftware.wildtools.command.commands;

import com.bgsoftware.wildtools.Locale;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import com.bgsoftware.wildtools.command.ICommand;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CommandList implements ICommand {

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

        for (Tool tool : toolsList) {
            if (tool.getToolMode() != currentMode) {
                if (!toolsListString.toString().isEmpty()) {
                    Locale.TOOL_LIST_TYPE.send(sender, currentMode, toolsListString.substring(1));
                }

                toolsListString = new StringBuilder();
                currentMode = tool.getToolMode();
            }

            toolsListString.append("\n").append(Locale.TOOL_LIST_LINE.getMessage(tool.getName()));
        }

        if (!toolsListString.toString().isEmpty()) {
            Locale.TOOL_LIST_TYPE.send(sender, currentMode, toolsListString.substring(1));
        }

        Locale.TOOL_LIST_FOOTER.send(sender);
    }

    @Override
    public List<String> tabComplete(WildToolsPlugin plugin, CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

}
