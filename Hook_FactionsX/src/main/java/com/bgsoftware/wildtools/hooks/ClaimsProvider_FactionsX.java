package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.api.hooks.ClaimsProvider;
import net.prosavage.factionsx.core.FPlayer;
import net.prosavage.factionsx.core.Faction;
import net.prosavage.factionsx.manager.GridManager;
import net.prosavage.factionsx.manager.PlayerManager;
import net.prosavage.factionsx.persist.data.FactionsKt;
import net.prosavage.factionsx.util.PlayerAction;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class ClaimsProvider_FactionsX implements ClaimsProvider {

    @Override
    public boolean isPlayerClaim(Player player, Location location) {
        FPlayer fPlayer = PlayerManager.INSTANCE.getFPlayer(player);
        Faction faction = GridManager.INSTANCE.getFactionAt(FactionsKt.getFLocation(location));
        return !faction.isWilderness() && (fPlayer.getFaction().equals(faction) || (fPlayer.hasFaction() &&
                faction.getRelationPerms().getPermForRelation(faction.getRelationTo(fPlayer.getFaction()), PlayerAction.BREAK_BLOCK)));
    }

}
