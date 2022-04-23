package com.bgsoftware.wildtools.nms;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.objects.WMaterial;
import com.bgsoftware.wildtools.recipes.AdvancedShapedRecipe;
import com.bgsoftware.wildtools.utils.Executor;
import com.bgsoftware.wildtools.utils.items.ToolItemStack;
import com.destroystokyo.paper.antixray.ChunkPacketBlockControllerAntiXray;
import io.papermc.paper.enchantments.EnchantmentRarity;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutCollect;
import net.minecraft.network.protocol.game.PacketPlayOutLightUpdate;
import net.minecraft.server.level.ChunkProviderServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.LightEngineThreaded;
import net.minecraft.server.level.PlayerChunkMap;
import net.minecraft.server.level.PlayerMap;
import net.minecraft.server.level.WorldServer;
import net.minecraft.stats.StatisticList;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.inventory.ContainerAnvil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.Chunk;
import org.bukkit.Bukkit;
import org.bukkit.CropState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.WorldBorder;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_18_R2.CraftChunk;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.block.CraftBlock;
import org.bukkit.craftbukkit.v1_18_R2.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftItem;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R2.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityCategory;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.bgsoftware.wildtools.nms.NMSMappings_v1_18_R2.*;

@SuppressWarnings({"unused", "ConstantConditions"})
public final class NMSAdapter_v1_18_R2 implements NMSAdapter {

    private static final ReflectField<PlayerMap> PLAYER_MAP_FIELD = new ReflectField<>(PlayerChunkMap.class, PlayerMap.class, "I");
    private static final ReflectField<ItemStack> ITEM_STACK_HANDLE = new ReflectField<>(CraftItemStack.class, ItemStack.class, "handle");
    private static final ReflectMethod<Void> UPDATE_NEARBY_BLOCKS = new ReflectMethod<>(
            "com.destroystokyo.paper.antixray.ChunkPacketBlockControllerAntiXray",
            "updateNearbyBlocks", World.class, BlockPosition.class);

    @Override
    public String getVersion() {
        return "v1_18_R2";
    }

    @Override
    public boolean isLegacy() {
        return false;
    }

    @Override
    public List<org.bukkit.inventory.ItemStack> getBlockDrops(Player pl, org.bukkit.block.Block bl, boolean silkTouch) {
        List<org.bukkit.inventory.ItemStack> drops = new ArrayList<>();

        EntityPlayer player = ((CraftPlayer) pl).getHandle();
        BlockPosition blockPosition = new BlockPosition(bl.getX(), bl.getY(), bl.getZ());
        WorldServer worldServer = getLevel(player);
        IBlockData blockData = getBlockState(worldServer, blockPosition);
        Block block = getBlock(blockData);
        ItemStack itemStack = CraftItemStack.asNMSCopy(pl.getInventory().getItemInMainHand());
        TileEntity tileEntity = getBlockEntity(worldServer, blockPosition);

        return getDrops(blockData, worldServer, blockPosition, tileEntity, player, itemStack)
                .stream().map(CraftItemStack::asBukkitCopy).collect(Collectors.toList());
    }

    @Override
    public List<org.bukkit.inventory.ItemStack> getCropDrops(Player pl, org.bukkit.block.Block bl) {
        //1.14 has a nice method to get drops of blocks, so we can safely call the getBlockDrops method.
        return getBlockDrops(pl, bl, false);
    }

    @Override
    public int getExpFromBlock(org.bukkit.block.Block block, Player player) {
        WorldServer world = ((CraftWorld) block.getWorld()).getHandle();
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        IBlockData blockData = getBlockState(world, blockPosition);
        return getBlock(blockData).getExpDrop(blockData, world, blockPosition,
                CraftItemStack.asNMSCopy(player.getInventory().getItemInMainHand()));
    }

