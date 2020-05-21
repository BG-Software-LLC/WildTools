package com.bgsoftware.wildtools.hooks;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class BlocksProvider_WorldGuardOld implements BlocksProvider {

    private final WorldGuardPlugin worldGuard = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");

    @Override
    public Plugin getPlugin() {
        return worldGuard;
    }

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        return hasBypass(player, block.getWorld()) || canBuild(player, block);
    }

    private boolean hasBypass(Player player, World world){
        return worldGuard.hasPermission(player, "worldguard.region.bypass." + world.getName());
    }

    private boolean canBuild(Player player, Block block) {
        return worldGuard.canBuild(player, block);
    }

}
