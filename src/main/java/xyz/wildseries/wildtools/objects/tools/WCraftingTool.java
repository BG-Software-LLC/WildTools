package xyz.wildseries.wildtools.objects.tools;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import xyz.wildseries.wildtools.Locale;
import xyz.wildseries.wildtools.api.objects.tools.CraftingTool;
import xyz.wildseries.wildtools.api.objects.ToolMode;
import xyz.wildseries.wildtools.utils.BukkitUtil;
import xyz.wildseries.wildtools.utils.ItemUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class WCraftingTool extends WTool implements CraftingTool {

    private final List<Recipe> craftings;

    public WCraftingTool(Material type, String name, List<String> craftings){
        super(type, name, ToolMode.CRAFTING);
        this.craftings = parseCraftings(craftings);
    }

    @Override
    public Iterator<Recipe> getCraftings(){
        return craftings.iterator();
    }

    @Override
    public boolean onBlockInteract(PlayerInteractEvent e) {
        if(!BukkitUtil.canInteract(e.getPlayer(), e.getClickedBlock()))
            return false;

        if(e.getClickedBlock().getType() != Material.CHEST && e.getClickedBlock().getType() != Material.TRAPPED_CHEST){
            Locale.INVALID_CONTAINER_CRAFTING.send(e.getPlayer());
            return false;
        }

        Inventory inventory = ((InventoryHolder) e.getClickedBlock().getState()).getInventory();
        Iterator<Recipe> craftings = getCraftings();

        new Thread(() -> {
            int craftedItemsAmount = 0;

            List<ItemStack> toAdd = new ArrayList<>();

            while(craftings.hasNext()){
                Recipe recipe = craftings.next();
                List<ItemStack> ingredients;

                //Get the ingredients for the recipe
                if (recipe instanceof ShapedRecipe) {
                    ingredients = getIngredients(new ArrayList<>(((ShapedRecipe) recipe).getIngredientMap().values()));
                } else if (recipe instanceof ShapelessRecipe) {
                    ingredients = getIngredients(((ShapelessRecipe) recipe).getIngredientList());
                } else continue;

                if (ingredients.isEmpty())
                    continue;

                int amountOfRecipes = Integer.MAX_VALUE;

                for(ItemStack ingredient : ingredients){
                    amountOfRecipes = Math.min(amountOfRecipes, countItems(ingredient, inventory) / ingredient.getAmount());
                }

                if(amountOfRecipes > 0) {
                    for (ItemStack ingredient : ingredients) {
                        ItemStack cloned = ingredient.clone();
                        cloned.setAmount(ingredient.getAmount() * amountOfRecipes);
                        inventory.removeItem(cloned);
                    }
                    ItemStack result = recipe.getResult().clone();
                    result.setAmount(result.getAmount() * amountOfRecipes);
                    toAdd.add(result);

                    craftedItemsAmount += amountOfRecipes;
                }
            }

            for(ItemStack itemStack : toAdd)
                ItemUtil.addItem(itemStack, inventory, e.getClickedBlock().getLocation());

            Locale.CRAFT_SUCCESS.send(e.getPlayer(), craftedItemsAmount);
        }).start();

        return true;
    }

    private List<Recipe> parseCraftings(List<String> recipes){
        List<Recipe> recipeList = new ArrayList<>();

        Recipe current;
        Iterator<Recipe> bukkitRecipes = Bukkit.recipeIterator();

        while (bukkitRecipes.hasNext()){
            current = bukkitRecipes.next();
            if(recipes.contains(current.getResult().getType().name()) ||
                    recipes.contains(current.getResult().getType() + ":" + current.getResult().getDurability()))
                recipeList.add(current);
        }

        return recipeList;
    }

    @SuppressWarnings("deprecation")
    private List<ItemStack> getIngredients(List<ItemStack> oldList){
        Map<ItemStack, Integer> counts = new HashMap<>();
        List<ItemStack> ingredients = new ArrayList<>();

        for(ItemStack itemStack : oldList){
            if(itemStack.getData().getData() < 0)
                itemStack.setDurability((short) 0);
            counts.put(itemStack, counts.getOrDefault(itemStack, 0) + itemStack.getAmount());
        }

        for(ItemStack ingredient : counts.keySet()){
            ingredient.setAmount(counts.get(ingredient));
            ingredients.add(ingredient);
        }

        return ingredients;
    }

    private int countItems(ItemStack itemStack, Inventory inventory){
        int amount = 0;

        for(ItemStack _itemStack : inventory.getContents()){
            if(_itemStack != null && _itemStack.isSimilar(itemStack))
                amount += _itemStack.getAmount();
        }

        return amount;
    }

}
