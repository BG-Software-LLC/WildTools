package com.bgsoftware.wildtools.nms;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.hooks.PaperHook;
import com.bgsoftware.wildtools.objects.WMaterial;
import com.bgsoftware.wildtools.recipes.AdvancedShapedRecipe;
import com.bgsoftware.wildtools.utils.items.ToolTaskManager;
import net.minecraft.server.v1_16_R1.Block;
import net.minecraft.server.v1_16_R1.BlockPosition;
import net.minecraft.server.v1_16_R1.Blocks;
import net.minecraft.server.v1_16_R1.Chunk;
import net.minecraft.server.v1_16_R1.ContainerAnvil;
import net.minecraft.server.v1_16_R1.EntityItem;
import net.minecraft.server.v1_16_R1.EntityLiving;
import net.minecraft.server.v1_16_R1.EntityPlayer;
import net.minecraft.server.v1_16_R1.IBlockData;
import net.minecraft.server.v1_16_R1.ItemStack;
import net.minecraft.server.v1_16_R1.Items;
import net.minecraft.server.v1_16_R1.NBTBase;
import net.minecraft.server.v1_16_R1.NBTTagCompound;
import net.minecraft.server.v1_16_R1.NBTTagList;
import net.minecraft.server.v1_16_R1.NBTTagString;
import net.minecraft.server.v1_16_R1.PacketPlayOutCollect;
import net.minecraft.server.v1_16_R1.PacketPlayOutMultiBlockChange;
import net.minecraft.server.v1_16_R1.TileEntity;
import net.minecraft.server.v1_16_R1.World;
import net.minecraft.server.v1_16_R1.WorldServer;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.CropState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.WorldBorder;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_16_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_16_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_16_R1.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
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
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "ConstantConditions"})
public final class NMSAdapter_v1_16_R1 implements NMSAdapter {

    @Override
    public String getVersion() {
        return "v1_16_R1";
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
        World world = ((CraftWorld) block.getWorld()).getHandle();
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        IBlockData blockData = world.getType(blockPosition);
        return blockData.getBlock().getExpDrop(blockData, world, blockPosition, entityPlayer.getItemInMainHand());
    }

    @Override
    public int getTag(org.bukkit.inventory.ItemStack is, String key, int def) {
        ItemStack nmsStack = CraftItemStack.asNMSCopy(is);

        if(nmsStack == null)
            return def;

        NBTTagCompound tag = new NBTTagCompound();

        if(nmsStack.hasTag()){
            tag = nmsStack.getTag();
        }

        if(tag.hasKey(key)){
            return tag.getInt(key);
        }

        return def;
    }

