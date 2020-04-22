package com.bgsoftware.wildtools.utils.inventory;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class InventoryUtils {

    public static int countItems(Inventory inventory, ItemStack itemStack){
        int amount = 0;

        if(itemStack == null)
            return amount;

        ItemStack[] contents = inventory.getContents();

        for(int i = 0; i < 36; i++){
            if (itemStack.isSimilar(contents[i]))
                amount += contents[i].getAmount();
        }

        if(contents.length == 41 && itemStack.isSimilar(contents[40]))
            amount += contents[40].getAmount();

        return amount;
    }

}
