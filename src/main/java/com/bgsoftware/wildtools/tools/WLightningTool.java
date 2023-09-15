package com.bgsoftware.wildtools.tools;

import com.bgsoftware.wildtools.api.events.LightningWandUseEvent;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.api.objects.tools.LightningTool;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.LinkedList;
import java.util.List;

public class WLightningTool extends WTool implements LightningTool {

    public WLightningTool(Material type, String name) {
        super(type, name, ToolMode.LIGHTNING);
    }

    @Override
    public boolean onAirInteract(PlayerInteractEvent e) {
        Location eye = e.getPlayer().getEyeLocation();

        for (Entity entity : e.getPlayer().getNearbyEntities(10, 10, 10)) {
            if (entity instanceof Creeper && !((Creeper) entity).isPowered()) {
                if (checkVector(((LivingEntity) entity).getEyeLocation(), eye) ||
                        checkVector(entity.getLocation(), eye) || checkVector(entity.getLocation().subtract(0, 1, 0), eye)) {
                    handleUse(e.getPlayer(), e.getItem(), entity);
                    break;
                }
            }
        }

        return true;
    }

    @Override
    public boolean onBlockInteract(PlayerInteractEvent e) {
        return onAirInteract(e);
    }

    private boolean checkVector(Location location, Location eye) {
        Vector toEntity = location.toVector().subtract(eye.toVector());
        double dot = toEntity.normalize().dot(eye.getDirection());
        return dot > 0.9D;
    }

    private void handleUse(Player player, ItemStack usedItem, Entity entity) {
        int toolDurability = getDurability(player, usedItem);
        if (toolDurability <= 0)
            return;

        List<Creeper> creeperList = new LinkedList<>();

        if (entity instanceof Creeper) {
            creeperList.add((Creeper) entity);
        }

        for (Entity nearby : entity.getNearbyEntities(3, 3, 3)) {
            if (nearby instanceof Creeper) {
                if (creeperList.size() >= toolDurability)
                    break;

                creeperList.add((Creeper) nearby);
            }
        }

        LightningWandUseEvent lightningWandUseEvent = new LightningWandUseEvent(player, this, creeperList);
        Bukkit.getPluginManager().callEvent(lightningWandUseEvent);

        player.getWorld().strikeLightningEffect(entity.getLocation());
        for (Creeper creeper : creeperList)
            creeper.setPowered(true);

        boolean usingDurability = isUsingDurability();

        if (creeperList.size() > 0)
            reduceDurablility(player, usingDurability ? creeperList.size() : 1, usedItem);
    }

}
