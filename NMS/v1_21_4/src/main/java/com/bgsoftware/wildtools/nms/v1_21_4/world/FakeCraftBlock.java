package com.bgsoftware.wildtools.nms.v1_21_4.world;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.block.CraftBlock;

public class FakeCraftBlock extends CraftBlock {

    private final BlockState originalState;
    private Material blockType;

    public FakeCraftBlock(Block block, Material material, BlockState originalState) {
        super(((CraftBlock) block).getHandle(), ((CraftBlock) block).getPosition());
        this.blockType = material;
        this.originalState = originalState;
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

    @Override
    public BlockData getBlockData() {
        return blockType.createBlockData((String) null);
    }

    @Override
    public BlockState getState() {
        return originalState;
    }

}

