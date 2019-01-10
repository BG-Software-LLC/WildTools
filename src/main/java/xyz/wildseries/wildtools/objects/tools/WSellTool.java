package xyz.wildseries.wildtools.objects.tools;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import xyz.wildseries.wildtools.Locale;
import xyz.wildseries.wildtools.api.events.SellWandUseEvent;
import xyz.wildseries.wildtools.api.objects.tools.SellTool;
import xyz.wildseries.wildtools.api.objects.ToolMode;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildtools.utils.BukkitUtil;

import java.util.ArrayList;
import java.util.List;

public final class WSellTool extends WTool implements SellTool {

    public WSellTool(Material type, String name){
        super(type, name, ToolMode.SELL);
    }

    @Override
    public void useOnBlock(Player pl, Block block){
        if(!plugin.getProviders().isVaultEnabled()){
            pl.sendMessage(ChatColor.RED + "You tried to use a sell-wand, but the server doesn't have Vault installed. " +
                    "Please contact the server administrators if you believe that this is an error.");
            return;
        }

        if(block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST){
            Locale.INVALID_CONTAINER_SELL_WAND.send(pl);
            return;
        }

        if(isOnlyInsideClaim() && !plugin.getProviders().inClaim(pl, block.getLocation()))
            return;

        WTool.toolBlockBreak.add(pl.getUniqueId());

        if(!BukkitUtil.canInteract(pl, block)) {
            WTool.toolBlockBreak.remove(pl.getUniqueId());
            return;
        }

        WTool.toolBlockBreak.remove(pl.getUniqueId());

        new Thread(() -> {
            if(!canUse(pl.getUniqueId())){
                Locale.COOLDOWN_TIME.send(pl, getTime(getTimeLeft(pl.getUniqueId())));
                return;
            }

            setLastUse(pl.getUniqueId());

            Inventory inventory = ((InventoryHolder) block.getState()).getInventory();

            double totalEarnings = 0.0;

            List<Integer> toSell = new ArrayList<>();

            for(int slot = 0; slot < inventory.getSize(); slot++){
                ItemStack is = inventory.getItem(slot);
                if(is != null && plugin.getProviders().canSellItem(pl, is)){
                    toSell.add(slot);
                    totalEarnings += plugin.getProviders().getPrice(pl, is);
                }
            }

            SellWandUseEvent sellWandUseEvent = new SellWandUseEvent(pl, (Chest) block.getState(), totalEarnings, Locale.SOLD_CHEST.getMessage());
            Bukkit.getPluginManager().callEvent(sellWandUseEvent);

            if(sellWandUseEvent.isCancelled())
                return;

            totalEarnings = sellWandUseEvent.getPrice();

            for(int slot : toSell){
                plugin.getProviders().trySellingItem(pl, inventory.getItem(slot));
                inventory.setItem(slot, new ItemStack(Material.AIR));
            }
            if(!toSell.isEmpty() && pl.getGameMode() != GameMode.CREATIVE && !isUnbreakable())
                reduceDurablility(pl);

            String message = sellWandUseEvent.getMessage().replace("{0}", totalEarnings + "");

            if(!message.isEmpty())
                pl.sendMessage(message);
        }).start();
    }

}
