package com.bgsoftware.wildtools.utils;

import com.bgsoftware.wildtools.utils.blocks.BlocksController;
import com.bgsoftware.wildtools.utils.items.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.tools.Tool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public final class BukkitUtils {

    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();
    private static final BlockFace[] blockFaces = new BlockFace[] {
            BlockFace.UP, BlockFace.DOWN, BlockFace.WEST, BlockFace.EAST, BlockFace.SOUTH, BlockFace.NORTH
    };

    public static void breakNaturally(Player player, BlocksController blocksController, Block block, ItemStack usedItem, Tool tool){
        boolean autoCollect = tool.isAutoCollect();
        boolean omniTool = tool.isOmni();

        Consumer<ItemStack> onItemDrop = itemConsumer -> {
            if (autoCollect)
                ItemUtils.addItem(itemConsumer, player.getInventory(), block.getLocation());
            else
                block.getWorld().dropItemNaturally(block.getLocation(), itemConsumer);
        };

        Consumer<Block> onBlockBreak = blockConsumer -> {
            if(omniTool || blockConsumer.getType().hasGravity() || Arrays.stream(blockFaces).anyMatch(blockFace -> {
                Material blockType = blockConsumer.getRelative(blockFace).getType();
                return blockType.name().contains("WATER") || blockType.name().contains("LAVA");
            })) {
                blockConsumer.setType(Material.AIR);
            }
            else {
                blocksController.setAir(blockConsumer.getLocation());
            }
        };

        breakNaturally(player, block, usedItem, tool, onBlockBreak, onItemDrop);
    }

    public static void breakNaturally(Player player, BlocksController blocksController, Block block, ItemStack usedItem, Tool tool, Consumer<ItemStack> onItemDrop){
        Consumer<Block> onBlockBreak = blockConsumer -> {
            if(blockConsumer.getRelative(BlockFace.UP).getType().name().contains("WATER") || blockConsumer.getType().hasGravity())
                blockConsumer.setType(Material.AIR);
            else
                blocksController.setAir(blockConsumer.getLocation());
        };

        breakNaturally(player, block, usedItem, tool, onBlockBreak, onItemDrop);
    }

    public static void breakNaturally(Player player, Block block, ItemStack usedItem, Tool tool, Consumer<Block> onBlockBreak, Consumer<ItemStack> onItemDrop){
        List<ItemStack> drops = new ArrayList<>();

        if((tool == null || !tool.hasSilkTouch()) && usedItem.getEnchantmentLevel(Enchantment.SILK_TOUCH) == 0) {
            int expFromBlock = plugin.getNMSAdapter().getExpFromBlock(block, player);
            if (expFromBlock > 0) {
                ExperienceOrb orb = block.getWorld().spawn(block.getLocation(), ExperienceOrb.class);
                orb.setExperience(expFromBlock);
            }
        }

        if(tool != null && tool.isOmni()){
            BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
            Bukkit.getPluginManager().callEvent(blockBreakEvent);
            if(blockBreakEvent.isCancelled())
                return;
        }

        if(onItemDrop != null) {
            drops.addAll(getBlockDrops(player, block, tool));
        }

        plugin.getProviders().onBlockBreak(player, block, usedItem);

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
    }

    public static List<ItemStack> getBlockDrops(Player player, Block block, Tool tool){
        List<ItemStack> drops = plugin.getProviders().getBlockDrops(player, block, false);

        if(!drops.isEmpty())
            return drops;

        if(!Boolean.parseBoolean(block.getWorld().getGameRuleValue("doTileDrops")))
            return new ArrayList<>();

        return ItemUtils.isCrops(block.getType()) ? plugin.getNMSAdapter().getCropDrops(player, block) :
                plugin.getNMSAdapter().getBlockDrops(player, block, tool.hasSilkTouch());
    }

}
