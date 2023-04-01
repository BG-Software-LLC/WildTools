package com.bgsoftware.wildtools.hooks;

import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public interface StackedItemProvider {

    ItemStack getItemStack(Item item);

    void setItemStack(Item item, ItemStack itemStack);

    void dropItem(Location location, ItemStack itemStack, int count);

    default void dropItem(Location location, ItemStack itemStack) {
        dropItem(location, itemStack, itemStack.getAmount());
    }

    default boolean skipPickupItemEventCall() {
        return false;
    }

}
