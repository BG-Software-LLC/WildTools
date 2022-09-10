package com.bgsoftware.wildtools.utils;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import com.bgsoftware.wildtools.utils.blocks.BlocksController;
import com.bgsoftware.wildtools.utils.items.ItemUtils;
import com.bgsoftware.wildtools.utils.items.ItemsDropper;
import org.bukkit.CropState;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public final class BukkitUtils {

    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();
    private static final List<BlockFace> blockFaces = new LinkedList<>(Arrays.asList(
            BlockFace.UP, BlockFace.DOWN, BlockFace.WEST, BlockFace.EAST, BlockFace.SOUTH, BlockFace.NORTH));

    public static final EnumSet<Material> DISALLOWED_BLOCKS = createDisallowedBlocks(new String[]{
            "BEDROCK", "COMMAND", "REPEATING_COMMAND_BLOCK", "CHAIN_COMMAND_BLOCK", "COMMAND_BLOCK", "WATER",
            "STATIONARY_WATER", "LAVA", "STATIONARY_LAVA", "END_PORTAL_FRAME", "ENDER_PORTAL_FRAME", "BARRIER",
            "STRUCTURE_BLOCK", "STRUCTURE_VOID", "CAVE_AIR", "END_PORTAL", "ENDER_PORTAL", "NETHER_PORTAL", "PORTAL",
            "BUBBLE_COLUMN", "REINFORCED_DEEPSLATE"
    });
    private static final EnumSet<Material> FORCE_UPDATE_MATERIALS = createDisallowedBlocks(new String[]{
            "WATER", "STATIONARY_WATER", "LAVA", "STATIONARY_LAVA", "ENDER_PORTAL", "NETHER_PORTAL", "PORTAL",
            "BUBBLE_COLUMN"
    });

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean canBreakBlock(Player player, Block block, Tool tool) {
        return canBreakBlock(player, block, block.getType(), block.getState().getData().toItemStack().getDurability(), tool);
    }

    public static boolean canBreakBlock(Player player, Block block, Material firstType, short firstData, Tool tool) {
        return !DISALLOWED_BLOCKS.contains(block.getType()) && tool.canBreakBlock(block, firstType, firstData) &&
                (!tool.isOnlyInsideClaim() || plugin.getProviders().isInsideClaim(player, block.getLocation())) &&
                !plugin.getNMSAdapter().isOutsideWorldborder(block.getLocation());
    }

    public static boolean hasBreakAccess(Block block, Player player) {
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        plugin.getEvents().callBreakEvent(blockBreakEvent, true);
        return !blockBreakEvent.isCancelled();
    }

    public static boolean canInteractBlock(Player player, Block block, ItemStack usedItem) {
        PlayerInteractEvent playerInteractEvent =
                new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, usedItem, block, BlockFace.SELF);
        plugin.getEvents().callInteractEvent(playerInteractEvent);
        return !playerInteractEvent.isCancelled();
    }

    public static boolean breakBlock(Player player, BlocksController blocksController, ItemsDropper itemsDropper, Block block, ItemStack usedItem,
                                     Tool tool, Function<ItemStack, ItemStack> dropItemFunction) {
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        List<ItemStack> drops = getBlockDrops(player, block, tool);
        block.setMetadata("drop-items", new FixedMetadataValue(plugin, tool == null));

        if ((tool == null || !tool.hasSilkTouch()) && usedItem.getEnchantmentLevel(Enchantment.SILK_TOUCH) == 0)
            blockBreakEvent.setExpToDrop(plugin.getNMSAdapter().getExpFromBlock(block, player));

        plugin.getEvents().callBreakEvent(blockBreakEvent, true);

        block.removeMetadata("drop-items", plugin);

        if (blockBreakEvent.isCancelled())
            return false;

        plugin.getEvents().callBreakEvent(blockBreakEvent, false);
        Material originalType = block.getType();

        if (blocksController == null || (tool != null && tool.isOmni()) || originalType.hasGravity() || shouldForceUpdate(block)) {
            block.setType(Material.AIR);
        } else {
            blocksController.setAir(block.getLocation());
        }

        if (tool != null) {
            boolean nullDropper = itemsDropper == null;
            if (nullDropper)
                itemsDropper = new ItemsDropper();

            for (ItemStack itemStack : tool.filterDrops(drops)) {
                itemStack = dropItemFunction.apply(itemStack);
                if (itemStack != null) {
                    if (tool.isAutoCollect()) {
                        ItemUtils.addItem(itemStack, player.getInventory(), block.getLocation(), itemsDropper);
                    } else {
                        itemsDropper.addDrop(itemStack, block.getLocation());
                    }
                }
            }

            if (nullDropper)
                itemsDropper.dropItems();

            if (tool.hasStatistics()) {
                try {
                    player.incrementStatistic(Statistic.MINE_BLOCK, originalType);
                } catch (IllegalArgumentException e) {
                }
            }
        }

        if (blockBreakEvent.getExpToDrop() > 0) {
            ExperienceOrb orb = block.getWorld().spawn(block.getLocation(), ExperienceOrb.class);
            orb.setExperience(blockBreakEvent.getExpToDrop());
        }

        return true;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean breakBlockAsBoolean(Player player, BlocksController blocksController, ItemsDropper itemsDropper, Block block, ItemStack usedItem, Tool tool, Function<ItemStack, Boolean> dropItemFunction) {
        return breakBlock(player, blocksController, itemsDropper, block, usedItem, tool, itemStack -> dropItemFunction.apply(itemStack) ? itemStack : null);
    }

    public static boolean seedBlock(Player player, Block block, Tool tool, Function<ItemStack, ItemStack> dropItemFunction, ItemsDropper itemsDropper) {
        List<ItemStack> drops = getBlockDrops(player, block, tool);
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        block.setMetadata("drop-items", new FixedMetadataValue(plugin, tool == null));

        plugin.getEvents().callBreakEvent(blockBreakEvent, true);

        block.removeMetadata("drop-items", plugin);

        if (blockBreakEvent.isCancelled())
            return false;

        plugin.getEvents().callBreakEvent(blockBreakEvent, false);

        plugin.getNMSAdapter().setCropState(block, CropState.SEEDED);

        if (tool != null) {
            boolean nullDropper = itemsDropper == null;
            if (nullDropper)
                itemsDropper = new ItemsDropper();

            for (ItemStack itemStack : tool.filterDrops(drops)) {
                itemStack = dropItemFunction.apply(itemStack);
                if (itemStack != null) {
                    if (tool.isAutoCollect()) {
                        ItemUtils.addItem(itemStack, player.getInventory(), block.getLocation(), itemsDropper);
                    } else {
                        itemsDropper.addDrop(itemStack, block.getLocation());
                    }
                }
            }

            if (nullDropper)
                itemsDropper.dropItems();
        }

        return true;
    }

    public static boolean seedBlockAsBoolean(Player player, Block block, Tool tool, Function<ItemStack, Boolean> dropItemFunction, ItemsDropper itemsDropper) {
        return seedBlock(player, block, tool, itemStack -> dropItemFunction.apply(itemStack) ? itemStack : null, itemsDropper);
    }

    public static boolean placeBlock(Player player, BlocksController blocksController, Block block, Block materialBlock) {
        BlockPlaceEvent blockPlaceEvent = plugin.getNMSAdapter().getFakePlaceEvent(player, block, materialBlock);
        plugin.getEvents().callPlaceEvent(blockPlaceEvent);

        if (blockPlaceEvent.isCancelled())
            return false;

        blocksController.setType(block.getLocation(), materialBlock);

        return true;
    }

    public static List<ItemStack> getBlockDrops(Player player, Block block, Tool tool) {
        List<ItemStack> drops = new ArrayList<>();

        if (plugin.getProviders().getBlockDrops(drops, player, block, false))
            return drops;

        if (!Boolean.parseBoolean(block.getWorld().getGameRuleValue("doTileDrops")))
            return new ArrayList<>();

        return ItemUtils.isCrops(block.getType()) ? plugin.getNMSAdapter().getCropDrops(player, block) :
                plugin.getNMSAdapter().getBlockDrops(player, block, tool.hasSilkTouch());
    }

    private static boolean shouldForceUpdate(Block frameBlock) {
        for (BlockFace blockFace : blockFaces) {
            if (FORCE_UPDATE_MATERIALS.contains(frameBlock.getRelative(blockFace).getType()))
                return true;
        }

        return false;
    }

    private static EnumSet<Material> createDisallowedBlocks(String[] materialNames) {
        EnumSet<Material> materials = EnumSet.noneOf(Material.class);

        for (String materialName : materialNames) {
            try {
                materials.add(Material.valueOf(materialName));
            } catch (IllegalArgumentException ignored) {
            }
        }

        return materials;
    }

}
