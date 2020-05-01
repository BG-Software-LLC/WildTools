package com.bgsoftware.wildtools.hooks;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.perms.Permissible;
import com.massivecraft.factions.perms.PermissibleAction;
import com.massivecraft.factions.zcore.fperms.Access;
import com.massivecraft.factions.zcore.fperms.PermissableAction;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.Map;

public final class BlocksProvider_FactionsUUID implements BlocksProvider {

    private static Method getRelationWithMethod;

    static {
        try{
            getRelationWithMethod = Faction.class.getDeclaredMethod("getRelationWish", Faction.class);
        }catch(Throwable ignored){}
    }

    @Override
    public Plugin getPlugin() {
        return FactionsPlugin.getInstance();
    }

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        Faction faction = Board.getInstance().getFactionAt(new FLocation(block.getLocation()));
        if(onlyInClaim && faction.isWilderness()) return false;
        return faction.isWilderness() || fPlayer.isAdminBypassing() || (fPlayer.hasFaction() && (fPlayer.getFaction().equals(faction) ||
                hasRelation(faction, fPlayer, "DESTROY")));
    }

    @Override
    public boolean canInteract(Player player, Block block, boolean onlyInClaim) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        Faction faction = Board.getInstance().getFactionAt(new FLocation(block.getLocation()));
        if(onlyInClaim && faction.isWilderness()) return false;
        return faction.isWilderness() || fPlayer.isAdminBypassing() || (fPlayer.hasFaction() && (fPlayer.getFaction().equals(faction) ||
                hasRelation(faction, fPlayer, "CONTAINER")));
    }

    @SuppressWarnings("all")
    private boolean hasRelation(Faction faction, FPlayer fPlayer, String permission){
        try {
            Permissible permissible = (Permissible) getRelationWithMethod.invoke(faction, fPlayer.getFaction());
            return (Boolean) ((Map) faction.getPermissions().get(permissible)).get(PermissibleAction.valueOf(permission));
        }catch(Throwable ex){
            return faction.getPermissions().get(faction.getRelationWish(fPlayer.getFaction()))
                    .get(PermissableAction.valueOf(permission)) == Access.ALLOW;
        }
    }

}
