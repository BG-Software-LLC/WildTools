package com.bgsoftware.wildtools.objects.tools;

import com.bgsoftware.wildtools.api.events.LightningWandUseEvent;
import com.bgsoftware.wildtools.utils.Executor;
import com.bgsoftware.wildtools.utils.items.ToolTaskManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import com.bgsoftware.wildtools.api.objects.tools.LightningTool;
import com.bgsoftware.wildtools.api.objects.ToolMode;

import org.bukkit.Material;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class WLightningTool extends WTool implements LightningTool {

    public WLightningTool(Material type, String name){
        super(type, name, ToolMode.LIGHTNING);
    }

    @Override
    public boolean onAirInteract(PlayerInteractEvent e) {
        Location eye = e.getPlayer().getEyeLocation();

        for(Entity entity : e.getPlayer().getNearbyEntities(10, 10, 10)){
            if(entity instanceof Creeper && !((Creeper) entity).isPowered()){
                Vector toEntity = ((LivingEntity) entity).getEyeLocation().toVector().subtract(eye.toVector());
                double dot = toEntity.normalize().dot(eye.getDirection());
                if(dot > 0.99D){
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

    private void handleUse(Player player, ItemStack usedItem, Entity entity){
        List<Entity> nearbyEntities = entity.getNearbyEntities(3, 3, 3);

        UUID taskId = ToolTaskManager.generateTaskId(usedItem, player);

        Executor.async(() -> {
            List<Creeper> creeperList = new ArrayList<>();

            int toolDurability = getDurability(player, taskId);
            boolean usingDurability = isUsingDurability();

            if(entity instanceof Creeper && (!usingDurability || creeperList.size() < toolDurability)) {
                creeperList.add((Creeper) entity);
            }

            for(Entity nearby : nearbyEntities){
                if(nearby instanceof Creeper) {
                    if(usingDurability && creeperList.size() >= toolDurability)
                        break;

                    creeperList.add((Creeper) nearby);
                }
            }

            Executor.sync(() -> {
                LightningWandUseEvent lightningWandUseEvent = new LightningWandUseEvent(player, this, creeperList);
                Bukkit.getPluginManager().callEvent(lightningWandUseEvent);

                player.getWorld().strikeLightningEffect(entity.getLocation());
                for(Creeper creeper : creeperList)
                    creeper.setPowered(true);
            });

            if(creeperList.size() > 0)
                reduceDurablility(player, usingDurability ? creeperList.size() : 1, taskId);

            ToolTaskManager.removeTask(taskId);
        });
    }

}
