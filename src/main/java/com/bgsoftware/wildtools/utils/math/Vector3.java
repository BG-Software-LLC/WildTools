package com.bgsoftware.wildtools.utils.math;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

public class Vector3 {

    private final int x;
    private final int y;
    private final int z;

    public static Vector3 of(Location location) {
        return new Vector3(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public Vector3(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public Location toLocation(World world) {
        return new Location(world, this.x, this.y, this.z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector3 that = (Vector3) o;
        return x == that.x && y == that.y && z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "Vector3{" + x + ", " + y + ", " + z + "}";
    }

}
