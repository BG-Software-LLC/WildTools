package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildtools.api.hooks.DropsProvider;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class DropsProvider_WildStacker implements DropsProvider {

    @Override
    public List<ItemStack> getBlockDrops(Player player, Block block) {
        if (!(block.getState() instanceof CreatureSpawner))
            return null;

        StackedSpawner stackedSpawner = WildStackerAPI.getStackedSpawner((CreatureSpawner) block.getState());

        int spawnerStackAmount = stackedSpawner.getStackAmount();

        ItemStack dropItem;
        try {
            dropItem = stackedSpawner.getDropItem();
        } catch (Throwable ex) {
            dropItem = DropsProviders_Default.getSpawnerItem((CreatureSpawner) block.getState());
            dropItem.setAmount(spawnerStackAmount);
        }

        // We want to unstack the spawner when getting drops from stacked spawners.
        stackedSpawner.runUnstack(spawnerStackAmount);

        return Collections.singletonList(dropItem);
    }

    @Override
    public boolean isSpawnersOnly() {
        return true;
    }
}
