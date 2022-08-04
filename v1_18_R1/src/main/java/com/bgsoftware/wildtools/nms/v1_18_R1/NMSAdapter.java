package com.bgsoftware.wildtools.nms.v1_18_R1;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.nms.mapping.Remap;
import com.bgsoftware.wildtools.nms.v1_18_R1.mappings.net.minecraft.nbt.NBTTagCompound;
import com.bgsoftware.wildtools.nms.v1_18_R1.mappings.net.minecraft.server.level.ChunkProviderServer;
import com.bgsoftware.wildtools.nms.v1_18_R1.mappings.net.minecraft.world.entity.Entity;
import com.bgsoftware.wildtools.nms.v1_18_R1.mappings.net.minecraft.world.inventory.ContainerProperty;
import com.bgsoftware.wildtools.nms.v1_18_R1.mappings.net.minecraft.world.item.Item;
import com.bgsoftware.wildtools.nms.v1_18_R1.mappings.net.minecraft.world.item.ItemStack;
import com.bgsoftware.wildtools.nms.v1_18_R1.mappings.net.minecraft.world.level.ChunkCoordIntPair;
import com.bgsoftware.wildtools.nms.v1_18_R1.mappings.net.minecraft.world.level.World;
import com.bgsoftware.wildtools.nms.v1_18_R1.mappings.net.minecraft.world.level.block.Block;
import com.bgsoftware.wildtools.nms.v1_18_R1.mappings.net.minecraft.world.level.block.state.IBlockData;
import com.bgsoftware.wildtools.nms.v1_18_R1.mappings.net.minecraft.world.level.chunk.Chunk;
import com.bgsoftware.wildtools.objects.WMaterial;
import com.bgsoftware.wildtools.recipes.AdvancedShapedRecipe;
import com.bgsoftware.wildtools.utils.Executor;
import com.bgsoftware.wildtools.utils.items.ToolItemStack;
import com.destroystokyo.paper.antixray.ChunkPacketBlockControllerAntiXray;
import io.papermc.paper.enchantments.EnchantmentRarity;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutCollect;
import net.minecraft.network.protocol.game.PacketPlayOutLightUpdate;
import net.minecraft.server.level.LightEngineThreaded;
import net.minecraft.server.level.PlayerChunkMap;
import net.minecraft.server.level.PlayerMap;
import net.minecraft.server.level.WorldServer;
import net.minecraft.stats.StatisticList;
import net.minecraft.stats.StatisticWrapper;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.ContainerAnvil;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TileEntity;
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
import org.bukkit.craftbukkit.v1_18_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_18_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_18_R1.util.CraftMagicNumbers;
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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "ConstantConditions"})
public final class NMSAdapter implements com.bgsoftware.wildtools.nms.NMSAdapter {

    private static final ReflectField<PlayerMap> PLAYER_MAP_FIELD = new ReflectField<>(
            PlayerChunkMap.class, PlayerMap.class, Modifier.PRIVATE | Modifier.FINAL, 1);
    private static final ReflectField<net.minecraft.world.item.ItemStack> ITEM_STACK_HANDLE = new ReflectField<>(
            CraftItemStack.class, net.minecraft.world.item.ItemStack.class, "handle");
    private static final ReflectMethod<Void> UPDATE_NEARBY_BLOCKS = new ReflectMethod<>(
            "com.destroystokyo.paper.antixray.ChunkPacketBlockControllerAntiXray",
            "updateNearbyBlocks", net.minecraft.world.level.World.class, BlockPosition.class);

    @Remap(classPath = "net.minecraft.stats.Stats", name = "ITEM_BROKEN", type = Remap.Type.FIELD, remappedName = "d")
    private static final StatisticWrapper<net.minecraft.world.item.Item> ITEM_BROKEN_STATISTIC = StatisticList.d;
    @Remap(classPath = "net.minecraft.world.level.block.Blocks", name = "FARMLAND", type = Remap.Type.FIELD, remappedName = "ce")
    private static final Block FARMLAND_BLOCK = new Block(Blocks.ce);
    @Remap(classPath = "net.minecraft.world.item.Items", name = "DIAMOND_AXE", type = Remap.Type.FIELD, remappedName = "mU")
    private static final Item DIAMOND_AXE_ITEM = new Item(Items.mU);
    @Remap(classPath = "net.minecraft.world.item.Items", name = "DIAMOND_SHOVEL", type = Remap.Type.FIELD, remappedName = "mS")
    private static final Item DIAMOND_SHOVEL_ITEM = new Item(Items.mS);

