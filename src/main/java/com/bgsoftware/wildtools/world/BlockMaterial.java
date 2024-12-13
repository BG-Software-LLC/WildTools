package com.bgsoftware.wildtools.world;

import com.bgsoftware.wildtools.utils.ServerVersion;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class BlockMaterial {

    private static final Map<Material, BlockMaterial> BLOCK_MATERIALS = initializeBlockMaterials();

    private final Material type;
    private final short data;

    public static BlockMaterial of(Block block) {
        Material type = block.getType();
        short data = ServerVersion.isLegacy() ? block.getState().getData().toItemStack().getDurability() : 0;
        return of(type, data);
    }

    public static BlockMaterial of(BlockState blockState) {
        Material type = blockState.getType();
        short data = ServerVersion.isLegacy() ? blockState.getData().toItemStack().getDurability() : 0;
        return of(type, data);
    }

    public static BlockMaterial of(Material type, short data) {
        BlockMaterial blockMaterial = null;
        if (data == 0)
            blockMaterial = BLOCK_MATERIALS.get(type);

        return blockMaterial == null ? new BlockMaterial(type, data) : blockMaterial;
    }

    private BlockMaterial(Material type, short data) {
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

    private static Map<Material, BlockMaterial> initializeBlockMaterials() {
        EnumMap<Material, BlockMaterial> blockMaterialsMap = new EnumMap<>(Material.class);

        for (Material material : Material.values()) {
            if (material.isBlock())
                blockMaterialsMap.put(material, new BlockMaterial(material, (short) 0));
        }

        return Collections.unmodifiableMap(blockMaterialsMap);
    }

}
