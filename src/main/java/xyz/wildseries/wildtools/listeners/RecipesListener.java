package xyz.wildseries.wildtools.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildtools.WildToolsPlugin;

import java.util.Map;

@SuppressWarnings("unused")
public final class RecipesListener implements Listener {

    private WildToolsPlugin plugin;

    public RecipesListener(WildToolsPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemCraft(InventoryClickEvent e){
        if(!(e.getInventory() instanceof CraftingInventory) || e.getRawSlot() > 9)
            return;

        CraftingInventory craftingInventory = (CraftingInventory) e.getInventory();

        if(craftingInventory.getRecipe() != null)
            return;

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            Map<String, ItemStack[]> recipes = plugin.getRecipes().getRecipes();
            ItemStack[] invRecipe = e.getInventory().getContents();

            outerLoop:
            for (String name : recipes.keySet()) {
                ItemStack[] recipe = recipes.get(name);
                for (int i = 0; i < 9; i++) {
                    if(!isSimilar(recipe[i], invRecipe[i + 1])){
                        continue outerLoop;
                    }
                }

                if(e.getRawSlot() != 0) {
                    e.getWhoClicked().getOpenInventory().setItem(0, plugin.getToolsManager().getTool(name).getFormattedItemStack());
                    //noinspection deprecation
                    ((Player) e.getWhoClicked()).updateInventory();
                }
                else{
                    for (int i = 1; i < e.getInventory().getSize(); i++) {
                        e.getInventory().setItem(i, new ItemStack(Material.AIR));
                    }
                    //noinspection deprecation
                    ((Player) e.getWhoClicked()).updateInventory();
                }

                break;
            }
        }, 2L);

    }

    private boolean isSimilar(ItemStack item1, ItemStack item2){
        return (item1.getType() == Material.AIR && item2.getType() == Material.AIR) || item1.isSimilar(item2);
    }

}
