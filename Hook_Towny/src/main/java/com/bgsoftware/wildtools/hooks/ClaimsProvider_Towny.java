package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.api.hooks.ClaimsProvider;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.object.WorldCoord;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class ClaimsProvider_Towny implements ClaimsProvider {

    @Override
    public boolean isPlayerClaim(Player player, Location location) {
        try {
            TownBlock townBlock = WorldCoord.parseWorldCoord(location).getTownBlock();
            Resident resident = TownyUniverse.getDataSource().getResident(player.getName());
            return resident != null && resident.hasTown() && resident.getTown().hasTownBlock(townBlock);
        } catch (NotRegisteredException ex) {
            return false;
        }
    }

}
