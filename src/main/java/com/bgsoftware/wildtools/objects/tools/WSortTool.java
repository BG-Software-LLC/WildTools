package com.bgsoftware.wildtools.objects.tools;

import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.bgsoftware.wildtools.Locale;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.api.objects.tools.SortTool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

        List<InventoryItem> inventoryItems = new ArrayList<>();

        new Thread(() -> {
            Inventory inventory = ((InventoryHolder) e.getClickedBlock().getState()).getInventory();

            ItemStack[] originContents = inventory.getContents();

            Arrays.stream(inventory.getContents())
                    .filter(Objects::nonNull)
                    .forEach(itemStack -> inventoryItems.add(new InventoryItem(itemStack)));

            inventory.clear();

            Collections.sort(inventoryItems);

            inventoryItems.forEach(inventoryItem -> inventory.addItem(inventoryItem.itemStack));

            if(!Arrays.equals(originContents, inventory.getContents())) {
                reduceDurablility(e.getPlayer());
                Locale.SORTED_CHEST.send(e.getPlayer());
            }
            else{
                Locale.NO_SORT_ITEMS.send(e.getPlayer());
            }

        }).start();

        return true;
    }

    private class InventoryItem implements Comparable<InventoryItem>{

        public ItemStack itemStack;

        InventoryItem(ItemStack itemStack){
            this.itemStack = itemStack;
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
