package com.bgsoftware.wildtools.hooks;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class BlockActionProvider_Jobs implements BlockActionProvider {

    @Override
    public void onBlockBreak(Player player, Block block, ItemStack usedItem) {
        com.gamingmesh.jobs.container.JobsPlayer jobsPlayer = com.gamingmesh.jobs.Jobs.getPlayerManager().getJobsPlayer(player);
        if (jobsPlayer != null)
            com.gamingmesh.jobs.Jobs.action(jobsPlayer, new com.gamingmesh.jobs.actions.BlockActionInfo(block, com.gamingmesh.jobs.container.ActionType.BREAK), block);
    }

}
