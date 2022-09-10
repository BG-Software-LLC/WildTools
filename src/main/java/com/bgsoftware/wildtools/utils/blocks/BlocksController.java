package com.bgsoftware.wildtools.utils.blocks;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.hooks.listener.IToolBlockListener;
import com.bgsoftware.wildtools.utils.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class BlocksController {

    private static final int MAX_BLOCK_LOCATION = 29999984;
    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    private final Map<CachedChunk, Map<Location, Integer>> cachedChunks = new HashMap<>();
    private final List<Location> affectedBlocks = new ArrayList<>();

    private Location blockToUpdate = null;
    private int combinedId = 0;

    public void setAir(Location location) {
        setType(location, 0);
    }

    public void setType(Location location, int blockId) {
        if (!isLocationValid(location))
            return;

        if (blockToUpdate == null) {
            blockToUpdate = location;
            combinedId = blockId;
        }

        affectedBlocks.add(location);
        cachedChunks.computeIfAbsent(new CachedChunk(location), map -> new HashMap<>()).put(location, blockId);
    }

    public void setType(Location target, Block block) {
        setType(target, plugin.getNMSAdapter().getCombinedId(block));
    }

    public List<Location> getAffectedBlocks() {
        return affectedBlocks;
    }

    public void updateSession() {
        //Refreshing chunks
        for (Map.Entry<CachedChunk, Map<Location, Integer>> entry : cachedChunks.entrySet()) {
            Set<Location> locations = entry.getValue().keySet();
            for (Map.Entry<Location, Integer> blockEntry : entry.getValue().entrySet()) {
                plugin.getNMSAdapter().setBlockFast(blockEntry.getKey(), blockEntry.getValue());
                plugin.getProviders().notifyToolBlockListeners(blockEntry.getKey(), blockEntry.getValue() == 0 ?
                        IToolBlockListener.Action.BLOCK_BREAK : IToolBlockListener.Action.BLOCK_PLACE);
            }
            plugin.getNMSAdapter().refreshChunk(entry.getKey().buildChunk(), locations);
        }

        //Method to recalculate light
        if (blockToUpdate != null && combinedId != 0) {
            Location toUpdate = blockToUpdate;
            plugin.getNMSAdapter().setCombinedId(toUpdate, combinedId);
        }

        //Defaults
        blockToUpdate = null;
        combinedId = 0;
        affectedBlocks.clear();
        cachedChunks.clear();
    }

    private static boolean isLocationValid(Location location) {
        return NumberUtils.range(location.getBlockY(), plugin.getNMSAdapter().getMinHeight(location.getWorld()),
                location.getWorld().getMaxHeight()) &&
                NumberUtils.range(location.getBlockX(), -MAX_BLOCK_LOCATION, MAX_BLOCK_LOCATION) &&
                NumberUtils.range(location.getBlockZ(), -MAX_BLOCK_LOCATION, MAX_BLOCK_LOCATION);
    }

    private static final class CachedChunk {

        private final String world;
        private final int x, z;

        CachedChunk(Location location) {
            this.world = location.getWorld().getName();
            this.x = location.getBlockX() >> 4;
            this.z = location.getBlockZ() >> 4;
        }

        Chunk buildChunk() {
            return Bukkit.getWorld(world).getChunkAt(x, z);
        }

        @Override
        public int hashCode() {
            int hash = 19 * 3 + Objects.hashCode(this.world);
            hash = 19 * hash + (int) (Double.doubleToLongBits(this.x) ^ Double.doubleToLongBits(this.x) >>> 32);
            hash = 19 * hash + (int) (Double.doubleToLongBits(this.z) ^ Double.doubleToLongBits(this.z) >>> 32);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof CachedChunk && isSimilar((CachedChunk) obj);
        }

        @Override
        public String toString() {
            return "CachedChunk{world=" + world + ",x=" + x + ",z=" + z + "}";
        }

        private boolean isSimilar(CachedChunk other) {
            return this.x == other.x && this.z == other.z && this.world.equals(other.world);
        }

    }

}
