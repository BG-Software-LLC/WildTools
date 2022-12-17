package com.bgsoftware.wildtools.nms.v1191;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import org.bukkit.craftbukkit.v1_19_R1.event.CraftEventFactory;

import java.lang.reflect.Modifier;
import java.util.Map;

public class NMSUtils {

    private static final ReflectMethod<Void> SEND_PACKETS_TO_RELEVANT_PLAYERS = new ReflectMethod<>(
            ChunkHolder.class, 1, Packet.class, boolean.class);
    private static final ReflectField<Map<Long, ChunkHolder>> VISIBLE_CHUNKS = new ReflectField<>(
            ChunkMap.class, Map.class, Modifier.PUBLIC | Modifier.VOLATILE, 1);

    private NMSUtils() {

    }

    public static void sendPacketToRelevantPlayers(ServerLevel serverLevel, int chunkX, int chunkZ, Packet<?> packet) {
        ChunkMap chunkMap = serverLevel.getChunkSource().chunkMap;
        ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
        ChunkHolder chunkHolder;

        try {
            chunkHolder = chunkMap.getVisibleChunkIfPresent(chunkPos.toLong());
        } catch (Throwable ex) {
            chunkHolder = VISIBLE_CHUNKS.get(chunkMap).get(chunkPos.toLong());
        }

        if (chunkHolder != null) {
            SEND_PACKETS_TO_RELEVANT_PLAYERS.invoke(chunkHolder, packet, false);
        }
    }

    public static boolean canMerge(ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getItem();
        return !itemStack.isEmpty() && itemStack.getCount() < itemStack.getMaxStackSize();
    }

    public static boolean mergeEntityItems(ItemEntity itemEntity, ItemEntity otherItemEntity) {
        ItemStack itemOfEntity = itemEntity.getItem();
        ItemStack itemOfOtherEntity = otherItemEntity.getItem();
        if (ItemEntity.areMergable(itemOfEntity, itemOfOtherEntity)) {
            if (!CraftEventFactory.callItemMergeEvent(otherItemEntity, itemEntity).isCancelled()) {
                mergeItemsInternal(itemEntity, itemOfEntity, itemOfOtherEntity);
                itemEntity.age = Math.max(itemEntity.age, otherItemEntity.age);
                itemEntity.pickupDelay = Math.max(itemEntity.pickupDelay, otherItemEntity.pickupDelay);
                if (itemOfOtherEntity.isEmpty()) {
                    otherItemEntity.discard();
                }
            }
        }

        return !itemEntity.isAlive();
    }

    private static void mergeItemsInternal(ItemEntity itemEntity, ItemStack itemStack, ItemStack otherItem) {
        ItemStack leftOver = ItemEntity.merge(itemStack, otherItem, itemStack.getMaxStackSize());
        if (!leftOver.isEmpty()) {
            itemEntity.setItem(leftOver);
        }
    }

}
