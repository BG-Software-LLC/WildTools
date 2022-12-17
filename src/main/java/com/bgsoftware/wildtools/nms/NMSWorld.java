package com.bgsoftware.wildtools.nms;

import com.bgsoftware.wildtools.utils.math.Vector3;
import com.bgsoftware.wildtools.utils.world.WorldEditSession;
import org.bukkit.Chunk;
import org.bukkit.CropState;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface NMSWorld {

    List<ItemStack> getBlockDrops(Player player, Block block, boolean silkTouch);

    int getExpFromBlock(Block block, Player player);

    boolean isFullyGrown(Block block);

    void setCropState(Block block, CropState cropState);

    void setBlockFast(World world, Vector3 location, int combinedId, boolean sendUpdate);

    void refreshChunk(Chunk chunk, List<WorldEditSession.BlockData> blocksList);

    int getCombinedId(Block block);

    boolean isOutsideWorldBorder(Location location);

    void dropItems(World world, Vector3 dropLocation, List<ItemStack> droppedItems);

    int getMinHeight(World world);

}
