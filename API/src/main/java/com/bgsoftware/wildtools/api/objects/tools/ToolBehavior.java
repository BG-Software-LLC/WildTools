package com.bgsoftware.wildtools.api.objects.tools;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public interface ToolBehavior {

    default boolean onBlockBreak(Tool tool, BlockBreakEvent e) {
        return false;
    }

    default boolean onBlockInteract(Tool tool, PlayerInteractEvent e) {
        return false;
    }

    default boolean onBlockHit(Tool tool, PlayerInteractEvent e) {
        return false;
    }

    default boolean onAirInteract(Tool tool, PlayerInteractEvent e) {
        return false;
    }

}
