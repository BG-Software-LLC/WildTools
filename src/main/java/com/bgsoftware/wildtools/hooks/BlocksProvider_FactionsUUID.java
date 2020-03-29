package com.bgsoftware.wildtools.hooks;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.Relation;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public final class BlocksProvider_FactionsUUID implements BlocksProvider {

    private static Method getRelationWithMethod;
    private static Object allyRelationEnum;

    static {
        try{
            getRelationWithMethod = Faction.class.getDeclaredMethod("getRelationWish", Faction.class);
            Class<?> relationClass = Class.forName("com.massivecraft.factions.perms.Relation");
            for(Object en : relationClass.getEnumConstants()){
                if("ALLY".equals(en + ""))
                    allyRelationEnum = en;
            }
        }catch(Throwable ignored){}
    }

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        Faction faction = Board.getInstance().getFactionAt(new FLocation(block.getLocation()));
        if(onlyInClaim && faction.isWilderness()) return false;
        return faction.isWilderness() || fPlayer.isAdminBypassing() || (fPlayer.hasFaction() && (fPlayer.getFaction().equals(faction) ||
                hasRelation(faction, fPlayer)));
    }

    private boolean hasRelation(Faction faction, FPlayer fPlayer){
        try {
            return getRelationWithMethod.invoke(faction, fPlayer.getFaction()) == allyRelationEnum;
        }catch(Throwable ex){
            return faction.getRelationWish(fPlayer.getFaction()) == Relation.ALLY;
        }
    }

}
