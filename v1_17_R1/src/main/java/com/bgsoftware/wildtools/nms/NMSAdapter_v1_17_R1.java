package com.bgsoftware.wildtools.nms;

import ca.spottedleaf.starlight.light.StarLightInterface;
import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.objects.WMaterial;
import com.bgsoftware.wildtools.recipes.AdvancedShapedRecipe;
import com.bgsoftware.wildtools.utils.Executor;
import com.bgsoftware.wildtools.utils.items.ToolItemStack;
import io.papermc.paper.enchantments.EnchantmentRarity;
import it.unimi.dsi.fastutil.shorts.ShortArraySet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.SectionPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutCollect;
import net.minecraft.network.protocol.game.PacketPlayOutLightUpdate;
import net.minecraft.network.protocol.game.PacketPlayOutMultiBlockChange;
import net.minecraft.server.level.ChunkProviderServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.LightEngineThreaded;
import net.minecraft.server.level.PlayerChunkMap;
import net.minecraft.server.level.PlayerMap;
import net.minecraft.server.level.WorldServer;
import net.minecraft.stats.StatisticList;
import net.minecraft.util.thread.ThreadedMailbox;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.inventory.ContainerAnvil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.ChunkSection;
import org.apache.commons.lang.Validate;
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
import org.bukkit.craftbukkit.v1_17_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_17_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "ConstantConditions"})
public final class NMSAdapter_v1_17_R1 implements NMSAdapter {

    private static final ReflectField<PlayerMap> PLAYER_MAP_FIELD = new ReflectField<>(PlayerChunkMap.class, PlayerMap.class, "F");
    private static final ReflectField<ItemStack> ITEM_STACK_HANDLE = new ReflectField<>(CraftItemStack.class, ItemStack.class, "handle");
    private static final ReflectField<Object> STAR_LIGHT_INTERFACE = new ReflectField<>(LightEngineThreaded.class, Object.class, "theLightEngine");
    private static final ReflectField<ThreadedMailbox<Runnable>> LIGHT_ENGINE_EXECUTOR = new ReflectField<>(LightEngineThreaded.class, ThreadedMailbox.class, "e");
    private static final ReflectMethod<Void> UPDATE_NEARBY_BLOCKS = new ReflectMethod<>(
            "com.destroystokyo.paper.antixray.ChunkPacketBlockControllerAntiXray",
            "updateNearbyBlocks", World.class, BlockPosition.class);

    private static Constructor<?> MULTI_BLOCK_CHANGE_CONSTRUCTOR;
    private static Class<?> SHORT_ARRAY_SET_CLASS = null;

    static {
        try {
            MULTI_BLOCK_CHANGE_CONSTRUCTOR = Arrays.stream(PacketPlayOutMultiBlockChange.class.getConstructors())
                    .filter(constructor -> constructor.getParameterCount() == 4).findFirst().orElse(null);
            SHORT_ARRAY_SET_CLASS = Class.forName("org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.shorts.ShortArraySet");
            Class<?> shortSetClass = Class.forName("org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.shorts.ShortSet");
        }catch (Exception ignored){}
    }

    @Override
    public String getVersion() {
        return "v1_17_R1";
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
        WorldServer worldServer = player.getWorldServer();
        IBlockData blockData = worldServer.getType(blockPosition);
        Block block = blockData.getBlock();
        ItemStack itemStack = player.getItemInMainHand();
        itemStack = itemStack.isEmpty() ? ItemStack.b : itemStack.cloneItemStack();
        TileEntity tileEntity = worldServer.getTileEntity(blockPosition);

        return Block.getDrops(blockData, worldServer, blockPosition, tileEntity, player, itemStack.isEmpty() ? ItemStack.b : itemStack.cloneItemStack())
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
        IBlockData blockData = world.getType(blockPosition);
        return blockData.getBlock().getExpDrop(blockData, world, blockPosition, entityPlayer.getItemInMainHand());
    }

    @Override
    public int getTag(ToolItemStack toolItemStack, String key, int def) {
        ItemStack nmsStack = (ItemStack) toolItemStack.getNMSItem();
        NBTTagCompound tagCompound = nmsStack.getTag();
        return tagCompound == null || !tagCompound.hasKey(key) ? def : tagCompound.getInt(key);
    }

