package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.api.hooks.DropsProvider;
import dev.warse.chunkhoppers.ChunkHoppers;
import dev.warse.chunkhoppers.utils.ChunkHopper;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class DropsProvider_ChunkHoppers implements DropsProvider {

    @Override
    public List<ItemStack> getBlockDrops(Player player, Block block) {
        List<ItemStack> drops = new ArrayList<>();

        for (ChunkHopper chunkHopper : ChunkHoppers.getInstance().getHoppers()) {
            if (chunkHopper.getLoc().equals(block.getLocation())) {
                drops.add(chunkHopper.getItem());
                break;
            }
        }

        return drops;
    }

    @Override
    public boolean isSpawnersOnly() {
        return false;
    }

}
