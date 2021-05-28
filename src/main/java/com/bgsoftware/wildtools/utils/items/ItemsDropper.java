package com.bgsoftware.wildtools.utils.items;

import com.bgsoftware.wildtools.WildToolsPlugin;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class ItemsDropper {

    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    private final List<Object> drops = new ArrayList<>();

    public void addDrop(ItemStack itemStack, Location location){
        drops.add(plugin.getNMSAdapter().getDroppedItem(itemStack, location));
    }

    public void dropItems(){
        plugin.getNMSAdapter().dropItems(drops);
    }

}