    @Override
    public void setTag(ToolItemStack toolItemStack, String key, int value) {
        ItemStack nmsStack = (ItemStack) toolItemStack.getNMSItem();
        NBTTagCompound tagCompound = nmsStack.getOrCreateTag();
        tagCompound.setInt(key, value);
    }

    @Override
    public String getTag(ToolItemStack toolItemStack, String key, String def) {
        ItemStack nmsStack = (ItemStack) toolItemStack.getNMSItem();
        NBTTagCompound tagCompound = nmsStack.getTag();
        return tagCompound == null || !tagCompound.hasKey(key) ? def : tagCompound.getString(key);
    }

    @Override
    public void setTag(ToolItemStack toolItemStack, String key, String value) {
        ItemStack nmsStack = (ItemStack) toolItemStack.getNMSItem();
        NBTTagCompound tagCompound = nmsStack.getOrCreateTag();
        tagCompound.setString(key, value);
    }

    @Override
    public void clearTasks(ToolItemStack toolItemStack) {
        ItemStack nmsStack = (ItemStack) toolItemStack.getNMSItem();
        NBTTagCompound tagCompound = nmsStack.getTag();
        if(tagCompound != null)
            tagCompound.remove("task-id");
    }

    @Override
    public void breakTool(ToolItemStack toolItemStack, Player player) {
        ItemStack nmsStack = (ItemStack) toolItemStack.getNMSItem();
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        entityPlayer.broadcastItemBreak(EnumItemSlot.a);
        Item item = nmsStack.getItem();

        if (nmsStack.getCount() == 1)
            CraftEventFactory.callPlayerItemBreakEvent(entityPlayer, nmsStack);

        nmsStack.subtract(1);

        entityPlayer.b(StatisticList.d.b(item));

        nmsStack.setDamage(0);
    }

    @Override
    public Object[] createSyncedItem(org.bukkit.inventory.ItemStack other) {
        CraftItemStack craftItemStack;
        ItemStack handle;
        if(other instanceof CraftItemStack){
            craftItemStack = (CraftItemStack) other;
            handle = ITEM_STACK_HANDLE.get(other);
        } else{
            handle = CraftItemStack.asNMSCopy(other);
            craftItemStack = CraftItemStack.asCraftMirror(handle);
        }

        return new Object[] {craftItemStack, handle};
    }

    @Override
    public org.bukkit.inventory.ItemStack getItemInHand(Player player) {
        return player.getInventory().getItemInMainHand();
    }

    @Override
    public org.bukkit.inventory.ItemStack getItemInHand(Player player, Event e) {
        boolean offHand = false;

        if(e instanceof PlayerInteractEvent) {
            offHand = ((PlayerInteractEvent) e).getHand() == EquipmentSlot.OFF_HAND;
        }else if(e instanceof PlayerInteractEntityEvent){
            offHand = ((PlayerInteractEntityEvent) e).getHand() == EquipmentSlot.OFF_HAND;
        }

        return offHand ? player.getInventory().getItemInOffHand() : getItemInHand(player);
    }

    @Override
    public boolean isFullyGrown(org.bukkit.block.Block block) {
        if(block.getType() == Material.CACTUS || block.getType() == WMaterial.SUGAR_CANE.parseMaterial() ||
            block.getType() == Material.PUMPKIN || block.getType() == WMaterial.MELON.parseMaterial() ||
                block.getType().name().equals("BAMBOO"))
            return true;
        CraftBlock craftBlock = (CraftBlock) block;
        BlockData blockData = craftBlock.getBlockData();
        return ((Ageable) blockData).getAge() == ((Ageable) blockData).getMaximumAge();
    }

