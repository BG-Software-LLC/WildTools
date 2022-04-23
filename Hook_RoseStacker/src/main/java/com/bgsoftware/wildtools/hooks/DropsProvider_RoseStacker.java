package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.api.hooks.DropsProvider;
import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.utils.ItemUtils;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class DropsProvider_RoseStacker implements DropsProvider {

    @Override
    public List<ItemStack> getBlockDrops(Player player, Block block) {
        if (!(block.getState() instanceof CreatureSpawner))
            return null;

        List<ItemStack> drops = new ArrayList<>();
        StackedSpawner stackedSpawner = RoseStackerAPI.getInstance().getStackedSpawner(block);

        if (stackedSpawner != null) {
            drops.add(ItemUtils.getSpawnerAsStackedItemStack(stackedSpawner.getSpawner().getSpawnedType(), stackedSpawner.getStackSize()));
            stackedSpawner.setStackSize(0);
            stackedSpawner.updateDisplay();
            RoseStackerAPI.getInstance().removeSpawnerStack(stackedSpawner);
        } else {
            drops.add(DropsProviders_Default.getSpawnerItem((CreatureSpawner) block.getState()));
        }

        return drops;
    }

    @Override
    public boolean isSpawnersOnly() {
        return true;
    }
}