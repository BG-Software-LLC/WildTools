package com.bgsoftware.wildtools.nms.v1_7_R4.world;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R4.CraftChunk;
import org.bukkit.craftbukkit.v1_7_R4.block.CraftBlock;

public class FakeCraftBlock extends CraftBlock {

    private Material blockType;

    public FakeCraftBlock(Block block, Material material) {
        super((CraftChunk) block.getChunk(), block.getX(), block.getY(), block.getZ());
        this.blockType = material;
    }

    @Override
    public Material getType() {
        return blockType;
    }

    @Override
    public void setType(Material type) {
        this.blockType = type;
        super.setType(type);
    }

}

