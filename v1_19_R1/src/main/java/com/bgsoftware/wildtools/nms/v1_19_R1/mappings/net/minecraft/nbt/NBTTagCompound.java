package com.bgsoftware.wildtools.nms.v1_19_R1.mappings.net.minecraft.nbt;

import com.bgsoftware.wildtools.nms.mapping.Remap;
import com.bgsoftware.wildtools.nms.v1_19_R1.mappings.MappedObject;

public class NBTTagCompound extends MappedObject<net.minecraft.nbt.NBTTagCompound> {

    public static NBTTagCompound ofNullable(net.minecraft.nbt.NBTTagCompound handle) {
        return handle == null ? null : new NBTTagCompound(handle);
    }

    public NBTTagCompound(net.minecraft.nbt.NBTTagCompound handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "contains",
            type = Remap.Type.METHOD,
            remappedName = "e")
    public boolean contains(String key) {
        return handle.e(key);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "getInt",
            type = Remap.Type.METHOD,
            remappedName = "h")
    public int getInt(String key) {
        return handle.h(key);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "putInt",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void putInt(String key, int value) {
        handle.a(key, value);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "getString",
            type = Remap.Type.METHOD,
            remappedName = "l")
    public String getString(String key) {
        return handle.l(key);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "putString",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void putString(String key, String value) {
        handle.a(key, value);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "remove",
            type = Remap.Type.METHOD,
            remappedName = "r")
    public void remove(String key) {
        handle.r(key);
    }

}
