package com.bgsoftware.wildtools.nms.v1_20_3.recipe;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.recipes.AdvancedShapedRecipe;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

import java.util.Map;

public class AdvancedRecipeClassImpl extends ShapedRecipe implements AdvancedShapedRecipe {

    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    private static final ReflectField<Map<Character, RecipeChoice>> INGREDIENTS_FIELD = new ReflectField<>(
            ShapedRecipe.class, Map.class, "ingredients");

    private Map<Character, RecipeChoice> ingredients;

    public AdvancedRecipeClassImpl(String toolName, org.bukkit.inventory.ItemStack result) {
        super(new NamespacedKey(plugin, "recipe_" + toolName), result);
        updateIngredients();
    }

    @Override
    public AdvancedRecipeClassImpl shape(String... shape) {
        super.shape(shape);
        updateIngredients();
        return this;
    }

    @Override
    public AdvancedRecipeClassImpl setIngredient(char key, org.bukkit.inventory.ItemStack itemStack) {
        this.ingredients.put(key, new RecipeChoice.MaterialChoice(itemStack.getType()));
        return this;
    }

    @Override
    public ShapedRecipe toRecipe() {
        return this;
    }

    private void updateIngredients() {
        ingredients = INGREDIENTS_FIELD.get(this);
    }

}
