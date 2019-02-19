package xyz.wildseries.wildtools.objects.tools;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Chest;
import org.bukkit.event.player.PlayerInteractEvent;
import xyz.wildseries.wildtools.Locale;
import xyz.wildseries.wildtools.api.events.SellWandUseEvent;
import xyz.wildseries.wildtools.api.objects.tools.SellTool;
import xyz.wildseries.wildtools.api.objects.ToolMode;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

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

        if(e.getClickedBlock().getType() != Material.CHEST && e.getClickedBlock().getType() != Material.TRAPPED_CHEST){
            Locale.INVALID_CONTAINER_SELL_WAND.send(e.getPlayer());
            return false;
        }

        new Thread(() -> {
            Inventory inventory = ((InventoryHolder) e.getClickedBlock().getState()).getInventory();

            double totalEarnings = 0.0;

            List<Integer> toSell = new ArrayList<>();

            for(int slot = 0; slot < inventory.getSize(); slot++){
                ItemStack is = inventory.getItem(slot);
                if(is != null && plugin.getProviders().canSellItem(e.getPlayer(), is)){
                    toSell.add(slot);
                    totalEarnings += plugin.getProviders().getPrice(e.getPlayer(), is);
                }
            }

            SellWandUseEvent sellWandUseEvent = new SellWandUseEvent(e.getPlayer(), (Chest) e.getClickedBlock().getState(), totalEarnings, Locale.SOLD_CHEST.getMessage());
            Bukkit.getPluginManager().callEvent(sellWandUseEvent);

            if(sellWandUseEvent.isCancelled())
                return;

            totalEarnings = sellWandUseEvent.getPrice();

            for(int slot : toSell){
                plugin.getProviders().trySellingItem(e.getPlayer(), inventory.getItem(slot));
                inventory.setItem(slot, new ItemStack(Material.AIR));
            }

            String message = sellWandUseEvent.getMessage().replace("{0}", totalEarnings + "");

            if(!message.isEmpty())
                e.getPlayer().sendMessage(message);
        }).start();

        return true;
    }

}
