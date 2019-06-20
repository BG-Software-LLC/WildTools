package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildchests.api.WildChestsAPI;
import com.bgsoftware.wildtools.utils.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class WildChestsHook {

    public static List<Inventory> getAllInventories(Chest chest){
        List<Inventory> inventories = new ArrayList<>();
        inventories.add(chest.getInventory());

        if(Bukkit.getPluginManager().isPluginEnabled("WildChests")){
            com.bgsoftware.wildchests.api.objects.chests.Chest wildChest = WildChestsAPI.getChest(chest.getLocation());
            if(wildChest != null)
                inventories = Arrays.asList(wildChest.getPages());
        }

        return inventories;
    }

    public static void addItems(Chest chest, List<ItemStack> itemStackList){
        if(Bukkit.getPluginManager().isPluginEnabled("WildChests")){
            com.bgsoftware.wildchests.api.objects.chests.Chest wildChest = WildChestsAPI.getChest(chest.getLocation());
            if(wildChest != null){
                wildChest.addItems(itemStackList.toArray(new ItemStack[]{}));
                return;
            }
        }

        Inventory inventory = chest.getInventory();
        itemStackList.forEach(itemStack -> ItemUtil.addItem(itemStack, inventory, chest.getLocation()));
    }



}
