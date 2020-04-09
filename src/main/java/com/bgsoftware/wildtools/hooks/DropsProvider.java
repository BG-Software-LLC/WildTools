package com.bgsoftware.wildtools.hooks;

import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface DropsProvider {

    List<ItemStack> getBlockDrops(Block block);

    default boolean isSpawnersOnly(){
        return false;
    }

    default boolean callEvent(){
        return true;
    }

}
