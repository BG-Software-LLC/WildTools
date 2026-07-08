package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.scheduler.Scheduler;
import com.bgsoftware.wildtools.utils.math.Vector2;
import com.bgsoftware.wildtools.utils.math.Vector3;
import com.bgsoftware.wildtools.utils.world.WorldEditSession;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Optional Orebfuscator integration.
 *
 * WildTools changes many blocks through fast NMS edits, so Orebfuscator does not always see
 * a normal Bukkit block update for every fake ore that needs to be revealed again. This hook
 * asks Orebfuscator to deobfuscate the affected positions after the WorldEditSession has applied.
 *
 * The integration is reflection based so WildTools can still compile and run without
 * Orebfuscator/Orebfuscator API installed.
 */
public final class OrebfuscatorHook {

    private static final AtomicInteger PENDING_BLOCKS = new AtomicInteger(0);

    private static boolean enabled = true;
    private static int maxBlocksPerBatch = 96;
    private static int delayBetweenBatchesTicks = 1;
    private static int maxBlocksPerChunkPerSession = 512;
    private static int maxPendingBlocks = 8192;

    private static boolean serviceChecked = false;
    private static boolean serviceLogged = false;
    private static boolean failureLogged = false;
    private static Object orebfuscatorService = null;
    private static Method deobfuscateMethod = null;

    private OrebfuscatorHook() {

    }

    public static void configure(boolean enabled, int maxBlocksPerBatch, int delayBetweenBatchesTicks,
                                 int maxBlocksPerChunkPerSession, int maxPendingBlocks) {
        OrebfuscatorHook.enabled = enabled;
        OrebfuscatorHook.maxBlocksPerBatch = Math.max(1, maxBlocksPerBatch);
        OrebfuscatorHook.delayBetweenBatchesTicks = Math.max(0, delayBetweenBatchesTicks);
        OrebfuscatorHook.maxBlocksPerChunkPerSession = Math.max(0, maxBlocksPerChunkPerSession);
        OrebfuscatorHook.maxPendingBlocks = Math.max(0, maxPendingBlocks);

        // Allow /tools reload to pick up Orebfuscator if it was loaded/reloaded after WildTools.
        serviceChecked = false;
        serviceLogged = false;
        failureLogged = false;
        orebfuscatorService = null;
        deobfuscateMethod = null;
    }

    public static void deobfuscate(World world, Map<Vector2, List<WorldEditSession.BlockData>> affectedBlocksByChunks) {
        if (!enabled || world == null || affectedBlocksByChunks == null || affectedBlocksByChunks.isEmpty())
            return;

        if (maxPendingBlocks > 0 && PENDING_BLOCKS.get() >= maxPendingBlocks)
            return;

        int batchDelayIndex = 0;

        for (Map.Entry<Vector2, List<WorldEditSession.BlockData>> entry : affectedBlocksByChunks.entrySet()) {
            List<WorldEditSession.BlockData> affectedBlocks = entry.getValue();
            if (affectedBlocks == null || affectedBlocks.isEmpty())
                continue;

            List<Vector3> positions = new ArrayList<>();
            int maxBlocks = maxBlocksPerChunkPerSession <= 0 ? affectedBlocks.size() :
                    Math.min(affectedBlocks.size(), maxBlocksPerChunkPerSession);

            for (int i = 0; i < maxBlocks; i++) {
                positions.add(affectedBlocks.get(i).location);
            }

            for (int index = 0; index < positions.size(); index += maxBlocksPerBatch) {
                if (maxPendingBlocks > 0 && PENDING_BLOCKS.get() >= maxPendingBlocks)
                    return;

                int endIndex = Math.min(index + maxBlocksPerBatch, positions.size());
                List<Vector3> batch = new ArrayList<>(positions.subList(index, endIndex));

                int newPending = PENDING_BLOCKS.addAndGet(batch.size());
                if (maxPendingBlocks > 0 && newPending > maxPendingBlocks) {
                    subtractPending(batch.size());
                    return;
                }

                Vector2 chunk = entry.getKey();
                long delay = (long) batchDelayIndex * (long) delayBetweenBatchesTicks;
                try {
                    Scheduler.runTask(world, chunk.getX(), chunk.getZ(), () -> deobfuscateNow(world, batch), delay);
                } catch (Throwable error) {
                    subtractPending(batch.size());
                    if (!failureLogged) {
                        failureLogged = true;
                        WildToolsPlugin.log("Orebfuscator hook could not schedule a refresh batch: " +
                                error.getClass().getSimpleName() + ": " + error.getMessage());
                    }
                }
                batchDelayIndex++;
            }
        }
    }

    private static void deobfuscateNow(World world, List<Vector3> positions) {
        try {
            Object service = loadService();
            if (service == null)
                return;

            List<Block> blocks = new ArrayList<>(positions.size());
            for (Vector3 position : positions) {
                blocks.add(world.getBlockAt(position.getX(), position.getY(), position.getZ()));
            }

            if (!blocks.isEmpty())
                deobfuscateMethod.invoke(service, blocks);
        } catch (Throwable error) {
            if (!failureLogged) {
                failureLogged = true;
                WildToolsPlugin.log("Orebfuscator hook failed once and will stay silent to avoid console spam: " +
                        error.getClass().getSimpleName() + ": " + error.getMessage());
            }
        } finally {
            subtractPending(positions.size());
        }
    }

    private static void subtractPending(int amount) {
        int current;
        int next;
        do {
            current = PENDING_BLOCKS.get();
            next = Math.max(0, current - amount);
        } while (!PENDING_BLOCKS.compareAndSet(current, next));
    }

    private static Object loadService() {
        if (serviceChecked)
            return orebfuscatorService;

        serviceChecked = true;

        try {
            Class<?> serviceClass = Class.forName("net.imprex.orebfuscator.api.OrebfuscatorService");

            if (!Bukkit.getServicesManager().isProvidedFor(serviceClass))
                return null;

            Object service = Bukkit.getServicesManager().load(serviceClass);
            if (service == null)
                return null;

            orebfuscatorService = service;
            deobfuscateMethod = serviceClass.getMethod("deobfuscate", Collection.class);

            if (!serviceLogged) {
                serviceLogged = true;
                WildToolsPlugin.log("Orebfuscator hook enabled. WildTools block sessions will be deobfuscated in throttled batches.");
            }

            return orebfuscatorService;
        } catch (Throwable ignored) {
            orebfuscatorService = null;
            deobfuscateMethod = null;
            return null;
        }
    }

}
