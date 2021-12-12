package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.common.reflection.ReflectMethod;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Set;

public final class McMMOHook {

    private static ReflectMethod<Object> MCMMO_GET_PLACESTORE = null;
    private static ReflectMethod<Void> MCMMO_PLACESTORE_SET = null;

    public static final boolean isEnabled = Bukkit.getPluginManager().isPluginEnabled("mcMMO");
    public static final Set<Location> doubleDropLocations = new HashSet<>();

    static {
        if (isEnabled) {
            MCMMO_GET_PLACESTORE = new ReflectMethod<>(com.gmail.nossr50.mcMMO.class, "getPlaceStore");

            try {
                Class<?> placeStoreRetClass = Class.forName("com.gmail.nossr50.util.blockmeta.chunkmeta.ChunkManager");
                MCMMO_PLACESTORE_SET = new ReflectMethod<>(placeStoreRetClass, "setTrue", Block.class);
            } catch (Throwable ignored) {}
        }
    }

    public static void markAsPlaced(Location loc) {
        if (!isEnabled)
            return;

        Block block = loc.getBlock();

        try {
            com.gmail.nossr50.mcMMO.getPlaceStore().setTrue(block);
        } catch (Throwable error) {
            Object placeStore = MCMMO_GET_PLACESTORE.invoke(null);
            MCMMO_PLACESTORE_SET.invoke(placeStore, block);
        }
    }

}
