package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.api.hooks.ClaimsProvider;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.object.WorldCoord;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ClaimsProvider_Towny96 implements ClaimsProvider {

    @Override
    public boolean isPlayerClaim(Player player, Location location) {
        try {
            Resident resident = TownyUniverse.getDataSource().getResident(player.getName());

            if (resident == null || !resident.hasTown()) {
                return false;
            }

            TownBlock townBlock = WorldCoord.parseWorldCoord(location).getTownBlock();

            if (townBlock == null) {
                return false;
            }

            return resident.getTown().hasTownBlock(townBlock);
        } catch (NotRegisteredException e) {
            return false;
        }
    }

}
