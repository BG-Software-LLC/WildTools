package com.bgsoftware.wildtools.nms.v1_18_R1.mappings.net.minecraft.world.level;

import com.bgsoftware.wildtools.nms.mapping.Remap;
import com.bgsoftware.wildtools.nms.v1_18_R1.mappings.MappedObject;
import com.bgsoftware.wildtools.nms.v1_18_R1.mappings.net.minecraft.server.level.ChunkProviderServer;
import com.bgsoftware.wildtools.nms.v1_18_R1.mappings.net.minecraft.world.level.block.state.IBlockData;
import com.bgsoftware.wildtools.nms.v1_18_R1.mappings.net.minecraft.world.level.chunk.Chunk;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.lighting.LightEngine;

public class World extends MappedObject<net.minecraft.world.level.World> {

    public World(net.minecraft.world.level.World handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.world.level.Level",
            name = "getBlockState",
            type = Remap.Type.METHOD,
            remappedName = "a_")
    public net.minecraft.world.level.block.state.IBlockData getBlockStateNoMappings(BlockPosition blockPosition) {
        return handle.a_(blockPosition);
    }

    public IBlockData getBlockState(BlockPosition blockPosition) {
        return new IBlockData(getBlockStateNoMappings(blockPosition));
    }

    @Remap(classPath = "net.minecraft.world.level.Level",
            name = "getBlockEntity",
            type = Remap.Type.METHOD,
            remappedName = "c_")
    public TileEntity getBlockEntity(BlockPosition blockPosition) {
        return handle.c_(blockPosition);
    }

    @Remap(classPath = "net.minecraft.world.level.Level",
            name = "getChunkAt",
            type = Remap.Type.METHOD,
            remappedName = "l")
    public Chunk getChunkAt(BlockPosition blockPosition) {
        return new Chunk(handle.l(blockPosition));
    }

    @Remap(classPath = "net.minecraft.world.level.Level",
            name = "getLightEngine",
            type = Remap.Type.METHOD,
            remappedName = "l_")
    public LightEngine getLightEngine() {
        return handle.l_();
    }

    @Remap(classPath = "net.minecraft.world.level.Level",
            name = "setBlock",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void setBlock(BlockPosition blockPosition, net.minecraft.world.level.block.state.IBlockData blockData, int i) {
        handle.a(blockPosition, blockData, i);
    }

    @Remap(classPath = "net.minecraft.world.level.LevelWriter",
            name = "addFreshEntity",
            type = Remap.Type.METHOD,
            remappedName = "b")
    public void addFreshEntity(Entity entity) {
        handle.b(entity);
    }

    @Remap(classPath = "net.minecraft.server.level.WorldGenRegion",
            name = "levelEvent",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void levelEvent(EntityPlayer entityPlayer, int i, BlockPosition blockPosition, int j) {
        handle.a(entityPlayer, i, blockPosition, j);
    }

    @Remap(classPath = "net.minecraft.server.level.ServerLevel",
            name = "getChunkSource",
            type = Remap.Type.METHOD,
            remappedName = "k")
    public ChunkProviderServer getChunkSource() {
        return new ChunkProviderServer(((WorldServer) handle).k());
    }

}
