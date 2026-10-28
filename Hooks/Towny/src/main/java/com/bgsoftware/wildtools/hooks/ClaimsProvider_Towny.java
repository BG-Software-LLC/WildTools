package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.api.hooks.ClaimsProvider;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ClaimsProvider_Towny implements ClaimsProvider {

    @Override
    public boolean isPlayerClaim(Player player, Location location) {
        TownyAPI towny = TownyAPI.getInstance();

        try {
            Resident resident = towny.getResident(player.getName());
            if (resident == null || !resident.hasTown()) {
                return false;
            }

            TownBlock locationTownBlock = towny.getTownBlock(location);
            if (locationTownBlock == null) {
                return false;
            }

            Town residentTown = resident.getTown();

            return residentTown.hasTownBlock(locationTownBlock);
        } catch (NotRegisteredException ex) {
            return false;
        }
    }
}
