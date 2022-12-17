package com.bgsoftware.wildtools.nms.v1_12_R1;

import net.minecraft.server.v1_12_R1.EntityHuman;
import net.minecraft.server.v1_12_R1.EntityItem;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.Packet;
import net.minecraft.server.v1_12_R1.PlayerChunkMap;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.craftbukkit.v1_12_R1.event.CraftEventFactory;

public class NMSUtils {

    private NMSUtils() {

    }

    public static void sendPacketToRelevantPlayers(WorldServer worldServer, int chunkX, int chunkZ, Packet<?> packet) {
        PlayerChunkMap playerChunkMap = worldServer.getPlayerChunkMap();
        for (EntityHuman entityHuman : worldServer.players) {
            if (entityHuman instanceof EntityPlayer && playerChunkMap.a((EntityPlayer) entityHuman, chunkX, chunkZ))
                ((EntityPlayer) entityHuman).playerConnection.sendPacket(packet);
        }
    }

    public static boolean canMerge(EntityItem entityItem) {
        ItemStack itemStack = entityItem.getItemStack();
        return !itemStack.isEmpty() && itemStack.getCount() < itemStack.getMaxStackSize();
    }

    public static boolean mergeEntityItems(EntityItem entityItem, EntityItem otherEntity) {
        ItemStack itemOfEntity = entityItem.getItemStack();
        ItemStack itemOfOtherEntity = otherEntity.getItemStack();
        if (canMergeTogetherInternal(itemOfEntity, itemOfOtherEntity)) {
            if (!CraftEventFactory.callItemMergeEvent(otherEntity, entityItem).isCancelled()) {
                mergeItemsInternal(entityItem, itemOfEntity, otherEntity, itemOfOtherEntity);
                entityItem.pickupDelay = Math.max(entityItem.pickupDelay, otherEntity.pickupDelay);
                if (itemOfOtherEntity.isEmpty()) {
                    otherEntity.die();
                }
            }
        }

        return entityItem.dead;
    }

    private static boolean canMergeTogetherInternal(ItemStack itemStack, ItemStack otherItem) {
        if (itemStack.getItem() != otherItem.getItem())
            return false;

        if (itemStack.getCount() + otherItem.getCount() > otherItem.getMaxStackSize())
            return false;

        if (itemStack.hasTag() ^ otherItem.hasTag())
            return false;

        return !otherItem.hasTag() || otherItem.getTag().equals(itemStack.getTag());
    }

    private static void mergeItemsInternal(EntityItem entityItem, ItemStack itemStack, EntityItem otherEntity, ItemStack otherItem) {
        int amountLeftUntilFullStack = Math.min(itemStack.getMaxStackSize() - itemStack.getCount(), otherItem.getCount());
        ItemStack itemStackClone = itemStack.cloneItemStack();
        itemStackClone.add(amountLeftUntilFullStack);
        entityItem.setItemStack(itemStackClone);
        otherItem.subtract(amountLeftUntilFullStack);
        if (!otherItem.isEmpty()) {
            otherEntity.setItemStack(otherItem);
        }
    }

}
