package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class ClaimProvider_FactionsUUID implements ClaimProvider {

    public ClaimProvider_FactionsUUID(){
        WildToolsPlugin.log(" - Using FactionsUUID as ClaimProvider.");
    }

    @Override
    public boolean inClaim(Player player, Location location) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        Faction faction = fPlayer.getFaction();
        Faction locationFaction = Board.getInstance().getFactionAt(new FLocation(location));
        return faction != null && locationFaction != null && faction.getId().equals(locationFaction.getId());
    }
}
