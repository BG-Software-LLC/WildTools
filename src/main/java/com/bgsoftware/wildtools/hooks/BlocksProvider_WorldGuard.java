package com.bgsoftware.wildtools.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class BlocksProvider_WorldGuard implements BlocksProvider {

    private WorldGuardPlugin worldGuard = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        return hasBypass(player, block.getWorld()) || canBuild(player, block);
    }

    private boolean hasBypass(Player player, World world){
        return worldGuard.hasPermission(player, "worldguard.region.bypass." + world.getName());
    }

    private boolean canBuild(Player player, Block block) {
        try {
            WorldGuardPlatform worldGuardPlatform = WorldGuard.getInstance().getPlatform();
            RegionContainer regionContainer = worldGuardPlatform.getRegionContainer();
            com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(block.getWorld());
            RegionManager regionManager = regionContainer.get(world);

            if(regionManager == null)
                return false;

            LocalPlayer localPlayer = worldGuard.wrapPlayer(player);
            BlockVector3 blockVector3 = BlockVector3.at(block.getX(), block.getY(), block.getZ());
            ApplicableRegionSet set = regionManager.getApplicableRegions(blockVector3);
            return worldGuardPlatform.getSessionManager().hasBypass(localPlayer, world) ||
                    set.testState(localPlayer, Flags.BUILD) || set.testState(localPlayer, Flags.BLOCK_BREAK);
        }catch(Throwable ex){
            try {
                return (boolean) worldGuard.getClass().getMethod("canBuild", Player.class, Block.class).invoke(worldGuard, player, block);
            }catch(Exception ignored){}
        }
        return false;
    }

}