    @Override
    public int getTag(ToolItemStack toolItemStack, String key, int def) {
        ItemStack nmsStack = (ItemStack) toolItemStack.getNMSItem();
        NBTTagCompound tagCompound = NMSMappings_v1_18_R2.getTag(nmsStack);
        return tagCompound == null || !contains(tagCompound, key) ? def : getInt(tagCompound, key);
    }

    @Override
    public void setTag(ToolItemStack toolItemStack, String key, int value) {
        ItemStack nmsStack = (ItemStack) toolItemStack.getNMSItem();
        NBTTagCompound tagCompound = getOrCreateTag(nmsStack);
        putInt(tagCompound, key, value);
    }

    @Override
    public String getTag(ToolItemStack toolItemStack, String key, String def) {
        ItemStack nmsStack = (ItemStack) toolItemStack.getNMSItem();
        NBTTagCompound tagCompound = NMSMappings_v1_18_R2.getTag(nmsStack);
        return tagCompound == null || !contains(tagCompound, key) ? def : getString(tagCompound, key);
    }

    @Override
    public void setTag(ToolItemStack toolItemStack, String key, String value) {
        ItemStack nmsStack = (ItemStack) toolItemStack.getNMSItem();
        NBTTagCompound tagCompound = getOrCreateTag(nmsStack);
        putString(tagCompound, key, value);
    }

    @Override
    public void clearTasks(ToolItemStack toolItemStack) {
        ItemStack nmsStack = (ItemStack) toolItemStack.getNMSItem();
        NBTTagCompound tagCompound = NMSMappings_v1_18_R2.getTag(nmsStack);
        if (tagCompound != null)
            remove(tagCompound, "task-id");
    }

    @Override
    public void breakTool(ToolItemStack toolItemStack, Player player) {
        ItemStack nmsStack = (ItemStack) toolItemStack.getNMSItem();
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        broadcastBreakEvent(entityPlayer, EnumItemSlot.a);
        Item item = getItem(nmsStack);

        if (getCount(nmsStack) == 1)
            CraftEventFactory.callPlayerItemBreakEvent(entityPlayer, nmsStack);

        shrink(nmsStack, 1);

        entityPlayer.b(StatisticList.d.b(item));

        setDamageValue(nmsStack, 0);
    }

    @Override
    public Object[] createSyncedItem(org.bukkit.inventory.ItemStack other) {
        CraftItemStack craftItemStack;
        ItemStack handle;
        if (other instanceof CraftItemStack) {
            craftItemStack = (CraftItemStack) other;
            handle = ITEM_STACK_HANDLE.get(other);
        } else {
            handle = CraftItemStack.asNMSCopy(other);
            craftItemStack = CraftItemStack.asCraftMirror(handle);
        }

        return new Object[]{craftItemStack, handle};
    }

    @Override
    public org.bukkit.inventory.ItemStack getItemInHand(Player player) {
        return player.getInventory().getItemInMainHand();
    }

    @Override
    public org.bukkit.inventory.ItemStack getItemInHand(Player player, Event e) {
        boolean offHand = false;

        if (e instanceof PlayerInteractEvent) {
            offHand = ((PlayerInteractEvent) e).getHand() == EquipmentSlot.OFF_HAND;
        } else if (e instanceof PlayerInteractEntityEvent) {
            offHand = ((PlayerInteractEntityEvent) e).getHand() == EquipmentSlot.OFF_HAND;
        }

        return offHand ? player.getInventory().getItemInOffHand() : getItemInHand(player);
    }

    @Override
    public boolean isFullyGrown(org.bukkit.block.Block block) {
        if (block.getType() == Material.CACTUS || block.getType() == WMaterial.SUGAR_CANE.parseMaterial() ||
                block.getType() == Material.PUMPKIN || block.getType() == WMaterial.MELON.parseMaterial() ||
                block.getType().name().equals("BAMBOO"))
            return true;
        CraftBlock craftBlock = (CraftBlock) block;
        BlockData blockData = craftBlock.getBlockData();
        return ((Ageable) blockData).getAge() == ((Ageable) blockData).getMaximumAge();
    }

