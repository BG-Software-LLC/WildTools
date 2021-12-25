package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildchests.api.WildChestsAPI;
import com.bgsoftware.wildchests.api.objects.chests.Chest;
import com.bgsoftware.wildchests.api.objects.chests.StorageChest;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.hooks.SellInfo;
import com.bgsoftware.wildtools.api.hooks.SoldItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public final class ContainerProvider_WildChests implements ExtendedContainerProvider {

    private final WildToolsPlugin plugin;

    public ContainerProvider_WildChests(WildToolsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isContainer(BlockState blockState) {
        return WildChestsAPI.getChest(blockState.getLocation()) != null;
    }

    @Override
    public SellInfo sellContainer(BlockState blockState, Inventory inventory, Player player) {
        Chest chest = WildChestsAPI.getChest(blockState.getLocation());

        Map<Integer, SoldItem> toSell = new HashMap<>();
        double totalEarnings = 0;

        if (chest instanceof StorageChest) {
            ItemStack itemStack = ((StorageChest) chest).getItemStack();
            BigInteger amount = ((StorageChest) chest).getAmount();
            int slots = amount.divide(BigInteger.valueOf(Integer.MAX_VALUE)).intValue();

            for (int i = 0; i < slots; i++) {
                itemStack.setAmount(Integer.MAX_VALUE);
                if (plugin.getProviders().canSellItem(player, itemStack)) {
                    SoldItem soldItem = new SoldItem(itemStack.clone(), plugin.getProviders().getPrice(player, itemStack));
                    toSell.put(0, soldItem);
                    totalEarnings += soldItem.getPrice();
                }
            }

            itemStack.setAmount(amount.remainder(BigInteger.valueOf(Integer.MAX_VALUE)).intValue());
            if (plugin.getProviders().canSellItem(player, itemStack)) {
                SoldItem soldItem = new SoldItem(itemStack.clone(), plugin.getProviders().getPrice(player, itemStack));
                toSell.put(0, soldItem);
                totalEarnings += soldItem.getPrice();
            }

            return new SellInfo(toSell, totalEarnings);
        } else {
            Inventory[] pages = chest.getPages();

            for (int i = 0; i < pages.length; i++) {
                inventory = pages[i];
                for (int slot = 0; slot < inventory.getSize(); slot++) {
                    ItemStack itemStack = inventory.getItem(slot);
                    if (itemStack != null && plugin.getProviders().canSellItem(player, itemStack)) {
                        SoldItem soldItem = new SoldItem(itemStack.clone(), plugin.getProviders().getPrice(player, itemStack));
                        toSell.put(i * 54 + slot, soldItem);
                        totalEarnings += soldItem.getPrice();
                    }
                }
            }

            return new SellInfo(toSell, totalEarnings);
        }
    }

    @Override
    public void removeContainer(BlockState blockState, Inventory inventory, SellInfo sellInfo) {
        Chest chest = WildChestsAPI.getChest(blockState.getLocation());

        if (chest instanceof StorageChest) {
            if (sellInfo.getSoldItems().containsKey(0))
                ((StorageChest) chest).setAmount(BigInteger.ZERO);
        } else {
            Inventory[] pages = chest.getPages();
            for (int i = 0; i < pages.length; i++) {
                inventory = pages[i];
                for (int slot = 0; slot < inventory.getSize(); slot++) {
                    if (sellInfo.getSoldItems().containsKey(i * 54 + slot))
                        inventory.setItem(slot, new ItemStack(Material.AIR));
                }
            }
        }
    }

    @Override
    public List<Inventory> getAllInventories(BlockState blockState, Inventory chestInventory) {
        List<Inventory> inventories = new ArrayList<>();

        Chest chest = WildChestsAPI.getChest(blockState.getLocation());
        if (chest instanceof StorageChest) {
            Inventory inventory = Bukkit.createInventory(null, 9 * 3);
            inventories.add(inventory);
        } else {
            inventories.addAll(Arrays.asList(chest.getPages()));
        }

        return inventories;
    }

    @Override
    public void addItems(BlockState blockState, Inventory chestInventory, List<ItemStack> itemStackList) {
        Chest chest = WildChestsAPI.getChest(blockState.getLocation());
        chest.addItems(itemStackList.toArray(new ItemStack[0]));
    }

}
