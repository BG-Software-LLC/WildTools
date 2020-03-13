package com.bgsoftware.wildtools.hooks;

import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface DropsProvider {

    List<ItemStack> getBlockDrops(Block block);

}
