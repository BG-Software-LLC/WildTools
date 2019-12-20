package com.bgsoftware.wildtools.objects.tools;

import com.bgsoftware.wildtools.utils.Executor;
import com.bgsoftware.wildtools.utils.items.ToolTaskManager;
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

        UUID taskId = ToolTaskManager.generateTaskId(usedItem, player.getInventory());

        Executor.async(() -> {
            List<Creeper> creeperList = new ArrayList<>();

            boolean reduceDurability = false;

            if(entity instanceof Creeper) {
                creeperList.add((Creeper) entity);
                //Tool is using durability, reduces every block
                if (isUsingDurability())
                    reduceDurablility(player, taskId);
                if(plugin.getNMSAdapter().getItemInHand(player).getType() == Material.AIR)
                    return;
                reduceDurability = true;
            }

            for(Entity nearby : nearbyEntities){
                if(nearby instanceof Creeper) {
                    creeperList.add((Creeper) nearby);
                    //Tool is using durability, reduces every block
                    if (isUsingDurability())
                        reduceDurablility(player, taskId);
                    if(plugin.getNMSAdapter().getItemInHand(player).getType() == Material.AIR)
                        break;
                    reduceDurability = true;
                }
            }

            //Tool is using durability, reduces every block
            if (reduceDurability && !isUsingDurability())
                reduceDurablility(player, taskId);

            Executor.sync(() -> {
                player.getWorld().strikeLightningEffect(entity.getLocation());
                for(Creeper creeper : creeperList)
                    creeper.setPowered(true);
            });
        });
    }

}
