package com.bgsoftware.wildtools.recipes;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public interface AdvancedShapedRecipe {

    @SuppressWarnings("UnusedReturnValue")
    AdvancedShapedRecipe setIngredient(char key, ItemStack itemStack);

    @SuppressWarnings("UnusedReturnValue")
    AdvancedShapedRecipe shape(String... shape);

    ShapedRecipe toRecipe();

}
