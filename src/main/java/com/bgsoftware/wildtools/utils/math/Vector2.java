package com.bgsoftware.wildtools.utils.math;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

public class Vector2 {

    private final int x;
    private final int z;

    public static Vector2 of(Location location) {
        return new Vector2(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    public Vector2(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public Chunk toChunk(World world) {
        return world.getChunkAt(this.x, this.z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector2 that = (Vector2) o;
        return x == that.x && z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }

    @Override
    public String toString() {
        return "Vector2{" + x + ", " + z + "}";
    }

}
