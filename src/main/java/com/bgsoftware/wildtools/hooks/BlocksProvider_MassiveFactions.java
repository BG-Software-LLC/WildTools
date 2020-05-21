package com.bgsoftware.wildtools.hooks;

import com.massivecraft.factions.Factions;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPerm;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public final class BlocksProvider_MassiveFactions implements BlocksProvider {

    private static Method isUsingAdminModeMethod;

    static {
        try{
            //noinspection JavaReflectionMemberAccess
            isUsingAdminModeMethod = MPlayer.class.getMethod("isUsingAdminMode");
        }catch(Throwable ignored){}
    }

    @Override
    public Plugin getPlugin() {
        return Factions.get();
    }

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        MPlayer mPlayer = MPlayer.get(player);
        boolean overriding = false;

        try {
            overriding = mPlayer.isOverriding();
        } catch (Throwable ex) {
            try {
                overriding = (boolean) isUsingAdminModeMethod.invoke(mPlayer);
            } catch (Exception ignored) { }
        }

        Faction faction = BoardColl.get().getFactionAt(PS.valueOf(block.getLocation()));

        if(onlyInClaim && faction.getId().equals(Factions.ID_NONE)) return false;

        return faction.getId().equals(Factions.ID_NONE) || overriding || (mPlayer.hasFaction() && (mPlayer.getFaction().equals(faction) ||
                faction.getPermitted(MPerm.getPermAccess()).contains(faction.getRelationWish(mPlayer.getFaction()))));
    }

    @Override
    public boolean canInteract(Player player, Block block, boolean onlyInClaim) {
        MPlayer mPlayer = MPlayer.get(player);
        boolean overriding = false;

        try {
            overriding = mPlayer.isOverriding();
        } catch (Throwable ex) {
            try {
                overriding = (boolean) isUsingAdminModeMethod.invoke(mPlayer);
            } catch (Exception ignored) { }
        }

        Faction faction = BoardColl.get().getFactionAt(PS.valueOf(block.getLocation()));

        if(onlyInClaim && faction.getId().equals(Factions.ID_NONE)) return false;


        return faction.getId().equals(Factions.ID_NONE) || overriding || (mPlayer.hasFaction() && (mPlayer.getFaction().equals(faction) ||
                faction.getPermitted(MPerm.getPermContainer()).contains(faction.getRelationWish(mPlayer.getFaction()))));
    }
}
