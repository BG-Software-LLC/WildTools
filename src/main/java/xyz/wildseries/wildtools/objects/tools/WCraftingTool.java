package xyz.wildseries.wildtools.objects.tools;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
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

    private final List<String> craftings;

    public WCraftingTool(Material type, String name, List<String> craftings){
        super(type, name, ToolMode.CRAFTING);
        this.craftings = new ArrayList<>(craftings);
    }

    @Override
    public List<String> getCraftings(){
        return new ArrayList<>(craftings);
    }

    @Override
    public void useOnBlock(Player pl, Block block) {
        if(Bukkit.isPrimaryThread()){
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> useOnBlock(pl, block));
            return;
        }

        if(block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST){
            Locale.INVALID_CONTAINER_CRAFTING.send(pl);
            return;
        }

        if(!canUse(pl.getUniqueId())){
            Locale.COOLDOWN_TIME.send(pl, getTime(getTimeLeft(pl.getUniqueId())));
            return;
        }

        if(isOnlyInsideClaim() && !plugin.getProviders().inClaim(pl, block.getLocation()))
            return;

        if(!BukkitUtil.canInteract(pl, block))
            return;

        setLastUse(pl.getUniqueId());

        Inventory inventory = ((InventoryHolder) block.getState()).getInventory();

        int craftedItemsAmount = 0;

        List<ItemStack> toAdd = new ArrayList<>();
        Iterator<Recipe> recipeIterator = Bukkit.recipeIterator();

        while(recipeIterator.hasNext()){
            Recipe recipe = recipeIterator.next();

            if (!craftings.contains(recipe.getResult().getType().name()) &&
                    !craftings.contains(recipe.getResult().getType().name() + ":" + recipe.getResult().getDurability()))
                continue;

            List<ItemStack> ingredients;

            //Get the ingredients for the recipe
            if (recipe instanceof ShapedRecipe) {
                ingredients = getIngredients(new ArrayList<>(((ShapedRecipe) recipe).getIngredientMap().values()));
            } else if (recipe instanceof ShapelessRecipe) {
                ingredients = getIngredients(((ShapelessRecipe) recipe).getIngredientList());
            } else continue;

            if (ingredients.isEmpty())
                continue;

            boolean canCraft;
            do {
                canCraft = true;

                //Check if all the ingredients exist
                for (ItemStack ingredient : ingredients) {
                    if (!inventory.containsAtLeast(ingredient, ingredient.getAmount())) {
                        canCraft = false;
                        break;
                    }
                }

                if (canCraft) {
                    //Only if we can craft, we need to remove the items
                    for(ItemStack ingredient : ingredients)
                        inventory.removeItem(ingredient);

                    //Add the recipe's result to the inventory
                    toAdd.add(recipe.getResult());
                    craftedItemsAmount += recipe.getResult().getAmount();
                }
            }while(canCraft);
        }

        for(ItemStack itemStack : toAdd)
            ItemUtil.addItem(itemStack, inventory, block.getLocation());

        if(craftedItemsAmount > 0 && pl.getGameMode() != GameMode.CREATIVE && !isUnbreakable())
            reduceDurablility(pl);

        Locale.CRAFT_SUCCESS.send(pl, craftedItemsAmount);
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

}
