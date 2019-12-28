package com.bgsoftware.wildtools.hooks;

import org.bukkit.block.CreatureSpawner;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public final class SpawnersProvider_Default implements SpawnersProvider {

    private static final SpawnersProvider_Default instance;

    static {
        instance = new SpawnersProvider_Default();
    }

    @Override
    public ItemStack getItem(CreatureSpawner creatureSpawner) {
        ItemStack dropItem = new ItemStack(creatureSpawner.getType(), 1);
        BlockStateMeta blockStateMeta = (BlockStateMeta) dropItem.getItemMeta();
        blockStateMeta.setBlockState(creatureSpawner);
        dropItem.setItemMeta(blockStateMeta);
        return dropItem;
    }

    public static ItemStack getSpawnerItem(CreatureSpawner creatureSpawner){
        return instance.getItem(creatureSpawner);
    }

}
