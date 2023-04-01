package com.bgsoftware.wildtools.hooks;

import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class StackedItemProvider_Default implements StackedItemProvider {

    @Override
    public ItemStack getItemStack(Item item) {
        return item.getItemStack();
    }

    @Override
    public void setItemStack(Item item, ItemStack itemStack) {
        item.setItemStack(itemStack);
    }

    @Override
    public void dropItem(Location location, ItemStack itemStack, int count) {
        ItemStack dropItem = itemStack.clone();

        if (count > itemStack.getMaxStackSize()) {
            int amountOfStacks = count / itemStack.getMaxStackSize();
            dropItem.setAmount(itemStack.getMaxStackSize());
            for (int i = 0; i < amountOfStacks; ++i) {
                location.getWorld().dropItemNaturally(location, dropItem);
            }
            count = count % itemStack.getMaxStackSize();
        }

        if (count > 0) {
            dropItem.setAmount(count);
            location.getWorld().dropItemNaturally(location, dropItem);
        }
    }

    @Override
    public boolean skipPickupItemEventCall() {
        return false;
    }

}
