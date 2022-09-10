package com.bgsoftware.wildtools.nms;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.wildtools.recipes.AdvancedShapedRecipe;
import com.bgsoftware.wildtools.utils.items.ToolItemStack;
import org.apache.commons.lang.Validate;
import org.bukkit.Chunk;
import org.bukkit.CropState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface NMSAdapter {

    String getVersion();

    default boolean isLegacy() {
        return true;
    }

    List<ItemStack> getBlockDrops(Player pl, Block bl, boolean silkTouch);

    List<ItemStack> getCropDrops(Player pl, Block bl);

    int getExpFromBlock(Block block, Player player);

    ItemStack getItemInHand(Player player);

    ItemStack getItemInHand(Player player, Event e);

    Object[] createSyncedItem(ItemStack other);

    void setTag(ToolItemStack toolItemStack, String key, int value);

    void setTag(ToolItemStack toolItemStack, String key, String value);

    int getTag(ToolItemStack toolItemStack, String key, int def);

    String getTag(ToolItemStack toolItemStack, String key, String def);

    void clearTasks(ToolItemStack toolItemStack);

    void breakTool(ToolItemStack toolItemStack, Player player);

    boolean isFullyGrown(Block block);

    void setCropState(Block block, CropState cropState);

    Collection<Player> getOnlinePlayers();

    void setBlockFast(Location location, int combinedId);

    void refreshChunk(Chunk chunk, Set<Location> blocksList);

    int getCombinedId(Block block);

    int getFarmlandId();

    void setCombinedId(Location location, int combinedId);

    Enchantment getGlowEnchant();

    boolean isOutsideWorldborder(Location location);

    BlockPlaceEvent getFakePlaceEvent(Player player, Block block, Block copyBlock);

    void playPickupAnimation(LivingEntity livingEntity, Item item);

    boolean isAxeType(Material material);

    boolean isShovelType(Material material);

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

    Object getDroppedItem(ItemStack itemStack, Location location);

    void dropItems(List<Object> droppedItems);

    default int getMinHeight(World world) {
        return 0;
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
