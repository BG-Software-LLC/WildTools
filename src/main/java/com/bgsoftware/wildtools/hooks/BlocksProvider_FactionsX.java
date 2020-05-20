package com.bgsoftware.wildtools.hooks;

import net.prosavage.factionsx.FactionsX;
import net.prosavage.factionsx.core.FPlayer;
import net.prosavage.factionsx.core.Faction;
import net.prosavage.factionsx.manager.GridManager;
import net.prosavage.factionsx.manager.PlayerManager;
import net.prosavage.factionsx.persist.data.FactionsKt;
import net.prosavage.factionsx.util.PlayerAction;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class BlocksProvider_FactionsX implements BlocksProvider {

    @Override
    public Plugin getPlugin() {
        return FactionsX.getInstance();
    }

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        FPlayer fPlayer = PlayerManager.INSTANCE.getFPlayer(player);
        boolean overriding = fPlayer.getInBypass();
        Faction faction = GridManager.INSTANCE.getFactionAt(FactionsKt.getFLocation(block.getLocation()));
        if(onlyInClaim && faction.isWilderness()) return false;
        return faction.isWilderness() || overriding || (fPlayer.hasFaction() && (fPlayer.getFaction().equals(faction) ||
                faction.getRelationPerms().getPermForRelation(faction.getRelationTo(fPlayer.getFaction()), PlayerAction.BREAK_BLOCK)));
    }

    @Override
    public boolean canInteract(Player player, Block block, boolean onlyInClaim) {
        FPlayer fPlayer = PlayerManager.INSTANCE.getFPlayer(player);
        boolean overriding = fPlayer.getInBypass();
        Faction faction = GridManager.INSTANCE.getFactionAt(FactionsKt.getFLocation(block.getLocation()));
        if(onlyInClaim && faction.isWilderness()) return false;
        return faction.isWilderness() || overriding || (fPlayer.hasFaction() && (fPlayer.getFaction().equals(faction) ||
                faction.getRelationPerms().getPermForRelation(faction.getRelationTo(fPlayer.getFaction()), PlayerAction.CHEST)));
    }
}
