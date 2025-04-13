package com.bgsoftware.wildtools.nms.v1_21_5;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.wildtools.nms.NMSAdapter;
import com.bgsoftware.wildtools.nms.v1_21_5.tool.ToolItemStackImpl;
import com.bgsoftware.wildtools.nms.v1_21_5.world.FakeCraftBlock;
import com.bgsoftware.wildtools.recipes.AdvancedShapedRecipe;
import com.bgsoftware.wildtools.utils.items.DestroySpeedCategory;
import com.bgsoftware.wildtools.utils.items.ToolItemStack;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.entity.CraftItem;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.legacy.CraftLegacy;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NMSAdapterImpl implements NMSAdapter {

    private static final ReflectField<ItemStack> ITEM_STACK_HANDLE = new ReflectField<>(
            CraftItemStack.class, ItemStack.class, "handle");

    @Override
    public void loadLegacy() {
        // Load legacy by accessing the CraftLegacy class.
        CraftLegacy.fromLegacy(Material.ACACIA_BOAT);
    }

    @Override
    public ToolItemStack createToolItemStack(org.bukkit.inventory.ItemStack bukkitItem) {
        ItemStack nmsItem;

        if (bukkitItem instanceof CraftItemStack) {
            nmsItem = ITEM_STACK_HANDLE.get(bukkitItem);
        } else {
            nmsItem = CraftItemStack.asNMSCopy(bukkitItem);
        }

        return new ToolItemStackImpl(nmsItem);
    }

    @Override
    public org.bukkit.inventory.ItemStack getItemInHand(Player player) {
        return player.getInventory().getItemInMainHand();
    }

    @Override
    public org.bukkit.inventory.ItemStack getItemInHand(Player player, Event event) {
        boolean offHand = false;

        if (event instanceof PlayerInteractEvent playerInteractEvent) {
            offHand = playerInteractEvent.getHand() == org.bukkit.inventory.EquipmentSlot.OFF_HAND;
        } else if (event instanceof PlayerInteractEntityEvent playerInteractEntityEvent) {
            offHand = playerInteractEntityEvent.getHand() == org.bukkit.inventory.EquipmentSlot.OFF_HAND;
        }

        return offHand ? player.getInventory().getItemInOffHand() : getItemInHand(player);
    }

    @Override
    public Collection<Player> getOnlinePlayers() {
        return new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    @Override
    public void makeItemGlow(ItemMeta itemMeta) {
        itemMeta.setEnchantmentGlintOverride(true);
    }

    @Override
    public int getFarmlandId() {
        return Block.getId(Blocks.FARMLAND.defaultBlockState());
    }

    @Override
    public BlockPlaceEvent getFakePlaceEvent(Player player, org.bukkit.block.Block block, org.bukkit.block.Block copyBlock) {
        org.bukkit.block.BlockState originalState = block.getState();
        FakeCraftBlock fakeBlock = new FakeCraftBlock(block, copyBlock.getType(), originalState);
        return new BlockPlaceEvent(
                fakeBlock,
                originalState,
                fakeBlock.getRelative(BlockFace.DOWN),
                new org.bukkit.inventory.ItemStack(copyBlock.getType()),
                player,
                true,
                org.bukkit.inventory.EquipmentSlot.HAND
        );
    }

    @Override
    public void playPickupAnimation(org.bukkit.entity.LivingEntity bukkitLivingEntity, org.bukkit.entity.Item item) {
        LivingEntity livingEntity = ((CraftLivingEntity) bukkitLivingEntity).getHandle();
        ItemEntity itemEntity = (ItemEntity) ((CraftItem) item).getHandle();
        ServerLevel serverLevel = (ServerLevel) livingEntity.level();

        ClientboundTakeItemEntityPacket takeItemEntityPacket = new ClientboundTakeItemEntityPacket(itemEntity.getId(),
                livingEntity.getId(), itemEntity.getItem().getCount());

        serverLevel.getChunkSource().broadcast(itemEntity, takeItemEntityPacket);
    }

    @Override
    public DestroySpeedCategory getDestroySpeedCategory(Material material) {
        BlockState blockState = ((CraftBlockData) material.createBlockData()).getState();

        if (Items.DIAMOND_AXE.getDestroySpeed(new ItemStack(Items.DIAMOND_AXE), blockState) == 8f)
            return DestroySpeedCategory.AXE;

        if (Items.DIAMOND_SHOVEL.getDestroySpeed(new ItemStack(Items.DIAMOND_SHOVEL), blockState) == 8f)
            return DestroySpeedCategory.SHOVEL;

        return DestroySpeedCategory.PICKAXE;
    }

    @Override
    public org.bukkit.inventory.ItemStack[] parseChoice(Recipe recipe, org.bukkit.inventory.ItemStack itemStack) {
        List<org.bukkit.inventory.ItemStack> ingredients = new ArrayList<>();
        List<RecipeChoice> recipeChoices = new ArrayList<>();

        ingredients.add(itemStack);

        if (recipe instanceof ShapedRecipe) {
            recipeChoices.addAll(((ShapedRecipe) recipe).getChoiceMap().values());
        } else if (recipe instanceof ShapelessRecipe) {
            recipeChoices.addAll(((ShapelessRecipe) recipe).getChoiceList());
        }

        if (!recipeChoices.isEmpty()) {
            for (RecipeChoice recipeChoice : recipeChoices) {
                if (recipeChoice instanceof RecipeChoice.MaterialChoice && recipeChoice.test(itemStack)) {
                    ingredients.clear();
                    for (Material material : ((RecipeChoice.MaterialChoice) recipeChoice).getChoices())
                        ingredients.add(new org.bukkit.inventory.ItemStack(material));
                    break;
                }
            }
        }

        return ingredients.toArray(new org.bukkit.inventory.ItemStack[0]);
    }

    @Override
    public void setExpCost(InventoryView inventoryView, int expCost) {
        AnvilMenu anvilMenu = (AnvilMenu) ((CraftInventoryView) inventoryView).getHandle();
        anvilMenu.cost.set(expCost);
    }

    @Override
    public int getExpCost(InventoryView inventoryView) {
        AnvilMenu anvilMenu = (AnvilMenu) ((CraftInventoryView) inventoryView).getHandle();
        return anvilMenu.getCost();
    }

    @Override
    public String getRenameText(InventoryView inventoryView) {
        return ((AnvilMenu) ((CraftInventoryView) inventoryView).getHandle()).itemName;
    }

    @Override
    public AdvancedShapedRecipe createRecipe(String toolName, org.bukkit.inventory.ItemStack result) {
        return new com.bgsoftware.wildtools.nms.v1_20_R3.recipe.AdvancedRecipeClassImpl(toolName, result);
    }

}
