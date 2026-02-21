package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.api.hooks.DropsProvider;
import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.event.SpawnerUnstackEvent;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import dev.rosewood.rosestacker.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class DropsProvider_RoseStacker implements DropsProvider {

    @Override
    public List<ItemStack> getBlockDrops(Player player, Block block) {
        if (!(block.getState() instanceof CreatureSpawner))
            return null;

        ItemStack dropItem;
        StackedSpawner stackedSpawner = RoseStackerAPI.getInstance().getStackedSpawner(block);

        if (stackedSpawner != null) {
            int stackSize = stackedSpawner.getStackSize();
            SpawnerUnstackEvent spawnerUnstackEvent = new SpawnerUnstackEvent(player, stackedSpawner, stackSize);
            Bukkit.getPluginManager().callEvent(spawnerUnstackEvent);
            int decreaseCount = spawnerUnstackEvent.getDecreaseAmount();

            dropItem = ItemUtils.getSpawnerAsStackedItemStack(stackedSpawner.getSpawner().getSpawnedType(), decreaseCount);
            stackedSpawner.increaseStackSize(-decreaseCount);

            if (stackedSpawner.getStackSize() <= 0) {
                RoseStackerAPI.getInstance().removeSpawnerStack(stackedSpawner);
            }
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