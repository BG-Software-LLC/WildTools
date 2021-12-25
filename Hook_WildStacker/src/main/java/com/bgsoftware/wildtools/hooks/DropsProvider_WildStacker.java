package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import com.bgsoftware.wildtools.api.hooks.DropsProvider;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class DropsProvider_WildStacker implements DropsProvider {

    @Override
    public List<ItemStack> getBlockDrops(Player player, Block block) {
        List<ItemStack> drops = new ArrayList<>();

        if(!(block.getState() instanceof CreatureSpawner))
            return drops;

        StackedSpawner stackedSpawner = WildStackerAPI.getStackedSpawner((CreatureSpawner) block.getState());

        int spawnerStackAmount = stackedSpawner.getStackAmount();

        try {
            drops.add(stackedSpawner.getDropItem());
        }catch(Throwable ex){
            ItemStack itemStack = DropsProviders_Default.getSpawnerItem((CreatureSpawner) block.getState());
            itemStack.setAmount(spawnerStackAmount);
            drops.add(itemStack);
        }

        // We want to unstack the spawner when getting drops from stacked spawners.
        stackedSpawner.runUnstack(spawnerStackAmount);

        return drops;
    }

    @Override
    public boolean isSpawnersOnly() {
        return true;
    }
}
