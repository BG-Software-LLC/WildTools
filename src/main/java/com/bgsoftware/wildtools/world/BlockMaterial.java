package com.bgsoftware.wildtools.world;

import com.bgsoftware.wildtools.utils.ServerVersion;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import java.util.Objects;

public class BlockMaterial {

    private final Material type;
    private final short data;

    public static BlockMaterial of(Block block) {
        Material type = block.getType();
        short data = ServerVersion.isLegacy() ? block.getState().getData().toItemStack().getDurability() : 0;
        return new BlockMaterial(type, data);
    }

    public static BlockMaterial of(BlockState blockState) {
        Material type = blockState.getType();
        short data = ServerVersion.isLegacy() ? blockState.getData().toItemStack().getDurability() : 0;
        return new BlockMaterial(type, data);
    }

    public BlockMaterial(Material type, short data) {
        this.type = type;
        this.data = data;
    }

    public Material getType() {
        return type;
    }

    public short getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockMaterial that = (BlockMaterial) o;
        return data == that.data && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, data);
    }

}
