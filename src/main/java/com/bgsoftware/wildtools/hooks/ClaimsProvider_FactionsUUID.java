package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.api.hooks.ClaimsProvider;
import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.listeners.FactionsBlockListener;
import com.massivecraft.factions.perms.PermissibleAction;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public final class ClaimsProvider_FactionsUUID implements ClaimsProvider {

    private static Method playerCanBuildDestroyBlockMethod;

    static {
        try{
            //noinspection JavaReflectionMemberAccess
            playerCanBuildDestroyBlockMethod = FactionsBlockListener.class.getMethod("playerCanBuildDestroyBlock",
                    Player.class, Location.class, PermissibleAction.class, boolean.class);
        }catch(Throwable ignored){}
    }

    @Override
    public boolean isPlayerClaim(Player player, Location location) {
        Faction faction = Board.getInstance().getFactionAt(new FLocation(location));
        return !faction.isWilderness() && playerCanBuildDestroyBlockMethod(player, location, "DESTROY");
    }

    private boolean playerCanBuildDestroyBlockMethod(Player player, Location location, String action){
        try{
            return (boolean) playerCanBuildDestroyBlockMethod.invoke(null, player, location, PermissibleAction.valueOf(action), true);
        }catch(Throwable ex){
            return FactionsBlockListener.playerCanBuildDestroyBlock(player, location, action, true);
        }
    }

}
