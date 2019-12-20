package com.bgsoftware.wildtools.objects.tools;

import com.bgsoftware.wildtools.hooks.WildChestsHook;
import com.bgsoftware.wildtools.utils.Executor;
import com.bgsoftware.wildtools.utils.NumberUtils;
import com.bgsoftware.wildtools.utils.items.ToolTaskManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Chest;
import org.bukkit.event.player.PlayerInteractEvent;
import com.bgsoftware.wildtools.Locale;
import com.bgsoftware.wildtools.api.events.SellWandUseEvent;
import com.bgsoftware.wildtools.api.objects.tools.SellTool;
import com.bgsoftware.wildtools.api.objects.ToolMode;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class WSellTool extends WTool implements SellTool {

    public WSellTool(Material type, String name){
        super(type, name, ToolMode.SELL);
    }

    @Override
    public boolean onBlockInteract(PlayerInteractEvent e) {
        if(!plugin.getProviders().isVaultEnabled()){
            e.getPlayer().sendMessage(ChatColor.RED + "You tried to use a sell-wand, but the server doesn't have Vault installed. " +
                    "Please contact the server administrators if you believe that this is an error.");
            return false;
        }

        if(!plugin.getProviders().canInteract(e.getPlayer(), e.getClickedBlock(), this))
            return false;

        if(e.getClickedBlock().getType() != Material.CHEST && e.getClickedBlock().getType() != Material.TRAPPED_CHEST){
            Locale.INVALID_CONTAINER_SELL_WAND.send(e.getPlayer());
            return false;
        }

        Chest chest = (Chest) e.getClickedBlock().getState();
        Inventory inventory = ((InventoryHolder) e.getClickedBlock().getState()).getInventory();
        UUID taskId = ToolTaskManager.generateTaskId(e.getItem(), e.getPlayer().getInventory());

        Executor.async(() -> {
            synchronized (getToolMutex(e.getClickedBlock())) {
                double totalEarnings = 0.0;
                boolean wildChest = false;

                Map<Integer, ItemStack> toSell = new HashMap<>();

                if (WildChestsHook.isWildChest(chest)) {
                    totalEarnings = WildChestsHook.getChestPrice(chest, e.getPlayer(), toSell);
                    wildChest = true;
                }

                if (!wildChest) {
                    totalEarnings = 0;
                    for (int slot = 0; slot < inventory.getSize(); slot++) {
                        ItemStack itemStack = inventory.getItem(slot);
                        if (itemStack != null && plugin.getProviders().canSellItem(e.getPlayer(), itemStack)) {
                            toSell.put(slot, itemStack);
                            totalEarnings += plugin.getProviders().getPrice(e.getPlayer(), itemStack);
                        }
                    }
                }

                double multiplier = getMultiplier();

                String message = toSell.isEmpty() ? Locale.NO_SELL_ITEMS.getMessage() : Locale.SOLD_CHEST.getMessage();

                SellWandUseEvent sellWandUseEvent = new SellWandUseEvent(e.getPlayer(), chest, totalEarnings, multiplier, message);
                Bukkit.getPluginManager().callEvent(sellWandUseEvent);

                if (sellWandUseEvent.isCancelled())
                    return;

                multiplier = sellWandUseEvent.getMultiplier();
                totalEarnings = sellWandUseEvent.getPrice() * multiplier;

                plugin.getProviders().depositPlayer(e.getPlayer(), totalEarnings);

                if(!wildChest){
                    toSell.keySet().forEach(slot -> inventory.setItem(slot, new ItemStack(Material.AIR)));
                }
                else {
                    WildChestsHook.removeItems(chest, toSell);
                }

                //noinspection all
                message = sellWandUseEvent.getMessage().replace("{0}", NumberUtils.format(totalEarnings))
                        .replace("{1}", multiplier != 1 && Locale.MULTIPLIER.getMessage() != null ? Locale.MULTIPLIER.getMessage(multiplier) : "");

                if (!toSell.isEmpty()) {
                    reduceDurablility(e.getPlayer(), taskId);
                }
                else{
                    ToolTaskManager.removeTask(taskId);
                }

                if (!message.isEmpty())
                    e.getPlayer().sendMessage(message);
            }
        });

        return true;
    }

}
