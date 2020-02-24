package com.bgsoftware.wildtools.nms;

import com.bgsoftware.wildtools.hooks.PaperHook;
import com.bgsoftware.wildtools.objects.WMaterial;
import net.minecraft.server.v1_13_R2.Block;
import net.minecraft.server.v1_13_R2.BlockBeetroot;
import net.minecraft.server.v1_13_R2.BlockCarrots;
import net.minecraft.server.v1_13_R2.BlockCocoa;
import net.minecraft.server.v1_13_R2.BlockCrops;
import net.minecraft.server.v1_13_R2.BlockNetherWart;
import net.minecraft.server.v1_13_R2.BlockPosition;
import net.minecraft.server.v1_13_R2.BlockPotatoes;
import net.minecraft.server.v1_13_R2.Blocks;
import net.minecraft.server.v1_13_R2.Chunk;
import net.minecraft.server.v1_13_R2.ContainerAnvil;
import net.minecraft.server.v1_13_R2.EnchantmentManager;
import net.minecraft.server.v1_13_R2.Enchantments;
import net.minecraft.server.v1_13_R2.EntityItem;
import net.minecraft.server.v1_13_R2.EntityLiving;
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.IBlockData;
import net.minecraft.server.v1_13_R2.Item;
import net.minecraft.server.v1_13_R2.ItemStack;
import net.minecraft.server.v1_13_R2.Items;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import net.minecraft.server.v1_13_R2.NBTTagList;
import net.minecraft.server.v1_13_R2.NBTTagString;
import net.minecraft.server.v1_13_R2.PacketPlayOutCollect;
import net.minecraft.server.v1_13_R2.PacketPlayOutMultiBlockChange;
import net.minecraft.server.v1_13_R2.PlayerInventory;
import net.minecraft.server.v1_13_R2.TileEntityShulkerBox;
import net.minecraft.server.v1_13_R2.World;

import net.minecraft.server.v1_13_R2.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.craftbukkit.v1_13_R2.CraftChunk;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.block.CraftBlock;
import org.bukkit.craftbukkit.v1_13_R2.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftItem;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftInventoryPlayer;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Ageable;
import org.bukkit.CropState;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_13_R2.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings({"unused", "ConstantConditions"})
public final class NMSAdapter_v1_13_R2 implements NMSAdapter {

    @Override
    public String getVersion() {
        return "v1_13_R2";
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
        World world = player.world;
        IBlockData blockData = world.getType(blockPosition);
        Block block = blockData.getBlock();

        if(!player.hasBlock(blockData) || player.playerInteractManager.isCreative())
            return drops;

        if(world.getTileEntity(blockPosition) instanceof TileEntityShulkerBox){
            TileEntityShulkerBox tileEntityShulkerBox = (TileEntityShulkerBox) world.getTileEntity(blockPosition);
            if (!tileEntityShulkerBox.s() && tileEntityShulkerBox.G()) {
                ItemStack itemStack = new ItemStack(block);
                itemStack.getOrCreateTag().set("BlockEntityTag", tileEntityShulkerBox.g(new NBTTagCompound()));
                if (tileEntityShulkerBox.hasCustomName()) {
                    itemStack.a(tileEntityShulkerBox.getCustomName());
                    tileEntityShulkerBox.setCustomName(null);
                }

                drops.add(CraftItemStack.asBukkitCopy(itemStack));
            }
            return drops;
        }

        // Has silk touch enchant
        if ((block.j() && !block.isTileEntity()) && (silkTouch || EnchantmentManager.a(Enchantments.SILK_TOUCH, player) > 0)) {
            Item item = block.getItem();
            ItemStack itemStack = new ItemStack(item);
            drops.add(CraftItemStack.asBukkitCopy(itemStack));
        }

        else if (!world.isClientSide) {
            int fortuneLevel = EnchantmentManager.a(Enchantments.LOOT_BONUS_BLOCKS, player),
                    dropCount = block.getDropCount(blockData, fortuneLevel, world, blockPosition, world.random);

            Item item = block.getDropType(blockData, world, blockPosition, fortuneLevel).getItem();
            if (item != null) {
                drops.add(CraftItemStack.asBukkitCopy(new ItemStack(item, dropCount)));
            }
        }

        return drops;
    }