    @Override
    public void setCropState(org.bukkit.block.Block block, CropState cropState) {
        if (block.getType() == Material.CHORUS_PLANT) {
            block.setType(Material.CHORUS_FLOWER);
        } else {
            CraftBlock craftBlock = (CraftBlock) block;
            BlockData blockData = craftBlock.getBlockData();
            if (blockData instanceof Ageable) {
                ((Ageable) blockData).setAge(cropState.ordinal());
                craftBlock.setBlockData(blockData, true);
            } else {
                block.setType(Material.AIR);
            }
        }
    }

    @Override
    public Collection<Player> getOnlinePlayers() {
        return new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    @Override
    public void setBlockFast(Location location, int combinedId) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        Chunk chunk = getChunkAt(world, blockPosition);

        if (combinedId == 0) {
            world.a(null, 2001, blockPosition,
                    NMSMappings_v1_18_R2.getId(getBlockState(world, blockPosition)));
        }

        setBlockState(chunk, blockPosition, getByCombinedId(combinedId), true);

        if (UPDATE_NEARBY_BLOCKS.isValid() && world.paperConfig.antiXray &&
                world.chunkPacketBlockController instanceof ChunkPacketBlockControllerAntiXray) {
            UPDATE_NEARBY_BLOCKS.invoke(world.chunkPacketBlockController, world, blockPosition);
        }
    }

    @Override
    public void refreshChunk(org.bukkit.Chunk bukkitChunk, Set<Location> blocksList) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
        Map<Integer, Set<Short>> blocks = new HashMap<>();
        WorldServer worldServer = NMSMappings_v1_18_R2.getLevel(chunk);

        ChunkProviderServer chunkProviderServer = getChunkSource(worldServer);

        for (Location location : blocksList) {
            BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            chunkProviderServer.a(blockPosition);
        }

        ChunkCoordIntPair chunkCoords = getPos(chunk);

        LightEngineThreaded lightEngine = (LightEngineThreaded) getLightEngine(worldServer);

        for (Location location : blocksList) {
            BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
            lightEngine.a(blockPosition);
        }

