package com.bgsoftware.wildtools.hooks;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.listeners.FactionsBlockListener;
import com.massivecraft.factions.zcore.fperms.PermissableAction;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public final class BlocksProvider_SaberFactions implements BlocksProvider {

    private static Method playerCanBuildDestroyBlockMethod;

    static {
        try{
            //noinspection JavaReflectionMemberAccess
            playerCanBuildDestroyBlockMethod = FactionsBlockListener.class.getMethod("playerCanBuildDestroyBlock",
                    Player.class, Location.class, PermissableAction.class, boolean.class);
        }catch(Throwable ignored){}
    }

    @Override
    public Plugin getPlugin() {
        return FactionsPlugin.getInstance();
    }

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        Faction faction = Board.getInstance().getFactionAt(new FLocation(block.getLocation()));
        if(onlyInClaim && faction.isWilderness()) return false;
        return playerCanBuildDestroyBlockMethod(player, block, "DESTROY");
    }

    @Override
    public boolean canInteract(Player player, Block block, boolean onlyInClaim) {
        Faction faction = Board.getInstance().getFactionAt(new FLocation(block.getLocation()));
        if(onlyInClaim && faction.isWilderness()) return false;
        return playerCanBuildDestroyBlockMethod(player, block, "CONTAINER");
    }

    private boolean playerCanBuildDestroyBlockMethod(Player player, Block block, String action){
        try{
            return (boolean) playerCanBuildDestroyBlockMethod.invoke(null, player, block.getLocation(), PermissableAction.valueOf(action), true);
        }catch(Throwable ex){
            return FactionsBlockListener.playerCanBuildDestroyBlock(player, block.getLocation(), action, true);
        }
    }

}
