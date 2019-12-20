package com.bgsoftware.wildtools.objects.tools;

import com.bgsoftware.wildtools.hooks.WildChestsHook;
import com.bgsoftware.wildtools.Locale;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.api.objects.tools.SortTool;

import com.bgsoftware.wildtools.utils.Executor;
import com.bgsoftware.wildtools.utils.items.ToolTaskManager;
import org.bukkit.Material;
import org.bukkit.block.Chest;
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
import java.util.UUID;
import java.util.stream.Collectors;

public final class WSortTool extends WTool implements SortTool {

    public WSortTool(Material type, String name){
        super(type, name, ToolMode.SORT);
    }

    @Override
    public boolean onBlockInteract(PlayerInteractEvent e) {
        if(!plugin.getProviders().canInteract(e.getPlayer(), e.getClickedBlock(), this))
            return false;

        if(e.getClickedBlock().getType() != Material.CHEST && e.getClickedBlock().getType() != Material.TRAPPED_CHEST){
            Locale.INVALID_CONTAINER_SORT_WAND.send(e.getPlayer());
            return false;
        }

        UUID taskId = ToolTaskManager.generateTaskId(e.getItem(), e.getPlayer().getInventory());

        Chest chest = (Chest) e.getClickedBlock().getState();
        Inventory chestInventory = ((InventoryHolder) e.getClickedBlock().getState()).getInventory();

        Executor.async(() -> {
            synchronized (getToolMutex(e.getClickedBlock())) {
                List<Inventory> inventories = WildChestsHook.getAllInventories(chest, chestInventory);
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

                WildChestsHook.addItems(chest.getLocation(), chestInventory, convert(inventoryItems));

                for (Inventory inventory : inventories) {
                    if (!Arrays.equals(originContents.get(inventory), inventory.getContents())) {
                        reduceDurablility(e.getPlayer(), taskId);
                        ToolTaskManager.removeTask(taskId);
                        Locale.SORTED_CHEST.send(e.getPlayer());
                        return;
                    }
                }

                ToolTaskManager.removeTask(taskId);
                Locale.NO_SORT_ITEMS.send(e.getPlayer());
            }
        });

        return true;
    }

    private List<ItemStack> convert(List<InventoryItem> original){
        return original.stream().map(InventoryItem::getItemStack).collect(Collectors.toList());
    }

    private static class InventoryItem implements Comparable<InventoryItem>{

        public ItemStack itemStack;

        InventoryItem(ItemStack itemStack){
            this.itemStack = itemStack;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        @Override
        public int compareTo(InventoryItem o) {
            //Comparing itemstack types
            if(itemStack.getType().ordinal() > o.itemStack.getType().ordinal())
                return 1;
            else if(itemStack.getType().ordinal() < o.itemStack.getType().ordinal())
                return -1;

            //Comparing durabilities
            return Integer.compare(itemStack.getDurability(), o.itemStack.getDurability());
        }
    }

}
