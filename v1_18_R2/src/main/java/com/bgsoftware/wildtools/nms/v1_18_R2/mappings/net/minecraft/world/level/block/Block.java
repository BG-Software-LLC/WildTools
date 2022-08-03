package com.bgsoftware.wildtools.nms.v1_18_R2.mappings.net.minecraft.world.level.block;

import com.bgsoftware.wildtools.nms.mapping.Remap;
import com.bgsoftware.wildtools.nms.v1_18_R2.mappings.MappedObject;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;

import java.util.List;

public class Block extends MappedObject<net.minecraft.world.level.block.Block> {

    public Block(net.minecraft.world.level.block.Block handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.world.level.block.Block",
            name = "defaultBlockState",
            type = Remap.Type.METHOD,
            remappedName = "n")
    public IBlockData defaultBlockState() {
        return handle.n();
    }

    @Remap(classPath = "net.minecraft.world.level.block.Block",
            name = "getId",
            type = Remap.Type.METHOD,
            remappedName = "i")
    public static int getId(IBlockData blockData) {
        return net.minecraft.world.level.block.Block.i(blockData);
    }

    @Remap(classPath = "net.minecraft.world.level.block.Block",
            name = "stateById",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public static IBlockData getByCombinedId(int combinedId) {
        return net.minecraft.world.level.block.Block.a(combinedId);
    }

    @Remap(classPath = "net.minecraft.world.level.block.Block",
            name = "getDrops",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public static List<ItemStack> getDrops(IBlockData blockData, WorldServer worldServer, BlockPosition blockPosition,
                                    TileEntity tileEntity, Entity entity, ItemStack itemStack) {
        return net.minecraft.world.level.block.Block.a(blockData, worldServer, blockPosition, tileEntity, entity, itemStack);
    }

}
