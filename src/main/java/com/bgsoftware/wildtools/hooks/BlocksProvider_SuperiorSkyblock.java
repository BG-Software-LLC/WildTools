package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

public final class BlocksProvider_SuperiorSkyblock implements BlocksProvider {

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        Island island = SuperiorSkyblockAPI.getIslandAt(block.getLocation());
        if(onlyInClaim && island == null) return false;
        return island == null || (island.isInsideRange(block.getLocation()) && island.hasPermission(SuperiorSkyblockAPI.getPlayer(player), IslandPermission.BUILD));
    }

    @Override
    public boolean canInteract(Player player, Block block, boolean onlyInClaim) {
        Island island = SuperiorSkyblockAPI.getIslandAt(block.getLocation());

        IslandPermission islandPermission;

        if(block.getState() instanceof Chest) islandPermission = IslandPermission.CHEST_ACCESS;
        else if(block.getState() instanceof InventoryHolder) islandPermission = IslandPermission.USE;
        else if(block.getState() instanceof Sign) islandPermission = IslandPermission.SIGN_INTERACT;
        else islandPermission = IslandPermission.INTERACT;

        if(onlyInClaim && island == null) return false;

        return island == null || (island.isInsideRange(block.getLocation()) && island.hasPermission(SuperiorSkyblockAPI.getPlayer(player), islandPermission));
    }
}
