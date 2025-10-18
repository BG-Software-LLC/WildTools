package com.bgsoftware.wildtools.tools;

import com.bgsoftware.wildtools.Locale;
import com.bgsoftware.wildtools.api.events.CraftingWandUseEvent;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.api.objects.tools.CraftingTool;
import com.bgsoftware.wildtools.recipes.RecipeChoice;
import com.bgsoftware.wildtools.scheduler.Scheduler;
import com.bgsoftware.wildtools.utils.BukkitUtils;
import com.bgsoftware.wildtools.utils.Materials;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WCraftingTool extends WTool implements CraftingTool {

    private final List<Recipe> craftings;

    public WCraftingTool(Material type, String name, List<String> craftings) {
        super(type, name, ToolMode.CRAFTING);
        this.craftings = parseCraftings(craftings);
    }

    @Override
    public Iterator<Recipe> getCraftings() {
        return craftings.iterator();
    }

    @Override
    public boolean onBlockInteract(PlayerInteractEvent e) {
        if ((e.getClickedBlock().getType() != Material.CHEST && e.getClickedBlock().getType() != Material.TRAPPED_CHEST) ||
                !BukkitUtils.canInteractBlock(e.getPlayer(), e.getClickedBlock(), e.getItem()))
            return false;

        BlockState blockState = e.getClickedBlock().getState();
        Inventory chestInventory = ((InventoryHolder) e.getClickedBlock().getState()).getInventory();
        List<Inventory> inventories = plugin.getProviders().getAllInventories(blockState, chestInventory);

        if (inventories.isEmpty()) {
            Locale.INVALID_CONTAINER_CRAFTING.send(e.getPlayer());
            return false;
        }

        Iterator<Recipe> craftings = getCraftings();
        List<ItemStack> toAdd = new ArrayList<>();
        int craftedItemsAmount = 0;

        while (craftings.hasNext()) {
            Recipe recipe = craftings.next();
            List<RecipeChoice> ingredients;

            //Get the ingredients for the recipe
            if (recipe instanceof ShapedRecipe) {
                ingredients = getIngredients(recipe, new ArrayList<>(((ShapedRecipe) recipe).getIngredientMap().values()));
            } else if (recipe instanceof ShapelessRecipe) {
                ingredients = getIngredients(recipe, ((ShapelessRecipe) recipe).getIngredientList());
            } else if (recipe instanceof FurnaceRecipe) {
                ingredients = Collections.singletonList(RecipeChoice.of(((FurnaceRecipe) recipe).getInput()));
            } else continue;

            if (ingredients.isEmpty())
                continue;

            for (Inventory inventory : inventories) {
                int amountOfRecipes = Integer.MAX_VALUE;

                for (RecipeChoice ingredient : ingredients) {
                    amountOfRecipes = Math.min(amountOfRecipes, countItems(ingredient, inventory) / ingredient.getAmount());
                }

                if (amountOfRecipes > 0) {
                    for (RecipeChoice ingredient : ingredients) {
                        RecipeChoice clonedIngredient = ingredient.copy();
                        clonedIngredient.setAmount(clonedIngredient.getAmount() * amountOfRecipes);
                        clonedIngredient.remove(inventory);
                        if (clonedIngredient.test(Materials::isBottle))
                            toAdd.add(new ItemStack(Material.GLASS_BOTTLE, clonedIngredient.getAmount()));
                        else if (clonedIngredient.test(Materials::isBucket))
                            toAdd.add(new ItemStack(Material.BUCKET, clonedIngredient.getAmount()));
                    }

                    ItemStack result = recipe.getResult().clone();
                    int resultAmount = result.getAmount() * amountOfRecipes;
                    while (resultAmount > result.getMaxStackSize()) {
                        ItemStack maxStackResult = result.clone();
                        maxStackResult.setAmount(result.getMaxStackSize());
                        toAdd.add(maxStackResult);
                        resultAmount -= result.getMaxStackSize();
                    }
                    if (resultAmount > 0) {
                        result.setAmount(resultAmount);
                        toAdd.add(result);
                    }

                    craftedItemsAmount += (amountOfRecipes * recipe.getResult().getAmount());
                }
            }
        }

        Scheduler.runTask(e.getPlayer(), () -> {
            CraftingWandUseEvent craftingWandUseEvent = new CraftingWandUseEvent(e.getPlayer(), this,
                    toAdd.stream().map(ItemStack::clone).collect(Collectors.toList()));
            Bukkit.getPluginManager().callEvent(craftingWandUseEvent);
        });

        plugin.getProviders().addItems(blockState, chestInventory, toAdd);

        if (craftedItemsAmount > 0) {
            reduceDurablility(e.getPlayer(), 1, e.getItem());
            Locale.CRAFT_SUCCESS.send(e.getPlayer(), craftedItemsAmount);
        } else {
            Locale.NO_CRAFT_ITEMS.send(e.getPlayer());
        }

        return true;
    }

    private List<Recipe> parseCraftings(List<String> recipes) {
        List<Recipe> recipeList = new ArrayList<>();

        Recipe current;
        Iterator<Recipe> bukkitRecipes = Bukkit.recipeIterator();

        while (bukkitRecipes.hasNext()) {
            current = bukkitRecipes.next();
            if (recipes.contains(current.getResult().getType().name()) ||
                    recipes.contains(current.getResult().getType() + ":" + current.getResult().getDurability()))
                recipeList.add(current);
        }

        return recipeList;
    }

    @SuppressWarnings("deprecation")
    private List<RecipeChoice> getIngredients(Recipe recipe, List<ItemStack> oldList) {
        Map<RecipeChoice, Integer> counts = new HashMap<>();
        List<RecipeChoice> ingredients = new ArrayList<>();

        for (ItemStack itemStack : oldList) {
            RecipeChoice recipeChoice;

            if (itemStack.getData().getData() < 0) {
                recipeChoice = RecipeChoice.of(plugin.getNMSAdapter().parseChoice(recipe, itemStack));
            } else {
                recipeChoice = RecipeChoice.of(itemStack);
            }

            counts.put(recipeChoice, counts.getOrDefault(recipeChoice, 0) + itemStack.getAmount());
        }

        for (RecipeChoice ingredient : counts.keySet()) {
            ingredient.setAmount(counts.get(ingredient));
            ingredients.add(ingredient);
        }

        return ingredients;
    }

    private int countItems(RecipeChoice recipeChoice, Inventory inventory) {
        int amount = 0;

        for (ItemStack _itemStack : inventory.getContents()) {
            if (recipeChoice.test(_itemStack)) {
                amount += _itemStack.getAmount();
            }
        }

        return amount;
    }

}
