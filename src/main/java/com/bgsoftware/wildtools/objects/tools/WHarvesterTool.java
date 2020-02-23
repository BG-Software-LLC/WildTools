package com.bgsoftware.wildtools.objects.tools;

import com.bgsoftware.wildtools.api.events.HarvesterHoeSellEvent;
import com.bgsoftware.wildtools.api.events.HarvesterHoeUseEvent;
import com.bgsoftware.wildtools.api.objects.tools.HarvesterTool;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.objects.WMaterial;
import com.bgsoftware.wildtools.utils.BukkitUtils;
import com.bgsoftware.wildtools.utils.blocks.BlocksController;
import com.bgsoftware.wildtools.utils.items.ItemUtils;
import com.bgsoftware.wildtools.utils.items.ToolTaskManager;
import com.bgsoftware.wildtools.Locale;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.CropState;
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
import java.util.UUID;
import java.util.function.Consumer;

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

    private static final Consumer<Block> onCropsBreak = blockConsumer ->
            plugin.getNMSAdapter().setCropState(blockConsumer, CropState.SEEDED);

    private int radius, farmlandRadius;
    private String activateAction;

    public WHarvesterTool(Material type, String name, int radius){
        super(type, name, ToolMode.HARVESTER);
        this.radius = radius;
        this.farmlandRadius = 0;
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
    public boolean onAirInteract(PlayerInteractEvent e) {
        if(e.getAction() != Action.RIGHT_CLICK_AIR || !e.getPlayer().isSneaking() || !e.getPlayer().hasPermission("wildtools.sellmode"))
            return false;

        boolean sellMode = hasSellMode(e.getItem());
        ItemStack itemStack;

        if(sellMode){
            itemStack = plugin.getNMSAdapter().setTag(e.getItem(), "sell-mode", 0);
            Locale.SELL_MODE_DISABLED.send(e.getPlayer());
        }
        else{
            itemStack = plugin.getNMSAdapter().setTag(e.getItem(), "sell-mode", 1);
            Locale.SELL_MODE_ENABLED.send(e.getPlayer());
        }

        ItemUtils.formatItemStack(this, itemStack, getDefaultUses(), !sellMode, () ->
            plugin.getNMSAdapter().setItemInHand(e.getPlayer(), itemStack));

        return true;
    }

    @Override
    public boolean onBlockInteract(PlayerInteractEvent e) {
        if(!getActivationAction().equals("RIGHT_CLICK") ){
            e.setCancelled(true);
            return false;
        }
        return handleUse(e.getPlayer(), e.getClickedBlock(), e.getItem());
    }

    @Override
    public boolean onBlockHit(PlayerInteractEvent e) {
        if(!getActivationAction().equals("LEFT_CLICK") ){
            e.setCancelled(true);
            return false;
        }
        return handleUse(e.getPlayer(), e.getClickedBlock(), e.getItem());
    }

    public boolean hasSellMode(ItemStack itemStack){
        return plugin.getNMSAdapter().getTag(itemStack, "sell-mode", 0) == 1;
    }

    private boolean isBetweenBlocks(Location max, Location min, Location location){
        return location.getBlockX() >= min.getBlockX() && location.getBlockX() <= max.getBlockX() &&
                location.getBlockY() >= min.getBlockY() && location.getBlockY() <= max.getBlockY() &&
                location.getBlockZ() >= min.getBlockZ() && location.getBlockZ() <= max.getBlockZ();
    }

    private boolean handleUse(Player player, Block block, ItemStack usedItem){
        UUID taskId = ToolTaskManager.generateTaskId(usedItem, player);

        Location farmlandMax = block.getLocation().add(farmlandRadius, farmlandRadius, farmlandRadius);
        Location farmlandMin = block.getLocation().subtract(farmlandRadius, farmlandRadius, farmlandRadius);
        Location cropsMax = block.getLocation().add(radius, radius, radius);
        Location cropsMin = block.getLocation().subtract(radius, radius, radius);

        Location absoluteMax = new Location(farmlandMax.getWorld(), Math.max(farmlandMax.getBlockX(), cropsMax.getBlockX()),
                Math.max(farmlandMax.getBlockY(), cropsMax.getBlockY()), Math.max(farmlandMax.getBlockZ(), cropsMax.getBlockZ()));
        Location absoluteMin = new Location(farmlandMin.getWorld(), Math.min(farmlandMin.getBlockX(), cropsMin.getBlockX()),
                Math.min(farmlandMin.getBlockY(), cropsMin.getBlockY()), Math.min(farmlandMin.getBlockZ(), cropsMin.getBlockZ()));

        BlocksController blocksController = new BlocksController();
        List<ItemStack> toSell = new ArrayList<>();
        ChangeableDouble _totalPrice = new ChangeableDouble();

        int toolDurability = getDurability(player, taskId);
        boolean usingDurability = isUsingDurability();
        int toolUsages = 0;

        Consumer<ItemStack> onItemDrop = itemConsumer -> {
            if(hasSellMode(usedItem) && player.hasPermission("wildtools.sellmode")) {
                double price = plugin.getProviders().getPrice(player, itemConsumer);
                if(price > 0) {
                    toSell.add(itemConsumer);
                    _totalPrice.add(price);
                }
            }
            else if (isAutoCollect()) {
                ItemUtils.addItem(itemConsumer, player.getInventory(), block.getLocation());
            }
            else {
                block.getWorld().dropItemNaturally(block.getLocation(), itemConsumer);
            }
        };

        outerLoop:
        for(int y = absoluteMax.getBlockY(); y >= absoluteMin.getBlockY(); y--){
            for(int x = absoluteMin.getBlockX(); x <= absoluteMax.getBlockX(); x++){
                for(int z = absoluteMin.getBlockZ(); z <= absoluteMax.getBlockZ(); z++){
                    if(usingDurability && toolUsages >= toolDurability)
                        break outerLoop;

                    Location blockLocation = new Location(player.getWorld(), x, y, z);
                    Block targetBlock = blockLocation.getBlock();

                    if(!plugin.getProviders().canBreak(player, targetBlock, this))
                        continue;

                    Material blockType = targetBlock.getType();

                    if((blockType == Material.DIRT || blockType == WMaterial.GRASS_BLOCK.parseMaterial()) &&
                        isBetweenBlocks(farmlandMax, farmlandMin, blockLocation)){
                        blocksController.setType(targetBlock.getLocation(), plugin.getNMSAdapter().getFarmlandId());
                        continue;
                    }

                    if(!isBetweenBlocks(cropsMax, cropsMin, blockLocation))
                        continue;

                    if(targetBlock.getType().name().contains("CHORUS")){
                        toolUsages += breakChorusFruit(player, blocksController, targetBlock, onItemDrop, new ArrayList<>(), toolUsages, toolDurability, usingDurability);
                        continue;
                    }

                    if(!crops.contains(blockType.name()) || !plugin.getNMSAdapter().isFullyGrown(targetBlock))
                        continue;

                    if (blockType == Material.CACTUS || blockType == WMaterial.SUGAR_CANE.parseMaterial() || blockType.name().equals("BAMBOO")) {
                        if(y == cropsMax.getBlockY()) {
                            toolUsages += breakTallCrop(player, blocksController, targetBlock, onItemDrop, toolUsages, toolDurability, usingDurability);
                            continue;
                        }

                        //Making sure it's not the bottom crop
                        if(targetBlock.getRelative(BlockFace.DOWN).getType() != blockType)
                            continue;

                        BukkitUtils.breakNaturally(player, blocksController, targetBlock, this, onItemDrop);
                        toolUsages++;
                        continue;
                    }

                    BukkitUtils.breakNaturally(player, targetBlock, this, onCropsBreak, onItemDrop);
                    toolUsages++;
                }
            }
        }

        HarvesterHoeUseEvent harvesterHoeUseEvent = new HarvesterHoeUseEvent(player, this, blocksController.getAffectedBlocks());
        Bukkit.getPluginManager().callEvent(harvesterHoeUseEvent);

        if(!toSell.isEmpty()){
            double multiplier = getMultiplier(), totalPrice = _totalPrice.value;

            String message = toSell.isEmpty() ? Locale.NO_SELL_ITEMS.getMessage() : Locale.HARVESTER_SELL_SUCCEED.getMessage();

            HarvesterHoeSellEvent harvesterHoeSellEvent = new HarvesterHoeSellEvent(player, totalPrice, multiplier, message);
            Bukkit.getPluginManager().callEvent(harvesterHoeSellEvent);

            if(!harvesterHoeSellEvent.isCancelled()) {
                multiplier = harvesterHoeSellEvent.getMultiplier();
                totalPrice = harvesterHoeSellEvent.getPrice() * multiplier;

                plugin.getProviders().depositPlayer(player, totalPrice);

                //noinspection all
                message = harvesterHoeSellEvent.getMessage()
                        .replace("{0}", toSell.size() + "")
                        .replace("{1}", totalPrice + "")
                        .replace("{2}", multiplier != 1 && Locale.MULTIPLIER.getMessage() != null ? Locale.MULTIPLIER.getMessage(multiplier) : "");

                if (!message.isEmpty())
                    player.sendMessage(message);
            }
        }

        blocksController.updateSession();

        if(toolUsages > 0) {
            reduceDurablility(player, usingDurability ? toolUsages : 1, taskId);
        } else {
            ToolTaskManager.removeTask(taskId);
        }

        return true;
    }

    private int breakChorusFruit(Player player, BlocksController blocksController, Block block, Consumer<ItemStack> onItemDrop, List<Block> alreadyBroken, int toolUsages, int toolDurability, boolean usingDurability){
        int currentUsages = 0;

        if(usingDurability && toolUsages >= toolDurability)
            return currentUsages;

        if(block.getType().name().contains("FLOWER") && block.getRelative(BlockFace.DOWN).getType().name().contains("END")){
            BukkitUtils.breakNaturally(player, block, this, onCropsBreak, onItemDrop);
            currentUsages++;
        }

        else {
            BukkitUtils.breakNaturally(player, blocksController, block, this, null);
            List<ItemStack> drops = new ArrayList<>();

            currentUsages++;

            if(block.getType().name().contains("FLOWER"))
                drops.add(new ItemStack(Material.matchMaterial("CHORUS_FLOWER")));
            else
                drops.addAll(BukkitUtils.getBlockDrops(player, block, this));

            drops = filterDrops(drops);

            for (ItemStack is : drops) {
                if (is != null && is.getType() != Material.AIR) {
                    onItemDrop.accept(is);
                }
            }
        }

        alreadyBroken.add(block);

        for(BlockFace blockFace : nearbyBlocks){
            Block nearbyBlock = block.getRelative(blockFace);
            if(nearbyBlock.getType().name().contains("CHORUS") && !alreadyBroken.contains(nearbyBlock)){
                currentUsages += breakChorusFruit(player, blocksController, nearbyBlock, onItemDrop, alreadyBroken, toolUsages + currentUsages, toolDurability, usingDurability);
            }
        }

        return currentUsages;
    }

    private int breakTallCrop(Player player, BlocksController blocksController, Block block, Consumer<ItemStack> onItemDrop, int toolUsages, int toolDurability, boolean usingDurability){
        int currentUsages = 0;

        Block aboveBlock = block.getRelative(BlockFace.UP);

        if(aboveBlock.getType() == block.getType())
            currentUsages += breakTallCrop(player, blocksController, aboveBlock, onItemDrop, toolUsages + currentUsages, toolDurability, usingDurability);

        if(usingDurability && (toolUsages + currentUsages) >= toolDurability)
            return currentUsages;

        BukkitUtils.breakNaturally(player, blocksController, block, this, onItemDrop);
        currentUsages++;

        return currentUsages;
    }

    private static final class ChangeableDouble{

        private double value = 0;

        void add(double value){
            this.value += value;
        }

    }

}
