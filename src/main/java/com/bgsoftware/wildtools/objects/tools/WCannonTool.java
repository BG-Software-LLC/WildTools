package com.bgsoftware.wildtools.objects.tools;

import com.bgsoftware.wildtools.api.events.CannonWandUseEvent;
import com.bgsoftware.wildtools.objects.WSelection;
import com.bgsoftware.wildtools.utils.items.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.bgsoftware.wildtools.Locale;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.api.objects.tools.CannonTool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public final class WCannonTool extends WTool implements CannonTool {

    private static final Map<UUID, WSelection> selections = new HashMap<>();

    private final int tntAmount;

    public WCannonTool(Material type, String name, int tntAmount){
        super(type, name, ToolMode.CANNON);
        this.tntAmount = tntAmount;
    }

    @Override
    public int getTNTAmount() {
        return tntAmount;
    }

    @Override
    public boolean onBlockInteract(PlayerInteractEvent e) {
        WCannonTool.addSelection(e.getPlayer(), e.getClickedBlock().getLocation(), null);
        Locale.SELECTION_RIGHT_CLICK.send(e.getPlayer());
        return false;
    }

    @Override
    public boolean onBlockHit(PlayerInteractEvent e) {
        WCannonTool.addSelection(e.getPlayer(),null, e.getClickedBlock().getLocation());
        Locale.SELECTION_LEFT_CLICK.send(e.getPlayer());
        return false;
    }

    @Override
    public boolean onAirInteract(PlayerInteractEvent e) {
        WSelection selection = selections.get(e.getPlayer().getUniqueId());

        if(selection == null || !selection.isReady()){
            Locale.SELECTION_NOT_READY.send(e.getPlayer());
            return false;
        }

        if(!selection.isInside()){
            Locale.SELECTION_MUST_BE_INSIDE.send(e.getPlayer());
            return false;
        }

        List<Dispenser> dispenserList = selection.getDispensers(this);

        int filledDispensers = 0;
        int totalTNT = 0;

        for(Dispenser dispenser : dispenserList){
            int amount = tntAmount, freeSpace;

            if((freeSpace = getFreeSpace(dispenser.getInventory(), new ItemStack(Material.TNT))) < amount)
                amount = freeSpace;

            if(amount <= 0)
                continue;

            if(e.getPlayer().getInventory().containsAtLeast(new ItemStack(Material.TNT), amount)){
                ItemUtils.addItem(new ItemStack(Material.TNT, amount), dispenser.getInventory(), null, null);
                e.getPlayer().getInventory().removeItem(new ItemStack(Material.TNT, amount));
                filledDispensers++;
                totalTNT += amount;
            }
            else if(plugin.getProviders().getTNTAmountFromBank(e.getPlayer()) >= amount){
                ItemUtils.addItem(new ItemStack(Material.TNT, amount), dispenser.getInventory(), null, null);
                plugin.getProviders().takeTNTFromBank(e.getPlayer(), amount);
                filledDispensers++;
                totalTNT += amount;
            }
            else break;
        }

        CannonWandUseEvent cannonWandUseEvent = new CannonWandUseEvent(e.getPlayer(), this,
                dispenserList.subList(0, filledDispensers).stream().map(Dispenser::getLocation).collect(Collectors.toList()));
        Bukkit.getPluginManager().callEvent(cannonWandUseEvent);

        if(filledDispensers > 0){
            reduceDurablility(e.getPlayer(), 1, e.getItem());
            Locale.FILLED_DISPENSERS.send(e.getPlayer(), filledDispensers, totalTNT);
        } else {
            Locale.NO_FILLED_DISPENSERS.send(e.getPlayer(), filledDispensers, totalTNT);
        }

        return true;
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
