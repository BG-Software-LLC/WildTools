package com.bgsoftware.wildtools.hooks;

import com.wasteofplastic.askyblock.ASkyBlockAPI;
import com.wasteofplastic.askyblock.Island;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class BlocksProvider_ASkyblock implements BlocksProvider {

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        Island island = ASkyBlockAPI.getInstance().getIslandAt(block.getLocation());
        if(onlyInClaim && (island == null || !island.onIsland(block.getLocation()))) return false;
        return player.hasPermission("askyblock.mod.bypassprotect") || island == null ||
                island.getOwner().equals(player.getUniqueId()) || island.getMembers().contains(player.getUniqueId());
    }

}
