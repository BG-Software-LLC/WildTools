package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.WildToolsPlugin;
import dev.warse.chunkhoppers.ChunkHoppers;
import dev.warse.chunkhoppers.utils.ChunkHopper;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class DropsProvider_ChunkHoppers implements DropsProvider {

    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    @Override
    public List<ItemStack> getBlockDrops(Block block) {
        List<ItemStack> drops = new ArrayList<>();

        if(!ChunkHoppers.getInstance().getChunks().contains(block.getChunk()))
            return drops;

        for (ChunkHopper chunkHopper : ChunkHoppers.getInstance().getHoppers()) {
            if (chunkHopper.getLoc().equals(block.getLocation())) {
                drops.add(chunkHopper.getItem());

                for(Entity entity : plugin.getNMSAdapter().getNearbyEntities(chunkHopper.getLoc().clone().add(0.5, -0.7, 0.5), 0.1)){
                    if (entity instanceof ArmorStand) {
                        entity.remove();
                        break;
                    }
                }

                ChunkHoppers.getInstance().getHoppers().remove(chunkHopper);
                ChunkHoppers.getInstance().removeChunk(block.getChunk());

                break;
            }
        }

        return drops;
    }
}
