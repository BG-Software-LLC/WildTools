package com.bgsoftware.wildtools.nms.v1_18_R2.mappings.net.minecraft.world.level.chunk;

import com.bgsoftware.wildtools.nms.mapping.Remap;
import com.bgsoftware.wildtools.nms.v1_18_R2.mappings.MappedObject;
import com.bgsoftware.wildtools.nms.v1_18_R2.mappings.net.minecraft.world.level.ChunkCoordIntPair;
import com.bgsoftware.wildtools.nms.v1_18_R2.mappings.net.minecraft.world.level.World;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.level.block.state.IBlockData;

public class Chunk extends MappedObject<net.minecraft.world.level.chunk.Chunk> {

    public Chunk(net.minecraft.world.level.chunk.Chunk handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.world.level.chunk.LevelChunk",
            name = "setBlockState",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void setBlockState(BlockPosition blockPosition, IBlockData blockData, boolean flag) {
        handle.a(blockPosition, blockData, flag);
    }

    @Remap(classPath = "net.minecraft.world.level.chunk.LevelChunk",
            name = "level",
            type = Remap.Type.FIELD,
            remappedName = "q")
    public World getLevel() {
        return new World(handle.q);
    }

    @Remap(classPath = "net.minecraft.world.level.chunk.ChunkAccess",
            name = "getPos",
            type = Remap.Type.METHOD,
            remappedName = "f")
    public ChunkCoordIntPair getPos() {
        return new ChunkCoordIntPair(handle.f());
    }

}
