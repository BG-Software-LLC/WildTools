package com.bgsoftware.wildtools.nms.v1_16_R3;

import com.bgsoftware.common.reflection.ClassInfo;
import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildtools.nms.NMSWorld;
import com.bgsoftware.wildtools.utils.Executor;
import com.bgsoftware.wildtools.utils.math.Vector3;
import com.bgsoftware.wildtools.utils.world.WorldEditSession;
import com.destroystokyo.paper.antixray.ChunkPacketBlockControllerAntiXray;
import com.tuinity.tuinity.chunk.light.StarLightInterface;
import net.minecraft.server.v1_16_R3.Block;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.Chunk;
import net.minecraft.server.v1_16_R3.ChunkProviderServer;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.ItemStack;
import net.minecraft.server.v1_16_R3.LightEngineThreaded;
import net.minecraft.server.v1_16_R3.PacketPlayOutLightUpdate;
import net.minecraft.server.v1_16_R3.ThreadedMailbox;
import net.minecraft.server.v1_16_R3.TileEntity;
import net.minecraft.server.v1_16_R3.World;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.CropState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldBorder;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NMSWorldImpl implements NMSWorld {

    private static final ReflectField<Object> STAR_LIGHT_INTERFACE = new ReflectField<>(
            LightEngineThreaded.class, Object.class, "theLightEngine");
    private static final ReflectField<ThreadedMailbox<Runnable>> LIGHT_ENGINE_EXECUTOR = new ReflectField<>(
            LightEngineThreaded.class, ThreadedMailbox.class, "b");
    private static final ReflectMethod<Void> UPDATE_NEARBY_BLOCKS = new ReflectMethod<>(
            new ClassInfo("com.destroystokyo.paper.antixray.ChunkPacketBlockControllerAntiXray", ClassInfo.PackageType.UNKNOWN),
            "updateNearbyBlocks", World.class, BlockPosition.class);

    @Override
    public List<org.bukkit.inventory.ItemStack> getBlockDrops(Player bukkitPlayer, org.bukkit.block.Block bukkitBlock, boolean unused) {
        List<org.bukkit.inventory.ItemStack> drops = new LinkedList<>();

        EntityPlayer entityPlayer = ((CraftPlayer) bukkitPlayer).getHandle();
        BlockPosition blockPosition = new BlockPosition(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());
        WorldServer worldServer = entityPlayer.playerInteractManager.world;
        IBlockData blockData = worldServer.getType(blockPosition);
        ItemStack itemStack = entityPlayer.getItemInMainHand();
        itemStack = itemStack.isEmpty() ? ItemStack.b : itemStack.cloneItemStack();
        TileEntity tileEntity = worldServer.getTileEntity(blockPosition);

        Block.getDrops(blockData, worldServer, blockPosition, tileEntity, entityPlayer, itemStack).forEach(nmsItem ->
                drops.add(CraftItemStack.asCraftMirror(nmsItem)));

        return drops;
    }

    @Override
    public int getExpFromBlock(org.bukkit.block.Block block, Player player) {
        WorldServer world = ((CraftWorld) block.getWorld()).getHandle();
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        IBlockData blockData = world.getType(blockPosition);
        return blockData.getBlock().getExpDrop(blockData, world, blockPosition, entityPlayer.getItemInMainHand());
    }

    @Override
    public boolean isFullyGrown(org.bukkit.block.Block block) {
        if (block.getType() == Material.CACTUS || block.getType() == Material.SUGAR_CANE ||
                block.getType() == Material.PUMPKIN || block.getType() == Material.MELON ||
                block.getType() == Material.BAMBOO)
            return true;

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
    public void setBlockFast(org.bukkit.World bukkitWorld, Vector3 location, int combinedId, boolean sendUpdate) {
        World world = ((CraftWorld) bukkitWorld).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());

        if (sendUpdate) {
            world.setTypeAndData(blockPosition, Block.getByCombinedId(combinedId), 18);
            return;
        }

        Chunk chunk = world.getChunkAt(location.getX() >> 4, location.getZ() >> 4);

        if (combinedId == 0)
            world.a(null, 2001, blockPosition, Block.getCombinedId(world.getType(blockPosition)));

        chunk.setType(blockPosition, Block.getByCombinedId(combinedId), true);

        if (UPDATE_NEARBY_BLOCKS.isValid() && world.paperConfig.antiXray &&
                world.chunkPacketBlockController instanceof ChunkPacketBlockControllerAntiXray) {
            UPDATE_NEARBY_BLOCKS.invoke(world.chunkPacketBlockController, world, blockPosition);
        }
    }

    @Override
    public void refreshChunk(org.bukkit.Chunk bukkitChunk, List<WorldEditSession.BlockData> blocksList) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
        Map<Integer, Set<Short>> blocks = new HashMap<>();
        WorldServer worldServer = (WorldServer) chunk.getWorld();

        ChunkProviderServer chunkProviderServer = worldServer.getChunkProvider();

        for (WorldEditSession.BlockData blockData : blocksList) {
            BlockPosition blockPosition = new BlockPosition(blockData.location.getX(), blockData.location.getY(), blockData.location.getZ());
            chunkProviderServer.flagDirty(blockPosition);
        }

        if (STAR_LIGHT_INTERFACE.isValid()) {
            LightEngineThreaded lightEngineThreaded = (LightEngineThreaded) worldServer.e();
            StarLightInterface starLightInterface = (StarLightInterface) STAR_LIGHT_INTERFACE.get(lightEngineThreaded);
            LIGHT_ENGINE_EXECUTOR.get(lightEngineThreaded).a(() ->
                    starLightInterface.relightChunks(Collections.singleton(chunk.getPos()), chunkPos ->
                            chunkProviderServer.serverThreadQueue.execute(() -> NMSUtils.sendPacketToRelevantPlayers(
                                    worldServer, chunkPos.x, chunkPos.z,
                                    new PacketPlayOutLightUpdate(chunkPos, lightEngineThreaded, true))
                            ), null));
        } else {
            LightEngineThreaded lightEngine = worldServer.getChunkProvider().getLightEngine();

            for (WorldEditSession.BlockData blockData : blocksList) {
                BlockPosition blockPosition = new BlockPosition(blockData.location.getX(), blockData.location.getY(), blockData.location.getZ());
                lightEngine.a(blockPosition);
            }

            Executor.sync(() -> NMSUtils.sendPacketToRelevantPlayers(worldServer, chunk.getPos().x, chunk.getPos().z,
                            new PacketPlayOutLightUpdate(chunk.getPos(), lightEngine, true)),
                    2L);
        }
    }

    @Override
    public int getCombinedId(org.bukkit.block.Block block) {
        return Block.getCombinedId(((CraftBlock) block).getNMS());
    }

    @Override
    public boolean isOutsideWorldBorder(Location location) {
        WorldBorder worldBorder = location.getWorld().getWorldBorder();
        int radius = (int) worldBorder.getSize() / 2;
        return location.getBlockX() > (worldBorder.getCenter().getBlockX() + radius) || location.getBlockX() < (worldBorder.getCenter().getBlockX() - radius) ||
                location.getBlockZ() > (worldBorder.getCenter().getBlockZ() + radius) || location.getBlockZ() < (worldBorder.getCenter().getBlockZ() - radius);
    }

    @Override
    public int getMinHeight(org.bukkit.World world) {
        return world.getMinHeight();
    }

}
