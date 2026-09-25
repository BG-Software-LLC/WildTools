package com.bgsoftware.wildtools.nms.v26_3;

import com.bgsoftware.common.reflection.ReflectMethod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.block.CraftBlock;

public class NMSWorldImpl extends com.bgsoftware.wildtools.nms.v26_3.AbstractNMSWorld {

    private static final ReflectMethod<BlockState> CRAFT_BLOCK_GET_NMS = new ReflectMethod<>(
            CraftBlock.class, BlockState.class, "getNMS");
    private static final GameRule<Boolean> blockDropsGameRule = initializeBlockDropsGameRule();

    @Override
    protected BlockState getBlockState(Block block) {
        // Spigot still uses the CraftBlock#getNMS() method.
        if (CRAFT_BLOCK_GET_NMS.isValid()) {
            return CRAFT_BLOCK_GET_NMS.invoke(block);
        }

        return ((CraftBlock) block).getBlockState();
    }

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

    @Override
    public boolean isBlockDropsGameRuleEnabled(World world) {
        if (blockDropsGameRule != null) {
            return world.getGameRuleValue(blockDropsGameRule);
        }

        return false;
    }

    private static GameRule<Boolean> initializeBlockDropsGameRule() {
        try {
            return org.bukkit.GameRules.BLOCK_DROPS;
        } catch (NoClassDefFoundError ignored) {
        }

        // Spigot still uses fields from GameRule class.
        try {
            //noinspection all
            return (GameRule<Boolean>) GameRule.class.getField("BLOCK_DROPS").get(null);
        } catch (Throwable ignored) {
            return null;
        }
    }

}
