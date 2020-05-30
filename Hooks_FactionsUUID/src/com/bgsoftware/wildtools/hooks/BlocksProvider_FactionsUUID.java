package com.bgsoftware.wildtools.hooks;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.P;
import com.massivecraft.factions.SavageFactions;
import com.massivecraft.factions.listeners.FactionsBlockListener;
import com.massivecraft.factions.perms.PermissibleAction;
import com.massivecraft.factions.zcore.fperms.PermissableAction;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public final class BlocksProvider_FactionsUUID implements BlocksProvider {

    private static Method playerCanBuildDestroyBlockMethod;

    static {
        try{
            //noinspection JavaReflectionMemberAccess
            playerCanBuildDestroyBlockMethod = FactionsBlockListener.class.getMethod("playerCanBuildDestroyBlock",
                    Player.class, Location.class, PermissibleAction.class, boolean.class);
        }catch(Throwable ignored){}
    }

    @Override
    public Plugin getPlugin() {
        try {
            return FactionsPlugin.getInstance();
        }catch(Throwable ex){
            try {
                return SavageFactions.plugin;
            }catch(Throwable ex2){
                // Some forks have the P class as an instance of the plugin
                return P.p;
            }
        }
    }

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        Faction faction = Board.getInstance().getFactionAt(new FLocation(block.getLocation()));
        if(onlyInClaim && faction.isWilderness()) return false;
        return playerCanBuildDestroyBlockMethod(player, block, PermissableAction.DESTROY);
    }

    @Override
    public boolean canInteract(Player player, Block block, boolean onlyInClaim) {
        Faction faction = Board.getInstance().getFactionAt(new FLocation(block.getLocation()));
        if(onlyInClaim && faction.isWilderness()) return false;
        return playerCanBuildDestroyBlockMethod(player, block, PermissableAction.CONTAINER);
    }

    private boolean playerCanBuildDestroyBlockMethod(Player player, Block block, PermissableAction action){
        try{
            return (boolean) playerCanBuildDestroyBlockMethod.invoke(player, block.getLocation(), action, true);
        }catch(Throwable ex){
            return FactionsBlockListener.playerCanBuildDestroyBlock(player, block.getLocation(), action.getName(), true);
        }
    }

}
