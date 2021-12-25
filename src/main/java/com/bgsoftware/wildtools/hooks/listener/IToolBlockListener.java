package com.bgsoftware.wildtools.hooks.listener;

import org.bukkit.Location;

public interface IToolBlockListener {

    void recordBlockChange(Location location, Action action);

    enum Action {

        BLOCK_PLACE,
        BLOCK_BREAK

    }

}