    private static final String BUILT_AGAINST_MAPPING = "20b026e774dbf715e40a0b2afe114792";

    @Override
    public boolean isMappingsSupported() {
        return ((CraftMagicNumbers) CraftMagicNumbers.INSTANCE).getMappingsVersion().equals(BUILT_AGAINST_MAPPING);
    }

    @Override
    public String getVersion() {
        return "v1_18_R1";
    }

    @Override
    public boolean isLegacy() {
        return false;
    }

    @Override
    public List<org.bukkit.inventory.ItemStack> getBlockDrops(Player pl, org.bukkit.block.Block bl, boolean silkTouch) {
        List<org.bukkit.inventory.ItemStack> drops = new ArrayList<>();

        Entity entityPlayer = new Entity(((CraftPlayer) pl).getHandle());
        BlockPosition blockPosition = new BlockPosition(bl.getX(), bl.getY(), bl.getZ());
        World world = entityPlayer.getLevel();
        IBlockData blockData = world.getBlockState(blockPosition);
        net.minecraft.world.level.block.Block block = blockData.getBlock();
        net.minecraft.world.item.ItemStack itemStack = CraftItemStack.asNMSCopy(pl.getInventory().getItemInMainHand());
        TileEntity tileEntity = world.getBlockEntity(blockPosition);

        return Block.getDrops(blockData.getHandle(), (WorldServer) world.getHandle(), blockPosition, tileEntity,
                entityPlayer.getHandle(), itemStack).stream().map(CraftItemStack::asBukkitCopy).collect(Collectors.toList());
    }

    @Override
    public List<org.bukkit.inventory.ItemStack> getCropDrops(Player pl, org.bukkit.block.Block bl) {
        //1.14 has a nice method to get drops of blocks, so we can safely call the getBlockDrops method.
        return getBlockDrops(pl, bl, false);
    }

    @Override
    public int getExpFromBlock(org.bukkit.block.Block block, Player player) {
        World world = new World(((CraftWorld) block.getWorld()).getHandle());
        Entity entityPlayer = new Entity(((CraftPlayer) player).getHandle());
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        IBlockData blockData = world.getBlockState(blockPosition);
        return blockData.getBlock().getExpDrop(blockData.getHandle(), (WorldServer) world.getHandle(), blockPosition,
                CraftItemStack.asNMSCopy(player.getInventory().getItemInMainHand()));
    }

    @Override
    public int getTag(ToolItemStack toolItemStack, String key, int def) {
        ItemStack nmsStack = new ItemStack((net.minecraft.world.item.ItemStack) toolItemStack.getNMSItem());
        NBTTagCompound tagCompound = nmsStack.getTag();
        return tagCompound == null || !tagCompound.contains(key) ? def : tagCompound.getInt(key);
    }

    @Override
    public void setTag(ToolItemStack toolItemStack, String key, int value) {
        ItemStack nmsStack = new ItemStack((net.minecraft.world.item.ItemStack) toolItemStack.getNMSItem());
        NBTTagCompound tagCompound = nmsStack.getOrCreateTag();
        tagCompound.putInt(key, value);
    }

    @Override
    public String getTag(ToolItemStack toolItemStack, String key, String def) {
        ItemStack nmsStack = new ItemStack((net.minecraft.world.item.ItemStack) toolItemStack.getNMSItem());
        NBTTagCompound tagCompound = nmsStack.getTag();
        return tagCompound == null || !tagCompound.contains(key) ? def : tagCompound.getString(key);
    }

    @Override
    public void setTag(ToolItemStack toolItemStack, String key, String value) {
        ItemStack nmsStack = new ItemStack((net.minecraft.world.item.ItemStack) toolItemStack.getNMSItem());
        NBTTagCompound tagCompound = nmsStack.getOrCreateTag();
        tagCompound.putString(key, value);
    }

    @Override
    public void clearTasks(ToolItemStack toolItemStack) {
        ItemStack nmsStack = new ItemStack((net.minecraft.world.item.ItemStack) toolItemStack.getNMSItem());
        NBTTagCompound tagCompound = nmsStack.getTag();
        if (tagCompound != null)
            tagCompound.remove("task-id");
    }

