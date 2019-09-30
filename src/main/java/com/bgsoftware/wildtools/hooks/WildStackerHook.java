package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildstacker.api.WildStackerAPI;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public final class WildStackerHook {

    public static ItemStack getItemStack(Item item){
        return WildStackerAPI.getStackedItem(item).getItemStack();
    }

}
