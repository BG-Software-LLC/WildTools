package com.bgsoftware.wildtools.hooks;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class BlocksProvider_MassiveFactions implements BlocksProvider {

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        MPlayer mPlayer = MPlayer.get(player);
        boolean overriding = false;

        try {
            overriding = mPlayer.isOverriding();
        } catch (Throwable ex) {
            try {
                overriding = (boolean) mPlayer.getClass().getMethod("isUsingAdminMode").invoke(mPlayer);
            } catch (Exception ignored) { }
        }

        Faction faction = BoardColl.get().getFactionAt(PS.valueOf(block.getLocation()));

        if(onlyInClaim && faction == null) return false;

        return faction == null || overriding || (mPlayer.hasFaction() && (mPlayer.getFaction().equals(faction) ||
                faction.getRelationWish(mPlayer.getFaction()) == Rel.ALLY));
    }
}
