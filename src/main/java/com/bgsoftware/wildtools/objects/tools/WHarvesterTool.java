package com.bgsoftware.wildtools.objects.tools;

import com.bgsoftware.wildtools.objects.WMaterial;
import com.bgsoftware.wildtools.utils.BukkitUtils;
import com.bgsoftware.wildtools.utils.Executor;
import com.bgsoftware.wildtools.utils.items.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.CropState;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.bgsoftware.wildtools.Locale;
import com.bgsoftware.wildtools.api.events.HarvesterHoeSellEvent;
import com.bgsoftware.wildtools.api.objects.tools.HarvesterTool;
import com.bgsoftware.wildtools.api.objects.ToolMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WHarvesterTool extends WTool implements HarvesterTool {

    public static String[] crops = new String[] {
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
    };

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

    boolean hasSellMode(ItemStack itemStack){
        return plugin.getNMSAdapter().getTag(itemStack, "sell-mode", 0) == 1;
    }

    private boolean handleUse(Player player, Block block, ItemStack toolItem){
        List<Block> dirtToHarvest = new ArrayList<>(), removeBlocks = new ArrayList<>(), seededBlocks = new ArrayList<>();
        Map<Block, List<ItemStack>> drops = new HashMap<>();

        Executor.async(() -> {
            Location max = block.getLocation().clone().add(farmlandRadius, farmlandRadius, farmlandRadius),
                    min = block.getLocation().clone().subtract(farmlandRadius, farmlandRadius, farmlandRadius);

            if(farmlandRadius > 0) {
                for (int y = max.getBlockY(); y >= min.getBlockY(); y--) {
                    for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
                        for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                            Block targetBlock = player.getWorld().getBlockAt(x, y, z);
                            if ((targetBlock.getType() == Material.DIRT || targetBlock.getType() == WMaterial.GRASS_BLOCK.parseMaterial()) &&
                                    targetBlock.getRelative(BlockFace.UP).getType() == Material.AIR) {
                                if (plugin.getProviders().canBreak(player, targetBlock, this)) {
                                    dirtToHarvest.add(targetBlock);
                                }
                            }
                        }
                    }
                }
            }

            max = block.getLocation().clone().add(radius, radius, radius);
            min = block.getLocation().clone().subtract(radius, radius, radius);

            for(int y = max.getBlockY(); y >= min.getBlockY(); y--) {
                for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
                    for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                        Block targetBlock = player.getWorld().getBlockAt(x, y, z);

                        if(removeBlocks.contains(targetBlock) || seededBlocks.contains(targetBlock) || dirtToHarvest.contains(targetBlock))
                            continue;

                        if(targetBlock.getType().name().contains("CHORUS")){
                            getChorusFruit(player, targetBlock, removeBlocks, seededBlocks, drops);
                            continue;
                        }

                        //Checks if it's a crop
                        if(!Arrays.asList(crops).contains(targetBlock.getType().name()))
                            continue;

                        //Checks if it's fully grown
                        if(!plugin.getNMSAdapter().isFullyGrown(targetBlock))
                            continue;

                        //Making sure the block is breakable.
                        if(!plugin.getProviders().canBreak(player, targetBlock, this))
                            continue;

                        if (targetBlock.getType() == Material.CACTUS || targetBlock.getType() == WMaterial.SUGAR_CANE.parseMaterial() ||
                                targetBlock.getType().name().equals("BAMBOO")) {
                            if(y == max.getBlockY()){
                                Material targetMaterial = targetBlock.getType();
                                while(targetBlock.getType() == targetMaterial){
                                    if(targetBlock.getRelative(BlockFace.DOWN).getType() == targetBlock.getType() &&
                                            plugin.getNMSAdapter().isFullyGrown(targetBlock) &&
                                            plugin.getProviders().canBreak(player, targetBlock, this)){
                                        removeBlocks.add(targetBlock);
                                        drops.put(targetBlock, BukkitUtils.getBlockDrops(player, targetBlock));
                                    }
                                    targetBlock = targetBlock.getRelative(BlockFace.UP);
                                }
                                continue;
                            }

                            //Making sure it's not the bottom crop
                            if(targetBlock.getRelative(BlockFace.DOWN).getType() != targetBlock.getType())
                                continue;

                            removeBlocks.add(targetBlock);
                            drops.put(targetBlock, BukkitUtils.getBlockDrops(player, targetBlock));
                            continue;
                        }

                        seededBlocks.add(targetBlock);
                        drops.put(targetBlock, BukkitUtils.getBlockDrops(player, targetBlock));
                    }
                }
            }

            Executor.sync(() -> {
                List<Block> toCheck = new ArrayList<>();
                boolean reduceDurability = false;

                for(Block _block : dirtToHarvest) {
                    if (plugin.getNMSAdapter().getItemInHand(player).getType() == Material.AIR)
                        break;

                    //Tool is using durability, reduces every block
                    if (isUsingDurability())
                        reduceDurablility(player);

                    _block.setType(WMaterial.FARMLAND.parseMaterial());
                    reduceDurability = true;
                }

                for(Block _block : removeBlocks) {
                    if (plugin.getNMSAdapter().getItemInHand(player).getType() == Material.AIR)
                        break;

                    //Tool is using durability, reduces every block
                    if (isUsingDurability())
                        reduceDurablility(player);

                    plugin.getNMSAdapter().setAirFast(_block);
                    toCheck.add(_block);
                    reduceDurability = true;
                }

                for(Block _block : seededBlocks) {
                    if (plugin.getNMSAdapter().getItemInHand(player).getType() == Material.AIR)
                        break;

                    //Tool is using durability, reduces every block
                    if (isUsingDurability())
                        reduceDurablility(player);

                    plugin.getNMSAdapter().setCropState(_block, CropState.SEEDED);
                    toCheck.add(_block);
                    reduceDurability = true;
                }

                double totalPrice = 0;
                List<ItemStack> toSell = new ArrayList<>();

                if(player.getGameMode() != GameMode.CREATIVE) {
                    for (Block targetBlock : toCheck) {
                        for (ItemStack drop : drops.getOrDefault(targetBlock, new ArrayList<>())) {
                            if (drop != null && drop.getType() != Material.AIR) {
                                if (hasSellMode(toolItem) && player.hasPermission("wildtools.sellmode") &&
                                        plugin.getProviders().canSellItem(player, drop)) {
                                    toSell.add(drop);
                                    totalPrice += plugin.getProviders().getPrice(player, drop);
                                } else if (isAutoCollect())
                                    ItemUtils.addItem(drop, player.getInventory(), block.getLocation());
                                else
                                    player.getWorld().dropItemNaturally(block.getLocation(), drop);
                            }
                        }
                    }
                }

                if(hasSellMode(toolItem)){
                    double multiplier = getMultiplier();

                    String message = toSell.isEmpty() ? Locale.NO_SELL_ITEMS.getMessage() : Locale.HARVESTER_SELL_SUCCEED.getMessage();

                    HarvesterHoeSellEvent harvesterHoeSellEvent = new HarvesterHoeSellEvent(player, totalPrice, multiplier, message);
                    Bukkit.getPluginManager().callEvent(harvesterHoeSellEvent);

                    if(!harvesterHoeSellEvent.isCancelled()) {
                        multiplier = harvesterHoeSellEvent.getMultiplier();
                        totalPrice *= multiplier;

                        for (ItemStack itemStack : toSell)
                            plugin.getProviders().trySellingItem(player, itemStack, multiplier);

                        //noinspection all
                        message = harvesterHoeSellEvent.getMessage()
                                .replace("{0}", toSell.size() + "")
                                .replace("{1}", totalPrice + "")
                                .replace("{2}", multiplier != 1 && Locale.MULTIPLIER.getMessage() != null ? Locale.MULTIPLIER.getMessage(multiplier) : "");

                        if (!message.isEmpty())
                            player.sendMessage(message);
                    }
                }

                //Tool is not using durability, reduces once per use
                if (reduceDurability && !isUsingDurability())
                    reduceDurablility(player);

            });
        });

        return true;
    }

    private BlockFace[] nearby = new BlockFace[]{
            BlockFace.UP, BlockFace.DOWN, BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH
    };

    private void getChorusFruit(Player player, Block block, List<Block> removeBlocks, List<Block> seededBlocks, Map<Block, List<ItemStack>> drops){
        if(block.getType().name().contains("FLOWER") && block.getRelative(BlockFace.DOWN).getType().name().contains("END")){
            seededBlocks.add(block);
        }
        else {
            removeBlocks.add(block);

            if(block.getType().name().contains("FLOWER"))
                drops.put(block, Collections.singletonList(new ItemStack(Material.matchMaterial("CHORUS_FLOWER"))));
            else
                drops.put(block, BukkitUtils.getBlockDrops(player, block));
        }

        for(BlockFace blockFace : nearby){
            Block nearbyBlock = block.getRelative(blockFace);
            if(nearbyBlock.getType().name().contains("CHORUS") && !removeBlocks.contains(nearbyBlock) && !seededBlocks.contains(nearbyBlock)){
                getChorusFruit(player, nearbyBlock, removeBlocks, seededBlocks, drops);
            }
        }
    }

}
