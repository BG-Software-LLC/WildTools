package com.bgsoftware.wildtools.utils;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.bukkit.Bukkit;

import java.util.concurrent.Executors;

public final class Executor {

    private static java.util.concurrent.Executor executor = Executors.newFixedThreadPool(2, new ThreadFactoryBuilder().setNameFormat("WildTools Thread - %d").build());
    private static WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    public static void sync(Runnable runnable, long delay){
        Bukkit.getScheduler().runTaskLater(plugin, runnable, delay);
    }

    public static void sync(Runnable runnable){
        if(!Bukkit.isPrimaryThread()){
            Bukkit.getScheduler().runTask(plugin, runnable);
        }else{
            runnable.run();
        }
    }

    public static void async(Runnable runnable){
        if(Bukkit.isPrimaryThread()){
            executor.execute(runnable);
        }else{
            runnable.run();
        }
    }


}
