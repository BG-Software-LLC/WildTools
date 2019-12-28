package com.bgsoftware.wildtools.hooks;

import org.bukkit.block.CreatureSpawner;
import org.bukkit.inventory.ItemStack;

public interface SpawnersProvider {

    ItemStack getItem(CreatureSpawner creatureSpawner);

}
