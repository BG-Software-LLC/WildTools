package com.bgsoftware.wildtools.utils.recipes;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface RecipeChoice extends Predicate<ItemStack> {

    @Override
    boolean test(ItemStack itemStack);

    void setAmount(int amount);

    int getAmount();

    void remove(Inventory inventory);

    static RecipeChoice of(ItemStack... itemStacks){
        return itemStacks.length == 1 ? new ExactChoice(itemStacks[0]) : new MaterialChoice(Arrays.asList(itemStacks));
    }

    final class MaterialChoice implements RecipeChoice {

        private List<ItemStack> itemStacks;

        MaterialChoice(List<ItemStack> itemStacks){
            this.itemStacks = itemStacks.stream().map(ItemStack::clone).collect(Collectors.toList());
        }

        @Override
        public boolean test(ItemStack itemStack) {
            return itemStacks.stream().anyMatch(_itemStack -> _itemStack.isSimilar(itemStack));
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
            for(ItemStack itemStack : itemStacks){
                Map<Integer, ItemStack> additionalItems = inventory.removeItem(itemStack);

                if(additionalItems.isEmpty())
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

    }

    final class ExactChoice implements RecipeChoice {

        private ItemStack itemStack;

        ExactChoice(ItemStack itemStack){
            this.itemStack = itemStack.clone();
        }

        @Override
        public boolean test(ItemStack itemStack) {
            return this.itemStack.isSimilar(itemStack);
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
            inventory.removeItem(itemStack);
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
    }

}
