package com.bgsoftware.wildtools.utils.blocks;

import com.bgsoftware.wildtools.WildToolsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class BlocksController {

    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    private final Map<CachedChunk, Set<Location>> cachedChunks = new HashMap<>();
    private final List<Location> affectedBlocks = new ArrayList<>();

    private Location blockToUpdate = null;
    private int combinedId = 0;

    public void setAir(Location location){
        setType(location, 0);
    }

    public void setType(Location location, int blockId){
        if(blockToUpdate == null) {
            blockToUpdate = location;
            combinedId = blockId;
        }

        plugin.getNMSAdapter().setBlockFast(location, blockId);
        affectedBlocks.add(location);
        cachedChunks.computeIfAbsent(new CachedChunk(location), map -> new HashSet<>()).add(location);
    }

    public void setType(Location target, Location block){
        setType(target, plugin.getNMSAdapter().getCombinedId(block));
    }

    public List<Location> getAffectedBlocks(){
        return affectedBlocks;
    }

    public void updateSession(){
        //Refreshing chunks
        for(Map.Entry<CachedChunk, Set<Location>> entry : cachedChunks.entrySet())
            plugin.getNMSAdapter().refreshChunk(entry.getKey().buildChunk(), entry.getValue());

        //Method to recalculate light
        if(blockToUpdate != null && combinedId != 0) {
            Location toUpdate = blockToUpdate;
            plugin.getNMSAdapter().setCombinedId(toUpdate, combinedId);
        }

        //Defaults
        blockToUpdate = null;
        combinedId = 0;
        affectedBlocks.clear();
        cachedChunks.clear();
    }

    private static final class CachedChunk{

        private final String world;
        private final int x, z;

        CachedChunk(Location location){
            this.world = location.getWorld().getName();
            this.x = location.getBlockX() >> 4;
            this.z = location.getBlockZ() >> 4;
        }

        Chunk buildChunk(){
            return Bukkit.getWorld(world).getChunkAt(x, z);
        }

        @Override
        public int hashCode() {
            int hash = 19 * 3 + Objects.hashCode(this.world);
            hash = 19 * hash + (int)(Double.doubleToLongBits(this.x) ^ Double.doubleToLongBits(this.x) >>> 32);
            hash = 19 * hash + (int)(Double.doubleToLongBits(this.z) ^ Double.doubleToLongBits(this.z) >>> 32);
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

        private boolean isSimilar(CachedChunk other){
            return this.x == other.x && this.z == other.z && this.world.equals(other.world);
        }

    }

}
