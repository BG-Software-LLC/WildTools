package com.bgsoftware.wildtools.utils;

import com.bgsoftware.wildtools.hooks.CoreProtectHook;
import com.bgsoftware.wildtools.utils.blocks.BlocksController;
import com.bgsoftware.wildtools.utils.items.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import com.bgsoftware.wildtools.objects.WMaterial;
import com.bgsoftware.wildtools.objects.tools.WHarvesterTool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class BukkitUtils {

    private static WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    public static void breakNaturally(Player player, Block block, Tool tool){
        boolean autoCollect = false;
        List<ItemStack> drops = getBlockDrops(player, block);

        if(block.getRelative(BlockFace.UP).getType().name().contains("WATER"))
            block.setType(Material.AIR);
        else
            BlocksController.setAir(block.getLocation());

        if(tool != null){
            drops = tool.filterDrops(drops);
            autoCollect = tool.isAutoCollect();
        }

        for(ItemStack is : drops) {
            if(is != null && is.getType() != Material.AIR) {
                if (autoCollect)
                    ItemUtils.addItem(is, player.getInventory(), block.getLocation());
                else
                    block.getWorld().dropItemNaturally(block.getLocation(), is);
            }
        }

        if(Bukkit.getPluginManager().isPluginEnabled("CoreProtect"))
            CoreProtectHook.recordBlockChange(player, block);
    }

    public static List<ItemStack> getBlockDrops(Player player, Block block){
        if(!Boolean.parseBoolean(block.getWorld().getGameRuleValue("doTileDrops")))
            return new ArrayList<>();

        Material type = block.getType();
        if(Arrays.asList(WHarvesterTool.crops).contains(type.name()) && type != Material.CACTUS &&
                type != WMaterial.SUGAR_CANE.parseMaterial() && type != WMaterial.MELON.parseMaterial() && type != Material.PUMPKIN && !type.name().equals("BAMBOO")) {
            return plugin.getNMSAdapter().getCropDrops(player, block);
        }

        boolean silkTouch = false;

        Tool tool;
        if((tool = plugin.getToolsManager().getTool(plugin.getNMSAdapter().getItemInHand(player))) != null)
            silkTouch = tool.hasSilkTouch();

        return plugin.getNMSAdapter().getBlockDrops(player, block, silkTouch);
    }

}