    @Override
    public void breakTool(ToolItemStack toolItemStack, Player player) {
        ItemStack nmsStack = new ItemStack((net.minecraft.world.item.ItemStack) toolItemStack.getNMSItem());
        Entity entityPlayer = new Entity(((CraftPlayer) player).getHandle());

        entityPlayer.broadcastBreakEvent(EnumItemSlot.a);
        net.minecraft.world.item.Item item = nmsStack.getItem();

        if (nmsStack.getCount() == 1)
            CraftEventFactory.callPlayerItemBreakEvent((EntityHuman) entityPlayer.getHandle(), nmsStack.getHandle());

        nmsStack.shrink(1);

        entityPlayer.awardStat(ITEM_BROKEN_STATISTIC.b(item));

        nmsStack.setDamageValue(0);
    }

    @Override
    public Object[] createSyncedItem(org.bukkit.inventory.ItemStack other) {
        CraftItemStack craftItemStack;
        net.minecraft.world.item.ItemStack handle;
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
        World world = new World(((CraftWorld) location.getWorld()).getHandle());
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        Chunk chunk = world.getChunkAt(blockPosition);

        if (combinedId == 0) {
            world.levelEvent(null, 2001, blockPosition, Block.getId(world.getBlockStateNoMappings(blockPosition)));
        }

        chunk.setBlockState(blockPosition, Block.getByCombinedId(combinedId), true);

        if (UPDATE_NEARBY_BLOCKS.isValid() && world.getHandle().chunkPacketBlockController instanceof ChunkPacketBlockControllerAntiXray) {
            UPDATE_NEARBY_BLOCKS.invoke(world.getHandle().chunkPacketBlockController, world, blockPosition);
        }
    }

    @Override
    public void refreshChunk(org.bukkit.Chunk bukkitChunk, Set<Location> blocksList) {
        Chunk chunk = new Chunk(((CraftChunk) bukkitChunk).getHandle());
        Map<Integer, Set<Short>> blocks = new HashMap<>();
        World worldServer = chunk.getLevel();

        ChunkProviderServer chunkProviderServer = worldServer.getChunkSource();

        for (Location location : blocksList) {
            BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            chunkProviderServer.blockChanged(blockPosition);
        }

        ChunkCoordIntPair chunkCoords = chunk.getPos();

        LightEngineThreaded lightEngine = (LightEngineThreaded) worldServer.getLightEngine();

        for (Location location : blocksList) {
            BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
            lightEngine.a(blockPosition);
        }

        Executor.sync(() -> sendPacketToRelevantPlayers(worldServer, chunkCoords.getX(), chunkCoords.getZ(),
                        new PacketPlayOutLightUpdate(chunkCoords.getHandle(), lightEngine, null, null, true)),
                2L);
    }

    @Override
    public int getCombinedId(Location location) {
        World world = new World(((CraftWorld) location.getWorld()).getHandle());
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        return Block.getId(world.getBlockStateNoMappings(blockPosition));
    }

    @Override
    public int getFarmlandId() {
        return Block.getId(FARMLAND_BLOCK.defaultBlockState());
    }

    @Override
    public void setCombinedId(Location location, int combinedId) {
        World world = new World(((CraftWorld) location.getWorld()).getHandle());
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        world.setBlock(blockPosition, Block.getByCombinedId(combinedId), 18);
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
        return location.getBlockX() > (worldBorder.getCenter().getBlockX() + radius) ||
                location.getBlockX() < (worldBorder.getCenter().getBlockX() - radius) ||
                location.getBlockZ() > (worldBorder.getCenter().getBlockZ() + radius) ||
                location.getBlockZ() < (worldBorder.getCenter().getBlockZ() - radius);
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
        World world = new World(((CraftWorld) livingEntity.getWorld()).getHandle());
        world.getChunkSource().broadcast(entityItem, new PacketPlayOutCollect(item.getEntityId(),
                livingEntity.getEntityId(), item.getItemStack().getAmount()));
    }

    @Override
    public boolean isAxeType(Material material) {
        float destroySpeed = DIAMOND_AXE_ITEM.getDestroySpeed(
                new net.minecraft.world.item.ItemStack(DIAMOND_AXE_ITEM.getHandle()),
                ((CraftBlockData) material.createBlockData()).getState());
        return destroySpeed == 8.0F;
    }

