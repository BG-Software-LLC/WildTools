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
import java.util.List;
import java.util.function.Consumer;

public final class BukkitUtils {

    private static WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    public static void breakNaturally(Player player, BlocksController blocksController, Block block, Tool tool){
        boolean autoCollect = tool.isAutoCollect();

        Consumer<ItemStack> onItemDrop = itemConsumer -> {
            if (autoCollect)
                ItemUtils.addItem(itemConsumer, player.getInventory(), block.getLocation());
            else
                block.getWorld().dropItemNaturally(block.getLocation(), itemConsumer);
        };

        Consumer<Block> onBlockBreak = blockConsumer -> {
            if(blockConsumer.getRelative(BlockFace.UP).getType().name().contains("WATER") || blockConsumer.getType().hasGravity())
                blockConsumer.setType(Material.AIR);
            else
                blocksController.setAir(blockConsumer.getLocation());
        };

        breakNaturally(player, block, tool, onBlockBreak, onItemDrop);
    }

    public static void breakNaturally(Player player, BlocksController blocksController, Block block, Tool tool, Consumer<ItemStack> onItemDrop){
        Consumer<Block> onBlockBreak = blockConsumer -> {
            if(blockConsumer.getRelative(BlockFace.UP).getType().name().contains("WATER") || blockConsumer.getType().hasGravity())
                blockConsumer.setType(Material.AIR);
            else
                blocksController.setAir(blockConsumer.getLocation());
        };

        breakNaturally(player, block, tool, onBlockBreak, onItemDrop);
    }

    public static void breakNaturally(Player player, Block block, Tool tool, Consumer<Block> onBlockBreak, Consumer<ItemStack> onItemDrop){
        List<ItemStack> drops = new ArrayList<>();

        if(onItemDrop != null)
            drops.addAll(getBlockDrops(player, block));

        if(onBlockBreak != null)
            onBlockBreak.accept(block);

        if(!drops.isEmpty()) {
            if (tool != null)
                drops = tool.filterDrops(drops);

            for (ItemStack is : drops) {
                if (is != null && is.getType() != Material.AIR) {
                    onItemDrop.accept(is);
                }
            }
        }

        if(Bukkit.getPluginManager().isPluginEnabled("CoreProtect"))
            CoreProtectHook.recordBlockChange(player, block);
    }

    public static List<ItemStack> getBlockDrops(Player player, Block block){
        if(!Boolean.parseBoolean(block.getWorld().getGameRuleValue("doTileDrops")))
            return new ArrayList<>();

        Material type = block.getType();
        if(WHarvesterTool.crops.contains(type.name()) && type != Material.CACTUS &&
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
