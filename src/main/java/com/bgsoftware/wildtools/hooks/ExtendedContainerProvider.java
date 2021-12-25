package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.api.hooks.ContainerProvider;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface ExtendedContainerProvider extends ContainerProvider {

    List<Inventory> getAllInventories(BlockState blockState, Inventory chestInventory);

    void addItems(BlockState blockState, Inventory chestInventory, List<ItemStack> itemStackList);

}
