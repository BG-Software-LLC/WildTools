package com.bgsoftware.wildtools.hooks;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class BlocksProvider_WorldGuard implements BlocksProvider {

    private WorldGuardPlugin worldGuard = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        return worldGuard.canBuild(player, block.getLocation());
    }
}
