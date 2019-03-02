package com.bgsoftware.wildtools.hooks;

import com.stefthedev.villages.Villages;
import com.stefthedev.villages.villages.Village;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class BlocksProvider_Villages implements BlocksProvider {

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        Village playerVillage = Villages.getInstance().getVillageManager().village(player);
        Village locationVillage = Villages.getInstance().getVillageManager().village(block.getChunk());
        if(onlyInClaim && playerVillage == null) return false;
        return playerVillage == null || playerVillage == locationVillage;
    }
}
