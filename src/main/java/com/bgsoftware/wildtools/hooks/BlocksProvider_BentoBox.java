package com.bgsoftware.wildtools.hooks;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;

public final class BlocksProvider_BentoBox implements BlocksProvider {

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        Optional<Island> island = BentoBox.getInstance().getIslands().getIslandAt(block.getLocation());
        if(onlyInClaim && (!island.isPresent() || !island.get().onIsland(block.getLocation()))) return false;
        return !island.isPresent() || island.get().getMembers().containsKey(player.getUniqueId());
    }
}
