package com.bgsoftware.wildtools.hooks;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class BlocksProvider_GriefPrevention implements BlocksProvider {

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        DataStore dataStore = GriefPrevention.instance.dataStore;
        PlayerData playerData = dataStore.getPlayerData(player.getUniqueId());
        Claim claim = dataStore.getClaimAt(block.getLocation(), false, playerData.lastClaim);
        if(onlyInClaim && claim == null) return false;
        return claim == null || playerData.ignoreClaims || claim.allowAccess(player) == null;
    }
}
