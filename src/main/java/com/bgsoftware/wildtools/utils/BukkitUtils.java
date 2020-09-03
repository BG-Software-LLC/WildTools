package com.bgsoftware.wildtools.utils;

import com.bgsoftware.wildtools.utils.blocks.BlocksController;
import com.bgsoftware.wildtools.utils.items.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.CropState;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public final class BukkitUtils {

    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();
    private static final BlockFace[] blockFaces = new BlockFace[] {
            BlockFace.UP, BlockFace.DOWN, BlockFace.WEST, BlockFace.EAST, BlockFace.SOUTH, BlockFace.NORTH
    };

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean canBreakBlock(Block block, Tool tool){
        return canBreakBlock(block, block.getType(), block.getState().getData().toItemStack().getDurability(), tool);
    }

    public static boolean canBreakBlock(Block block, Material firstType, short firstData, Tool tool){
        return tool.canBreakBlock(block, firstType, firstData) &&
                !plugin.getNMSAdapter().isOutsideWorldborder(block.getLocation())
                && block.getType() != Material.BEDROCK;
    }

    public static boolean hasBreakAccess(Block block, Player player){
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        plugin.getProviders().runWithBypass(player, () -> Bukkit.getPluginManager().callEvent(blockBreakEvent));
        return !blockBreakEvent.isCancelled();
    }

    public static boolean canInteractBlock(Player player, Block block, ItemStack usedItem){
        PlayerInteractEvent playerInteractEvent =
                new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, usedItem, block, BlockFace.SELF);
        plugin.getProviders().runWithBypass(player, () -> Bukkit.getPluginManager().callEvent(playerInteractEvent));
        return !playerInteractEvent.isCancelled();
    }

    public static boolean breakBlock(Player player, BlocksController blocksController, Block block, ItemStack usedItem, Tool tool, Function<ItemStack, ItemStack> dropItemFunction){
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        List<ItemStack> drops = getBlockDrops(player, block, tool);
        block.setMetadata("drop-items", new FixedMetadataValue(plugin, tool == null));

        if((tool == null || !tool.hasSilkTouch()) && usedItem.getEnchantmentLevel(Enchantment.SILK_TOUCH) == 0)
            blockBreakEvent.setExpToDrop(plugin.getNMSAdapter().getExpFromBlock(block, player));

        plugin.getProviders().runWithBypass(player, () -> Bukkit.getPluginManager().callEvent(blockBreakEvent));

        block.removeMetadata("drop-items", plugin);

        if(blockBreakEvent.isCancelled())
            return false;

        if(blocksController == null || (tool != null && tool.isOmni()) || block.getType().hasGravity() || hasNearbyWater(block)) {
            block.setType(Material.AIR);
        }
        else {
            blocksController.setAir(block.getLocation());
        }

        if(tool != null) {
            tool.filterDrops(drops).forEach(itemStack -> {
                itemStack = dropItemFunction.apply(itemStack);
                if(itemStack != null) {
                    if (tool.isAutoCollect()) {
                        ItemUtils.addItem(itemStack, player.getInventory(), block.getLocation());
                    } else {
                        block.getWorld().dropItemNaturally(block.getLocation(), itemStack);
                    }
                }
            });
        }

        plugin.getNMSAdapter().dropExp(block.getLocation(), blockBreakEvent.getExpToDrop());

        return true;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean breakBlockAsBoolean(Player player, BlocksController blocksController, Block block, ItemStack usedItem, Tool tool, Function<ItemStack, Boolean> dropItemFunction){
        return breakBlock(player, blocksController, block, usedItem, tool, itemStack -> dropItemFunction.apply(itemStack) ? itemStack : null);
    }

    public static boolean seedBlock(Player player, Block block, Tool tool, Function<ItemStack, ItemStack> dropItemFunction){
        List<ItemStack> drops = getBlockDrops(player, block, tool);
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        block.setMetadata("drop-items", new FixedMetadataValue(plugin, tool == null));

        plugin.getProviders().runWithBypass(player, () -> Bukkit.getPluginManager().callEvent(blockBreakEvent));

        block.removeMetadata("drop-items", plugin);

        if(blockBreakEvent.isCancelled())
            return false;

        plugin.getNMSAdapter().setCropState(block, CropState.SEEDED);

        if (tool != null) {
            tool.filterDrops(drops).forEach(itemStack -> {
                itemStack = dropItemFunction.apply(itemStack);
                if(itemStack != null) {
                    if (tool.isAutoCollect()) {
                        ItemUtils.addItem(itemStack, player.getInventory(), block.getLocation());
                    } else {
                        block.getWorld().dropItemNaturally(block.getLocation(), itemStack);
                    }
                }
            });
        }

        return true;
    }

    public static boolean seedBlockAsBoolean(Player player, Block block, Tool tool, Function<ItemStack, Boolean> dropItemFunction){
        return seedBlock(player, block, tool, itemStack -> dropItemFunction.apply(itemStack) ? itemStack : null);
    }

    public static boolean placeBlock(Player player, BlocksController blocksController, Block block, Block materialBlock){
        BlockPlaceEvent blockPlaceEvent = plugin.getNMSAdapter().getFakePlaceEvent(player, block.getLocation(), materialBlock);
        plugin.getProviders().runWithBypass(player, () -> Bukkit.getPluginManager().callEvent(blockPlaceEvent));

        if(blockPlaceEvent.isCancelled())
            return false;

        blocksController.setType(block.getLocation(), materialBlock.getLocation());

        return true;
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

    private static boolean hasNearbyWater(Block block){
        return Arrays.stream(blockFaces).anyMatch(blockFace -> {
            Material blockType = block.getRelative(blockFace).getType();
            return blockType.name().contains("WATER") || blockType.name().contains("LAVA");
        });
    }

}
