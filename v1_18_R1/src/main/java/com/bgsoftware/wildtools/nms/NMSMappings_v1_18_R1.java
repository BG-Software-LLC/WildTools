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
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.ChunkSection;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.lighting.LightEngine;

import java.util.List;

public final class NMSMappings_v1_18_R1 {

    private NMSMappings_v1_18_R1() {

    }

    public static WorldServer getWorldServer(EntityPlayer entityPlayer) {
        return entityPlayer.x();
    }

    public static IBlockData getType(World world, BlockPosition blockPosition) {
        return world.a_(blockPosition);
    }

    public static Block getBlock(IBlockData blockData) {
        return blockData.b();
    }

    public static TileEntity getTileEntity(World world, BlockPosition blockPosition) {
        return world.c_(blockPosition);
    }

    public static List<ItemStack> getDrops(IBlockData blockData, WorldServer worldServer, BlockPosition blockPosition,
                                           TileEntity tileEntity, Entity entity, ItemStack itemStack) {
        return Block.a(blockData, worldServer, blockPosition, tileEntity, entity, itemStack);
    }

    public static NBTTagCompound getTag(ItemStack itemStack) {
        return itemStack.s();
    }

    public static boolean hasKey(NBTTagCompound nbtTagCompound, String key) {
        return nbtTagCompound.e(key);
    }

    public static int getInt(NBTTagCompound nbtTagCompound, String key) {
        return nbtTagCompound.h(key);
    }

    public static NBTTagCompound getOrCreateTag(ItemStack itemStack) {
        return itemStack.t();
    }

    public static void setInt(NBTTagCompound nbtTagCompound, String key, int value) {
        nbtTagCompound.a(key, value);
    }

    public static String getString(NBTTagCompound nbtTagCompound, String key) {
        return nbtTagCompound.l(key);
    }

    public static void setString(NBTTagCompound nbtTagCompound, String key, String value) {
        nbtTagCompound.a(key, value);
    }

    public static void remove(NBTTagCompound nbtTagCompound, String key) {
        nbtTagCompound.r(key);
    }

    public static Item getItem(ItemStack itemStack) {
        return itemStack.c();
    }

    public static void broadcastItemBreak(EntityLiving entityLiving, EnumItemSlot enumItemSlot) {
        entityLiving.c(enumItemSlot);
    }

    public static int getCount(ItemStack itemStack) {
        return itemStack.I();
    }

    public static void subtract(ItemStack itemStack, int amount) {
        itemStack.g(amount);
    }

    public static void setDamage(ItemStack itemStack, int damage) {
        itemStack.b(damage);
    }

    public static Chunk getChunkAtWorldCoords(World world, BlockPosition blockPosition) {
        return world.l(blockPosition);
    }

    public static int getCombinedId(IBlockData blockData) {
        return Block.i(blockData);
    }

    public static IBlockData getByCombinedId(int combinedId) {
        return Block.a(combinedId);
    }

    public static void setType(Chunk chunk, BlockPosition blockPosition, IBlockData blockData, boolean flag) {
        chunk.a(blockPosition, blockData, flag);
    }

    public static WorldServer getWorld(Chunk chunk) {
        return chunk.q;
    }

    public static ChunkSection[] getSections(IChunkAccess chunkAccess) {
        return chunkAccess.d();
    }

    public static ChunkCoordIntPair getPos(IChunkAccess chunk) {
        return chunk.f();
    }

    public static LightEngine getLightEngine(WorldServer worldServer) {
        return worldServer.l_();
    }

    public static IBlockData getBlockData(Block block) {
        return block.n();
    }

    public static void setTypeAndData(WorldServer worldServer, BlockPosition blockPosition, IBlockData blockData, int i) {
        worldServer.a(blockPosition, blockData, i);
    }

    public static ChunkProviderServer getChunkProvider(WorldServer worldServer) {
        return worldServer.k();
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

    public static World getWorld(Entity entity) {
        return entity.cA();
    }

    public static void addEntity(World world, Entity entity) {
        world.b(entity);
    }

    public static boolean isAlive(Entity entity) {
        return entity.bl();
    }

    public static ItemStack getItemStack(EntityItem entityItem) {
        return entityItem.h();
    }

    public static boolean isEmpty(ItemStack itemStack) {
        return itemStack.b();
    }

    public static boolean isRemoved(Entity entity) {
        return !isAlive(entity);
    }

    public static int getMaxStackSize(ItemStack itemStack) {
        return itemStack.d();
    }

    public static void die(Entity entity) {
        entity.ah();
    }

    public static void setItemStack(EntityItem entityItem, ItemStack itemStack) {
        entityItem.a(itemStack);
    }

    public static void sendPacket(PlayerConnection playerConnection, Packet<?> packet) {
        playerConnection.a(packet);
    }

    public static int getSectionIndex(LevelHeightAccessor levelHeightAccessor, int y) {
        return levelHeightAccessor.e(y);
    }

}
