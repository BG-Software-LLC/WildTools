package com.bgsoftware.wildtools.utils;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import com.bgsoftware.wildtools.utils.items.ItemUtils;
import com.bgsoftware.wildtools.utils.world.WorldEditSession;
import com.bgsoftware.wildtools.world.BlockMaterial;
import org.bukkit.CropState;
import org.bukkit.Location;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class BukkitUtils {

    private static final ReflectMethod<Boolean> BLOCK_BREAK_EVENT_IS_DROP_ITEMS = new ReflectMethod<>(
            BlockBreakEvent.class, "isDropItems");

    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();
    private static final List<BlockFace> blockFaces = new LinkedList<>(Arrays.asList(
            BlockFace.UP, BlockFace.DOWN, BlockFace.WEST, BlockFace.EAST, BlockFace.SOUTH, BlockFace.NORTH));

    public static boolean canBreakBlock(Player player, Block block, Tool tool) {
        return canBreakBlock(player, block, BlockMaterial.of(block), tool);
    }

    public static boolean canBreakBlock(Player player, Block block, BlockMaterial firstBlockMaterial, Tool tool) {
        return canBreakBlock(player, block, firstBlockMaterial, tool, true);
    }

    public static boolean canBreakBlock(Player player, Block block, BlockMaterial firstBlockMaterial, Tool tool,
                                        boolean checkDisallowedBlocks) {
        return (!checkDisallowedBlocks || !Materials.isBlacklisted(block.getType())) &&
                tool.canBreakBlock(block, firstBlockMaterial.getType(), firstBlockMaterial.getData()) &&
                (!tool.isOnlyInsideClaim() || plugin.getProviders().isInsideClaim(player, block.getLocation())) &&
                !plugin.getNMSWorld().isOutsideWorldBorder(block.getLocation());
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

    public static boolean breakBlock(Player player, Block block, ItemStack usedItem,
                                     @Nullable Tool tool,
                                     @Nullable WorldEditSession editSession,
                                     @Nullable Function<ItemStack, ItemStack> dropItemFunction) {
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        block.setMetadata("drop-items", new FixedMetadataValue(plugin, tool == null));

        if ((tool == null || !tool.hasSilkTouch()) && usedItem.getEnchantmentLevel(Enchantment.SILK_TOUCH) == 0)
            blockBreakEvent.setExpToDrop(plugin.getNMSWorld().getExpFromBlock(block, player));

        plugin.getEvents().callBreakEvent(blockBreakEvent, true);

        block.removeMetadata("drop-items", plugin);

        if (blockBreakEvent.isCancelled())
            return false;

        plugin.getEvents().callBreakEvent(blockBreakEvent, false);
        Material originalType = block.getType();
        Location blockLocation = block.getLocation();

        if (editSession == null) {
            block.setType(Material.AIR);
            // Drop exp
            ExperienceOrb orb = block.getWorld().spawn(block.getLocation(), ExperienceOrb.class);
            orb.setExperience(blockBreakEvent.getExpToDrop());
        } else {
            boolean result;

            if ((tool != null && tool.isOmni()) || originalType.hasGravity() || shouldForceUpdate(block)) {
                result = editSession.setType(blockLocation, false, vec -> block.setType(Material.AIR), WorldEditSession.SetBlockPriority.UPDATES);
            } else {
                result = editSession.setAir(blockLocation);
            }

            if (!result)
                return false;

            editSession.addExp(blockBreakEvent.getExpToDrop());
        }

        if (tool != null) {
            if (!BLOCK_BREAK_EVENT_IS_DROP_ITEMS.isValid() || BLOCK_BREAK_EVENT_IS_DROP_ITEMS.invoke(blockBreakEvent))
                collectDropsFromTool(player, block, tool, editSession, dropItemFunction);

            if (tool.hasStatistics()) {
                try {
                    player.incrementStatistic(Statistic.MINE_BLOCK, originalType);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        return true;
    }

    public static boolean seedBlock(Player player, Block block,
                                    @Nullable Tool tool,
                                    @Nullable WorldEditSession editSession,
                                    @Nullable Function<ItemStack, ItemStack> dropItemFunction) {
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        block.setMetadata("drop-items", new FixedMetadataValue(plugin, tool == null));

        plugin.getEvents().callBreakEvent(blockBreakEvent, true);

        block.removeMetadata("drop-items", plugin);

        if (blockBreakEvent.isCancelled())
            return false;

        plugin.getEvents().callBreakEvent(blockBreakEvent, false);

        Material originalType = block.getType();
        Location blockLocation = block.getLocation();

        if (editSession == null) {
            plugin.getNMSWorld().setCropState(block, CropState.SEEDED);
        } else {
            boolean result = editSession.setType(blockLocation, false,
                    vec -> plugin.getNMSWorld().setCropState(block, CropState.SEEDED),
                    WorldEditSession.SetBlockPriority.CROPS);

            if (!result)
                return false;
        }

        if (tool != null) {
            collectDropsFromTool(player, block, tool, editSession, dropItemFunction);

            if (tool.hasStatistics()) {
                try {
                    player.incrementStatistic(Statistic.MINE_BLOCK, originalType);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        return true;
    }

    public static boolean placeBlock(Player player, Block block, Block materialBlock,
                                     @Nullable WorldEditSession editSession) {
        BlockPlaceEvent blockPlaceEvent = plugin.getNMSAdapter().getFakePlaceEvent(player, block, materialBlock);
        plugin.getEvents().callPlaceEvent(blockPlaceEvent);

        if (blockPlaceEvent.isCancelled())
            return false;

        if (editSession == null) {
            block.setType(materialBlock.getType());
            block.setData(materialBlock.getData());
        } else {
            boolean result = editSession.setType(block.getLocation(), materialBlock);
            if (!result)
                return false;
        }

        return true;
    }

    public static List<ItemStack> getBlockDrops(Player player, Block block, Tool tool) {
        List<ItemStack> drops = new ArrayList<>();

        if (plugin.getProviders().getBlockDrops(drops, player, block, false))
            return drops;

        if (!Boolean.parseBoolean(block.getWorld().getGameRuleValue("doTileDrops")))
            return new ArrayList<>();

        return plugin.getNMSWorld().getBlockDrops(player, block, tool.hasSilkTouch());
    }

    private static void collectDropsFromTool(Player player, Block block, Tool tool,
                                             @Nullable WorldEditSession editSession,
                                             @Nullable Function<ItemStack, ItemStack> dropItemFunction) {
        List<ItemStack> naturalDrops = tool.filterDrops(getBlockDrops(player, block, tool));
        List<ItemStack> dropsToEditSession = new LinkedList<>();

        Location blockLocation = block.getLocation();

        for (ItemStack itemStack : naturalDrops) {
            if (dropItemFunction != null)
                itemStack = dropItemFunction.apply(itemStack);

            if (itemStack != null) {
                if (tool.isAutoCollect()) {
                    ItemUtils.addItem(itemStack, player.getInventory(), blockLocation, editSession);
                } else {
                    dropsToEditSession.add(itemStack);
                }
            }
        }

        if (editSession != null && !dropsToEditSession.isEmpty())
            editSession.addDrops(dropsToEditSession);
    }


    private static boolean shouldForceUpdate(Block frameBlock) {
        for (BlockFace blockFace : blockFaces) {
            Material blockType = frameBlock.getRelative(blockFace).getType();
            if ((blockFace == BlockFace.UP && !blockType.isSolid() && blockType != Material.AIR) ||
                    Materials.isForceUpdate(blockType))
                return true;
        }

        return false;
    }

}
