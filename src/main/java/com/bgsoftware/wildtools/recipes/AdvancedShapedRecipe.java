package com.bgsoftware.wildtools.recipes;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public interface AdvancedShapedRecipe {

    AdvancedShapedRecipe setIngredient(char key, ItemStack itemStack);

    AdvancedShapedRecipe shape(String... shape);

    ShapedRecipe toRecipe();

}
