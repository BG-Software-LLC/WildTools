package com.bgsoftware.wildtools.nms.v1182;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildtools.nms.alogrithms.PaperGlowEnchantment;
import com.bgsoftware.wildtools.nms.alogrithms.SpigotGlowEnchantment;
import com.bgsoftware.wildtools.nms.v1182.world.FakeCraftBlock;
import com.bgsoftware.wildtools.recipes.AdvancedShapedRecipe;
import com.bgsoftware.wildtools.utils.Executor;
import com.bgsoftware.wildtools.utils.items.ToolItemStack;
import com.destroystokyo.paper.antixray.ChunkPacketBlockControllerAntiXray;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.Bukkit;
import org.bukkit.CropState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.BlockFace;
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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class NMSAdapter implements com.bgsoftware.wildtools.nms.NMSAdapter {

    private static final ReflectMethod<Void> SEND_PACKETS_TO_RELEVANT_PLAYERS = new ReflectMethod<>(
            ChunkHolder.class, 1, Packet.class, boolean.class);
    private static final ReflectField<Map<Long, ChunkHolder>> VISIBLE_CHUNKS = new ReflectField<>(
            ChunkMap.class, Map.class, Modifier.PUBLIC | Modifier.VOLATILE, 1);
    private static final ReflectField<ItemStack> ITEM_STACK_HANDLE = new ReflectField<>(
            CraftItemStack.class, ItemStack.class, "handle");
    private static final ReflectMethod<Void> UPDATE_NEARBY_BLOCKS = new ReflectMethod<>(
            "com.destroystokyo.paper.antixray.ChunkPacketBlockControllerAntiXray",
            "updateNearbyBlocks", Level.class, BlockPos.class);

    @Override
    public String getVersion() {
        return "v1_18_R2";
    }

    @Override
    public boolean isLegacy() {
        return false;
    }

    @Override
    public List<org.bukkit.inventory.ItemStack> getBlockDrops(Player bukkitPlayer, org.bukkit.block.Block bukkitBlock, boolean silkTouch) {
        ServerPlayer serverPlayer = ((CraftPlayer) bukkitPlayer).getHandle();
        ServerLevel serverLevel = serverPlayer.getLevel();
        BlockPos blockPos = new BlockPos(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());
        BlockState blockState = serverLevel.getBlockState(blockPos);
        ItemStack itemStack = serverPlayer.getMainHandItem();
        BlockEntity blockEntity = serverPlayer.getLevel().getBlockEntity(blockPos);

        return Block.getDrops(blockState, serverLevel, blockPos, blockEntity, serverPlayer, itemStack)
                .stream().map(CraftItemStack::asBukkitCopy).collect(Collectors.toList());
    }

    @Override
    public List<org.bukkit.inventory.ItemStack> getCropDrops(Player bukkitPlayer, org.bukkit.block.Block bukkitBlock) {
        // 1.18 has a nice method to get drops of blocks, so we can safely call the getBlockDrops method.
        return getBlockDrops(bukkitPlayer, bukkitBlock, false);
    }

    @Override
    public int getExpFromBlock(org.bukkit.block.Block bukkitBlock, Player bukkitPlayer) {
        ServerPlayer serverPlayer = ((CraftPlayer) bukkitPlayer).getHandle();
        BlockState blockState = ((CraftBlock) bukkitBlock).getNMS();
        return blockState.getBlock().getExpDrop(blockState,
                serverPlayer.getLevel(),
                ((CraftBlock) bukkitBlock).getPosition(),
                serverPlayer.getMainHandItem());
    }

    @Override
    public int getTag(ToolItemStack toolItemStack, String key, int def) {
        ItemStack itemStack = (ItemStack) toolItemStack.getNMSItem();
        CompoundTag compoundTag = itemStack.getTag();
        return compoundTag == null || !compoundTag.contains(key) ? def : compoundTag.getInt(key);
    }

    @Override
    public void setTag(ToolItemStack toolItemStack, String key, int value) {
        ItemStack itemStack = (ItemStack) toolItemStack.getNMSItem();
        CompoundTag compoundTag = itemStack.getOrCreateTag();
        compoundTag.putInt(key, value);
    }

    @Override
    public String getTag(ToolItemStack toolItemStack, String key, String def) {
        ItemStack itemStack = (ItemStack) toolItemStack.getNMSItem();
        CompoundTag compoundTag = itemStack.getTag();
        return compoundTag == null || !compoundTag.contains(key) ? def : compoundTag.getString(key);
    }

    @Override
    public void setTag(ToolItemStack toolItemStack, String key, String value) {
        ItemStack itemStack = (ItemStack) toolItemStack.getNMSItem();
        CompoundTag compoundTag = itemStack.getOrCreateTag();
        compoundTag.putString(key, value);
    }

    @Override
    public void clearTasks(ToolItemStack toolItemStack) {
        ItemStack itemStack = (ItemStack) toolItemStack.getNMSItem();
        CompoundTag compoundTag = itemStack.getTag();
        if (compoundTag != null)
            compoundTag.remove("task-id");
    }

    @Override
    public void breakTool(ToolItemStack toolItemStack, Player player) {
        ItemStack itemStack = (ItemStack) toolItemStack.getNMSItem();
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();

        serverPlayer.broadcastBreakEvent(EquipmentSlot.MAINHAND);
        Item item = itemStack.getItem();

        if (itemStack.getCount() == 1)
            CraftEventFactory.callPlayerItemBreakEvent(serverPlayer, itemStack);

        itemStack.shrink(1);

        serverPlayer.awardStat(Stats.ITEM_BROKEN.get(item));

        itemStack.setDamageValue(0);
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
    public boolean isFullyGrown(org.bukkit.block.Block block) {
        switch (block.getType()) {
            case CACTUS:
            case SUGAR_CANE:
            case PUMPKIN:
            case MELON:
            case BAMBOO:
                return true;
        }

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
        World bukkitWorld = location.getWorld();

        if (bukkitWorld == null)
            return;

        ServerLevel serverLevel = ((CraftWorld) bukkitWorld).getHandle();
        BlockPos blockPos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        LevelChunk levelChunk = serverLevel.getChunkAt(blockPos);

        if (combinedId == 0)
            serverLevel.levelEvent(null, 2001, blockPos, Block.getId(serverLevel.getBlockState(blockPos)));

        levelChunk.setBlockState(blockPos, Block.stateById(combinedId), true);

        if (UPDATE_NEARBY_BLOCKS.isValid() && serverLevel.chunkPacketBlockController instanceof ChunkPacketBlockControllerAntiXray) {
            UPDATE_NEARBY_BLOCKS.invoke(serverLevel.chunkPacketBlockController, serverLevel, blockPos);
        }
    }

    @Override
    public void refreshChunk(org.bukkit.Chunk bukkitChunk, Set<Location> blocksList) {
        LevelChunk levelChunk = ((CraftChunk) bukkitChunk).getHandle();
        ServerChunkCache serverChunkCache = levelChunk.level.getChunkSource();
        ThreadedLevelLightEngine lightEngine = (ThreadedLevelLightEngine) levelChunk.level.getLightEngine();

        for (Location location : blocksList) {
            BlockPos blockPos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            serverChunkCache.blockChanged(blockPos);
            lightEngine.checkBlock(blockPos);
        }

        ChunkPos chunkPos = levelChunk.getPos();

        Executor.sync(() -> {
            ClientboundLightUpdatePacket lightUpdatePacket = new ClientboundLightUpdatePacket(
                    chunkPos, lightEngine, null, null, true);
            sendPacketToRelevantPlayers(levelChunk.level, chunkPos.x, chunkPos.z, lightUpdatePacket);
        }, 2L);
    }

    @Override
    public int getCombinedId(org.bukkit.block.Block bukkitBlock) {
        return Block.getId(((CraftBlock) bukkitBlock).getNMS());
    }

    @Override
    public int getFarmlandId() {
        return Block.getId(Blocks.FARMLAND.defaultBlockState());
    }

    @Override
    public void setCombinedId(Location location, int combinedId) {
        World bukkitWorld = location.getWorld();

        if (bukkitWorld == null)
            throw new IllegalArgumentException("Cannot set combined id of blocks in null world.");

        ServerLevel serverLevel = ((CraftWorld) bukkitWorld).getHandle();
        BlockPos blockPos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        serverLevel.setBlock(blockPos, Block.stateById(combinedId), 18);
    }

    @Override
    public Enchantment getGlowEnchant() {
        try {
            return new PaperGlowEnchantment("wildtools_glowing_enchant");
        } catch (Throwable error) {
            return new SpigotGlowEnchantment("wildtools_glowing_enchant");
        }
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
        ServerLevel serverLevel = (ServerLevel) livingEntity.level;

        ClientboundTakeItemEntityPacket takeItemEntityPacket = new ClientboundTakeItemEntityPacket(itemEntity.getId(),
                livingEntity.getId(), itemEntity.getItem().getCount());

        serverLevel.getChunkSource().broadcast(itemEntity, takeItemEntityPacket);
    }

    @Override
    public boolean isAxeType(Material material) {
        float destroySpeed = Items.DIAMOND_AXE.getDestroySpeed(new ItemStack(Items.DIAMOND_AXE),
                ((CraftBlockData) material.createBlockData()).getState());
        return destroySpeed == 8.0F;
    }

    @Override
    public boolean isShovelType(Material material) {
        float destroySpeed = Items.DIAMOND_SHOVEL.getDestroySpeed(
                new ItemStack(Items.DIAMOND_SHOVEL),
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
        return new com.bgsoftware.wildtools.nms.recipe.AdvancedRecipeClassImpl(toolName, result);
    }

    @Override
    public Object getDroppedItem(org.bukkit.inventory.ItemStack itemStack, Location location) {
        World bukkitWorld = location.getWorld();

        if (bukkitWorld == null)
            throw new IllegalArgumentException("Cannot drop item in a null world.");

        ServerLevel serverLevel = ((CraftWorld) bukkitWorld).getHandle();
        ItemEntity itemEntity = new ItemEntity(serverLevel, location.getX(), location.getY(), location.getZ(),
                CraftItemStack.asNMSCopy(itemStack));
        itemEntity.pickupDelay = 10;

        return itemEntity;
    }

    @Override
    public void dropItems(List<Object> droppedItemsRaw) {
        droppedItemsRaw.removeIf(droppedItem -> !(droppedItem instanceof ItemEntity));

        for (Object droppedItemObject : droppedItemsRaw) {
            ItemEntity itemEntity = (ItemEntity) droppedItemObject;
            if (canMerge(itemEntity)) {
                for (Object otherDroppedItemObject : droppedItemsRaw) {
                    ItemEntity otherItemEntity = (ItemEntity) otherDroppedItemObject;
                    if (otherItemEntity != itemEntity && canMerge(otherItemEntity)) {
                        if (mergeEntityItems(itemEntity, otherItemEntity))
                            break;
                    }
                }
            }
        }

        droppedItemsRaw.forEach(droppedItemObject -> {
            ItemEntity itemEntity = (ItemEntity) droppedItemObject;
            if (itemEntity.isAlive()) {
                itemEntity.getLevel().addFreshEntity(itemEntity);
            }
        });
    }

    @Override
    public int getMinHeight(World world) {
        return world.getMinHeight();
    }

    private static boolean canMerge(ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getItem();
        return !itemStack.isEmpty() && itemStack.getCount() < itemStack.getMaxStackSize();
    }

    private static boolean mergeEntityItems(ItemEntity itemEntity, ItemEntity otherItemEntity) {
        ItemStack itemOfEntity = itemEntity.getItem();
        ItemStack itemOfOtherEntity = otherItemEntity.getItem();
        if (ItemEntity.areMergable(itemOfEntity, itemOfOtherEntity)) {
            if (!CraftEventFactory.callItemMergeEvent(otherItemEntity, itemEntity).isCancelled()) {
                mergeItems(itemEntity, itemOfEntity, itemOfOtherEntity);
                itemEntity.age = Math.max(itemEntity.age, otherItemEntity.age);
                itemEntity.pickupDelay = Math.max(itemEntity.pickupDelay, otherItemEntity.pickupDelay);
                if (itemOfOtherEntity.isEmpty()) {
                    otherItemEntity.discard();
                }
            }
        }

        return !itemEntity.isAlive();
    }

    private static void mergeItems(ItemEntity itemEntity, ItemStack itemStack, ItemStack otherItem) {
        ItemStack leftOver = ItemEntity.merge(itemStack, otherItem, itemStack.getMaxStackSize());
        if (!leftOver.isEmpty()) {
            itemEntity.setItem(leftOver);
        }
    }

    private static void sendPacketToRelevantPlayers(ServerLevel serverLevel, int chunkX, int chunkZ, Packet<?> packet) {
        ChunkMap chunkMap = serverLevel.getChunkSource().chunkMap;
        ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
        ChunkHolder chunkHolder;

        try {
            chunkHolder = chunkMap.getVisibleChunkIfPresent(chunkPos.toLong());
        } catch (Throwable ex) {
            chunkHolder = VISIBLE_CHUNKS.get(chunkMap).get(chunkPos.toLong());
        }

        if (chunkHolder != null) {
            SEND_PACKETS_TO_RELEVANT_PLAYERS.invoke(chunkHolder, packet, false);
        }
    }

}
