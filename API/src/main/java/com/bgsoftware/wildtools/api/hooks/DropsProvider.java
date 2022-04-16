package com.bgsoftware.wildtools.api.hooks;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public interface DropsProvider {

    /**
     * Get all the drops for the block.
     * This method is used to get custom drops for blocks, and add them to the inventory of the player if the tool
     * has auto collect enabled. WildTools adds "drop-items" metadata to the block. If it's enabled, then you should
     * drop the items of the block on your own. Otherwise, WildTools will handle that part.
     * @param player The player that broke the block.
     * @param block The block that was broken.
     * @return If you want WildTools to handle block drops, return null. Otherwise, WildTools will drop the items
     * returned by this method, even if the list is empty.
     */
    @Nullable
    List<ItemStack> getBlockDrops(Player player, Block block);

    /**
     * Check whether or not this handler is used only for spawners.
     */
    boolean isSpawnersOnly();

}
