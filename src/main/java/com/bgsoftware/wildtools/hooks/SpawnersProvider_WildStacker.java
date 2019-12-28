package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.inventory.ItemStack;

public final class SpawnersProvider_WildStacker implements SpawnersProvider {

    @Override
    public ItemStack getItem(CreatureSpawner creatureSpawner) {
        StackedSpawner stackedSpawner = WildStackerAPI.getStackedSpawner(creatureSpawner);
        try {
            return stackedSpawner.getDropItem();
        }catch(Throwable ex){
            ItemStack itemStack = SpawnersProvider_Default.getSpawnerItem(creatureSpawner);
            itemStack.setAmount(stackedSpawner.getStackAmount());
            return itemStack;
        }
    }

}
