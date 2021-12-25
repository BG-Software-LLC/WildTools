package com.bgsoftware.wildtools.objects.tools;

import com.bgsoftware.wildtools.Locale;
import com.bgsoftware.wildtools.api.events.SortWandUseEvent;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.api.objects.tools.SortTool;
import com.bgsoftware.wildtools.utils.BukkitUtils;
import com.bgsoftware.wildtools.utils.Executor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class WSortTool extends WTool implements SortTool {

    public WSortTool(Material type, String name) {
        super(type, name, ToolMode.SORT);
    }

    @Override
    public boolean onBlockInteract(PlayerInteractEvent e) {
        if (!BukkitUtils.canInteractBlock(e.getPlayer(), e.getClickedBlock(), e.getItem()))
            return false;

        if (e.getClickedBlock().getType() != Material.CHEST && e.getClickedBlock().getType() != Material.TRAPPED_CHEST) {
            Locale.INVALID_CONTAINER_SORT_WAND.send(e.getPlayer());
            return false;
        }

        BlockState blockState = e.getClickedBlock().getState();
        Inventory chestInventory = ((InventoryHolder) e.getClickedBlock().getState()).getInventory();
        List<Inventory> inventories = plugin.getProviders().getAllInventories(blockState, chestInventory);

        if (inventories.isEmpty()) {
            Locale.INVALID_CONTAINER_SORT_WAND.send(e.getPlayer());
            return false;
        }

        List<InventoryItem> inventoryItems = new ArrayList<>();
        Map<Inventory, ItemStack[]> originContents = new HashMap<>();

        for (Inventory inventory : inventories) {
            originContents.put(inventory, inventory.getContents());
            Arrays.stream(inventory.getContents())
                    .filter(Objects::nonNull)
                    .forEach(itemStack -> inventoryItems.add(new InventoryItem(itemStack)));
            inventory.clear();
        }

        Collections.sort(inventoryItems);

        List<ItemStack> affectedItems = convert(inventoryItems);

        Executor.sync(() -> {
            SortWandUseEvent sortWandUseEvent = new SortWandUseEvent(e.getPlayer(), this,
                    affectedItems.stream().map(ItemStack::clone).collect(Collectors.toList()));
            Bukkit.getPluginManager().callEvent(sortWandUseEvent);
        });

        plugin.getProviders().addItems(blockState, chestInventory, affectedItems);

        for (Inventory inventory : inventories) {
            if (!Arrays.equals(originContents.get(inventory), inventory.getContents())) {
                reduceDurablility(e.getPlayer(), 1, e.getItem());
                Locale.SORTED_CHEST.send(e.getPlayer());
                return true;
            }
        }

        Locale.NO_SORT_ITEMS.send(e.getPlayer());

        return true;
    }

    private List<ItemStack> convert(List<InventoryItem> original) {
        return original.stream().map(InventoryItem::getItemStack).collect(Collectors.toList());
    }

    private static class InventoryItem implements Comparable<InventoryItem> {

        public ItemStack itemStack;

        InventoryItem(ItemStack itemStack) {
            this.itemStack = itemStack;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        @Override
        public int compareTo(InventoryItem o) {
            //Comparing itemstack types
            if (itemStack.getType().ordinal() > o.itemStack.getType().ordinal())
                return 1;
            else if (itemStack.getType().ordinal() < o.itemStack.getType().ordinal())
                return -1;

            //Comparing durabilities
            return Integer.compare(itemStack.getDurability(), o.itemStack.getDurability());
        }
    }

}
