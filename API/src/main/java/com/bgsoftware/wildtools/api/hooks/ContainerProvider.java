package com.bgsoftware.wildtools.api.hooks;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

public interface ContainerProvider {

    /**
     * Checks whether or not a block is a container.
     * @param blockState The block to check.
     */
    boolean isContainer(BlockState blockState);

    /**
     * Performs a sell task on the container.
     * @param blockState The container to sell.
     * @param player The player who sold the container.
     * @return A SellInfo object with all the information about the transaction.
     */
    SellInfo sellContainer(BlockState blockState, Player player);

    /**
     * Performs a cleaning of items after the sell task completes.
     * @param blockState The container to remove items from.
     * @param sellInfo The object that was returned by the sellContainer method.
     */
    void removeContainer(BlockState blockState, SellInfo sellInfo);

}
