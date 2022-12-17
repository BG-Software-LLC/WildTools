package com.bgsoftware.wildtools.nms.v1_16_R3;

import com.bgsoftware.common.reflection.ReflectField;
import it.unimi.dsi.fastutil.shorts.ShortArraySet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.minecraft.server.v1_16_R3.ChunkSection;
import net.minecraft.server.v1_16_R3.EntityItem;
import net.minecraft.server.v1_16_R3.ItemStack;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketPlayOutMultiBlockChange;
import net.minecraft.server.v1_16_R3.PlayerChunkMap;
import net.minecraft.server.v1_16_R3.PlayerMap;
import net.minecraft.server.v1_16_R3.SectionPosition;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.craftbukkit.v1_16_R3.event.CraftEventFactory;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Set;

public class NMSUtils {

    private static final ReflectField<PlayerMap> PLAYER_MAP_FIELD = new ReflectField<>(PlayerChunkMap.class, PlayerMap.class, "playerMap");
    private static Constructor<?> MULTI_BLOCK_CHANGE_CONSTRUCTOR;
    private static Class<?> SHORT_ARRAY_SET_CLASS = null;

    static {
        try {
            MULTI_BLOCK_CHANGE_CONSTRUCTOR = Arrays.stream(PacketPlayOutMultiBlockChange.class.getConstructors())
                    .filter(constructor -> constructor.getParameterCount() == 4).findFirst().orElse(null);
            SHORT_ARRAY_SET_CLASS = Class.forName("org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.shorts.ShortArraySet");
        } catch (Exception ignored) {
        }
    }

    private NMSUtils() {

    }

    public static void sendPacketToRelevantPlayers(WorldServer worldServer, int chunkX, int chunkZ, Packet<?> packet) {
        PlayerChunkMap playerChunkMap = worldServer.getChunkProvider().playerChunkMap;
        PLAYER_MAP_FIELD.get(playerChunkMap).a(1)
                .forEach(entityPlayer -> entityPlayer.playerConnection.sendPacket(packet));
    }

    public static boolean canMerge(EntityItem entityItem) {
        ItemStack itemStack = entityItem.getItemStack();
        return !itemStack.isEmpty() && itemStack.getCount() < itemStack.getMaxStackSize();
    }

    public static boolean mergeEntityItems(EntityItem entityItem, EntityItem otherEntity) {
        ItemStack itemOfEntity = entityItem.getItemStack();
        ItemStack itemOfOtherEntity = otherEntity.getItemStack();
        if (EntityItem.a(itemOfEntity, itemOfOtherEntity)) {
            if (!CraftEventFactory.callItemMergeEvent(otherEntity, entityItem).isCancelled()) {
                mergeItemsInternal(entityItem, itemOfEntity, itemOfOtherEntity);
                entityItem.pickupDelay = Math.max(entityItem.pickupDelay, otherEntity.pickupDelay);
                entityItem.age = Math.min(entityItem.age, otherEntity.age);
                if (itemOfOtherEntity.isEmpty()) {
                    otherEntity.die();
                }
            }
        }

        return entityItem.dead;
    }

    public static Set<Short> createShortSet() {
        if (SHORT_ARRAY_SET_CLASS == null)
            return new ShortArraySet();

        try {
            return (Set<Short>) SHORT_ARRAY_SET_CLASS.newInstance();
        } catch (Throwable ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static PacketPlayOutMultiBlockChange createMultiBlockChangePacket(SectionPosition sectionPosition, Set<Short> shortSet, ChunkSection chunkSection) {
        if (MULTI_BLOCK_CHANGE_CONSTRUCTOR == null) {
            return new PacketPlayOutMultiBlockChange(
                    sectionPosition,
                    (ShortSet) shortSet,
                    chunkSection,
                    true
            );
        }

        try {
            return (PacketPlayOutMultiBlockChange) MULTI_BLOCK_CHANGE_CONSTRUCTOR.newInstance(
                    sectionPosition, shortSet, chunkSection, true);
        } catch (Throwable ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static void mergeItemsInternal(EntityItem entityItem, ItemStack itemStack, ItemStack otherItem) {
        ItemStack leftOver = EntityItem.a(itemStack, otherItem, 64);
        if (!leftOver.isEmpty()) {
            entityItem.setItemStack(leftOver);
        }
    }

}
