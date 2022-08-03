package com.bgsoftware.wildtools.nms.v1_19_R1.mappings.net.minecraft.world.item;

import com.bgsoftware.wildtools.nms.mapping.Remap;
import com.bgsoftware.wildtools.nms.v1_19_R1.mappings.MappedObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.IBlockData;

public class Item extends MappedObject<net.minecraft.world.item.Item> {

    public Item(net.minecraft.world.item.Item handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.world.item.Item",
            name = "getDestroySpeed",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public float getDestroySpeed(ItemStack itemStack, IBlockData blockData) {
        return handle.a(itemStack, blockData);
    }

}
