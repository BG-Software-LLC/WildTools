package com.bgsoftware.wildtools.tools;

import com.bgsoftware.wildtools.Locale;
import com.bgsoftware.wildtools.api.events.HarvesterHoeSellEvent;
import com.bgsoftware.wildtools.api.events.HarvesterHoeUseEvent;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.api.objects.tools.HarvesterTool;
import com.bgsoftware.wildtools.scheduler.Scheduler;
import com.bgsoftware.wildtools.utils.BukkitUtils;
import com.bgsoftware.wildtools.utils.Materials;
import com.bgsoftware.wildtools.utils.items.ItemUtils;
import com.bgsoftware.wildtools.utils.items.ToolItemStack;
import com.bgsoftware.wildtools.utils.math.NumberUtils;
import com.bgsoftware.wildtools.utils.world.WorldEditSession;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class WHarvesterTool extends WTool implements HarvesterTool {

    private static final Material BAMBOO = Materials.getSafeMaterial("BAMBOO").orElse(null);
    private static final Material CHORUS_FLOWER = Materials.getSafeMaterial("CHORUS_FLOWER").orElse(null);

    private static final List<BlockFace> nearbyBlocks = new LinkedList<>(
            Arrays.asList(BlockFace.UP, BlockFace.DOWN, BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH));

    private final int radius;

    private int farmlandRadius;
    private HarvesterAction activateAction;
    private boolean oneLayerOnly;

    public WHarvesterTool(Material type, String name, int radius) {
        super(type, name, ToolMode.HARVESTER);
        this.radius = radius;
        this.farmlandRadius = -1;
        this.oneLayerOnly = false;
        this.activateAction = HarvesterAction.RIGHT_CLICK;
    }

    @Override
    public int getRadius() {
        return radius;
    }

    @Override
    public int getFarmlandRadius() {
        return farmlandRadius;
    }

    @Override
    public void setFarmlandRadius(int farmlandRadius) {
        this.farmlandRadius = farmlandRadius;
    }

    @Override
    public String getActivationAction() {
        return activateAction.name();
    }

    @Override
    public void setActivationAction(String activateAction) {
        try {
            this.activateAction = HarvesterAction.valueOf(activateAction.toUpperCase(java.util.Locale.ENGLISH));
        } catch (Throwable ignored) {
        }
    }

    @Override
    public boolean isOneLayerOnly() {
        return oneLayerOnly;
    }

    @Override
    public void setOneLayerOnly(boolean oneLayerOnly) {
        this.oneLayerOnly = oneLayerOnly;
    }

    @Override
    public boolean onAirInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR || !e.getPlayer().isSneaking() || !e.getPlayer().hasPermission("wildtools.sellmode"))
            return false;

        ToolItemStack toolItemStack = ToolItemStack.of(e.getItem());

        boolean sellMode = toolItemStack.hasSellMode();

        if (sellMode) {
            toolItemStack.setSellMode(false);
            Locale.SELL_MODE_DISABLED.send(e.getPlayer());
        } else {
            toolItemStack.setSellMode(true);
            Locale.SELL_MODE_ENABLED.send(e.getPlayer());
        }

        ItemUtils.formatItemStack(toolItemStack);

        return true;
    }

    @Override
    public boolean onBlockInteract(PlayerInteractEvent e) {
        // Preventing usage of harvester hoes as regular hoes
        e.setCancelled(true);

        if (this.activateAction != HarvesterAction.RIGHT_CLICK)
            return false;

        return handleUse(e.getPlayer(), e.getClickedBlock(), ToolItemStack.of(e.getItem()));
    }

    @Override
    public boolean onBlockHit(PlayerInteractEvent e) {
        // Preventing usage of harvester hoes as regular hoes
        e.setCancelled(true);

        if (this.activateAction != HarvesterAction.LEFT_CLICK)
            return false;

        return handleUse(e.getPlayer(), e.getClickedBlock(), ToolItemStack.of(e.getItem()));
    }

    private boolean isBetweenBlocks(Location max, Location min, Location location) {
        return location.getBlockX() >= min.getBlockX() && location.getBlockX() <= max.getBlockX() &&
                location.getBlockY() >= min.getBlockY() && location.getBlockY() <= max.getBlockY() &&
                location.getBlockZ() >= min.getBlockZ() && location.getBlockZ() <= max.getBlockZ();
    }

    private boolean handleUse(Player player, Block block, ToolItemStack usedItem) {
        Location farmlandMax = block.getLocation().add(farmlandRadius, oneLayerOnly ? 0 : farmlandRadius, farmlandRadius);
        Location farmlandMin = block.getLocation().subtract(farmlandRadius, oneLayerOnly ? 0 : farmlandRadius, farmlandRadius);
        Location cropsMax = block.getLocation().add(radius, oneLayerOnly ? 0 : radius, radius);
        Location cropsMin = block.getLocation().subtract(radius, oneLayerOnly ? 0 : radius, radius);

        Location absoluteMax = new Location(farmlandMax.getWorld(), Math.max(farmlandMax.getBlockX(), cropsMax.getBlockX()),
                Math.max(farmlandMax.getBlockY(), cropsMax.getBlockY()), Math.max(farmlandMax.getBlockZ(), cropsMax.getBlockZ()));
        Location absoluteMin = new Location(farmlandMin.getWorld(), Math.min(farmlandMin.getBlockX(), cropsMin.getBlockX()),
                Math.min(farmlandMin.getBlockY(), cropsMin.getBlockY()), Math.min(farmlandMin.getBlockZ(), cropsMin.getBlockZ()));

        World world = block.getWorld();

        WorldEditSession editSession = new WorldEditSession(world);
        SellInfo sellInfo = new SellInfo(usedItem.hasSellMode() && player.hasPermission("wildtools.sellmode"));

        int toolDurability = getDurability(player, usedItem.getItem());
        boolean usingDurability = isUsingDurability();
        Set<Location> alreadyBroken = new HashSet<>();
        int toolUsages = 0;

        outerLoop:
        for (int y = absoluteMax.getBlockY(); y >= absoluteMin.getBlockY(); y--) {
            for (int x = absoluteMin.getBlockX(); x <= absoluteMax.getBlockX(); x++) {
                for (int z = absoluteMin.getBlockZ(); z <= absoluteMax.getBlockZ(); z++) {
                    if (usingDurability && toolUsages >= toolDurability)
                        break outerLoop;

                    Location blockLocation = new Location(world, x, y, z);
                    Block targetBlock = blockLocation.getBlock();
                    Material blockType = targetBlock.getType();

                    if (!Materials.isHarvestable(blockType) || !BukkitUtils.canBreakBlock(player, targetBlock, this))
                        continue;

                    if (farmlandRadius >= 0 && Materials.isFarmland(blockType) &&
                            isBetweenBlocks(farmlandMax, farmlandMin, blockLocation) &&
                            BukkitUtils.hasBreakAccess(block, player)) {
                        editSession.setType(blockLocation, Materials.getFarmlandId());
                        toolUsages++;
                        continue;
                    }

                    if (!isBetweenBlocks(cropsMax, cropsMin, blockLocation))
                        continue;

                    if (Materials.isChorus(blockType)) {
                        toolUsages += breakChorusFruit(player, targetBlock, usedItem.getItem(), sellInfo,
                                alreadyBroken, toolUsages, toolDurability, usingDurability, false, editSession);
                        continue;
                    }

                    if (!Materials.isCrop(blockType) || !plugin.getNMSWorld().isFullyGrown(targetBlock))
                        continue;

                    if (blockType == Material.CACTUS || blockType == Materials.SUGAR_CANE.toBukkitType() ||
                            blockType == BAMBOO) {
                        if (y == cropsMax.getBlockY()) {
                            // Checking if the block is the bottom crop
                            if (targetBlock.getRelative(BlockFace.DOWN).getType() != blockType) {
                                Block aboveBlock = targetBlock.getRelative(BlockFace.UP);
                                //Making sure there's a valid crop on top of the bottom one
                                if (aboveBlock.getType() == blockType)
                                    toolUsages += breakTallCrop(player, aboveBlock, usedItem.getItem(), sellInfo,
                                            toolUsages, toolDurability, usingDurability, editSession);
                            } else {
                                toolUsages += breakTallCrop(player, targetBlock, usedItem.getItem(), sellInfo,
                                        toolUsages, toolDurability, usingDurability, editSession);
                            }

                            continue;
                        }

                        //Making sure it's not the bottom crop
                        if (targetBlock.getRelative(BlockFace.DOWN).getType() != blockType)
                            continue;

                        if (BukkitUtils.breakBlock(player, targetBlock, usedItem.getItem(), this, editSession,
                                itemStack -> sellInfo.handleItem(player, itemStack)))
                            toolUsages++;
                        continue;
                    }

                    if (BukkitUtils.seedBlock(player, targetBlock, this, editSession,
                            itemStack -> sellInfo.handleItem(player, itemStack)))
                        toolUsages++;
                }
            }
        }

        HarvesterHoeUseEvent harvesterHoeUseEvent = new HarvesterHoeUseEvent(player, this, editSession.getAffectedBlocks());
        Bukkit.getPluginManager().callEvent(harvesterHoeUseEvent);

        if (harvesterHoeUseEvent.isCancelled())
            return true;

        if (sellInfo.hasSellMode) {
            double multiplier = getMultiplier();

            String message = sellInfo.itemsToSell.isEmpty() ? Locale.NO_SELL_ITEMS.getMessage() :
                    Locale.HARVESTER_SELL_SUCCEED.getMessage();

            HarvesterHoeSellEvent harvesterHoeSellEvent = new HarvesterHoeSellEvent(player, sellInfo.totalPrice,
                    multiplier, message == null ? "" : message);
            Bukkit.getPluginManager().callEvent(harvesterHoeSellEvent);

            if (!harvesterHoeSellEvent.isCancelled()) {
                multiplier = harvesterHoeSellEvent.getMultiplier();
                sellInfo.totalPrice = harvesterHoeSellEvent.getPrice() * multiplier;

                plugin.getProviders().getEconomyProvider().depositPlayer(player, sellInfo.totalPrice);

                //noinspection all
                message = harvesterHoeSellEvent.getMessage()
                        .replace("{0}", sellInfo.totalAmount + "")
                        .replace("{1}", NumberUtils.format(sellInfo.totalPrice))
                        .replace("{2}", multiplier != 1 && Locale.MULTIPLIER.getMessage() != null ? Locale.MULTIPLIER.getMessage(multiplier) : "");
            }

            if (message != null && !message.isEmpty())
                player.sendMessage(message);
        }

        editSession.apply();

        if (toolUsages > 0)
            reduceDurablility(player, usingDurability ? toolUsages : 1, usedItem.getItem());

        return true;
    }

    private int breakChorusFruit(Player player, Block block, ItemStack usedItem, SellInfo sellInfo,
                                 Set<Location> alreadyBroken, int toolUsages, int toolDurability,
                                 boolean usingDurability, boolean foundFlower, WorldEditSession editSession) {
        int currentUsages = 0;

        if (usingDurability && toolUsages >= toolDurability)
            return currentUsages;

        Location blockLocation = block.getLocation();

        if (!alreadyBroken.add(blockLocation))
            return currentUsages;

        if (Materials.isEndBlock(block.getRelative(BlockFace.DOWN).getType())) {
            Scheduler.runTask(blockLocation, () -> {
                block.setType(CHORUS_FLOWER);
                if (Scheduler.isRegionScheduler()) {
                    Scheduler.runTask(player, () -> player.getInventory().removeItem(new ItemStack(CHORUS_FLOWER)));
                } else {
                    player.getInventory().removeItem(new ItemStack(CHORUS_FLOWER));
                }
            }, 2L);
            return currentUsages;
        }

        boolean isFlower = Materials.isFlower(block.getType());

        if (BukkitUtils.breakBlock(player, block, usedItem, this, editSession, itemStack ->
                sellInfo.handleItem(player, isFlower ? new ItemStack(CHORUS_FLOWER) : itemStack)))
            currentUsages++;

        for (BlockFace blockFace : nearbyBlocks) {
            Block nearbyBlock = block.getRelative(blockFace);
            if (Materials.isChorus(nearbyBlock.getType()))
                currentUsages += breakChorusFruit(player, nearbyBlock, usedItem, sellInfo, alreadyBroken,
                        toolUsages + currentUsages, toolDurability, usingDurability,
                        isFlower || foundFlower, editSession);
        }

        return currentUsages;
    }

    private int breakTallCrop(Player player, Block block, ItemStack usedItem, SellInfo sellInfo, int toolUsages,
                              int toolDurability, boolean usingDurability, WorldEditSession editSession) {
        int currentUsages = 0;

        Block aboveBlock = block.getRelative(BlockFace.UP);

        if (aboveBlock.getType() == block.getType())
            currentUsages += breakTallCrop(player, aboveBlock, usedItem, sellInfo,
                    toolUsages + currentUsages, toolDurability, usingDurability, editSession);

        if (usingDurability && (toolUsages + currentUsages) >= toolDurability)
            return currentUsages;

        if (BukkitUtils.breakBlock(player, block, usedItem, this, editSession,
                itemStack -> sellInfo.handleItem(player, itemStack)))
            currentUsages++;

        return currentUsages;
    }

    private static class SellInfo {

        private final boolean hasSellMode;

        private final List<ItemStack> itemsToSell = new ArrayList<>();
        private double totalPrice = 0;
        private int totalAmount = 0;

        SellInfo(boolean hasSellMode) {
            this.hasSellMode = hasSellMode;
        }

        ItemStack handleItem(Player player, ItemStack itemStack) {
            if (!hasSellMode)
                return itemStack;

            double price = plugin.getProviders().getPrice(player, itemStack);
            if (price > 0) {
                itemsToSell.add(itemStack);
                totalPrice += price;
                totalAmount += itemStack.getAmount();
                return null;
            }

            return itemStack;
        }

    }

    private enum HarvesterAction {

        RIGHT_CLICK,
        LEFT_CLICK

    }

}
