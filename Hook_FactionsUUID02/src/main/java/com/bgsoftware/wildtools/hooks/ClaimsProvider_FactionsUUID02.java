package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.api.hooks.ClaimsProvider;
import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.listeners.FactionsBlockListener;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class ClaimsProvider_FactionsUUID02 implements ClaimsProvider {

    @Override
    public boolean isPlayerClaim(Player player, Location location) {
        Faction faction = Board.getInstance().getFactionAt(new FLocation(location));
        return !faction.isWilderness() && FactionsBlockListener
                .playerCanBuildDestroyBlock(player, location, "DESTROY", true);
    }

}
