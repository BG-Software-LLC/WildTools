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

    boolean test(String material);

    void setAmount(int amount);

    int getAmount();

    void remove(Inventory inventory);

    RecipeChoice copy();

    static RecipeChoice of(ItemStack... itemStacks){
        return itemStacks.length == 1 ? new ExactChoice(itemStacks[0]) : new MaterialChoice(Arrays.asList(itemStacks));
    }

    final class MaterialChoice implements RecipeChoice {

        private final List<ItemStack> itemStacks;

        MaterialChoice(List<ItemStack> itemStacks){
            this.itemStacks = itemStacks.stream().map(ItemStack::clone).collect(Collectors.toList());
        }

        @Override
        public boolean test(ItemStack itemStack) {
            return itemStacks.stream().anyMatch(_itemStack -> _itemStack.isSimilar(itemStack));
        }

        @Override
        public boolean test(String material) {
            return itemStacks.stream().anyMatch(_itemStack -> _itemStack.getType().name().contains(material));
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

        @Override
        public RecipeChoice copy() {
            return new MaterialChoice(itemStacks.stream().map(ItemStack::clone).collect(Collectors.toList()));
        }

    }

    final class ExactChoice implements RecipeChoice {

        private final ItemStack itemStack;

        ExactChoice(ItemStack itemStack){
            this.itemStack = itemStack.clone();
        }

        @SuppressWarnings("deprecation")
        @Override
        public boolean test(ItemStack itemStack) {
            return this.itemStack.getData().getData() < 0 ?
                    itemStack != null && this.itemStack.getType() == itemStack.getType() : this.itemStack.isSimilar(itemStack);
        }

        @Override
        public boolean test(String material) {
            return this.itemStack.getType().name().contains(material);
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
            for(int i = 0; i < inventory.getSize(); i++){
                ItemStack itemStack = inventory.getItem(i);
                if(test(itemStack)) {
                    int leftOvers = itemStack.getAmount() - amountLeft;
                    if(leftOvers <= 0) {
                        amountLeft -= itemStack.getAmount();
                        inventory.setItem(i, null);
                    }
                    else{
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

}
