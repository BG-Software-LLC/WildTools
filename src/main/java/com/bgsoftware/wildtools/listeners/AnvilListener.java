package com.bgsoftware.wildtools.listeners;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import com.bgsoftware.wildtools.utils.Executor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public final class AnvilListener implements Listener {

    private final Map<AnvilInventory, String> renameTexts = new HashMap<>();
    private final Set<AnvilInventory> recentPrepares = new HashSet<>();
    private final WildToolsPlugin plugin;

    public AnvilListener(WildToolsPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAnvilAdd(InventoryClickEvent e){
        if(!(e.getClickedInventory() instanceof AnvilInventory))
            return;

        AnvilInventory inventory = (AnvilInventory) e.getClickedInventory();

        if(e.getRawSlot() != 0 && inventory.getItem(0) == null)
            return;

        ItemStack itemStack = null;

        if(e.getRawSlot() != 0){
            itemStack = inventory.getItem(0);
        }

        else switch (e.getClick()){
            case RIGHT:
            case LEFT:
                itemStack = e.getCursor();
                break;
            case SHIFT_RIGHT:
            case SHIFT_LEFT:
                itemStack = e.getCurrentItem();
                break;
            case NUMBER_KEY:
                itemStack = e.getView().getBottomInventory().getItem(e.getHotbarButton());
                break;
        }

        Tool tool = plugin.getToolsManager().getTool(itemStack);

        if(tool == null) {
            renameTexts.remove(inventory);
            return;
        }

        renameTexts.put(inventory, plugin.getNMSAdapter().getRenameText(e.getView()));
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

        String originalRenameText = renameTexts.remove(e.getInventory());
        String renameText = plugin.getNMSAdapter().getRenameText(e.getView());

        int firstUses = plugin.getNMSAdapter().getTag(firstItem, "tool-uses", firstSlot.getDefaultUses());
        int secondUses = plugin.getNMSAdapter().getTag(secondItem, "tool-uses", secondSlot.getDefaultUses());
        int finalUses = firstSlot.hasAnvilCombineLimit() ?
                Math.min(firstSlot.getAnvilCombineLimit(), firstUses + secondUses) : firstUses + secondUses;

        ItemStack result = firstSlot.getFormattedItemStack(finalUses);
        ItemMeta itemMeta = result.getItemMeta();

        int expCost = firstSlot.getAnvilCombineExp();

        if(!recentPrepares.contains(anvilInventory)) {
            if (!renameText.equals(originalRenameText)) {
                itemMeta.setDisplayName(renameText);
                result.setItemMeta(itemMeta);
                //We must set the exp 1 tick later - or renaming the item won't refresh exp
                Executor.sync(() -> plugin.getNMSAdapter().setExpCost(e.getView(), expCost + 1), 1L);
            } else {
                //We must set the exp 1 tick later - or renaming the item won't refresh exp
                Executor.sync(() -> plugin.getNMSAdapter().setExpCost(e.getView(), expCost), 1L);
            }
        }

        recentPrepares.add(anvilInventory);
        Executor.sync(() -> recentPrepares.remove(anvilInventory), 5L);

        result = plugin.getNMSAdapter().setTag(result, "tool-uses", finalUses);

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

        if(Math.abs(firstSlot.getAnvilCombineExp() - plugin.getNMSAdapter().getExpCost(e.getView())) > 1)
            e.setCancelled(true);
    }

}
