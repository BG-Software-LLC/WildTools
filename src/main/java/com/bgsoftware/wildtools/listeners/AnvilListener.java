package com.bgsoftware.wildtools.listeners;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import com.bgsoftware.wildtools.utils.Executor;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@SuppressWarnings("unused")
public final class AnvilListener implements Listener {

    private final WildToolsPlugin plugin;

    public AnvilListener(WildToolsPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAnvilRecipe(PrepareAnvilEvent e){
        AnvilInventory anvilInventory = e.getInventory();

        ItemStack firstItem = anvilInventory.getItem(0), secondItem = anvilInventory.getItem(1);

        Tool firstSlot = plugin.getToolsManager().getTool(firstItem),
                secondSlot = plugin.getToolsManager().getTool(secondItem);

        if(firstSlot == null || !firstSlot.equals(secondSlot) || !firstSlot.isAnvilCombine() ||
                firstSlot.isUnbreakable() || firstSlot.isUsingDurability())
            return;

        String renameText = plugin.getNMSAdapter().getRenameText(e.getView()).trim();

        int firstUses = plugin.getNMSAdapter().getTag(firstItem, "tool-uses", firstSlot.getDefaultUses());
        int secondUses = plugin.getNMSAdapter().getTag(secondItem, "tool-uses", firstSlot.getDefaultUses());
        int finalUses = firstSlot.hasAnvilCombineLimit() ?
                Math.min(firstSlot.getAnvilCombineLimit(), firstUses + secondUses) : firstUses + secondUses;

        ItemStack result = firstSlot.getFormattedItemStack(finalUses);
        ItemMeta itemMeta = result.getItemMeta();

        String displayName = itemMeta.getDisplayName() == null ? "" : ChatColor.stripColor(itemMeta.getDisplayName()).trim();
        int expCost = firstSlot.getAnvilCombineExp();

        if(!renameText.equals(displayName)){
            itemMeta.setDisplayName(renameText);
            result.setItemMeta(itemMeta);
            //We must set the exp 1 tick later - or renaming the item won't refresh exp
            Executor.sync(() -> plugin.getNMSAdapter().setExpCost(e.getView(), expCost + 1), 1L);
        }
        else{
            //We must set the exp 1 tick later - or renaming the item won't refresh exp
            Executor.sync(() -> plugin.getNMSAdapter().setExpCost(e.getView(), expCost), 1L);
        }

        e.setResult(result);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAnvilResultClick(InventoryClickEvent e){
        if(e.getClickedInventory() == null || e.getClickedInventory().getType() != InventoryType.ANVIL || e.getRawSlot() != 2)
            return;

        ItemStack firstItem = e.getClickedInventory().getItem(0), secondItem = e.getClickedInventory().getItem(1);

        Tool firstSlot = plugin.getToolsManager().getTool(firstItem),
                secondSlot = plugin.getToolsManager().getTool(secondItem);

        if(firstSlot == null || !firstSlot.equals(secondSlot) || !firstSlot.isAnvilCombine() ||
                firstSlot.isUnbreakable() || firstSlot.isUsingDurability())
            return;

        if(firstSlot.getAnvilCombineExp() != plugin.getNMSAdapter().getExpCost(e.getView()))
            e.setCancelled(true);
    }

}
