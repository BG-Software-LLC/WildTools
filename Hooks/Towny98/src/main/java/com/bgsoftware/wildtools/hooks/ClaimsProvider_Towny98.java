package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.api.hooks.ClaimsProvider;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ClaimsProvider_Towny98 implements ClaimsProvider {

    @Override
    public boolean isPlayerClaim(Player player, Location location) {
        TownyAPI towny = TownyAPI.getInstance();

        try {
            Resident resident = towny.getResident(player.getName());

            if (resident == null || !resident.hasTown()) {
                return false;
            }

            TownBlock townBlock = WorldCoord.parseWorldCoord(location).getTownBlock();

            if (townBlock == null) {
                return false;
            }

            boolean isSameTown = resident.getTown().hasTownBlock(townBlock);

            if (!isSameTown) {
                return false;
            }

            Material blockMaterial = location.getBlock().getType();

            return (
                isPlayerPermitted(player, location, blockMaterial, TownyPermission.ActionType.BUILD) &&
                isPlayerPermitted(player, location, blockMaterial, TownyPermission.ActionType.DESTROY) &&
                isPlayerPermitted(player, location, blockMaterial, TownyPermission.ActionType.SWITCH)
            );
        } catch (NotRegisteredException e) {
            return false;
        }
    }

    private boolean isPlayerPermitted(Player player, Location location, Material blockMaterial, TownyPermission.ActionType actionType) {
        return PlayerCacheUtil.getCachePermission(player, location, blockMaterial, actionType);
    }

}
