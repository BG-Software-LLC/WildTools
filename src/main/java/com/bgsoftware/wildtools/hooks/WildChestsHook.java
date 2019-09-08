package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildchests.api.WildChestsAPI;
import com.bgsoftware.wildchests.api.objects.chests.StorageChest;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.utils.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class WildChestsHook {

    private static WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    public static double getChestPrice(Chest bukkitChest, Player player, Map<Integer, ItemStack> toSell){
        com.bgsoftware.wildchests.api.objects.chests.Chest chest = WildChestsAPI.getChest(bukkitChest.getLocation());

        double totalEarnings = 0;

        if(chest instanceof StorageChest){
            ItemStack itemStack = ((StorageChest) chest).getItemStack();
            BigInteger amount = ((StorageChest) chest).getExactAmount();
            int slots = amount.divide(BigInteger.valueOf(Integer.MAX_VALUE)).intValue();

            for(int i = 0; i < slots; i++){
                itemStack.setAmount(Integer.MAX_VALUE);
                if(plugin.getProviders().canSellItem(player, itemStack)) {
                    toSell.put(0, itemStack.clone());
                    totalEarnings += plugin.getProviders().getPrice(player, itemStack);
                }
            }

            itemStack.setAmount(amount.remainder(BigInteger.valueOf(Integer.MAX_VALUE)).intValue());
            if(plugin.getProviders().canSellItem(player, itemStack)) {
                toSell.put(0, itemStack.clone());
                totalEarnings += plugin.getProviders().getPrice(player, itemStack);
            }

            return totalEarnings;
        }

        else{
            Inventory[] pages = chest.getPages();
            for(int i = 0; i < pages.length; i++){
                Inventory inventory = pages[i];
                for (int slot = 0; slot < inventory.getSize(); slot++) {
                    ItemStack itemStack = inventory.getItem(slot);
                    if (itemStack != null && plugin.getProviders().canSellItem(player, itemStack)) {
                        toSell.put(i * 54 + slot, itemStack);
                        totalEarnings += plugin.getProviders().getPrice(player, itemStack);
                    }
                }
            }
            return totalEarnings;
        }
    }

    public static void removeItems(Chest bukkitChest, Map<Integer, ItemStack> toSell){
        com.bgsoftware.wildchests.api.objects.chests.Chest chest = WildChestsAPI.getChest(bukkitChest.getLocation());

        if(chest instanceof StorageChest){
            ((StorageChest) chest).setAmount(0);
        }

        else{
            Inventory[] pages = chest.getPages();
            for(int i = 0; i < pages.length; i++){
                Inventory inventory = pages[i];
                for (int slot = 0; slot < inventory.getSize(); slot++) {
                    if(toSell.containsKey(i * 54 + slot))
                        inventory.setItem(slot, new ItemStack(Material.AIR));
                }
            }
        }
    }

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

        itemStackList.forEach(itemStack -> ItemUtil.addItem(itemStack, chestInventory, location));
    }



}
