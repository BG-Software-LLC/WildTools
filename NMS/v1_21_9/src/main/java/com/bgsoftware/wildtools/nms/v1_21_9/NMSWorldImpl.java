package com.bgsoftware.wildtools.nms.v1_21_9;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

public class NMSWorldImpl extends com.bgsoftware.wildtools.nms.v1_21_9.AbstractNMSWorld {

    @Override
    protected int getExpDrop(BlockState blockState, ServerPlayer serverPlayer, BlockPos blockPos) {
        return blockState.getBlock().getExpDrop(blockState, serverPlayer.level(), blockPos, serverPlayer.getMainHandItem(), true);
    }

    @Override
    protected void setBlockState(LevelChunk levelChunk, BlockPos blockPos, BlockState blockState) {
        levelChunk.setBlockState(blockPos, blockState, 3);
    }

    @Override
    protected ClientboundLightUpdatePacket createLightUpdatePacket(ChunkPos chunkPos, ThreadedLevelLightEngine lightEngine) {
        return new ClientboundLightUpdatePacket(chunkPos, lightEngine, null, null);
    }

}
