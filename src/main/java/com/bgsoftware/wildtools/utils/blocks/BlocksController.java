package com.bgsoftware.wildtools.utils.blocks;

import com.bgsoftware.wildtools.WildToolsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class BlocksController {

    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();
    private static final Set<CachedChunk> cachedChunks = new HashSet<>();

    private static Location blockToUpdate = null;
    private static int combinedId = 0;

    public static void setAir(Location location){
        if(blockToUpdate == null) {
            blockToUpdate = location;
            combinedId = plugin.getNMSAdapter().getCombinedId(blockToUpdate);
        }

        plugin.getNMSAdapter().setAirFast(location);
        cachedChunks.add(new CachedChunk(location.getWorld().getName(), location.getBlockX() >> 4, location.getBlockZ() >> 4));
    }

    public static void updateSession(){
        //Refreshing chunks
        List<Chunk> chunksList = cachedChunks.stream().map(CachedChunk::buildChunk).collect(Collectors.toList());
        plugin.getNMSAdapter().refreshChunks(chunksList);

        //Method to recalculate light
        if(blockToUpdate != null && combinedId != 0) {
            Location toUpdate = blockToUpdate;
            plugin.getNMSAdapter().setCombinedId(toUpdate, combinedId);
            Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getNMSAdapter().setCombinedId(toUpdate, 0), 2L);
        }

        //Defaults
        blockToUpdate = null;
        combinedId = 0;
        cachedChunks.clear();
    }

    private static final class CachedChunk{

        private final String world;
        private final int x, z;

        CachedChunk(String world, int x, int z){
            this.world = world;
            this.x = x;
            this.z = z;
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
