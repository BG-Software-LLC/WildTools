package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildstacker.api.WildStackerAPI;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class StackedItemProvider_WildStacker implements StackedItemProvider {

    @Override
    public ItemStack getItemStack(Item item) {
        return WildStackerAPI.getStackedItem(item).getItemStack();
    }

    @Override
    public void setItemStack(Item item, ItemStack itemStack) {
        WildStackerAPI.getStackedItem(item).setStackAmount(itemStack.getAmount(), true);
    }

    @Override
    public void dropItem(Location location, ItemStack itemStack, int count) {
        WildStackerAPI.getWildStacker().getSystemManager().spawnItemWithAmount(location, itemStack, count);
    }

}
