package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.api.hooks.DropsProvider;
import com.vk2gpz.mergedspawner.api.MergedSpawnerAPI;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public final class DropsProvider_MergedSpawner implements DropsProvider {

    @Override
    public List<ItemStack> getBlockDrops(Player player, Block block) {
        BlockState blockState = block.getState();

        if (!(blockState instanceof CreatureSpawner))
            return null;

        int count = MergedSpawnerAPI.getInstance().getCountFor(block);
        return Collections.singletonList(MergedSpawnerAPI.getInstance().getSpawnerFor(((CreatureSpawner) blockState).getSpawnedType(), count));
    }

    @Override
    public boolean isSpawnersOnly() {
        return true;
    }
}
