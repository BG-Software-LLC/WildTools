package com.bgsoftware.wildtools.nms.v1_19_R1.mappings.net.minecraft.world.item;

import com.bgsoftware.wildtools.nms.mapping.Remap;
import com.bgsoftware.wildtools.nms.v1_19_R1.mappings.MappedObject;
import com.bgsoftware.wildtools.nms.v1_19_R1.mappings.net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.item.Item;

public class ItemStack extends MappedObject<net.minecraft.world.item.ItemStack> {

    public ItemStack(net.minecraft.world.item.ItemStack handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.world.item.ItemStack",
            name = "getTag",
            type = Remap.Type.METHOD,
            remappedName = "u")
    public NBTTagCompound getTag() {
        return NBTTagCompound.ofNullable(handle.u());
    }

    @Remap(classPath = "net.minecraft.world.item.ItemStack",
            name = "getOrCreateTag",
            type = Remap.Type.METHOD,
            remappedName = "v")
    public NBTTagCompound getOrCreateTag() {
        return new NBTTagCompound(handle.v());
    }

    @Remap(classPath = "net.minecraft.world.item.ItemStack",
            name = "getItem",
            type = Remap.Type.METHOD,
            remappedName = "c")
    public Item getItem() {
        return handle.c();
    }

    @Remap(classPath = "net.minecraft.world.item.ItemStack",
            name = "getCount",
            type = Remap.Type.METHOD,
            remappedName = "K")
    public int getCount() {
        return handle.K();
    }

    @Remap(classPath = "net.minecraft.world.item.ItemStack",
            name = "shrink",
            type = Remap.Type.METHOD,
            remappedName = "g")
    public void shrink(int amount) {
        handle.g(amount);
    }

    @Remap(classPath = "net.minecraft.world.item.ItemStack",
            name = "setDamageValue",
            type = Remap.Type.METHOD,
            remappedName = "b")
    public void setDamageValue(int damage) {
        handle.b(damage);
    }

    @Remap(classPath = "net.minecraft.world.item.ItemStack",
            name = "isEmpty",
            type = Remap.Type.METHOD,
            remappedName = "b")
    public boolean isEmpty() {
        return handle.b();
    }

    @Remap(classPath = "net.minecraft.world.item.ItemStack",
            name = "getMaxStackSize",
            type = Remap.Type.METHOD,
            remappedName = "f")
    public int getMaxStackSize() {
        return handle.f();
    }

}
