package com.bgsoftware.wildtools.nms;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.wildtools.recipes.AdvancedShapedRecipe;
import com.bgsoftware.wildtools.utils.items.DestroySpeedCategory;
import com.bgsoftware.wildtools.utils.items.ToolItemStack;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

public interface NMSAdapter {

    default void loadLegacy() {

    }

    ToolItemStack createToolItemStack(ItemStack other);

    ItemStack getItemInHand(Player player);

    ItemStack getItemInHand(Player player, Event e);

    Collection<Player> getOnlinePlayers();

    @Nullable
    Enchantment getGlowEnchant();

    @Nullable
    default Enchantment createGlowEnchantment() {
        Enchantment glowEnchant = Enchantment.getByName("wildtools_glowing_enchant");
        if (glowEnchant != null)
            return glowEnchant;

        glowEnchant = getGlowEnchant();

        if (glowEnchant != null) {
            try {
                Field field = Enchantment.class.getDeclaredField("acceptingNew");
                field.setAccessible(true);
                field.set(null, true);
                field.setAccessible(false);
            } catch (Exception ignored) {
            }

            try {
                Enchantment.registerEnchantment(glowEnchant);
            } catch (Exception ignored) {
            }
        }

        return glowEnchant;
    }

    int getFarmlandId();

    BlockPlaceEvent getFakePlaceEvent(Player player, Block block, Block copyBlock);

    void playPickupAnimation(LivingEntity livingEntity, Item item);

    DestroySpeedCategory getDestroySpeedCategory(Material material);

    default ItemStack[] parseChoice(Recipe recipe, ItemStack itemStack) {
        return new ItemStack[]{itemStack};
    }

    default void setExpCost(InventoryView inventoryView, int expCost) {

    }

    default int getExpCost(InventoryView inventoryView) {
        return 0;
    }

    default String getRenameText(InventoryView inventoryView) {
        return "";
    }

    default AdvancedShapedRecipe createRecipe(String toolName, ItemStack result) {
        return new AdvancedRecipeClassImpl(result);
    }

    class AdvancedRecipeClassImpl extends ShapedRecipe implements AdvancedShapedRecipe {

        private static final ReflectField<Map<Character, ItemStack>> INGREDIENTS_FIELD = new ReflectField<>(
                ShapedRecipe.class, Map.class, "ingredients");

        private Map<Character, ItemStack> ingredients;

        public AdvancedRecipeClassImpl(org.bukkit.inventory.ItemStack result) {
            super(result);
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
            Validate.isTrue(this.ingredients.containsKey(key), "Symbol does not appear in the shape: ", key);
            this.ingredients.put(key, itemStack);
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

}
