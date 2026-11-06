package com.bgsoftware.wildtools.recipes;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.function.Predicate;

public interface RecipeChoice extends Predicate<ItemStack> {

    @Override
    boolean test(ItemStack itemStack);

    boolean test(Predicate<Material> predicate);

    void setAmount(int amount);

    int getAmount();

    void remove(Inventory inventory);

    RecipeChoice copy();

    static RecipeChoice of(ItemStack... itemStacks){
        return itemStacks.length == 1 ? new ExactChoice(itemStacks[0]) : new MaterialChoice(Arrays.asList(itemStacks));
    }

}
