package com.bgsoftware.wildtools.hooks;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface BlockActionProvider {

    void onBlockBreak(Player player, Block block, ItemStack usedItem);

}
