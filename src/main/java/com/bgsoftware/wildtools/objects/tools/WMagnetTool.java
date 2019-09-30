package com.bgsoftware.wildtools.objects.tools;

import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.api.objects.tools.MagnetTool;
import com.bgsoftware.wildtools.hooks.WildStackerHook;
import com.bgsoftware.wildtools.utils.Executor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.stream.Stream;

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
        handleUse(e.getPlayer());
        return true;
    }

    @Override
    public boolean onBlockInteract(PlayerInteractEvent e) {
        handleUse(e.getPlayer());
        return true;
    }

    private void handleUse(Player player){
        Stream<Item> nearbyItems = player.getNearbyEntities(radius, radius, radius).stream()
                .filter(entity -> entity instanceof Item).map(entity -> (Item) entity);
        Executor.async(() -> nearbyItems.forEach(item -> {
            if(!item.isValid() || item.isDead())
                return;

            ItemStack itemStack = Bukkit.getPluginManager().isPluginEnabled("WildStacker") ?
                    WildStackerHook.getItemStack(item) : item.getItemStack();

            if(player.getInventory().addItem(itemStack).isEmpty()){
                item.remove();
                plugin.getNMSAdapter().playPickupAnimation(player, item);
            }

        }));
    }

}