    @Override
    public org.bukkit.inventory.ItemStack setTag(org.bukkit.inventory.ItemStack is, String key, int value) {
        ItemStack nmsStack = CraftItemStack.asNMSCopy(is);
        NBTTagCompound tag = new NBTTagCompound();

        if(nmsStack.hasTag()){
            tag = nmsStack.getTag();
        }

        tag.setInt(key, value);

        nmsStack.setTag(tag);

        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    @Override
    public String getTag(org.bukkit.inventory.ItemStack is, String key, String def) {
        ItemStack nmsStack = CraftItemStack.asNMSCopy(is);

        if(nmsStack == null)
            return def;

        NBTTagCompound tag = new NBTTagCompound();

        if(nmsStack.hasTag()){
            tag = nmsStack.getTag();
        }

        if(tag.hasKey(key)){
            return tag.getString(key);
        }

        return def;
    }

    @Override
    public org.bukkit.inventory.ItemStack setTag(org.bukkit.inventory.ItemStack is, String key, String value) {
        ItemStack nmsStack = CraftItemStack.asNMSCopy(is);
        NBTTagCompound tag = new NBTTagCompound();

        if(nmsStack.hasTag()){
            tag = nmsStack.getTag();
        }

        tag.setString(key, value);

        nmsStack.setTag(tag);

        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    @Override
    public List<UUID> getTasks(org.bukkit.inventory.ItemStack itemStack) {
        ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsStack.hasTag() ? nmsStack.getTag() : new NBTTagCompound();
        List<UUID> taskIds = new ArrayList<>();

        if(tag.hasKeyOfType("task-id", 8)){
            try {
                taskIds.add(UUID.fromString(tag.getString("task-id")));
            }catch(Exception ignored){}
        }
        else if(tag.hasKeyOfType("task-id", 9)){
            NBTTagList nbtTagList = tag.getList("task-id", 8);
            for(int i = 0; i < nbtTagList.size(); i++){
                try {
                    taskIds.add(UUID.fromString(nbtTagList.getString(i)));
                }catch(Exception ignored){}
            }
        }

        return taskIds;
    }

    @Override
    public org.bukkit.inventory.ItemStack addTask(org.bukkit.inventory.ItemStack itemStack, UUID taskId) {
        ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsStack.hasTag() ? nmsStack.getTag() : new NBTTagCompound();
        NBTTagList nbtTagList;

        if(tag.hasKeyOfType("task-id", 9)){
            nbtTagList = tag.getList("task-id", 8);
        }
        else{
            nbtTagList = new NBTTagList();
            if(tag.hasKeyOfType("task-id", 8))
                nbtTagList.add(tag.get("task-id"));
        }

        nbtTagList.add(NBTTagString.a(taskId.toString()));
        tag.set("task-id", nbtTagList);

        nmsStack.setTag(tag);

        return CraftItemStack.asCraftMirror(nmsStack);
    }

    @Override
    public org.bukkit.inventory.ItemStack removeTask(org.bukkit.inventory.ItemStack itemStack, UUID taskId) {
        ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsStack.hasTag() ? nmsStack.getTag() : new NBTTagCompound();
        NBTTagList nbtTagList = new NBTTagList();

        if(tag.hasKeyOfType("task-id", 9)){
            NBTTagList currentTaskIds = tag.getList("task-id", 8);
            for (NBTBase currentTaskId : currentTaskIds) {
                NBTTagString nbtTagString = (NBTTagString) currentTaskId;
                if (!nbtTagString.asString().equals(taskId.toString()) && ToolTaskManager.isTaskActive(UUID.fromString(nbtTagString.asString()))) {
                    nbtTagList.add(nbtTagString);
                }
            }
        }
        else{
            if(tag.hasKeyOfType("task-id", 8)) {
                NBTTagString tagString = (NBTTagString) tag.get("task-id");
                if(!tagString.asString().equals(taskId.toString()) && ToolTaskManager.isTaskActive(UUID.fromString(tagString.asString())))
                    nbtTagList.add(tagString);
            }
        }

        tag.set("task-id", nbtTagList);

        nmsStack.setTag(tag);

        return CraftItemStack.asCraftMirror(nmsStack);
    }

    @Override
    public org.bukkit.inventory.ItemStack clearTasks(org.bukkit.inventory.ItemStack itemStack) {
        ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsStack.getTag();

        if(tag != null)
            tag.remove("task-id");

        return CraftItemStack.asCraftMirror(nmsStack);
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
    public void setItemInHand(Player player, org.bukkit.inventory.ItemStack itemStack) {
        player.getInventory().setItemInMainHand(itemStack);
    }

    @Override
    public void setItemInHand(Player player, org.bukkit.inventory.ItemStack itemStack, Event e) {
        boolean offHand = false;

        if(e instanceof PlayerInteractEvent) {
            offHand = ((PlayerInteractEvent) e).getHand() == EquipmentSlot.OFF_HAND;
        }else if(e instanceof PlayerInteractEntityEvent){
            offHand = ((PlayerInteractEntityEvent) e).getHand() == EquipmentSlot.OFF_HAND;
        }

        if(offHand){
            player.getInventory().setItemInOffHand(itemStack);
        }
        else{
            setItemInHand(player, itemStack);
        }
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
        int blocksAmount = blocksList.size();
        short[] values = new short[blocksAmount];

        Location firstLocation = null;

        int counter = 0;
        for(Location location : blocksList) {
            if(firstLocation == null)
                firstLocation = location;

            values[counter++] = (short) ((location.getBlockX() & 15) << 12 | (location.getBlockZ() & 15) << 8 | location.getBlockY());
        }

        PacketPlayOutMultiBlockChange multiBlockChange = new PacketPlayOutMultiBlockChange(blocksAmount, values, chunk);

        for(Entity player : bukkitChunk.getWorld().getNearbyEntities(firstLocation, 60, 200, 60, entity -> entity instanceof Player))
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(multiBlockChange);
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
    public Object getBlockData(Material type, byte data) {
        return CraftBlockData.fromData(CraftMagicNumbers.getBlock(type, data));
    }

    @Override
    public BlockPlaceEvent getFakePlaceEvent(Player player, Location location, org.bukkit.block.Block copyBlock) {
        FakeCraftBlock fakeBlock = FakeCraftBlock.at(location, copyBlock.getType());
        org.bukkit.block.Block original = location.getBlock();
        return new BlockPlaceEvent(
                fakeBlock,
                original.getState(),
                fakeBlock.getRelative(BlockFace.DOWN),
                new org.bukkit.inventory.ItemStack(copyBlock.getType()),
                player,
                true,
                EquipmentSlot.HAND
        );
    }

    @Override
    public void playPickupAnimation(LivingEntity livingEntity, Item item) {
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
    public Collection<Entity> getNearbyEntities(Location location, double range) {
        return location.getWorld().getNearbyEntities(location, range, range, range);
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

        private Material blockType;

        FakeCraftBlock(WorldServer worldServer, BlockPosition blockPosition, Material material){
            super(worldServer, blockPosition);
            this.blockType = material;
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

        static FakeCraftBlock at(Location location, Material type){
            WorldServer worldServer = ((CraftWorld) location.getWorld()).getHandle();
            BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            return new FakeCraftBlock(worldServer, blockPosition, type);
        }

    }

}