        Executor.sync(() -> sendPacketToRelevantPlayers(worldServer, chunkCoords.c, chunkCoords.d,
                        new PacketPlayOutLightUpdate(chunkCoords, lightEngine, null, null, true)),
                2L);
    }

    @Override
    public int getCombinedId(Location location) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        return NMSMappings_v1_18_R2.getId(getBlockState(world, blockPosition));
    }

    @Override
    public int getFarmlandId() {
        return NMSMappings_v1_18_R2.getId(NMSMappings_v1_18_R2.defaultBlockState(Blocks.ce));
    }

    @Override
    public void setCombinedId(Location location, int combinedId) {
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        setBlock(world, blockPosition, getByCombinedId(combinedId), 18);
    }

    @Override
    public Enchantment getGlowEnchant() {
        //noinspection NullableProblems
        return new Enchantment(NamespacedKey.minecraft("glowing_enchant")) {
            @Override
            public String getName() {
                return "WildToolsGlow";
            }

            @Override
            public int getMaxLevel() {
                return 1;
            }

            @Override
            public int getStartLevel() {
                return 0;
            }

            @Override
            public EnchantmentTarget getItemTarget() {
                return null;
            }

            @Override
            public boolean conflictsWith(Enchantment enchantment) {
                return false;
            }

            @Override
            public boolean canEnchantItem(org.bukkit.inventory.ItemStack itemStack) {
                return true;
            }

            @Override
            public boolean isTreasure() {
                return false;
            }

            @Override
            public boolean isCursed() {
                return false;
            }

            public Component displayName(int i) {
                return null;
            }

            public boolean isTradeable() {
                return false;
            }

            public boolean isDiscoverable() {
                return false;
            }

            public EnchantmentRarity getRarity() {
                return null;
            }

            public float getDamageIncrease(int i, EntityCategory entityCategory) {
                return 0;
            }

            public Set<EquipmentSlot> getActiveSlots() {
                return null;
            }

            public String translationKey() {
                return "";
            }
        };
    }

    @Override
    public boolean isOutsideWorldborder(Location location) {
        WorldBorder worldBorder = location.getWorld().getWorldBorder();
        int radius = (int) worldBorder.getSize() / 2;
        return location.getBlockX() > (worldBorder.getCenter().getBlockX() + radius) || location.getBlockX() < (worldBorder.getCenter().getBlockX() - radius) ||
                location.getBlockZ() > (worldBorder.getCenter().getBlockZ() + radius) || location.getBlockZ() < (worldBorder.getCenter().getBlockZ() - radius);
    }

    @Override
    public BlockPlaceEvent getFakePlaceEvent(Player player, Location location, org.bukkit.block.Block copyBlock) {
        org.bukkit.block.Block original = location.getBlock();
        BlockState originalState = original.getState();
        FakeCraftBlock fakeBlock = FakeCraftBlock.at(location, copyBlock.getType(), originalState);
        return new BlockPlaceEvent(
                fakeBlock,
                originalState,
                fakeBlock.getRelative(BlockFace.DOWN),
                new org.bukkit.inventory.ItemStack(copyBlock.getType()),
                player,
                true,
                EquipmentSlot.HAND
        );
    }

    @Override
    public void playPickupAnimation(LivingEntity livingEntity, org.bukkit.entity.Item item) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        EntityItem entityItem = (EntityItem) ((CraftItem) item).getHandle();
        WorldServer worldServer = ((CraftWorld) livingEntity.getWorld()).getHandle();
        broadcast(getChunkSource(worldServer), entityItem, new PacketPlayOutCollect(item.getEntityId(),
                livingEntity.getEntityId(), item.getItemStack().getAmount()));
    }

    @Override
    public boolean isAxeType(Material material) {
        float destroySpeed = getDestroySpeed(Items.mU, new ItemStack(Items.mU),
                ((CraftBlockData) material.createBlockData()).getState());
        return destroySpeed == 8.0F;
    }

    @Override
    public boolean isShovelType(Material material) {
        float destroySpeed = getDestroySpeed(Items.mS, new ItemStack(Items.mS),
                ((CraftBlockData) material.createBlockData()).getState());
        return destroySpeed == 8.0F;
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
        ContainerAnvil container = (ContainerAnvil) ((CraftInventoryView) inventoryView).getHandle();
        set(container.w, expCost);
    }

    @Override
    public int getExpCost(InventoryView inventoryView) {
        return get(((ContainerAnvil) ((CraftInventoryView) inventoryView).getHandle()).w);
    }

    @Override
    public String getRenameText(InventoryView inventoryView) {
        return ((ContainerAnvil) ((CraftInventoryView) inventoryView).getHandle()).v;
    }

    @Override
    public AdvancedShapedRecipe createRecipe(String toolName, org.bukkit.inventory.ItemStack result) {
        return new AdvancedRecipeClassImpl(toolName, result);
    }

    @Override
    public Object getDroppedItem(org.bukkit.inventory.ItemStack itemStack, Location location) {
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        EntityItem entityitem = new EntityItem(world, location.getX(), location.getY(), location.getZ(), CraftItemStack.asNMSCopy(itemStack));
        entityitem.ap = 10;
        return entityitem;
    }

    @Override
    public void dropItems(List<Object> droppedItemsRaw) {
        droppedItemsRaw.removeIf(droppedItem -> !(droppedItem instanceof EntityItem));

        for (Object entityItem : droppedItemsRaw) {
            if (canMerge((EntityItem) entityItem)) {
                for (Object otherEntityItem : droppedItemsRaw) {
                    if (entityItem != otherEntityItem && canMerge((EntityItem) otherEntityItem)) {
                        if (mergeEntityItems((EntityItem) entityItem, (EntityItem) otherEntityItem))
                            break;
                    }
                }
            }
        }

        droppedItemsRaw.forEach(droppedItemObject -> {
            EntityItem entityItem = (EntityItem) droppedItemObject;
            if (isAlive(entityItem)) {
                addFreshEntity(NMSMappings_v1_18_R2.getLevel(entityItem), entityItem);
            }
        });
    }

    @Override
    public int getMinHeight(org.bukkit.World world) {
        return world.getMinHeight();
    }

    private static boolean canMerge(EntityItem entityItem) {
        ItemStack itemStack = NMSMappings_v1_18_R2.getItem(entityItem);
        return !isEmpty(itemStack) && getCount(itemStack) < getMaxStackSize(itemStack);
    }

    private static boolean mergeEntityItems(EntityItem entityItem, EntityItem otherEntity) {
        ItemStack itemOfEntity = NMSMappings_v1_18_R2.getItem(entityItem);
        ItemStack itemOfOtherEntity = NMSMappings_v1_18_R2.getItem(otherEntity);
        if (EntityItem.a(itemOfEntity, itemOfOtherEntity)) {
            if (!CraftEventFactory.callItemMergeEvent(otherEntity, entityItem).isCancelled()) {
                mergeItems(entityItem, itemOfEntity, itemOfOtherEntity);
                entityItem.ao = Math.max(entityItem.ao, otherEntity.ao);
                entityItem.ap = Math.min(entityItem.ap, otherEntity.ap);
                if (isEmpty(itemOfOtherEntity)) {
                    discard(otherEntity);
                }
            }
        }

        return !isAlive(entityItem);
    }

    private static void mergeItems(EntityItem entityItem, ItemStack itemStack, ItemStack otherItem) {
        ItemStack leftOver = EntityItem.a(itemStack, otherItem, 64);
        if (!isEmpty(leftOver)) {
            setItem(entityItem, leftOver);
        }
    }

    private static void sendPacketToRelevantPlayers(WorldServer worldServer, int chunkX, int chunkZ, Packet<?> packet) {
        PlayerChunkMap playerChunkMap = getChunkSource(worldServer).a;
        PLAYER_MAP_FIELD.get(playerChunkMap).a(1)
                .forEach(entityPlayer -> send(entityPlayer.b, packet));
    }

    @SuppressWarnings("NullableProblems")
    public static class AdvancedRecipeClassImpl extends ShapedRecipe implements AdvancedShapedRecipe {

        private static Field ingredientsField;

        static {
            try {
                ingredientsField = ShapedRecipe.class.getDeclaredField("ingredients");
                ingredientsField.setAccessible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        private Map<Character, RecipeChoice> ingredients;

        public AdvancedRecipeClassImpl(String toolName, org.bukkit.inventory.ItemStack result) {
            super(new NamespacedKey(WildToolsPlugin.getPlugin(), "recipe_" + toolName), result);
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
            try {
                //noinspection unchecked
                ingredients = (Map<Character, RecipeChoice>) ingredientsField.get(this);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

    }

    @SuppressWarnings("NullableProblems")
    private static class FakeCraftBlock extends CraftBlock {

        private final BlockState originalState;
        private Material blockType;

        FakeCraftBlock(WorldServer worldServer, BlockPosition blockPosition, Material material, BlockState originalState) {
            super(worldServer, blockPosition);
            this.blockType = material;
            this.originalState = originalState;
        }

        @Override
        public Material getType() {
            return blockType;
        }

        @Override
        public void setType(Material type) {
            this.blockType = type;
            super.setType(type);
        }

        @Override
        public BlockData getBlockData() {
            return CraftBlockData.newData(blockType, null);
        }

        @Override
        public BlockState getState() {
            return originalState;
        }

        static FakeCraftBlock at(Location location, Material type, BlockState originalState) {
            WorldServer worldServer = ((CraftWorld) location.getWorld()).getHandle();
            BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            return new FakeCraftBlock(worldServer, blockPosition, type, originalState);
        }

    }

}
