package com.bgsoftware.wildtools.nms.v1_18_R1.mappings.net.minecraft.world.inventory;

import com.bgsoftware.wildtools.nms.mapping.Remap;
import com.bgsoftware.wildtools.nms.v1_18_R1.mappings.MappedObject;

public class ContainerProperty extends MappedObject<net.minecraft.world.inventory.ContainerProperty> {

    public ContainerProperty(net.minecraft.world.inventory.ContainerProperty handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.world.inventory.DataSlot",
            name = "set",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void set(int value) {
        handle.a(value);
    }

    @Remap(classPath = "net.minecraft.world.inventory.DataSlot",
            name = "get",
            type = Remap.Type.METHOD,
            remappedName = "b")
    public int get() {
        return handle.b();
    }

}
