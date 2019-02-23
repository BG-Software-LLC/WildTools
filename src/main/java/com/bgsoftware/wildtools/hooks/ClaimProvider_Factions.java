package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class ClaimProvider_Factions implements ClaimProvider {

    public ClaimProvider_Factions(){
        WildToolsPlugin.log(" - Using MassiveCore Factions as ClaimProvider.");
    }

    @Override
    public boolean inClaim(Player player, Location location) {
        MPlayer mPlayer = MPlayer.get(player);
        Faction faction = mPlayer.getFaction();
        Faction locationFaction = BoardColl.get().getFactionAt(PS.valueOf(location));
        return faction != null && locationFaction != null && faction.getId().equals(locationFaction.getId());
    }
}
