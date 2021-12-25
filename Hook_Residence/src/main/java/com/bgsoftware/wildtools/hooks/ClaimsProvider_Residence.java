package com.bgsoftware.wildtools.hooks;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bgsoftware.wildtools.api.hooks.ClaimsProvider;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class ClaimsProvider_Residence implements ClaimsProvider {

    @Override
    public boolean isPlayerClaim(Player player, Location location) {
        ClaimedResidence residence = ResidenceApi.getResidenceManager().getByLoc(location);
        return residence != null && residence.getPermissions().playerHas(player, Flags.destroy, false);
    }

}
