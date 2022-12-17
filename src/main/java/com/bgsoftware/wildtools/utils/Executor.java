package com.bgsoftware.wildtools.utils;

import com.bgsoftware.wildtools.WildToolsPlugin;
import org.bukkit.Bukkit;

public class Executor {
    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    public static void sync(Runnable runnable, long delay) {
        Bukkit.getScheduler().runTaskLater(plugin, runnable, delay);
    }

    public static void sync(Runnable runnable) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, runnable);
        } else {
            runnable.run();
        }
    }

    public static void async(Runnable runnable) {
        if (!Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
        }
    }


}
