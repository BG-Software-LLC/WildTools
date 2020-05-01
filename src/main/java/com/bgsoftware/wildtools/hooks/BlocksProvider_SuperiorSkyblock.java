package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;

public final class BlocksProvider_SuperiorSkyblock implements BlocksProvider {

    @Override
    public Plugin getPlugin() {
        return (Plugin) SuperiorSkyblockAPI.getSuperiorSkyblock();
    }

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

    @Override
    public boolean canPickupItem(Player player, Item item) {
        Island island = SuperiorSkyblockAPI.getIslandAt(item.getLocation());
        return island == null || island.hasPermission(SuperiorSkyblockAPI.getPlayer(player), IslandPermission.PICKUP_DROPS);
    }
}
