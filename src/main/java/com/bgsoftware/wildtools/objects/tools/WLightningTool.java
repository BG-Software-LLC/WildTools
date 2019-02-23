package com.bgsoftware.wildtools.objects.tools;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import com.bgsoftware.wildtools.api.objects.tools.LightningTool;
import com.bgsoftware.wildtools.api.objects.ToolMode;

import org.bukkit.Material;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public final class WLightningTool extends WTool implements LightningTool {

    public WLightningTool(Material type, String name){
        super(type, name, ToolMode.LIGHTNING);
    }

    @Override
    public boolean onAirInteract(PlayerInteractEvent e) {
        Location eye = e.getPlayer().getEyeLocation();

        for(Entity entity : e.getPlayer().getNearbyEntities(10, 10, 10)){
            if(entity instanceof LivingEntity){
                Vector toEntity = ((LivingEntity) entity).getEyeLocation().toVector().subtract(eye.toVector());
                double dot = toEntity.normalize().dot(eye.getDirection());
                if(dot > 0.99D){
                    handleUse(e.getPlayer(), entity);
                    break;
                }
            }
        }

        return true;
    }

    private void handleUse(Player player, Entity entity){
        new Thread(() -> {
            player.getWorld().strikeLightningEffect(entity.getLocation());

            if(entity instanceof Creeper)
                ((Creeper) entity).setPowered(true);

            for(Entity nearby : entity.getNearbyEntities(3, 3, 3)){
                if(nearby instanceof Creeper)
                    ((Creeper) nearby).setPowered(true);
            }
        }).start();
    }

}
