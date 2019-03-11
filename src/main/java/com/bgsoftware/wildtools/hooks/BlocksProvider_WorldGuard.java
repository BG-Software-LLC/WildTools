package com.bgsoftware.wildtools.hooks;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
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

    private boolean canBuild(Player player, Block block){
        try {
            RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
            LocalPlayer localPlayer = worldGuard.wrapPlayer(player);
            Location location = new Location(localPlayer.getExtent(), block.getX(), block.getY(), block.getZ());
            return regionContainer.createQuery().testBuild(location, worldGuard.wrapPlayer(player));
        }catch(Throwable ex){
            try {
                return (boolean) worldGuard.getClass().getMethod("canBuild", Player.class, Block.class).invoke(worldGuard, player, block);
            }catch(Exception ignored){}
        }
        return false;
    }

}
