package com.bgsoftware.wildtools.hooks;

import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.object.WorldCoord;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class BlocksProvider_Towny implements BlocksProvider {

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        try {
            TownBlock townBlock = WorldCoord.parseWorldCoord(block.getLocation()).getTownBlock();
            Resident resident = TownyUniverse.getDataSource().getResident(player.getName());
            if(onlyInClaim && !resident.hasTown()) return false;
            return !resident.hasTown() || resident.getTown().hasTownBlock(townBlock);
        } catch (Exception ignored) {}
        return true;
    }
}
