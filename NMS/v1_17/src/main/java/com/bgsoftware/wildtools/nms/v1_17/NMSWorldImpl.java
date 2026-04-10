package com.bgsoftware.wildtools.nms.v1_17;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;

public class NMSWorldImpl extends com.bgsoftware.wildtools.nms.v1_17.AbstractNMSWorld {

    @Override
    protected BlockState getBlockState(Block block) {
        return ((CraftBlock) block).getNMS();
    }

    @Override
    protected int getExpDrop(BlockState blockState, ServerPlayer serverPlayer, BlockPos blockPos) {
        return blockState.getBlock().getExpDrop(blockState, serverPlayer.getLevel(), blockPos, serverPlayer.getMainHandItem());
    }

    @Override
    protected void setBlockState(LevelChunk levelChunk, BlockPos blockPos, BlockState blockState) {
        levelChunk.setBlockState(blockPos, blockState, true);
    }

    @Override
    protected ClientboundLightUpdatePacket createLightUpdatePacket(ChunkPos chunkPos, ThreadedLevelLightEngine lightEngine) {
        return new ClientboundLightUpdatePacket(chunkPos, lightEngine, null, null, true);
    }

}
