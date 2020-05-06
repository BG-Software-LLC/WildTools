package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.utils.container.SellInfo;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

public interface ContainerProvider {

    boolean isContainer(BlockState blockState);

    SellInfo sellContainer(BlockState blockState, Player player);

    void removeContainer(BlockState blockState, SellInfo sellInfo);

}
