package com.bgsoftware.wildtools.hooks;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.listeners.FactionsBlockListener;
import com.massivecraft.factions.zcore.fperms.PermissableAction;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class BlocksProvider_SaberFactions implements BlocksProvider {

    @Override
    public Plugin getPlugin() {
        return FactionsPlugin.getInstance();
    }

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        Faction faction = Board.getInstance().getFactionAt(new FLocation(block.getLocation()));
        if(onlyInClaim && faction.isWilderness()) return false;
        return FactionsBlockListener.playerCanBuildDestroyBlock(player, block.getLocation(), PermissableAction.DESTROY, true);
    }

    @Override
    public boolean canInteract(Player player, Block block, boolean onlyInClaim) {
        Faction faction = Board.getInstance().getFactionAt(new FLocation(block.getLocation()));
        if(onlyInClaim && faction.isWilderness()) return false;
        return FactionsBlockListener.playerCanBuildDestroyBlock(player, block.getLocation(), PermissableAction.CONTAINER, true);
    }

}
