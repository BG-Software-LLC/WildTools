package com.bgsoftware.wildtools.scheduler;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

public class Scheduler {
    private static final ISchedulerImplementation IMP = initializeSchedulerImplementation();

    private static ISchedulerImplementation initializeSchedulerImplementation() {
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.RegionScheduler");
        } catch (ClassNotFoundException error) {
            return BukkitSchedulerImplementation.INSTANCE;
        }

        // Detected Folia, create its scheduler
        try {
            Class<?> foliaSchedulerClass = Class.forName("com.bgsoftware.wildtools.scheduler.FoliaSchedulerImplementation");
            return (ISchedulerImplementation) foliaSchedulerClass.getField("INSTANCE").get(null);
        } catch (Throwable error) {
            throw new RuntimeException(error);
        }
    }

    private Scheduler() {

    }

    public static void initialize() {
        // Do nothing, load static initializer
    }

    public static boolean isRegionScheduler() {
        return IMP.isRegionScheduler();
    }

    public static ScheduledTask runTask(World world, int chunkX, int chunkZ, Runnable task, long delay) {
        return IMP.scheduleTask(world, chunkX, chunkZ, task, delay);
    }

    public static ScheduledTask runTask(Entity entity, Runnable task, long delay) {
        return IMP.scheduleTask(entity, task, delay);
    }

    public static ScheduledTask runTask(Runnable task, long delay) {
        return IMP.scheduleTask(task, delay);
    }

    public static ScheduledTask runTaskAsync(Runnable task, long delay) {
        return IMP.scheduleAsyncTask(task, delay);
    }

    public static ScheduledTask runTask(Location location, Runnable task, long delay) {
        return runTask(location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4, task, delay);
    }

    public static ScheduledTask runTask(Entity entity, Runnable task) {
        return runTask(entity, task, 0L);
    }

    public static ScheduledTask runTask(Location location, Runnable task) {
        return runTask(location, task, 0L);
    }

    public static ScheduledTask runTask(Runnable task) {
        return runTask(task, 0L);
    }

    public static ScheduledTask runTaskAsync(Runnable task) {
        return runTaskAsync(task, 0L);
    }

}
