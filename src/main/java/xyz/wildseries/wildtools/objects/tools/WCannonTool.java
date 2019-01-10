package xyz.wildseries.wildtools.objects.tools;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import xyz.wildseries.wildtools.Locale;
import xyz.wildseries.wildtools.api.objects.ToolMode;
import xyz.wildseries.wildtools.api.objects.tools.CannonTool;
import xyz.wildseries.wildtools.objects.WSelection;
import xyz.wildseries.wildtools.utils.ItemUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class WCannonTool extends WTool implements CannonTool {

    private static Map<UUID, WSelection> selections = new HashMap<>();

    private int tntAmount;

    public WCannonTool(Material type, String name, int tntAmount){
        super(type, name, ToolMode.CANNON);
        this.tntAmount = tntAmount;
    }

    @Override
    public int getTNTAmount() {
        return tntAmount;
    }

    @Override
    public void useOnBlock(Player pl, Block block) {
        if(Bukkit.isPrimaryThread()){
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> useOnBlock(pl, block));
            return;
        }

        if(!canUse(pl.getUniqueId())){
            Locale.COOLDOWN_TIME.send(pl, getTime(getTimeLeft(pl.getUniqueId())));
            return;
        }

        WSelection selection = selections.get(pl.getUniqueId());

        if(selection == null || !selection.isReady()){
            Locale.SELECTION_NOT_READY.send(pl);
            return;
        }

        if(!selection.isInside()){
            Locale.SELECTION_MUST_BE_INSIDE.send(pl);
            return;
        }

        setLastUse(pl.getUniqueId());

        int filledDispensers = 0;
        int totalTNT = 0;

        toolBlockBreak.add(pl.getUniqueId());

        for(Dispenser dispenser : selection.getDispensers()){
            if(isOnlyInsideClaim() && !plugin.getProviders().inClaim(pl, dispenser.getLocation()))
                continue;
            int amount = tntAmount, freeSpace;
            if((freeSpace = getFreeSpace(dispenser.getInventory(), new ItemStack(Material.TNT))) < amount)
                amount = freeSpace;
            if(amount <= 0)
                continue;
            if(pl.getInventory().containsAtLeast(new ItemStack(Material.TNT), amount)){
                ItemUtil.addItem(new ItemStack(Material.TNT, amount), dispenser.getInventory(), null);
                pl.getInventory().removeItem(new ItemStack(Material.TNT, amount));
                filledDispensers++;
                totalTNT += amount;
            }
            else if(plugin.getProviders().getTNTAmountFromBank(pl) >= amount){
                ItemUtil.addItem(new ItemStack(Material.TNT, amount), dispenser.getInventory(), null);
                plugin.getProviders().takeTNTFromBank(pl, amount);
                filledDispensers++;
                totalTNT += amount;
            }
            else break;
        }

        if(filledDispensers > 0 && pl.getGameMode() != GameMode.CREATIVE && !isUnbreakable())
            reduceDurablility(pl);

        toolBlockBreak.remove(pl.getUniqueId());

        Locale.FILLED_DISPENSERS.send(pl, filledDispensers, totalTNT);
    }

    public static void addSelection(Player player, Location rightClick, Location leftClick){
        if(selections.containsKey(player.getUniqueId())){
            if(rightClick != null)
                selections.get(player.getUniqueId()).setRightClick(rightClick);
            if(leftClick != null)
                selections.get(player.getUniqueId()).setLeftClick(leftClick);
        }else {
            selections.put(player.getUniqueId(), new WSelection(player.getUniqueId(), player.getWorld(), rightClick, leftClick));
        }
    }

    public static WSelection getSelection(Player player){
        return selections.get(player.getUniqueId());
    }

    public static void removeSelection(Player player){
        selections.remove(player.getUniqueId());
    }

    private int getFreeSpace(Inventory inv, ItemStack is){
        int freeSpace = 0;

        for(int i = 0; i < inv.getSize(); i++){
            if(inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR)
                freeSpace += 64;
            else if(is.isSimilar(inv.getItem(i)))
                freeSpace += inv.getItem(i).getMaxStackSize() - inv.getItem(i).getAmount();
        }

        return freeSpace;
    }

}
