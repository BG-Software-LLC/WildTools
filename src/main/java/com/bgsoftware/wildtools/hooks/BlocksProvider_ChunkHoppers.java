package com.bgsoftware.wildtools.hooks;

import dev.warse.chunkhoppers.ChunkHoppers;
import dev.warse.chunkhoppers.utils.ChunkHopper;
import dev.warse.chunkhoppers.utils.other.Member;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class BlocksProvider_ChunkHoppers implements BlocksProvider {

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        if(!ChunkHoppers.getInstance().getChunks().contains(block.getChunk()))
            return true;

        for (ChunkHopper chunkHopper : ChunkHoppers.getInstance().getHoppers()) {
            if (chunkHopper.getLoc().equals(block.getLocation())) {
                for (Member member : chunkHopper.getMembers()) {
                    if ((member.getUuid().equals(player.getUniqueId()) && member.canRemove()) || player.hasPermission("chunkhoppers.admin")) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

}
