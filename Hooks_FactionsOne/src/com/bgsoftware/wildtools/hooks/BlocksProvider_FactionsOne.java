package com.bgsoftware.wildtools.hooks;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.P;
import com.massivecraft.factions.struct.FPerm;
import de.erethon.factionsone.FactionsOneAPI;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class BlocksProvider_FactionsOne implements BlocksProvider {

    @Override
    public Plugin getPlugin() {
        return P.p;
    }

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        FPlayer fPlayer = FactionsOneAPI.getFPlayer(player.getUniqueId());
        boolean overriding = fPlayer.hasAdminMode();
        Faction faction = null;

        try{
            //noinspection JavaReflectionMemberAccess
            faction = (Faction) Board.class.getMethod("getFactionAt", Location.class).invoke(null, block.getLocation());
        }catch(Throwable ignored){}

        if(onlyInClaim && faction == null) return false;

        return faction == null || overriding || (fPlayer.hasFaction() && (fPlayer.getFaction().equals(faction) ||
                faction.getPermittedRelations(FPerm.BUILD).contains(faction.getRelationWish(fPlayer.getFaction()))));
    }

    @Override
    public boolean canInteract(Player player, Block block, boolean onlyInClaim) {
        FPlayer fPlayer = FactionsOneAPI.getFPlayer(player.getUniqueId());
        boolean overriding = fPlayer.hasAdminMode();
        Faction faction = null;

        try{
            //noinspection JavaReflectionMemberAccess
            faction = (Faction) Board.class.getMethod("getFactionAt", Location.class).invoke(null, block.getLocation());
        }catch(Throwable ignored){}

        if(onlyInClaim && faction == null) return false;

        return faction == null || overriding || (fPlayer.hasFaction() && (fPlayer.getFaction().equals(faction) ||
                faction.getPermittedRelations(FPerm.CONTAINER).contains(faction.getRelationWish(fPlayer.getFaction()))));
    }
}
