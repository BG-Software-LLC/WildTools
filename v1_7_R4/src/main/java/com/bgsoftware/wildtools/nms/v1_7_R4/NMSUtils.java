package com.bgsoftware.wildtools.nms.v1_7_R4;

import net.minecraft.server.v1_7_R4.EntityItem;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.ItemStack;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PlayerChunkMap;
import net.minecraft.server.v1_7_R4.WorldServer;

public class NMSUtils {

    private NMSUtils() {

    }

    public static void sendPacketToRelevantPlayers(WorldServer worldServer, int chunkX, int chunkZ, Packet packet) {
        PlayerChunkMap playerChunkMap = worldServer.getPlayerChunkMap();
        for (Object entityHuman : worldServer.players) {
            if (entityHuman instanceof EntityPlayer && playerChunkMap.a((EntityPlayer) entityHuman, chunkX, chunkZ))
                ((EntityPlayer) entityHuman).playerConnection.sendPacket(packet);
        }
    }

    public static boolean canMerge(EntityItem entityItem) {
        ItemStack itemStack = entityItem.getItemStack();
        return itemStack.count < itemStack.getMaxStackSize();
    }

    public static boolean mergeEntityItems(EntityItem entityItem, EntityItem otherEntity) {
        ItemStack itemOfEntity = entityItem.getItemStack();
        ItemStack itemOfOtherEntity = otherEntity.getItemStack();
        if (canMergeTogetherInternal(itemOfEntity, itemOfOtherEntity)) {
            mergeItemsInternal(entityItem, itemOfEntity, otherEntity, itemOfOtherEntity);
            entityItem.pickupDelay = Math.max(entityItem.pickupDelay, otherEntity.pickupDelay);
            if (itemOfOtherEntity.count <= 0) {
                otherEntity.die();
            }
        }

        return entityItem.dead;
    }

    private static void mergeItemsInternal(EntityItem entityItem, ItemStack itemStack, EntityItem otherEntity, ItemStack otherItem) {
        int amountLeftUntilFullStack = Math.min(itemStack.getMaxStackSize() - itemStack.count, otherItem.count);
        ItemStack itemStackClone = itemStack.cloneItemStack();
        itemStackClone.count += amountLeftUntilFullStack;
        entityItem.setItemStack(itemStackClone);
        otherItem.count -= amountLeftUntilFullStack;
        if (otherItem.count <= 0) {
            otherEntity.setItemStack(otherItem);
        }
    }

    private static boolean canMergeTogetherInternal(ItemStack itemStack, ItemStack otherItem) {
        if (itemStack.getItem() != otherItem.getItem())
            return false;

        if (itemStack.count + otherItem.count > otherItem.getMaxStackSize())
            return false;

        if (itemStack.hasTag() ^ otherItem.hasTag())
            return false;

        return !otherItem.hasTag() || otherItem.getTag().equals(itemStack.getTag());
    }

}
