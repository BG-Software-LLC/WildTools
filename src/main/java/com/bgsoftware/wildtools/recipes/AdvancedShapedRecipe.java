package com.bgsoftware.wildtools.recipes;

import org.apache.commons.lang.Validate;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import java.lang.reflect.Field;
import java.util.Map;

public final class AdvancedShapedRecipe extends ShapedRecipe {

    private static Field ingredientsField;

    static {
        try{
            ingredientsField = ShapedRecipe.class.getDeclaredField("ingredients");
            ingredientsField.setAccessible(true);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private Map<Character, ItemStack> ingredients;

    public AdvancedShapedRecipe(ItemStack result){
        super(result);
        updateIngredients();
    }

    @Override
    public AdvancedShapedRecipe shape(String... shape) {
        super.shape(shape);
        updateIngredients();
        return this;
    }

    public AdvancedShapedRecipe setIngredient(char key, ItemStack itemStack) {
        Validate.isTrue(this.ingredients.containsKey(key), "Symbol does not appear in the shape: ", key);
        this.ingredients.put(key, itemStack);
        return this;
    }

    private void updateIngredients(){
        try{
            //noinspection unchecked
            ingredients = (Map<Character, ItemStack>) ingredientsField.get(this);
        }catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

}
