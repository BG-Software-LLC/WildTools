package com.bgsoftware.wildtools.hooks;

import net.prosavage.core.FPlayer;
import net.prosavage.core.Faction;
import net.prosavage.manager.GridManager;
import net.prosavage.manager.PlayerManager;
import net.prosavage.persist.data.FactionsKt;
import net.prosavage.util.PlayerAction;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class BlocksProvider_FactionsX implements BlocksProvider {

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        FPlayer fPlayer = PlayerManager.INSTANCE.getFPlayer(player);
        boolean overriding = fPlayer.getInBypass();
        Faction faction = GridManager.INSTANCE.getFactionAt(FactionsKt.getFLocation(block.getLocation()));
        if(onlyInClaim && faction.isWilderness()) return false;
        return faction.isWilderness() || overriding || (fPlayer.hasFaction() && (fPlayer.getFaction().equals(faction) ||
                faction.getRelationPerms().getPermForRelation(faction.getRelationTo(fPlayer.getFaction()), PlayerAction.BREAK_BLOCK)));
    }
}
