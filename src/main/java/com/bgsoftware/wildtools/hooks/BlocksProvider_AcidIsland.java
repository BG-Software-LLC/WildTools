package com.bgsoftware.wildtools.hooks;

import com.wasteofplastic.acidisland.ASkyBlockAPI;
import com.wasteofplastic.acidisland.Island;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class BlocksProvider_AcidIsland implements BlocksProvider {

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        Island island = ASkyBlockAPI.getInstance().getIslandAt(block.getLocation());
        if(onlyInClaim && island == null) return false;
        return player.hasPermission("acidisland.mod.bypassprotect") || island == null ||
                island.getOwner().equals(player.getUniqueId()) || island.getMembers().contains(player.getUniqueId());
    }

}
