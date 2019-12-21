package com.bgsoftware.wildtools.utils.inventory;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class InventoryUtils {

    public static int countItems(Inventory inventory, ItemStack itemStack){
        int amount = 0;

        if(itemStack == null)
            return amount;

        ItemStack[] content = inventory.getContents();

        for (ItemStack _itemStack : content) {
            if (itemStack.isSimilar(_itemStack))
                amount += _itemStack.getAmount();
        }

        return amount;
    }

}
