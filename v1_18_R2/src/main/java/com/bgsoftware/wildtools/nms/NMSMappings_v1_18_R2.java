package com.bgsoftware.wildtools.nms;

import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ChunkProviderServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.inventory.ContainerProperty;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.IWorldWriter;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.lighting.LightEngine;

import java.util.List;

public final class NMSMappings_v1_18_R2 {

    private NMSMappings_v1_18_R2() {

    }

    public static WorldServer getLevel(EntityPlayer entityPlayer) {
        return entityPlayer.x();
    }

    public static IBlockData getBlockState(World world, BlockPosition blockPosition) {
        return world.a_(blockPosition);
    }

    public static TileEntity getBlockEntity(World world, BlockPosition blockPosition) {
        return world.c_(blockPosition);
    }

    public static Chunk getChunkAt(World world, BlockPosition blockPosition) {
        return world.l(blockPosition);
    }

    public static LightEngine getLightEngine(World world) {
        return world.l_();
    }

    public static void setBlock(World world, BlockPosition blockPosition, IBlockData blockData, int i) {
        world.a(blockPosition, blockData, i);
    }

    public static void addFreshEntity(IWorldWriter worldWriter, Entity entity) {
        worldWriter.b(entity);
    }

    public static Block getBlock(IBlockData blockData) {
        return blockData.b();
    }

    public static List<ItemStack> getDrops(IBlockData blockData, WorldServer worldServer, BlockPosition blockPosition,
                                           TileEntity tileEntity, Entity entity, ItemStack itemStack) {
        return Block.a(blockData, worldServer, blockPosition, tileEntity, entity, itemStack);
    }

    public static NBTTagCompound getTag(ItemStack itemStack) {
        return itemStack.t();
    }

    public static NBTTagCompound getOrCreateTag(ItemStack itemStack) {
        return itemStack.u();
    }

    public static Item getItem(ItemStack itemStack) {
        return itemStack.c();
    }

    public static int getCount(ItemStack itemStack) {
        return itemStack.J();
    }

    public static void shrink(ItemStack itemStack, int amount) {
        itemStack.g(amount);
    }

    public static void setDamageValue(ItemStack itemStack, int damage) {
        itemStack.b(damage);
    }

    public static boolean contains(NBTTagCompound nbtTagCompound, String key) {
        return nbtTagCompound.e(key);
    }

    public static int getInt(NBTTagCompound nbtTagCompound, String key) {
        return nbtTagCompound.h(key);
    }

    public static void putInt(NBTTagCompound nbtTagCompound, String key, int value) {
        nbtTagCompound.a(key, value);
    }

    public static String getString(NBTTagCompound nbtTagCompound, String key) {
        return nbtTagCompound.l(key);
    }

    public static void putString(NBTTagCompound nbtTagCompound, String key, String value) {
        nbtTagCompound.a(key, value);
    }

    public static void remove(NBTTagCompound nbtTagCompound, String key) {
        nbtTagCompound.r(key);
    }

    public static void broadcastBreakEvent(EntityLiving entityLiving, EnumItemSlot enumItemSlot) {
        entityLiving.c(enumItemSlot);
    }

    public static int getId(IBlockData blockData) {
        return Block.i(blockData);
    }

    public static IBlockData getByCombinedId(int combinedId) {
        return Block.a(combinedId);
    }

    public static void setBlockState(Chunk chunk, BlockPosition blockPosition, IBlockData blockData, boolean flag) {
        chunk.a(blockPosition, blockData, flag);
    }

    public static WorldServer getLevel(Chunk chunk) {
        return chunk.q;
    }

    public static ChunkCoordIntPair getPos(IChunkAccess chunk) {
        return chunk.f();
    }

    public static ChunkProviderServer getChunkSource(WorldServer worldServer) {
        return worldServer.k();
    }

    public static IBlockData defaultBlockState(Block block) {
        return block.n();
    }

    public static void broadcast(ChunkProviderServer chunkProviderServer, Entity entity, Packet<?> packet) {
        chunkProviderServer.b(entity, packet);
    }

    public static float getDestroySpeed(Item item, ItemStack itemStack, IBlockData blockData) {
        return item.a(itemStack, blockData);
    }

    public static void set(ContainerProperty containerProperty, int value) {
        containerProperty.a(value);
    }

    public static int get(ContainerProperty containerProperty) {
        return containerProperty.b();
    }

    public static World getLevel(Entity entity) {
        return entity.W();
    }

    public static boolean isAlive(Entity entity) {
        return entity.bl();
    }

    public static void discard(Entity entity) {
        entity.ah();
    }

    public static ItemStack getItem(EntityItem entityItem) {
        return entityItem.h();
    }

    public static void setItem(EntityItem entityItem, ItemStack itemStack) {
        entityItem.a(itemStack);
    }

    public static boolean isEmpty(ItemStack itemStack) {
        return itemStack.b();
    }

    public static int getMaxStackSize(ItemStack itemStack) {
        return itemStack.e();
    }

    public static void send(PlayerConnection playerConnection, Packet<?> packet) {
        playerConnection.a(packet);
    }

}
