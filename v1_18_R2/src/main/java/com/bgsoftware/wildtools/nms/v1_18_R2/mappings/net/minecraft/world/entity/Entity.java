package com.bgsoftware.wildtools.nms.v1_18_R2.mappings.net.minecraft.world.entity;

import com.bgsoftware.wildtools.nms.mapping.Remap;
import com.bgsoftware.wildtools.nms.v1_18_R2.mappings.MappedObject;
import com.bgsoftware.wildtools.nms.v1_18_R2.mappings.net.minecraft.server.network.PlayerConnection;
import com.bgsoftware.wildtools.nms.v1_18_R2.mappings.net.minecraft.world.item.ItemStack;
import com.bgsoftware.wildtools.nms.v1_18_R2.mappings.net.minecraft.world.level.World;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.stats.Statistic;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.item.EntityItem;

public class Entity extends MappedObject<net.minecraft.world.entity.Entity> {

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
            remappedName = "bl")
    public boolean isAlive() {
        return handle.bl();
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
            remappedName = "h")
    public ItemStack getItem() {
        return new ItemStack(((EntityItem) handle).h());
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
//        ((EntityLiving) handle).d(enumItemSlot);
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
