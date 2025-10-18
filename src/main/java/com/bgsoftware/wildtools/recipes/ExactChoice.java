package com.bgsoftware.wildtools.recipes;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.function.Predicate;

public class ExactChoice implements RecipeChoice {

    private final ItemStack itemStack;

    ExactChoice(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean test(ItemStack itemStack) {
        return this.itemStack.getData().getData() < 0 ?
                itemStack != null && this.itemStack.getType() == itemStack.getType() : this.itemStack.isSimilar(itemStack);
    }

    @Override
    public boolean test(Predicate<Material> predicate) {
        return predicate.test(this.itemStack.getType());
    }

    @Override
    public void setAmount(int amount) {
        this.itemStack.setAmount(amount);
    }

    @Override
    public int getAmount() {
        return itemStack.getAmount();
    }

    @Override
    public void remove(Inventory inventory) {
        int amountLeft = this.itemStack.getAmount();
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (test(itemStack)) {
                int leftOvers = itemStack.getAmount() - amountLeft;
                if (leftOvers <= 0) {
                    amountLeft -= itemStack.getAmount();
                    inventory.setItem(i, null);
                } else {
                    itemStack.setAmount(itemStack.getAmount() - amountLeft);
                    inventory.setItem(i, itemStack);
                    break;
                }
            }
        }
    }

    @Override
    public int hashCode() {
        return itemStack.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ExactChoice && Objects.equals(itemStack, ((ExactChoice) obj).itemStack);
    }

    @Override
    public String toString() {
        return "ExactChoice{item=" + itemStack.toString() + "}";
    }

    @Override
    public RecipeChoice copy() {
        return new ExactChoice(itemStack.clone());
    }

}