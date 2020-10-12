package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.api.hooks.ClaimsProvider;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPerm;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class ClaimsProvider_MassiveFactions implements ClaimsProvider {

    @Override
    public boolean isPlayerClaim(Player player, Location location) {
        MPlayer mPlayer = MPlayer.get(player);
        Faction faction = BoardColl.get().getFactionAt(PS.valueOf(location));
        return faction != null && (mPlayer.getFaction().equals(faction) || (mPlayer.hasFaction() &&
                faction.getPermitted(MPerm.getPermAccess()).contains(faction.getRelationWish(mPlayer.getFaction()))));
    }

}
