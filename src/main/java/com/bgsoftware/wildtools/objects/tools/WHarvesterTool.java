package com.bgsoftware.wildtools.objects.tools;

import com.bgsoftware.wildtools.objects.WMaterial;
import com.bgsoftware.wildtools.utils.BukkitUtil;
import com.bgsoftware.wildtools.utils.ItemUtil;
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
import xyz.wildseries.wildtools.api.events.HarvesterHoeSellEvent;
import xyz.wildseries.wildtools.api.objects.tools.HarvesterTool;
import xyz.wildseries.wildtools.api.objects.ToolMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class WHarvesterTool extends WTool implements HarvesterTool {

    public static String[] crops = new String[] {
            "CROPS", "WHEAT",
            "CARROT", "CARROTS",
            "POTATO", "POTATOES",
            "BEETROOT_BLOCK", "BEETROOTS",
            "NETHER_WARTS", "NETHER_WART",
            "CACTUS",
            "SUGAR_CANE_BLOCK", "SUGAR_CANE",
            "MELON_BLOCK", "MELON",
            "PUMPKIN",
            "COCOA"
    };

    private Set<UUID> sellModesPlayers;

    private int radius, farmlandRadius;
    private String activateAction;

    public WHarvesterTool(Material type, String name, int radius){
        super(type, name, ToolMode.HARVESTER);
        this.radius = radius;
        this.farmlandRadius = 0;
        this.sellModesPlayers = new HashSet<>();
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

        if(sellModesPlayers.contains(e.getPlayer().getUniqueId())){
            sellModesPlayers.remove(e.getPlayer().getUniqueId());
            Locale.SELL_MODE_DISABLED.send(e.getPlayer());
        }
        else{
            sellModesPlayers.add(e.getPlayer().getUniqueId());
            Locale.SELL_MODE_ENABLED.send(e.getPlayer());
        }

        return true;
    }

    @Override
    public boolean onBlockInteract(PlayerInteractEvent e) {
        return handleUse(e.getPlayer(), e.getClickedBlock());
    }

    @Override
    public boolean onBlockHit(PlayerInteractEvent e) {
        return handleUse(e.getPlayer(), e.getClickedBlock());
    }

    private boolean handleUse(Player player, Block block){
        Location max = block.getLocation().clone().add(farmlandRadius, farmlandRadius, farmlandRadius),
                min = block.getLocation().clone().subtract(farmlandRadius, farmlandRadius, farmlandRadius);

        if(farmlandRadius > 0) {
            for (int y = max.getBlockY(); y >= min.getBlockY(); y--) {
                for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
                    for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                        Block targetBlock = player.getWorld().getBlockAt(x, y, z);
                        if ((targetBlock.getType() == Material.DIRT || targetBlock.getType() == WMaterial.GRASS_BLOCK.parseMaterial()) &&
                                targetBlock.getRelative(BlockFace.UP).getType() == Material.AIR) {
                            if(isOnlyInsideClaim() && !plugin.getProviders().inClaim(player, targetBlock.getLocation()))
                                continue;
                            if (BukkitUtil.canBreak(player, targetBlock) && canBreakBlock(block, targetBlock)) {
                                targetBlock.setType(WMaterial.FARMLAND.parseMaterial());
                            }
                        }
                    }
                }
            }
        }

        max = block.getLocation().clone().add(radius, radius, radius);
        min = block.getLocation().clone().subtract(radius, radius, radius);

        double totalPrice = 0;
        List<ItemStack> toSell = new ArrayList<>();

        for(int y = max.getBlockY(); y >= min.getBlockY(); y--) {
            for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                    Block targetBlock = player.getWorld().getBlockAt(x, y, z);
                    if (Arrays.asList(crops).contains(targetBlock.getType().name())) {
                        if (targetBlock.getType() == Material.CACTUS || targetBlock.getType() == WMaterial.SUGAR_CANE.parseMaterial()) {
                            if(targetBlock.getRelative(BlockFace.DOWN).getType() != targetBlock.getType()){
                                continue;
                            }
                        }
                        if (plugin.getNMSAdapter().isFullyGrown(targetBlock) &&
                                BukkitUtil.canBreak(player, targetBlock) && canBreakBlock(block, targetBlock)) {
                            if(player.getGameMode() != GameMode.CREATIVE) {
                                for(ItemStack drop : BukkitUtil.getBlockDrops(player, targetBlock)){
                                    if(sellModesPlayers.contains(player.getUniqueId()) && player.hasPermission("wildtools.sellmode") &&
                                            plugin.getProviders().canSellItem(player, drop)) {
                                        toSell.add(drop);
                                        totalPrice += plugin.getProviders().getPrice(player, drop);
                                    }
                                    else if(isAutoCollect())
                                        ItemUtil.addItem(drop, player.getInventory(), block.getLocation());
                                    else
                                        player.getWorld().dropItemNaturally(block.getLocation(), drop);
                                }
                                //Tool is using durability, reduces every block
                                if (!isUnbreakable() && isUsingDurability() && player.getGameMode() != GameMode.CREATIVE)
                                    reduceDurablility(player);
                                if (plugin.getNMSAdapter().getItemInHand(player) == null) {
                                    break;
                                }
                            }
                            if(targetBlock.getType() == Material.CACTUS || targetBlock.getType() == WMaterial.SUGAR_CANE.parseMaterial() ||
                                    targetBlock.getType() == WMaterial.MELON.parseMaterial() || targetBlock.getType() == Material.PUMPKIN)
                                targetBlock.setType(Material.AIR);
                            else{
                                plugin.getNMSAdapter().setCropState(targetBlock, CropState.SEEDED);
                            }
                        }
                    }
                }
            }
        }

        if(sellModesPlayers.contains(player.getUniqueId())){
            HarvesterHoeSellEvent harvesterHoeSellEvent = new HarvesterHoeSellEvent(player, totalPrice, Locale.HARVESTER_SELL_SUCCEED.getMessage());
            Bukkit.getPluginManager().callEvent(harvesterHoeSellEvent);

            totalPrice = harvesterHoeSellEvent.getPrice();

            if(!harvesterHoeSellEvent.isCancelled()) {

                for(ItemStack itemStack : toSell)
                    plugin.getProviders().trySellingItem(player, itemStack);

                player.sendMessage(harvesterHoeSellEvent.getMessage()
                        .replace("{0}", toSell.size() + "").replace("{1}", totalPrice + ""));
            }
        }

        return true;
    }

}
