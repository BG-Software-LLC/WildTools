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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class DropsProvider_RoseStacker implements DropsProvider {

    @Override
    public List<ItemStack> getBlockDrops(Player player, Block block) {
        if (!(block.getState() instanceof CreatureSpawner))
            return null;

        ItemStack dropItem;
        StackedSpawner stackedSpawner = RoseStackerAPI.getInstance().getStackedSpawner(block);

        if (stackedSpawner != null) {
            dropItem = ItemUtils.getSpawnerAsStackedItemStack(stackedSpawner.getSpawner().getSpawnedType(), stackedSpawner.getStackSize());
            stackedSpawner.setStackSize(0);
            stackedSpawner.updateDisplay();
            RoseStackerAPI.getInstance().removeSpawnerStack(stackedSpawner);
        } else {
            dropItem = DropsProviders_Default.getSpawnerItem((CreatureSpawner) block.getState());
        }

        return Collections.singletonList(dropItem);
    }

    @Override
    public boolean isSpawnersOnly() {
        return true;
    }
}