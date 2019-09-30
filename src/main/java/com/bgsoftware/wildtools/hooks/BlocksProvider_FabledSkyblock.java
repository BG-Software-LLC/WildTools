package com.bgsoftware.wildtools.hooks;

import com.songoda.skyblock.api.SkyBlockAPI;
import com.songoda.skyblock.api.island.Island;
import com.songoda.skyblock.api.island.IslandRole;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class BlocksProvider_FabledSkyblock implements BlocksProvider {

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        Island island = SkyBlockAPI.getIslandManager().getIslandAtLocation(block.getLocation());
        if(onlyInClaim && island == null) return false;
        return island == null || player.isOp() || island.isCoopPlayer(player.getUniqueId()) ||
                (island.getRole(player) != null && island.getRole(player) != IslandRole.VISITOR);
    }
}
