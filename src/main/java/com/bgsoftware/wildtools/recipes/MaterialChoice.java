package com.bgsoftware.wildtools.recipes;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MaterialChoice implements RecipeChoice {

    private final List<ItemStack> itemStacks;

    public MaterialChoice(List<ItemStack> itemStacks) {
        this.itemStacks = itemStacks.stream().map(ItemStack::clone).collect(Collectors.toList());
    }

    @Override
    public boolean test(ItemStack itemStack) {
        return itemStacks.stream().anyMatch(_itemStack -> _itemStack.isSimilar(itemStack));
    }

    @Override
    public boolean test(Predicate<Material> predicate) {
        return itemStacks.stream().anyMatch(_itemStack -> predicate.test(_itemStack.getType()));
    }

    @Override
    public void setAmount(int amount) {
        itemStacks.forEach(itemStack -> itemStack.setAmount(amount));
    }

    @Override
    public int getAmount() {
        return itemStacks.isEmpty() ? 0 : itemStacks.get(0).getAmount();
    }

    @Override
    public void remove(Inventory inventory) {
        for (ItemStack itemStack : itemStacks) {
            Map<Integer, ItemStack> additionalItems = inventory.removeItem(itemStack);

            if (additionalItems.isEmpty())
                break;

            setAmount(additionalItems.get(0).getAmount());
        }
    }

    @Override
    public int hashCode() {
        return 37 * 3 + Objects.hashCode(this.itemStacks);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MaterialChoice && Objects.equals(itemStacks, ((MaterialChoice) obj).itemStacks);
    }

    @Override
    public String toString() {
        return "MaterialChoice{items=" + itemStacks.toString() + "}";
    }

    @Override
    public RecipeChoice copy() {
        return new MaterialChoice(itemStacks.stream().map(ItemStack::clone).collect(Collectors.toList()));
    }

}