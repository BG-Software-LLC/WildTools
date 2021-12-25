package com.bgsoftware.wildtools.hooks;

import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public final class StackedItemProvider_Default implements StackedItemProvider {

    @Override
    public ItemStack getItemStack(Item item) {
        return item.getItemStack();
    }

    @Override
    public void setItemStack(Item item, ItemStack itemStack) {
        item.setItemStack(itemStack);
    }

}