    @Override
    public List<org.bukkit.inventory.ItemStack> getCropDrops(Player pl, org.bukkit.block.Block bl) {
        List<org.bukkit.inventory.ItemStack> drops = new ArrayList<>();

        EntityPlayer player = ((CraftPlayer) pl).getHandle();
        BlockPosition blockPosition = new BlockPosition(bl.getX(), bl.getY(), bl.getZ());
        World world = player.world;
        Block block = world.getType(blockPosition).getBlock();

        int age = ((Ageable) bl.getBlockData()).getAge();

        int fortuneLevel = EnchantmentManager.a(Enchantments.LOOT_BONUS_BLOCKS, player);

        if(block instanceof BlockCrops){
            int growthAge = 7;

            if(block instanceof BlockBeetroot)
                growthAge = 3;

            if (age >= growthAge) {
                //Give the item itself to the player
                if(block instanceof BlockCarrots) {
                    drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.CARROT)));
                }else if(block instanceof BlockPotatoes){
                    drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.POTATO)));
                }else if(block instanceof BlockBeetroot) {
                    drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.BEETROOT)));
                }else{
                    drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.WHEAT)));
                }
                //Give the "seeds" to the player. I run -1 iteration for "replant"
                for(int i = 0; i < (fortuneLevel + 3) - 1; i++) {
                    if (world.random.nextInt(2 * growthAge) <= age) {
                        if(block instanceof BlockCarrots) {
                            drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.CARROT)));
                        }else if(block instanceof BlockPotatoes){
                            drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.POTATO)));
                            if (world.random.nextInt(50) == 0) {
                                drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.POISONOUS_POTATO)));
                            }
                        }else if(block instanceof BlockBeetroot) {
                            drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.BEETROOT_SEEDS)));
                        }else{
                            drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.WHEAT_SEEDS)));
                        }
                    }
                }
            }
        }
        else if(block instanceof BlockCocoa){
            if(age >= 2) {
                drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.COCOA_BEANS, 3)));
            }
        }
        else if(block instanceof BlockNetherWart){
            if (age >= 3) {
                int amount = 2 + world.random.nextInt(3);
                if (fortuneLevel > 0) {
                    amount += world.random.nextInt(fortuneLevel + 1);
                }
                drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.NETHER_WART, amount)));
            }
        }

        return drops;
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

        nbtTagList.add(new NBTTagString(taskId.toString()));
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
            for(int i = 0; i < currentTaskIds.size(); i++){
                NBTTagString nbtTagString = (NBTTagString) currentTaskIds.c(i);
                if(!nbtTagString.asString().equals(taskId.toString())) {
                    nbtTagList.add(nbtTagString);
                }
            }
        }
        else{
            if(tag.hasKeyOfType("task-id", 8)) {
                NBTTagString tagString = (NBTTagString) tag.get("task-id");
                if(!tagString.asString().equals(taskId.toString()))
                    nbtTagList.add(tagString);
            }
        }

        tag.set("task-id", nbtTagList);

        nmsStack.setTag(tag);

        return CraftItemStack.asCraftMirror(nmsStack);
    }

    @Override
    public org.bukkit.inventory.ItemStack getItemInHand(Player player) {
        ItemStack itemStack = ((CraftInventoryPlayer) player.getInventory()).getInventory().getItemInHand();
        return CraftItemStack.asBukkitCopy(itemStack);
    }

    @Override
    public void setItemInHand(Player player, org.bukkit.inventory.ItemStack itemStack) {
        PlayerInventory playerInventory = ((CraftInventoryPlayer) player.getInventory()).getInventory();
        playerInventory.setItem(playerInventory.itemInHandIndex, CraftItemStack.asNMSCopy(itemStack));
    }

    @Override
    public boolean isFullyGrown(org.bukkit.block.Block block) {
        if(block.getType() == Material.CACTUS || block.getType() == WMaterial.SUGAR_CANE.parseMaterial() ||
            block.getType() == Material.PUMPKIN || block.getType() == WMaterial.MELON.parseMaterial())
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
    public void copyBlock(org.bukkit.block.Block from, org.bukkit.block.Block to) {
        CraftBlock fromBlock = (CraftBlock) from, toBlock = (CraftBlock) to;
        toBlock.setType(fromBlock.getType());
        toBlock.setBlockData(fromBlock.getBlockData(), true);
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
        chunk.setType(blockPosition, Block.getByCombinedId(combinedId), false);
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
    public org.bukkit.enchantments.Enchantment getGlowEnchant() {
        return new org.bukkit.enchantments.Enchantment(NamespacedKey.minecraft("glowing_enchant")) {
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
    public void playPickupAnimation(LivingEntity livingEntity, org.bukkit.entity.Item item) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        EntityItem entityItem = (EntityItem) ((CraftItem) item).getHandle();
        ((WorldServer) entityLiving.world).getTracker().a(entityItem, new PacketPlayOutCollect(entityItem.getId(), entityLiving.getId(), item.getItemStack().getAmount()));
    }

    @Override
    public org.bukkit.inventory.ItemStack[] parseChoice(Recipe recipe, org.bukkit.inventory.ItemStack itemStack) {
        List<org.bukkit.inventory.ItemStack> ingredients = Collections.singletonList(itemStack);
        List<RecipeChoice> recipeChoices = new ArrayList<>();

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
        container.levelCost = expCost;
    }

    @Override
    public int getExpCost(InventoryView inventoryView) {
        return ((ContainerAnvil) ((CraftInventoryView) inventoryView).getHandle()).levelCost;
    }

    @Override
    public String getRenameText(InventoryView inventoryView) {
        return ((ContainerAnvil) ((CraftInventoryView) inventoryView).getHandle()).renameText;
    }

}
