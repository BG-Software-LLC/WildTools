package com.bgsoftware.wildtools.nms;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.hooks.PaperHook;
import com.bgsoftware.wildtools.objects.WMaterial;
import com.bgsoftware.wildtools.recipes.AdvancedShapedRecipe;
import com.bgsoftware.wildtools.utils.Executor;
import com.bgsoftware.wildtools.utils.items.ToolItemStack;
import net.minecraft.server.v1_15_R1.Block;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.Blocks;
import net.minecraft.server.v1_15_R1.Chunk;
import net.minecraft.server.v1_15_R1.ContainerAnvil;
import net.minecraft.server.v1_15_R1.EntityItem;
import net.minecraft.server.v1_15_R1.EntityLiving;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.EnumItemSlot;
import net.minecraft.server.v1_15_R1.IBlockData;
import net.minecraft.server.v1_15_R1.Item;
import net.minecraft.server.v1_15_R1.ItemStack;
import net.minecraft.server.v1_15_R1.Items;
import net.minecraft.server.v1_15_R1.LightEngineThreaded;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.Packet;
import net.minecraft.server.v1_15_R1.PacketPlayOutCollect;
import net.minecraft.server.v1_15_R1.PacketPlayOutLightUpdate;
import net.minecraft.server.v1_15_R1.PacketPlayOutMultiBlockChange;
import net.minecraft.server.v1_15_R1.PlayerChunkMap;
import net.minecraft.server.v1_15_R1.PlayerMap;
import net.minecraft.server.v1_15_R1.StatisticList;
import net.minecraft.server.v1_15_R1.TileEntity;
import net.minecraft.server.v1_15_R1.World;
import net.minecraft.server.v1_15_R1.WorldServer;
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
import org.bukkit.craftbukkit.v1_15_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_15_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "ConstantConditions"})
public final class NMSAdapter_v1_15_R1 implements NMSAdapter {

    private static final ReflectField<PlayerMap> PLAYER_MAP_FIELD = new ReflectField<>(PlayerChunkMap.class, PlayerMap.class, "playerMap");
    private static final ReflectField<ItemStack> ITEM_STACK_HANDLE = new ReflectField<>(CraftItemStack.class, ItemStack.class, "handle");

    @Override
    public String getVersion() {
        return "v1_15_R1";
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
        WorldServer worldServer = player.playerInteractManager.world;
        IBlockData blockData = worldServer.getType(blockPosition);
        Block block = blockData.getBlock();
        ItemStack itemStack = player.getItemInMainHand();
        itemStack = itemStack.isEmpty() ? ItemStack.a : itemStack.cloneItemStack();
        TileEntity tileEntity = worldServer.getTileEntity(blockPosition);

        return Block.getDrops(blockData, worldServer, blockPosition, tileEntity, player, itemStack.isEmpty() ? ItemStack.a : itemStack.cloneItemStack())
                .stream().map(CraftItemStack::asBukkitCopy).collect(Collectors.toList());
    }

    @Override
    public List<org.bukkit.inventory.ItemStack> getCropDrops(Player pl, org.bukkit.block.Block bl) {
        //1.14 has a nice method to get drops of blocks, so we can safely call the getBlockDrops method.
        return getBlockDrops(pl, bl, false);
    }

    @Override
    public int getExpFromBlock(org.bukkit.block.Block block, Player player) {
        World world = ((CraftWorld) block.getWorld()).getHandle();
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

        entityPlayer.broadcastItemBreak(EnumItemSlot.MAINHAND);
        Item item = nmsStack.getItem();

        if (nmsStack.getCount() == 1)
            CraftEventFactory.callPlayerItemBreakEvent(entityPlayer, nmsStack);

        nmsStack.subtract(1);

        entityPlayer.b(StatisticList.ITEM_BROKEN.b(item));

        nmsStack.setDamage(0);
    }

