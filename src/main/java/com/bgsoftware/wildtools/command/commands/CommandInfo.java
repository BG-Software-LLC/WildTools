package com.bgsoftware.wildtools.command.commands;

import com.bgsoftware.wildtools.Locale;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.tools.CannonTool;
import com.bgsoftware.wildtools.api.objects.tools.CuboidTool;
import com.bgsoftware.wildtools.api.objects.tools.HarvesterTool;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import com.bgsoftware.wildtools.command.ICommand;
import com.bgsoftware.wildtools.utils.text.TextUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandInfo implements ICommand {

    @Override
    public String getLabel() {
        return "info";
    }

    @Override
    public String getUsage() {
        return "tools info <tool-name>";
    }

    @Override
    public String getPermission() {
        return "wildtools.info";
    }

    @Override
    public String getDescription() {
        return "Checks information about each tool.";
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public int getMaxArgs() {
        return 2;
    }

    @Override
    public void run(WildToolsPlugin plugin, CommandSender sender, String[] args) {
        Tool tool = plugin.getToolsManager().getTool(args[1]);

        if (tool == null) {
            Locale.INVALID_TOOL.send(sender, args[1]);
            return;
        }

        ItemStack itemStack = tool.getItemStack();

        //Header
        Locale.TOOL_INFO_HEADER.send(sender);

        //Sections which applied for all tools
        Locale.TOOL_INFO_NAME.send(sender, tool.getName());
        Locale.TOOL_INFO_MODE.send(sender, TextUtils.getFormattedName(tool.getToolMode().name()));
        Locale.TOOL_INFO_TYPE.send(sender, TextUtils.getFormattedName(itemStack.getType().name()));

        //Optional sections
        if (tool.hasWhitelistedMaterials()) {
            Locale.TOOL_INFO_WHITELISTED_BLOCKS.send(sender, TextUtils.buildStringFromSet(tool.getWhitelistedMaterials()));
        }
        if (tool.hasWhitelistedDrops()) {
            Locale.TOOL_INFO_WHITELISTED_DROPS.send(sender, TextUtils.buildStringFromSet(tool.getWhitelistedDrops()));
        }
        if (tool.hasBlacklistedMaterials()) {
            Locale.TOOL_INFO_BLACKLISTED_BLOCKS.send(sender, TextUtils.buildStringFromSet(tool.getBlacklistedMaterials()));
        }
        if (tool.hasBlacklistedDrops()) {
            Locale.TOOL_INFO_BLACKLISTED_DROPS.send(sender, TextUtils.buildStringFromSet(tool.getBlacklistedDrops()));
        }
        Locale.TOOL_INFO_UNBREAKABLE.send(sender, tool.isUnbreakable());
        if (!tool.isUsingDurability() && !tool.isUnbreakable()) {
            Locale.TOOL_INFO_USES.send(sender, tool.getDefaultUses());
        }
        if (tool.getCooldown() != 0) {
            Locale.TOOL_INFO_COOLDOWN.send(sender, tool.getCooldown());
        }
        Locale.TOOL_INFO_AUTO_COLLECT.send(sender, tool.isAutoCollect());
        Locale.TOOL_INFO_SILK_TOUCH.send(sender, tool.hasSilkTouch());
        Locale.TOOL_INFO_SAME_TYPE.send(sender, tool.isOnlySameType());
        Locale.TOOL_INFO_INSIDE_CLAIM.send(sender, tool.isOnlyInsideClaim());

        //Sections which applied for specific tools
        if (tool instanceof CuboidTool) {
            Locale.TOOL_INFO_BREAK_LEVEL.send(sender, ((CuboidTool) tool).getBreakLevel());
        }
        if (tool instanceof HarvesterTool) {
            Locale.TOOL_INFO_RADIUS.send(sender, ((HarvesterTool) tool).getRadius());
        }
        if (tool instanceof CannonTool) {
            Locale.TOOL_INFO_TNT_AMOUNT.send(sender, ((CannonTool) tool).getTNTAmount());
        }

        //Footer
        Locale.TOOL_INFO_FOOTER.send(sender);
    }

    @Override
    public List<String> tabComplete(WildToolsPlugin plugin, CommandSender sender, String[] args) {
        if (args.length == 2) {
            List<String> list = new ArrayList<>();
            String arg = args[1].toLowerCase();

            for (Tool tool : plugin.getToolsManager().getTools()) {
                if (tool.getName().toLowerCase().startsWith(arg)) {
                    list.add(tool.getName());
                }
            }

            return Collections.unmodifiableList(list);
        }

        return Collections.emptyList();
    }

}
