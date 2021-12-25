package com.bgsoftware.wildtools.hooks;

import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public interface StackedItemProvider {

    ItemStack getItemStack(Item item);

    void setItemStack(Item item, ItemStack itemStack);

}
