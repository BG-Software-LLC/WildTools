package com.bgsoftware.wildtools.nms.v1_20_1;

import com.bgsoftware.common.reflection.ClassInfo;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildtools.nms.NMSWorld;
import com.bgsoftware.wildtools.utils.Executor;
import com.bgsoftware.wildtools.utils.math.Vector3;
import com.bgsoftware.wildtools.utils.world.WorldEditSession;
import com.destroystokyo.paper.antixray.ChunkPacketBlockControllerAntiXray;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.CropState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;

public class NMSWorldImpl implements NMSWorld {

    private static final ReflectMethod<Void> UPDATE_NEARBY_BLOCKS = new ReflectMethod<>(
            new ClassInfo("com.destroystokyo.paper.antixray.ChunkPacketBlockControllerAntiXray", ClassInfo.PackageType.UNKNOWN),
            "updateNearbyBlocks", Level.class, BlockPos.class);

    @Override
    public List<org.bukkit.inventory.ItemStack> getBlockDrops(Player bukkitPlayer, org.bukkit.block.Block bukkitBlock, boolean silkTouch) {
        List<org.bukkit.inventory.ItemStack> drops = new LinkedList<>();

        ServerPlayer serverPlayer = ((CraftPlayer) bukkitPlayer).getHandle();
        ServerLevel serverLevel = serverPlayer.serverLevel();
        BlockPos blockPos = new BlockPos(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());
        BlockState blockState = serverLevel.getBlockState(blockPos);
        ItemStack itemStack = serverPlayer.getMainHandItem();
        BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);

        Block.getDrops(blockState, serverLevel, blockPos, blockEntity, serverPlayer, itemStack).forEach(dropItem ->
                drops.add(CraftItemStack.asCraftMirror(dropItem)));

        return drops;
    }

    @Override
    public int getExpFromBlock(org.bukkit.block.Block bukkitBlock, Player bukkitPlayer) {
        ServerPlayer serverPlayer = ((CraftPlayer) bukkitPlayer).getHandle();
        BlockState blockState = ((CraftBlock) bukkitBlock).getNMS();
        return blockState.getBlock().getExpDrop(blockState,
                serverPlayer.serverLevel(),
                ((CraftBlock) bukkitBlock).getPosition(),
                serverPlayer.getMainHandItem(),
                true);
    }

    @Override
    public boolean isFullyGrown(org.bukkit.block.Block block) {
        switch (block.getType()) {
            case CACTUS:
            case SUGAR_CANE:
            case PUMPKIN:
            case MELON:
            case BAMBOO:
                return true;
        }

        CraftBlock craftBlock = (CraftBlock) block;
        BlockData blockData = craftBlock.getBlockData();
        return ((Ageable) blockData).getAge() == ((Ageable) blockData).getMaximumAge();
    }

    @Override
    public void setCropState(org.bukkit.block.Block block, CropState cropState) {
        if (block.getType() == Material.CHORUS_PLANT) {
            block.setType(Material.CHORUS_FLOWER);
        } else {
            CraftBlock craftBlock = (CraftBlock) block;
            BlockData blockData = craftBlock.getBlockData();
            if (blockData instanceof Ageable) {
                ((Ageable) blockData).setAge(cropState.ordinal());
                craftBlock.setBlockData(blockData, true);
            } else {
                block.setType(Material.AIR);
            }
        }
    }

    @Override
    public void setBlockFast(World world, Vector3 location, int combinedId, boolean sendUpdate) {
        ServerLevel serverLevel = ((CraftWorld) world).getHandle();
        BlockPos blockPos = new BlockPos(location.getX(), location.getY(), location.getZ());

        if (sendUpdate) {
            serverLevel.setBlock(blockPos, Block.stateById(combinedId), 18);
            return;
        }

        LevelChunk levelChunk = serverLevel.getChunkAt(blockPos);

        if (combinedId == 0)
            serverLevel.levelEvent(null, 2001, blockPos, Block.getId(serverLevel.getBlockState(blockPos)));

        levelChunk.setBlockState(blockPos, Block.stateById(combinedId), true);

        if (UPDATE_NEARBY_BLOCKS.isValid() && serverLevel.chunkPacketBlockController instanceof ChunkPacketBlockControllerAntiXray) {
            UPDATE_NEARBY_BLOCKS.invoke(serverLevel.chunkPacketBlockController, serverLevel, blockPos);
        }
    }

    @Override
    public void refreshChunk(org.bukkit.Chunk bukkitChunk, List<WorldEditSession.BlockData> blocksList) {
        ServerLevel serverLevel = ((CraftChunk) bukkitChunk).getCraftWorld().getHandle();
        LevelChunk levelChunk = serverLevel.getChunk(bukkitChunk.getX(), bukkitChunk.getZ());
        ServerChunkCache serverChunkCache = levelChunk.level.getChunkSource();
        ThreadedLevelLightEngine lightEngine = (ThreadedLevelLightEngine) levelChunk.level.getLightEngine();

        for (WorldEditSession.BlockData blockData : blocksList) {
            BlockPos blockPos = new BlockPos(blockData.location.getX(), blockData.location.getY(), blockData.location.getZ());
            serverChunkCache.blockChanged(blockPos);
            lightEngine.checkBlock(blockPos);
        }

        ChunkPos chunkPos = levelChunk.getPos();

        Executor.sync(() -> {
            ClientboundLightUpdatePacket lightUpdatePacket = new ClientboundLightUpdatePacket(
                    chunkPos, lightEngine, null, null);
            NMSUtils.sendPacketToRelevantPlayers(levelChunk.level, chunkPos.x, chunkPos.z, lightUpdatePacket);
        }, 2L);
    }

    @Override
    public int getCombinedId(org.bukkit.block.Block bukkitBlock) {
        BlockState blockState = ((CraftBlock) bukkitBlock).getNMS();
        if (blockState.getValue(BlockStateProperties.WATERLOGGED))
            blockState = blockState.setValue(BlockStateProperties.WATERLOGGED, false);
        return Block.getId(blockState);
    }

    @Override
    public boolean isOutsideWorldBorder(Location location) {
        WorldBorder worldBorder = location.getWorld().getWorldBorder();
        int radius = (int) worldBorder.getSize() / 2;
        return location.getBlockX() > (worldBorder.getCenter().getBlockX() + radius) ||
                location.getBlockX() < (worldBorder.getCenter().getBlockX() - radius) ||
                location.getBlockZ() > (worldBorder.getCenter().getBlockZ() + radius) ||
                location.getBlockZ() < (worldBorder.getCenter().getBlockZ() - radius);
    }

    @Override
    public int getMinHeight(World world) {
        return world.getMinHeight();
    }

}
