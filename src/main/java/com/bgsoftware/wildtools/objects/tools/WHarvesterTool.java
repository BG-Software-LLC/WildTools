package com.bgsoftware.wildtools.objects.tools;

import com.bgsoftware.wildtools.api.events.HarvesterHoeSellEvent;
import com.bgsoftware.wildtools.api.events.HarvesterHoeUseEvent;
import com.bgsoftware.wildtools.api.objects.tools.HarvesterTool;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.objects.WMaterial;
import com.bgsoftware.wildtools.utils.BukkitUtils;
import com.bgsoftware.wildtools.utils.Executor;
import com.bgsoftware.wildtools.utils.NumberUtils;
import com.bgsoftware.wildtools.utils.items.ItemsDropper;
import com.bgsoftware.wildtools.utils.items.ToolItemStack;
import com.bgsoftware.wildtools.utils.blocks.BlocksController;
import com.bgsoftware.wildtools.utils.items.ItemUtils;
import com.bgsoftware.wildtools.Locale;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class WHarvesterTool extends WTool implements HarvesterTool {

    public static final List<String> crops = Arrays.asList(
            "CROPS", "WHEAT",
            "CARROT", "CARROTS",
            "POTATO", "POTATOES",
            "BEETROOT_BLOCK", "BEETROOTS",
            "NETHER_WARTS", "NETHER_WART",
            "CACTUS", "BAMBOO",
            "SUGAR_CANE_BLOCK", "SUGAR_CANE",
            "MELON_BLOCK", "MELON",
            "PUMPKIN",
            "COCOA"
    );

    private static final BlockFace[] nearbyBlocks = new BlockFace[]{
            BlockFace.UP, BlockFace.DOWN, BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH
    };

    private static final Material CHORUS_FLOWER = Material.getMaterial("CHORUS_FLOWER");

    private final int radius;

    private int farmlandRadius;
    private String activateAction;
    private boolean oneLayerOnly;

    public WHarvesterTool(Material type, String name, int radius){
        super(type, name, ToolMode.HARVESTER);
        this.radius = radius;
        this.farmlandRadius = -1;
        this.oneLayerOnly = false;
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
        return activateAction;
    }

    @Override
    public void setActivationAction(String activateAction) {
        if(activateAction.equals("RIGHT_CLICK") || activateAction.equals("LEFT_CLICK"))
            this.activateAction = activateAction;
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
        if(e.getAction() != Action.RIGHT_CLICK_AIR || !e.getPlayer().isSneaking() || !e.getPlayer().hasPermission("wildtools.sellmode"))
            return false;

        ToolItemStack toolItemStack = ToolItemStack.of(e.getItem());

        boolean sellMode = toolItemStack.hasSellMode();

        if(sellMode){
            toolItemStack.setSellMode(false);
            Locale.SELL_MODE_DISABLED.send(e.getPlayer());
        }
        else{
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

        if(!getActivationAction().equals("RIGHT_CLICK"))
            return false;

        return handleUse(e.getPlayer(), e.getClickedBlock(), ToolItemStack.of(e.getItem()));
    }

    @Override
    public boolean onBlockHit(PlayerInteractEvent e) {
        // Preventing usage of harvester hoes as regular hoes
        e.setCancelled(true);

        if(!getActivationAction().equals("LEFT_CLICK"))
            return false;

        return handleUse(e.getPlayer(), e.getClickedBlock(), ToolItemStack.of(e.getItem()));
    }

    private boolean isBetweenBlocks(Location max, Location min, Location location){
        return location.getBlockX() >= min.getBlockX() && location.getBlockX() <= max.getBlockX() &&
                location.getBlockY() >= min.getBlockY() && location.getBlockY() <= max.getBlockY() &&
                location.getBlockZ() >= min.getBlockZ() && location.getBlockZ() <= max.getBlockZ();
    }

    private boolean handleUse(Player player, Block block, ToolItemStack usedItem){
        Location farmlandMax = block.getLocation().add(farmlandRadius, oneLayerOnly ? 0 : farmlandRadius, farmlandRadius);
        Location farmlandMin = block.getLocation().subtract(farmlandRadius, oneLayerOnly ? 0 : farmlandRadius, farmlandRadius);
        Location cropsMax = block.getLocation().add(radius, oneLayerOnly ? 0 : radius, radius);
        Location cropsMin = block.getLocation().subtract(radius, oneLayerOnly ? 0 : radius, radius);

        Location absoluteMax = new Location(farmlandMax.getWorld(), Math.max(farmlandMax.getBlockX(), cropsMax.getBlockX()),
                Math.max(farmlandMax.getBlockY(), cropsMax.getBlockY()), Math.max(farmlandMax.getBlockZ(), cropsMax.getBlockZ()));
        Location absoluteMin = new Location(farmlandMin.getWorld(), Math.min(farmlandMin.getBlockX(), cropsMin.getBlockX()),
                Math.min(farmlandMin.getBlockY(), cropsMin.getBlockY()), Math.min(farmlandMin.getBlockZ(), cropsMin.getBlockZ()));

        BlocksController blocksController = new BlocksController();
        ItemsDropper itemsDropper = new ItemsDropper();
        SellInfo sellInfo = new SellInfo(usedItem.hasSellMode() && player.hasPermission("wildtools.sellmode"));

        int toolDurability = getDurability(player, usedItem.getItem());
        boolean usingDurability = isUsingDurability();
        List<Location> alreadyBroken = new ArrayList<>();
        int toolUsages = 0;

        outerLoop:
        for(int y = absoluteMax.getBlockY(); y >= absoluteMin.getBlockY(); y--){
            for(int x = absoluteMin.getBlockX(); x <= absoluteMax.getBlockX(); x++){
                for(int z = absoluteMin.getBlockZ(); z <= absoluteMax.getBlockZ(); z++){
                    if(usingDurability && toolUsages >= toolDurability)
                        break outerLoop;

                    Location blockLocation = new Location(player.getWorld(), x, y, z);
                    Block targetBlock = blockLocation.getBlock();
                    Material blockType = targetBlock.getType();

                    if(!isHarvestableBlock(blockType) || !BukkitUtils.canBreakBlock(player, targetBlock, this))
                        continue;

                    if(farmlandRadius >= 0 && (blockType == Material.DIRT || blockType == WMaterial.GRASS_BLOCK.parseMaterial()) &&
                            isBetweenBlocks(farmlandMax, farmlandMin, blockLocation) && BukkitUtils.hasBreakAccess(block, player)){
                        blocksController.setType(targetBlock.getLocation(), plugin.getNMSAdapter().getFarmlandId());
                        continue;
                    }

                    if(!isBetweenBlocks(cropsMax, cropsMin, blockLocation))
                        continue;

                    if(targetBlock.getType().name().contains("CHORUS")){
                        toolUsages += breakChorusFruit(player, blocksController, itemsDropper, targetBlock, usedItem.getItem(), sellInfo, alreadyBroken, toolUsages, toolDurability, usingDurability, false);
                        continue;
                    }

                    if(!crops.contains(blockType.name()) || !plugin.getNMSAdapter().isFullyGrown(targetBlock))
                        continue;

                    if (blockType == Material.CACTUS || blockType == WMaterial.SUGAR_CANE.parseMaterial() || blockType.name().equals("BAMBOO")) {
                        if(y == cropsMax.getBlockY()) {
                            // Checking if the block is the bottom crop
                            if(targetBlock.getRelative(BlockFace.DOWN).getType() != blockType) {
                                Block aboveBlock = targetBlock.getRelative(BlockFace.UP);
                                //Making sure there's a valid crop on top of the bottom one
                                if(aboveBlock.getType() == blockType)
                                    toolUsages += breakTallCrop(player, blocksController, itemsDropper, aboveBlock, usedItem.getItem(), sellInfo, toolUsages, toolDurability, usingDurability);
                            }
                            else {
                                toolUsages += breakTallCrop(player, blocksController, itemsDropper, targetBlock, usedItem.getItem(), sellInfo, toolUsages, toolDurability, usingDurability);
                            }
                            continue;
                        }

                        //Making sure it's not the bottom crop
                        if(targetBlock.getRelative(BlockFace.DOWN).getType() != blockType)
                            continue;

                        if(BukkitUtils.breakBlockAsBoolean(player, blocksController, itemsDropper, targetBlock, usedItem.getItem(), this,
                                itemStack -> !sellInfo.handleItem(player, itemStack)))
                            toolUsages++;
                        continue;
                    }

                    if(BukkitUtils.seedBlockAsBoolean(player, targetBlock, this, itemStack -> !sellInfo.handleItem(player, itemStack), itemsDropper))
                        toolUsages++;
                }
            }
        }

        HarvesterHoeUseEvent harvesterHoeUseEvent = new HarvesterHoeUseEvent(player, this, blocksController.getAffectedBlocks());
        Bukkit.getPluginManager().callEvent(harvesterHoeUseEvent);

        if(sellInfo.hasSellMode) {
            double multiplier = getMultiplier();

            String message = sellInfo.itemsToSell.isEmpty() ? Locale.NO_SELL_ITEMS.getMessage() :
                    Locale.HARVESTER_SELL_SUCCEED.getMessage();

            HarvesterHoeSellEvent harvesterHoeSellEvent = new HarvesterHoeSellEvent(player, sellInfo.totalPrice,
                    multiplier, message == null ? "" : message);
            Bukkit.getPluginManager().callEvent(harvesterHoeSellEvent);

            if (!harvesterHoeSellEvent.isCancelled()) {
                multiplier = harvesterHoeSellEvent.getMultiplier();
                sellInfo.totalPrice = harvesterHoeSellEvent.getPrice() * multiplier;

                plugin.getProviders().depositPlayer(player, sellInfo.totalPrice);

                //noinspection all
                message = harvesterHoeSellEvent.getMessage()
                        .replace("{0}", sellInfo.totalAmount + "")
                        .replace("{1}", NumberUtils.format(sellInfo.totalPrice))
                        .replace("{2}", multiplier != 1 && Locale.MULTIPLIER.getMessage() != null ? Locale.MULTIPLIER.getMessage(multiplier) : "");
            }

            if (message != null && !message.isEmpty())
                player.sendMessage(message);
        }

        blocksController.updateSession();
        itemsDropper.dropItems();

        if(toolUsages > 0)
            reduceDurablility(player, usingDurability ? toolUsages : 1, usedItem.getItem());

        return true;
    }

    private int breakChorusFruit(Player player, BlocksController blocksController, ItemsDropper itemsDropper,
                                 Block block, ItemStack usedItem, SellInfo sellInfo, List<Location> alreadyBroken,
                                 int toolUsages, int toolDurability, boolean usingDurability, boolean foundFlower){
        int currentUsages = 0;

        if(alreadyBroken.contains(block.getLocation()))
            return currentUsages;

        if(usingDurability && toolUsages >= toolDurability)
            return currentUsages;

        alreadyBroken.add(block.getLocation());

        if(block.getRelative(BlockFace.DOWN).getType().name().contains("END")) {
            Executor.sync(() -> {
                block.setType(CHORUS_FLOWER);
                player.getInventory().removeItem(new ItemStack(CHORUS_FLOWER));
            }, 2L);
            return currentUsages;
        }

        boolean isFlower = block.getType().name().contains("FLOWER");

        if(BukkitUtils.breakBlockAsBoolean(player, blocksController, itemsDropper, block, usedItem, this, itemStack ->
                !sellInfo.handleItem(player, isFlower ? new ItemStack(CHORUS_FLOWER) : itemStack)))
            currentUsages++;

        for(BlockFace blockFace : nearbyBlocks){
            Block nearbyBlock = block.getRelative(blockFace);
            if(nearbyBlock.getType().name().contains("CHORUS"))
                currentUsages += breakChorusFruit(player, blocksController, itemsDropper, nearbyBlock, usedItem,
                        sellInfo, alreadyBroken, toolUsages + currentUsages, toolDurability, usingDurability,
                        isFlower || foundFlower);
        }

        return currentUsages;
    }

    private int breakTallCrop(Player player, BlocksController blocksController, ItemsDropper itemsDropper, Block block,
                              ItemStack usedItem, SellInfo sellInfo, int toolUsages, int toolDurability, boolean usingDurability){
        int currentUsages = 0;

        Block aboveBlock = block.getRelative(BlockFace.UP);

        if(aboveBlock.getType() == block.getType())
            currentUsages += breakTallCrop(player, blocksController, itemsDropper, aboveBlock, usedItem, sellInfo, toolUsages + currentUsages, toolDurability, usingDurability);

        if(usingDurability && (toolUsages + currentUsages) >= toolDurability)
            return currentUsages;

        if(BukkitUtils.breakBlockAsBoolean(player, blocksController, itemsDropper, block, usedItem, this,
                itemStack -> !sellInfo.handleItem(player, itemStack)))
            currentUsages++;

        return currentUsages;
    }

    private boolean isHarvestableBlock(Material type){
        return type == Material.DIRT || type == WMaterial.GRASS_BLOCK.parseMaterial() ||
                type.name().contains("CHORUS") || crops.contains(type.name());
    }

    private static final class SellInfo {

        private final boolean hasSellMode;

        private final List<ItemStack> itemsToSell = new ArrayList<>();
        private double totalPrice = 0;
        private int totalAmount = 0;

        SellInfo(boolean hasSellMode){
            this.hasSellMode = hasSellMode;
        }

        @SuppressWarnings("all")
        boolean handleItem(Player player, ItemStack itemStack){
            if(!hasSellMode)
                return false;

            double price = plugin.getProviders().getPrice(player, itemStack);
            if(price > 0){
                itemsToSell.add(itemStack);
                totalPrice += price;
                totalAmount += itemStack.getAmount();
                return true;
            }

            return false;
        }

    }

}
