package com.bgsoftware.wildtools.hooks;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import pl.islandworld.IslandWorld;
import pl.islandworld.entity.SimpleIsland;

public final class BlocksProvider_IslandWorld implements BlocksProvider {

    private final IslandWorld plugin = IslandWorld.getInstance();

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        String coords = plugin.hashMeFromLoc(block.getLocation());
        SimpleIsland island = plugin.getCoordList().get(coords);
        return !(onlyInClaim && island == null) && plugin.canBuildOnLocation(player, block.getLocation());
    }

}