    @Override
    public void setCropState(org.bukkit.block.Block block, CropState cropState) {
        if(block.getType() == Material.CHORUS_PLANT){
            block.setType(Material.CHORUS_FLOWER);
        }
        else {
            CraftBlock craftBlock = (CraftBlock) block;
            BlockData blockData = craftBlock.getBlockData();
            if(blockData instanceof Ageable) {
                ((Ageable) blockData).setAge(cropState.ordinal());
                craftBlock.setBlockData(blockData, true);
            }
            else{
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
        Chunk chunk = world.getChunkAtWorldCoords(blockPosition);

        if(combinedId == 0)
            world.a(null, 2001, blockPosition, Block.getCombinedId(world.getType(blockPosition)));

        chunk.setType(blockPosition, Block.getByCombinedId(combinedId), true);

        if(UPDATE_NEARBY_BLOCKS.isValid() && world.paperConfig.antiXray)
            UPDATE_NEARBY_BLOCKS.invoke(world.chunkPacketBlockController, world, blockPosition);
    }

    @Override
    public void refreshChunk(org.bukkit.Chunk bukkitChunk, Set<Location> blocksList) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
        Map<Integer, Set<Short>> blocks = new HashMap<>();
        WorldServer worldServer = (WorldServer) chunk.getWorld();

        ChunkProviderServer chunkProviderServer = worldServer.getChunkProvider();

        for(Location location : blocksList){
            BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            chunkProviderServer.flagDirty(blockPosition);
        }

        if (STAR_LIGHT_INTERFACE.isValid()) {
            LightEngineThreaded lightEngineThreaded = (LightEngineThreaded) worldServer.k_();
            StarLightInterface starLightInterface = (StarLightInterface) STAR_LIGHT_INTERFACE.get(lightEngineThreaded);
            LIGHT_ENGINE_EXECUTOR.get(lightEngineThreaded).a(() ->
                    starLightInterface.relightChunks(Collections.singleton(chunk.getPos()), chunkPos ->
                            chunkProviderServer.h.execute(() -> sendPacketToRelevantPlayers(worldServer, chunkPos.b, chunkPos.c,
                                    new PacketPlayOutLightUpdate(chunkPos, lightEngineThreaded, null, null, true))
                            ), null));
        } else {
            LightEngineThreaded lightEngine = worldServer.getChunkProvider().getLightEngine();
            List<CompletableFuture<Void>> lightQueueFutures = new ArrayList<>();

            for (Location location : blocksList) {
                BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
                lightEngine.a(blockPosition);
            }

            Executor.sync(() -> sendPacketToRelevantPlayers(worldServer, chunk.getPos().b, chunk.getPos().c,
                    new PacketPlayOutLightUpdate(chunk.getPos(), lightEngine, null, null, true)),
                    2L);
        }
    }

    @Override
    public int getCombinedId(Location location) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        return Block.getCombinedId(world.getType(blockPosition));
    }

    @Override
    public int getFarmlandId() {
        return Block.getCombinedId(Blocks.ce.getBlockData());
    }