    @Override
    public Object[] createSyncedItem(org.bukkit.inventory.ItemStack other) {
        CraftItemStack craftItemStack;
        ItemStack handle;
        if(other instanceof CraftItemStack){
            craftItemStack = (CraftItemStack) other;
            handle = ITEM_STACK_HANDLE.get(other);
        }else{
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
        Chunk chunk = world.getChunkAt(location.getChunk().getX(), location.getChunk().getZ());
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        if(combinedId == 0)
            world.a(null, 2001, blockPosition, Block.getCombinedId(world.getType(blockPosition)));

        chunk.setType(blockPosition, Block.getByCombinedId(combinedId), true);
        if(PaperHook.isAntiXRayAvailable())
            PaperHook.handleLeftClickBlockMethod(world, blockPosition);
    }

    @Override
    public void refreshChunk(org.bukkit.Chunk bukkitChunk, Set<Location> blocksList) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
        WorldServer worldServer = (WorldServer) chunk.getWorld();
        int blocksAmount = blocksList.size();
        short[] values = new short[blocksAmount];

        int counter = 0;
        for(Location location : blocksList) {
            values[counter++] = (short) ((location.getBlockX() & 15) << 12 | (location.getBlockZ() & 15) << 8 | location.getBlockY());
        }

        sendPacketToRelevantPlayers((WorldServer) chunk.world, chunk.getPos().x, chunk.getPos().z,
                new PacketPlayOutMultiBlockChange(blocksAmount, values, chunk));

        LightEngineThreaded lightEngine = worldServer.getChunkProvider().getLightEngine();
        List<CompletableFuture<Void>> lightQueueFutures = new ArrayList<>();

        for (Location location : blocksList) {
            BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
            lightEngine.a(blockPosition);
        }

        Executor.sync(() -> sendPacketToRelevantPlayers(worldServer, chunk.getPos().x, chunk.getPos().z,
                        new PacketPlayOutLightUpdate(chunk.getPos(), lightEngine)),
                2L);
    }

    @Override
    public int getCombinedId(Location location) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        return Block.getCombinedId(world.getType(blockPosition));
    }

    @Override
    public int getFarmlandId() {
        return Block.getCombinedId(Blocks.FARMLAND.getBlockData());
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
        ((WorldServer) entityLiving.world).getChunkProvider().broadcast(entityItem, new PacketPlayOutCollect(entityItem.getId(), entityLiving.getId(), item.getItemStack().getAmount()));
    }

    @Override
    public boolean isAxeType(Material material) {
        float destroySpeed = Items.DIAMOND_AXE.getDestroySpeed(
                new ItemStack(Items.DIAMOND_AXE), ((CraftBlockData) material.createBlockData()).getState());
        return destroySpeed == 8.0F;
    }

    @Override
    public boolean isShovelType(Material material) {
        float destroySpeed = Items.DIAMOND_SHOVEL.getDestroySpeed(
                new ItemStack(Items.DIAMOND_SHOVEL), ((CraftBlockData) material.createBlockData()).getState());
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
        container.levelCost.set(expCost);
    }

    @Override
    public int getExpCost(InventoryView inventoryView) {
        return ((ContainerAnvil) ((CraftInventoryView) inventoryView).getHandle()).levelCost.get();
    }

    @Override
    public String getRenameText(InventoryView inventoryView) {
        return ((ContainerAnvil) ((CraftInventoryView) inventoryView).getHandle()).renameText;
    }

    @Override
    public AdvancedShapedRecipe createRecipe(String toolName, org.bukkit.inventory.ItemStack result) {
        return new AdvancedRecipeClassImpl(toolName, result);
    }

    @Override
    public Object getDroppedItem(org.bukkit.inventory.ItemStack itemStack, Location location) {
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        EntityItem entityitem = new EntityItem(world, location.getX(), location.getY(), location.getZ(), CraftItemStack.asNMSCopy(itemStack));
        entityitem.pickupDelay = 10;
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
                entityItem.world.addEntity(entityItem);
            }
        });
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
                entityItem.pickupDelay = Math.max(entityItem.pickupDelay, otherEntity.pickupDelay);
                entityItem.age = Math.min(entityItem.age, otherEntity.age);
                if (itemOfOtherEntity.isEmpty()) {
                    otherEntity.die();
                }
            }
        }

        return entityItem.dead;
    }

    private static void mergeItems(EntityItem entityItem, ItemStack itemStack, ItemStack otherItem) {
        ItemStack leftOver = EntityItem.a(itemStack, otherItem, 64);
        if (!leftOver.isEmpty()) {
            entityItem.setItemStack(leftOver);
        }
    }

    private static void sendPacketToRelevantPlayers(WorldServer worldServer, int chunkX, int chunkZ, Packet<?> packet){
        PlayerChunkMap playerChunkMap = worldServer.getChunkProvider().playerChunkMap;
        PLAYER_MAP_FIELD.get(playerChunkMap).a(1)
                .forEach(entityPlayer -> entityPlayer.playerConnection.sendPacket(packet));
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
