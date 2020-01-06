package com.bgsoftware.wildtools.objects.tools;

import com.bgsoftware.wildtools.api.events.MagnetWandUseEvent;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.api.objects.tools.MagnetTool;
import com.bgsoftware.wildtools.hooks.WildStackerHook;
import com.bgsoftware.wildtools.utils.Executor;
import com.bgsoftware.wildtools.utils.items.ToolTaskManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public final class WMagnetTool extends WTool implements MagnetTool {

    private int radius;

    public WMagnetTool(Material type, String name, int radius){
        super(type, name, ToolMode.MAGNET);
        this.radius = radius;
    }

    @Override
    public int getRadius() {
        return radius;
    }

    @Override
    public boolean onAirInteract(PlayerInteractEvent e) {
        handleUse(e.getPlayer(), e.getItem());
        return true;
    }

    @Override
    public boolean onBlockInteract(PlayerInteractEvent e) {
        handleUse(e.getPlayer(), e.getItem());
        return true;
    }

    private void handleUse(Player player, ItemStack usedItem){
        List<Item> nearbyItems = player.getNearbyEntities(radius, radius, radius).stream()
                .filter(entity -> entity instanceof Item).map(entity -> (Item) entity).collect(Collectors.toList());

        UUID taskId = ToolTaskManager.generateTaskId(usedItem, player.getInventory());

        Executor.async(() -> {
            boolean reduceDurability = false;

            List<Item> affectedItems = new ArrayList<>();

            for(Item item : nearbyItems) {
                if (!item.isValid() || item.isDead())
                    continue;

                ItemStack itemStack = Bukkit.getPluginManager().isPluginEnabled("WildStacker") ?
                        WildStackerHook.getItemStack(item) : item.getItemStack();

                Map<Integer, ItemStack> additionalItems = player.getInventory().addItem(itemStack);

                if (additionalItems.isEmpty()) {
                    affectedItems.add(item);
                    item.remove();
                    plugin.getNMSAdapter().playPickupAnimation(player, item);
                    reduceDurability = true;
                }
                else{
                    ItemStack additionalItem = additionalItems.get(0);
                    affectedItems.add(item);
                    if(Bukkit.getPluginManager().isPluginEnabled("WildStacker")){
                        WildStackerHook.setItemStack(item, additionalItem);
                    }
                    else {
                        item.setItemStack(additionalItem);
                    }
                    reduceDurability = true;
                }
            }

            Executor.sync(() -> {
                MagnetWandUseEvent magnetWandUseEvent = new MagnetWandUseEvent(player, this, affectedItems);
                Bukkit.getPluginManager().callEvent(magnetWandUseEvent);
            });

            if(reduceDurability)
                reduceDurablility(player, 1, taskId);

            ToolTaskManager.removeTask(taskId);
        });
    }

}
