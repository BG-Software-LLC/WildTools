package com.bgsoftware.wildtools.utils.items;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.google.common.collect.Maps;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Map;
import java.util.UUID;

public final class ToolTaskManager {

    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();
    private static final Map<UUID, Object> runningTasks = Maps.newConcurrentMap();

    public static UUID generateTaskId(ItemStack itemStack, Player player){
        UUID generatedTaskId;

        //Generate ids until not found.
        //noinspection StatementWithEmptyBody
        while(runningTasks.containsKey((generatedTaskId = UUID.randomUUID())));

        runningTasks.put(generatedTaskId, player);

        int heldItem = ItemUtils.getItemSlot(player.getInventory(), itemStack);
        player.getInventory().setItem(heldItem, plugin.getNMSAdapter().addTask(itemStack, generatedTaskId));

        return generatedTaskId;
    }

    public static ItemStack getItemFromTask(UUID taskId){
        Object itemHolder = runningTasks.get(taskId);
        ItemStack itemStack;

        if(itemHolder instanceof Player) {
            Player player = (Player) itemHolder;

            //Checking for the cursor.
            if (isSameTask((itemStack = player.getItemOnCursor()), taskId))
                return itemStack;

            PlayerInventory playerInventory = player.getInventory();

            //Checking for the held slot.
            if(isSameTask((itemStack = playerInventory.getItem(playerInventory.getHeldItemSlot())), taskId))
                return itemStack;

            //Checking off hand slot
            if(isSameTask((itemStack = playerInventory.getItem(40)), taskId))
                return itemStack;

            for(int i = 0; i < playerInventory.getSize(); i++){
                if(isSameTask((itemStack = playerInventory.getItem(i)), taskId))
                    return itemStack;
            }
        }

        else if(itemHolder instanceof Item){
            Item item = (Item) itemHolder;
            if(isSameTask((itemStack = item.getItemStack()), taskId))
                return itemStack;
        }

        throw new IllegalArgumentException("The item wasn't found in the inventory.");
    }

    public static void setItemOfTask(UUID taskId, ItemStack newItem){
        Object itemHolder = runningTasks.get(taskId);

        newItem = plugin.getNMSAdapter().removeTask(newItem, taskId);

        if(itemHolder instanceof Player) {
            Player player = (Player) itemHolder;

            //Checking for the cursor.
            if (isSameTask(player.getItemOnCursor(), taskId)) {
                player.setItemOnCursor(newItem);
                return;
            }

            PlayerInventory playerInventory = player.getInventory();

            //Checking for the held slot.
            if (isSameTask(playerInventory.getItem(playerInventory.getHeldItemSlot()), taskId)) {
                playerInventory.setItem(playerInventory.getHeldItemSlot(), newItem);
                return;
            }

            //Checking off hand slot
            if (isSameTask(playerInventory.getItem(40), taskId)) {
                playerInventory.setItem(40, newItem);
                return;
            }

            for (int i = 0; i < playerInventory.getSize(); i++) {
                if (isSameTask(playerInventory.getItem(i), taskId)) {
                    playerInventory.setItem(i, newItem);
                    return;
                }
            }
        }

        else if(itemHolder instanceof Item){
            Item item = (Item) itemHolder;
            if(isSameTask(item.getItemStack(), taskId))
                item.setItemStack(newItem);
        }

    }

    public static void removeTask(UUID taskId){
        runningTasks.remove(taskId);
    }

    public static void handleDropItem(UUID taskId, Item item){
        runningTasks.put(taskId, item);
    }

    public static void handlePickupItem(UUID taskId, Player player){
        runningTasks.put(taskId, player);
    }

    private static boolean isSameTask(ItemStack itemStack, UUID taskId){
        return itemStack != null && plugin.getNMSAdapter().getTasks(itemStack).contains(taskId);
    }

}
