package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.api.hooks.ClaimsProvider;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class ClaimsProvider_GriefPrevention implements ClaimsProvider {

    @Override
    public boolean isPlayerClaim(Player player, Location location) {
        DataStore dataStore = GriefPrevention.instance.dataStore;
        PlayerData playerData = dataStore.getPlayerData(player.getUniqueId());
        Claim claim = dataStore.getClaimAt(location, false, playerData.lastClaim);
        return claim != null && claim.allowBreak(player, location.getBlock().getType()) == null;
    }

}