    @Override
    public boolean isShovelType(Material material) {
        float destroySpeed = DIAMOND_SHOVEL_ITEM.getDestroySpeed(
                new net.minecraft.world.item.ItemStack(DIAMOND_SHOVEL_ITEM.getHandle()),
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

    @Remap(classPath = "net.minecraft.world.inventory.AnvilMenu",
            name = "cost",
            type = Remap.Type.FIELD,
            remappedName = "w")
    @Override
    public void setExpCost(InventoryView inventoryView, int expCost) {
        ContainerAnvil container = (ContainerAnvil) ((CraftInventoryView) inventoryView).getHandle();
        ContainerProperty property = new ContainerProperty(container.w);
        property.set(expCost);
    }

    @Override
    public int getExpCost(InventoryView inventoryView) {
        ContainerAnvil container = (ContainerAnvil) ((CraftInventoryView) inventoryView).getHandle();
        ContainerProperty property = new ContainerProperty(container.w);
        return property.get();
    }

    @Remap(classPath = "net.minecraft.world.inventory.AnvilMenu",
            name = "itemName",
            type = Remap.Type.FIELD,
            remappedName = "v")
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
        Entity entityitem = new Entity(new EntityItem(world, location.getX(), location.getY(), location.getZ(),
                CraftItemStack.asNMSCopy(itemStack)));
        entityitem.setPickupDelay(10);
        return entityitem.getHandle();
    }

    @Override
    public void dropItems(List<Object> droppedItemsRaw) {
        droppedItemsRaw.removeIf(droppedItem -> !(droppedItem instanceof EntityItem));

        for (Object nmsEntityItem : droppedItemsRaw) {
            Entity entityItem = new Entity((EntityItem) nmsEntityItem);
            if (canMerge(entityItem)) {
                for (Object nmsOtherEntityItem : droppedItemsRaw) {
                    Entity otherEntityItem = new Entity((EntityItem) nmsOtherEntityItem);
                    if (nmsEntityItem != nmsOtherEntityItem && canMerge(otherEntityItem)) {
                        if (mergeEntityItems(entityItem, otherEntityItem))
                            break;
                    }
                }
            }
        }

        droppedItemsRaw.forEach(droppedItemObject -> {
            Entity entityItem = new Entity((EntityItem) droppedItemObject);
            if (entityItem.isAlive()) {
                entityItem.getLevel().addFreshEntity(entityItem.getHandle());
            }
        });
    }

    @Override
    public int getMinHeight(org.bukkit.World world) {
        return world.getMinHeight();
    }

    private static boolean canMerge(Entity entityItem) {
        ItemStack itemStack = entityItem.getItem();
        return !itemStack.isEmpty() && itemStack.getCount() < itemStack.getMaxStackSize();
    }

    private static boolean mergeEntityItems(Entity entityItem, Entity otherEntity) {
        ItemStack itemOfEntity = entityItem.getItem();
        ItemStack itemOfOtherEntity = otherEntity.getItem();
        if (EntityItem.a(itemOfEntity.getHandle(), itemOfOtherEntity.getHandle())) {
            if (!CraftEventFactory.callItemMergeEvent((EntityItem) otherEntity.getHandle(),
                    (EntityItem) entityItem.getHandle()).isCancelled()) {
                mergeItems(entityItem, itemOfEntity, itemOfOtherEntity);
                entityItem.setAge(Math.max(entityItem.getAge(), otherEntity.getAge()));
                entityItem.setPickupDelay(Math.max(entityItem.getPickupDelay(), otherEntity.getPickupDelay()));
                if (itemOfOtherEntity.isEmpty()) {
                    otherEntity.discard();
                }
            }
        }

        return !entityItem.isAlive();
    }

    private static void mergeItems(Entity entityItem, ItemStack itemStack, ItemStack otherItem) {
        ItemStack leftOver = new ItemStack(EntityItem.a(itemStack.getHandle(), otherItem.getHandle(), 64));
        if (!leftOver.isEmpty()) {
            entityItem.setItem(leftOver.getHandle());
        }
    }

    private static void sendPacketToRelevantPlayers(World world, int chunkX, int chunkZ, Packet<?> packet) {
        PlayerChunkMap playerChunkMap = world.getChunkSource().getPlayerChunkMap();
        PLAYER_MAP_FIELD.get(playerChunkMap).a(1).forEach(nmsEntityPlayer -> {
            Entity entity = new Entity(nmsEntityPlayer);
            entity.getPlayerConnection().send(packet);
        });
    }

    @SuppressWarnings("NullableProblems")
    public static class AdvancedRecipeClassImpl extends ShapedRecipe implements AdvancedShapedRecipe {

        private static final ReflectField<Map<Character, RecipeChoice>> INGREDIENTS_FIELD = new ReflectField<>(
                ShapedRecipe.class, Map.class, "ingredients");

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
            ingredients = INGREDIENTS_FIELD.get(this);
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