    @Override
    public void setCombinedId(Location location, int combinedId) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        world.setTypeAndData(blockPosition, Block.getByCombinedId(combinedId), 18);
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
        ((WorldServer) entityLiving.getWorld()).getChunkProvider()
                .broadcast(entityItem, new PacketPlayOutCollect(entityItem.getId(), entityLiving.getId(), item.getItemStack().getAmount()));
    }

    @Override
    public boolean isAxeType(Material material) {
        float destroySpeed = Items.mU.getDestroySpeed(
                new ItemStack(Items.mU), ((CraftBlockData) material.createBlockData()).getState());
        return destroySpeed == 8.0F;
    }

    @Override
    public boolean isShovelType(Material material) {
        float destroySpeed = Items.mS.getDestroySpeed(
                new ItemStack(Items.mS), ((CraftBlockData) material.createBlockData()).getState());
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

        if(!recipeChoices.isEmpty()) {
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
        container.w.set(expCost);
    }

    @Override
    public int getExpCost(InventoryView inventoryView) {
        return ((ContainerAnvil) ((CraftInventoryView) inventoryView).getHandle()).w.get();
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

        for(Object entityItem : droppedItemsRaw){
            if(canMerge((EntityItem) entityItem)) {
                for (Object otherEntityItem : droppedItemsRaw) {
                    if (entityItem != otherEntityItem && canMerge((EntityItem) otherEntityItem)) {
                        if(mergeEntityItems((EntityItem) entityItem, (EntityItem) otherEntityItem))
                            break;
                    }
                }
            }
        }

        droppedItemsRaw.forEach(droppedItemObject -> {
            EntityItem entityItem = (EntityItem) droppedItemObject;
            if(entityItem.isAlive()){
                entityItem.getWorld().addEntity(entityItem);
            }
        });
    }

    @Override
    public int getMinHeight(org.bukkit.World world) {
        return world.getMinHeight();
    }

    private static boolean canMerge(EntityItem entityItem){
        ItemStack itemStack = entityItem.getItemStack();
        return !itemStack.isEmpty() && itemStack.getCount() < itemStack.getMaxStackSize();
    }

    private static boolean mergeEntityItems(EntityItem entityItem, EntityItem otherEntity){
        ItemStack itemOfEntity = entityItem.getItemStack();
        ItemStack itemOfOtherEntity = otherEntity.getItemStack();
        if (EntityItem.a(itemOfEntity, itemOfOtherEntity)) {
            if (!CraftEventFactory.callItemMergeEvent(otherEntity, entityItem).isCancelled()) {
                mergeItems(entityItem, itemOfEntity, itemOfOtherEntity);
                entityItem.ap = Math.max(entityItem.ap, otherEntity.ap);
                entityItem.ao = Math.min(entityItem.ao, otherEntity.ao);
                if (itemOfOtherEntity.isEmpty()) {
                    otherEntity.die();
                }
            }
        }

        return entityItem.isRemoved();
    }

    private static void mergeItems(EntityItem entityItem, ItemStack itemStack, ItemStack otherItem) {
        ItemStack leftOver = EntityItem.a(itemStack, otherItem, 64);
        if (!leftOver.isEmpty()) {
            entityItem.setItemStack(leftOver);
        }
    }

    @SuppressWarnings("all")
    private static Set<Short> createShortSet(){
        if(SHORT_ARRAY_SET_CLASS == null)
            return new ShortArraySet();

        try{
            return (Set<Short>) SHORT_ARRAY_SET_CLASS.newInstance();
        }catch (Throwable ex){
            ex.printStackTrace();
            return null;
        }
    }

    private static PacketPlayOutMultiBlockChange createMultiBlockChangePacket(SectionPosition sectionPosition,
                                                                              Set<Short> shortSet,
                                                                              ChunkSection chunkSection){
        if(MULTI_BLOCK_CHANGE_CONSTRUCTOR == null){
            return new PacketPlayOutMultiBlockChange(
                    sectionPosition,
                    (ShortSet) shortSet,
                    chunkSection,
                    true
            );
        }

        try{
            return (PacketPlayOutMultiBlockChange) MULTI_BLOCK_CHANGE_CONSTRUCTOR.newInstance(
                    sectionPosition, shortSet, chunkSection, true);
        }catch (Throwable ex){
            ex.printStackTrace();
            return null;
        }
    }

    private static void sendPacketToRelevantPlayers(WorldServer worldServer, int chunkX, int chunkZ, Packet<?> packet){
        PlayerChunkMap playerChunkMap = worldServer.getChunkProvider().a;
        PLAYER_MAP_FIELD.get(playerChunkMap).a(1)
                .forEach(entityPlayer -> entityPlayer.b.sendPacket(packet));
    }

    @SuppressWarnings("NullableProblems")
    public static class AdvancedRecipeClassImpl extends ShapedRecipe implements AdvancedShapedRecipe {

        private static Field ingredientsField;

        static {
            try{
                ingredientsField = ShapedRecipe.class.getDeclaredField("ingredients");
                ingredientsField.setAccessible(true);
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }

        private Map<Character, RecipeChoice> ingredients;

        public AdvancedRecipeClassImpl(String toolName, org.bukkit.inventory.ItemStack result){
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
            Validate.isTrue(this.ingredients.containsKey(key), "Symbol does not appear in the shape: ", key);
            this.ingredients.put(key, new RecipeChoice.MaterialChoice(itemStack.getType()));
            return this;
        }

        @Override
        public ShapedRecipe toRecipe() {
            return this;
        }

        private void updateIngredients(){
            try{
                //noinspection unchecked
                ingredients = (Map<Character, RecipeChoice>) ingredientsField.get(this);
            }catch(Exception ex){
                throw new RuntimeException(ex);
            }
        }

    }

    @SuppressWarnings("NullableProblems")
    private static class FakeCraftBlock extends CraftBlock{

        private final BlockState originalState;
        private Material blockType;

        FakeCraftBlock(WorldServer worldServer, BlockPosition blockPosition, Material material, BlockState originalState){
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

        static FakeCraftBlock at(Location location, Material type, BlockState originalState){
            WorldServer worldServer = ((CraftWorld) location.getWorld()).getHandle();
            BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            return new FakeCraftBlock(worldServer, blockPosition, type, originalState);
        }

    }

}
