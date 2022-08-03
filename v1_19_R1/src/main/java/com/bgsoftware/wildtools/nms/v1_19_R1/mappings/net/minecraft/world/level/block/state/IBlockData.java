package com.bgsoftware.wildtools.nms.v1_19_R1.mappings.net.minecraft.world.level.block.state;

import com.bgsoftware.wildtools.nms.mapping.Remap;
import com.bgsoftware.wildtools.nms.v1_19_R1.mappings.MappedObject;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.TileEntity;

import java.util.List;

public class IBlockData extends MappedObject<net.minecraft.world.level.block.state.IBlockData> {

    public IBlockData(net.minecraft.world.level.block.state.IBlockData handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.world.level.block.state.BlockBehaviour$BlockStateBase",
            name = "getBlock",
            type = Remap.Type.METHOD,
            remappedName = "b")
    public Block getBlock() {
        return handle.b();
    }

}
