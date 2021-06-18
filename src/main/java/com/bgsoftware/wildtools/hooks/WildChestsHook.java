package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildchests.api.WildChestsAPI;
import com.bgsoftware.wildchests.api.objects.chests.StorageChest;
import com.bgsoftware.wildtools.utils.items.ItemUtils;
import com.bgsoftware.wildtools.utils.items.ItemsDropper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class WildChestsHook {

    public static List<Inventory> getAllInventories(Chest chest, Inventory chestInventory){
        List<Inventory> inventories = new ArrayList<>();
        inventories.add(chestInventory);

        if(isWildChest(chest)){
            com.bgsoftware.wildchests.api.objects.chests.Chest wildChest = WildChestsAPI.getChest(chest.getLocation());
            inventories.clear();
            if(wildChest instanceof StorageChest){
                Inventory inventory = Bukkit.createInventory(null, 9*3);
                inventories.add(inventory);
            }
            else {
                inventories.addAll(Arrays.asList(wildChest.getPages()));
            }
        }

        return inventories;
    }

    public static boolean isWildChest(Chest chest){
        if(Bukkit.getPluginManager().isPluginEnabled("WildChests")) {
            com.bgsoftware.wildchests.api.objects.chests.Chest wildChest = WildChestsAPI.getChest(chest.getLocation());
            return wildChest != null;
        }
        return false;
    }

    public static void addItems(Location location, Inventory chestInventory, List<ItemStack> itemStackList){
        if(Bukkit.getPluginManager().isPluginEnabled("WildChests")){
            com.bgsoftware.wildchests.api.objects.chests.Chest wildChest = WildChestsAPI.getChest(location);
            if(wildChest != null){
                wildChest.addItems(itemStackList.toArray(new ItemStack[]{}));
                return;
            }
        }

        ItemsDropper itemsDropper = new ItemsDropper();
        itemStackList.forEach(itemStack -> ItemUtils.addItem(itemStack, chestInventory, location, itemsDropper));
        itemsDropper.dropItems();
    }



}
