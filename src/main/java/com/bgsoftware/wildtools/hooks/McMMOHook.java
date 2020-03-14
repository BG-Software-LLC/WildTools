package com.bgsoftware.wildtools.hooks;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public final class McMMOHook {

    public static void markAsPlaced(Location loc){
        if(Bukkit.getPluginManager().isPluginEnabled("mcMMO"))
            com.gmail.nossr50.mcMMO.getPlaceStore().setTrue(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld());
    }

}
