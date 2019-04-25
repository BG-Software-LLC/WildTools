package com.bgsoftware.wildtools.objects.tools;

import com.bgsoftware.wildtools.hooks.WildChestsHook;
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
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        new Thread(() -> {
            double totalEarnings = 0.0;

            Map<Inventory, List<Integer>> toSell = new HashMap<>();

            for(Inventory inventory : WildChestsHook.getAllInventories(e.getClickedBlock())) {
                toSell.put(inventory, new ArrayList<>());
                for (int slot = 0; slot < inventory.getSize(); slot++) {
                    ItemStack is = inventory.getItem(slot);
                    if (is != null && plugin.getProviders().canSellItem(e.getPlayer(), is)) {
                        toSell.get(inventory).add(slot);
                        totalEarnings += plugin.getProviders().getPrice(e.getPlayer(), is);
                    }
                }
            }

            double multiplier = getMultiplier();

            String message = toSell.values().stream().allMatch(List::isEmpty) ?
                    Locale.NO_SELL_ITEMS.getMessage() : Locale.SOLD_CHEST.getMessage();

            SellWandUseEvent sellWandUseEvent = new SellWandUseEvent(e.getPlayer(), (Chest) e.getClickedBlock().getState(), totalEarnings, multiplier, message);
            Bukkit.getPluginManager().callEvent(sellWandUseEvent);

            if(sellWandUseEvent.isCancelled())
                return;

            multiplier = sellWandUseEvent.getMultiplier();
            totalEarnings *= multiplier;

            for(Inventory inventory : toSell.keySet()){
                for(int slot : toSell.get(inventory)){
                    plugin.getProviders().trySellingItem(e.getPlayer(), inventory.getItem(slot), multiplier);
                    inventory.setItem(slot, new ItemStack(Material.AIR));
                }
            }

            //noinspection all
            message = sellWandUseEvent.getMessage().replace("{0}", totalEarnings + "")
                    .replace("{1}", multiplier != 1 && Locale.MULTIPLIER.getMessage() != null ? Locale.MULTIPLIER.getMessage(multiplier) : "");

            if(!toSell.isEmpty())
                reduceDurablility(e.getPlayer());

            if(!message.isEmpty())
                e.getPlayer().sendMessage(message);
        }).start();

        return true;
    }

}
