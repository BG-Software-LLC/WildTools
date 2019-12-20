package com.bgsoftware.wildtools.utils.items;

import com.bgsoftware.wildtools.WildToolsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ToolTaskManager {

    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();
    private static final Set<UUID> runningTasks = new HashSet<>();

    public static UUID generateTaskId(ItemStack itemStack, PlayerInventory playerInventory){
        UUID generatedTaskId;

        //Generate ids until not found.
        //noinspection StatementWithEmptyBody
        while(runningTasks.contains((generatedTaskId = UUID.randomUUID())));

        runningTasks.add(generatedTaskId);

        int heldItem = ItemUtils.getItemSlot(playerInventory, itemStack);
        playerInventory.setItem(heldItem, plugin.getNMSAdapter().setTag(itemStack, "task-id", generatedTaskId.toString()));

        return generatedTaskId;
    }

    public static ItemStack getItemFromTask(PlayerInventory playerInventory, UUID taskId){
        ItemStack itemStack;

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

        throw new IllegalArgumentException("The item wasn't found in the inventory.");
    }

    public static void setItemOfTask(Player player, UUID taskId, ItemStack newItem){
        //Checking for the cursor.
        if(isSameTask(player.getItemOnCursor(), taskId)){
            player.setItemOnCursor(newItem);
            return;
        }

        PlayerInventory playerInventory = player.getInventory();

        //Checking for the held slot.
        if(isSameTask(playerInventory.getItem(playerInventory.getHeldItemSlot()), taskId)){
            playerInventory.setItem(playerInventory.getHeldItemSlot(), newItem);
            return;
        }

        //Checking off hand slot
        if(isSameTask(playerInventory.getItem(40), taskId)) {
            playerInventory.setItem(40, newItem);
            return;
        }

        for(int i = 0; i < playerInventory.getSize(); i++){
            if(isSameTask(playerInventory.getItem(i), taskId)) {
                playerInventory.setItem(i, newItem);
                return;
            }
        }
    }

    public static void removeTask(UUID taskId){
        runningTasks.remove(taskId);
    }

    private static boolean isSameTask(ItemStack itemStack, UUID taskId){
        return itemStack != null && plugin.getNMSAdapter().getTag(itemStack, "task-id", "").equals(taskId.toString());
    }

}
