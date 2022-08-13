package com.bgsoftware.wildtools.nms.v1_19_R1.mappings.net.minecraft.world.entity;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.common.remaps.Remap;
import com.bgsoftware.wildtools.nms.v1_19_R1.mappings.MappedObject;
import com.bgsoftware.wildtools.nms.v1_19_R1.mappings.net.minecraft.server.network.PlayerConnection;
import com.bgsoftware.wildtools.nms.v1_19_R1.mappings.net.minecraft.world.item.ItemStack;
import com.bgsoftware.wildtools.nms.v1_19_R1.mappings.net.minecraft.world.level.World;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.stats.Statistic;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.item.EntityItem;

public class Entity extends MappedObject<net.minecraft.world.entity.Entity> {

    private static final ReflectMethod<Boolean> ENTITY_IS_ALIVE;
    private static final ReflectMethod<net.minecraft.world.item.ItemStack> ENTITY_ITEM_GET_ITEM;

    static {
        ReflectMethod<?> method119 = new ReflectMethod<>(EntityItem.class, net.minecraft.world.item.ItemStack.class, "h");
        boolean is119Mappings = method119.isValid();

        if (is119Mappings) {
            ENTITY_IS_ALIVE = new ReflectMethod<>(net.minecraft.world.entity.Entity.class, "bp");
            ENTITY_ITEM_GET_ITEM = new ReflectMethod<>(EntityItem.class, "h");
        } else {
            ENTITY_IS_ALIVE = null;
            ENTITY_ITEM_GET_ITEM = null;
        }

    }

    public Entity(net.minecraft.world.entity.Entity handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "getLevel",
            type = Remap.Type.METHOD,
            remappedName = "W")
    public World getLevel() {
        return new World(handle.W());
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "isAlive",
            type = Remap.Type.METHOD,
            remappedName = "bo")
    public boolean isAlive() {
        return ENTITY_IS_ALIVE == null ? handle.bo() : ENTITY_IS_ALIVE.invoke(handle);
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "discard",
            type = Remap.Type.METHOD,
            remappedName = "ah")
    public void discard() {
        handle.ah();
    }

    @Remap(classPath = "net.minecraft.world.entity.item.ItemEntity",
            name = "getItem",
            type = Remap.Type.METHOD,
            remappedName = "i")
    public ItemStack getItem() {
        return new ItemStack(ENTITY_ITEM_GET_ITEM == null ? ((EntityItem) handle).i() :
                ENTITY_ITEM_GET_ITEM.invoke(handle));
    }

    @Remap(classPath = "net.minecraft.world.entity.item.ItemEntity",
            name = "setItem",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void setItem(net.minecraft.world.item.ItemStack itemStack) {
        ((EntityItem) handle).a(itemStack);
    }

    @Remap(classPath = "net.minecraft.world.entity.item.ItemEntity",
            name = "age",
            type = Remap.Type.FIELD,
            remappedName = "ao")
    public void setAge(int age) {
        ((EntityItem) handle).ao = age;
    }

    public int getAge() {
        return ((EntityItem) handle).ao;
    }

    @Remap(classPath = "net.minecraft.world.entity.item.ItemEntity",
            name = "pickupDelay",
            type = Remap.Type.FIELD,
            remappedName = "ap")
    public void setPickupDelay(int pickupDelay) {
        ((EntityItem) handle).ap = pickupDelay;
    }

    public int getPickupDelay() {
        return ((EntityItem) handle).ap;
    }

    @Remap(classPath = "net.minecraft.world.entity.LivingEntity",
            name = "broadcastBreakEvent",
            type = Remap.Type.METHOD,
            remappedName = "d")
    public void broadcastBreakEvent(EnumItemSlot enumItemSlot) {
        ((EntityLiving) handle).d(enumItemSlot);
    }

    @Remap(classPath = "net.minecraft.server.level.ServerPlayer",
            name = "connection",
            type = Remap.Type.FIELD,
            remappedName = "b")
    public PlayerConnection getPlayerConnection() {
        return new PlayerConnection(((EntityPlayer) handle).b);
    }

    @Remap(classPath = "net.minecraft.world.entity.player.Player",
            name = "awardStat",
            type = Remap.Type.METHOD,
            remappedName = "b")
    public void awardStat(Statistic<?> statistic) {
        ((EntityPlayer) handle).b(statistic);
    }

}
