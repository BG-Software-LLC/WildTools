package com.bgsoftware.wildtools.hooks;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class BlocksProvider_FactionsUUID implements BlocksProvider {

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        Faction faction = Board.getInstance().getFactionAt(new FLocation(block.getLocation()));
        if(onlyInClaim && faction.isWilderness()) return false;
        return faction.isWilderness() || fPlayer.isAdminBypassing() || (fPlayer.hasFaction() && fPlayer.getFaction().equals(faction));
    }
}
