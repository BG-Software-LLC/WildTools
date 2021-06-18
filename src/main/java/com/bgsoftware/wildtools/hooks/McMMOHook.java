package com.bgsoftware.wildtools.hooks;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;

public final class McMMOHook {

    public static final boolean isEnabled = Bukkit.getPluginManager().isPluginEnabled("mcMMO");
    public static final Set<Location> doubleDropLocations = new HashSet<>();

    public static void markAsPlaced(Location loc){
        if(isEnabled)
            com.gmail.nossr50.mcMMO.getPlaceStore().setTrue(loc.getBlock());
    }

}
