package com.bgsoftware.wildtools.hooks.listener;

import com.bgsoftware.wildtools.utils.math.Vector3;
import org.bukkit.World;

public interface IToolBlockListener {

    void recordBlockChange(World world, Vector3 location, Action action);

    enum Action {

        BLOCK_PLACE,
        BLOCK_BREAK

    }

}
