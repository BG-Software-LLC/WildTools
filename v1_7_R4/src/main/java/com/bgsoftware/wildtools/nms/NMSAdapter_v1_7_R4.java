package com.bgsoftware.wildtools.nms;

import com.bgsoftware.wildtools.utils.items.ToolTaskManager;
import net.minecraft.server.v1_7_R4.AxisAlignedBB;
import net.minecraft.server.v1_7_R4.Block;
import net.minecraft.server.v1_7_R4.BlockCarrots;
import net.minecraft.server.v1_7_R4.BlockCocoa;
import net.minecraft.server.v1_7_R4.BlockCrops;
import net.minecraft.server.v1_7_R4.BlockNetherWart;
import net.minecraft.server.v1_7_R4.BlockPotatoes;
import net.minecraft.server.v1_7_R4.Blocks;
import net.minecraft.server.v1_7_R4.Chunk;
import net.minecraft.server.v1_7_R4.EnchantmentManager;
import net.minecraft.server.v1_7_R4.EntityItem;
import net.minecraft.server.v1_7_R4.EntityLiving;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.Item;
import net.minecraft.server.v1_7_R4.ItemStack;
import net.minecraft.server.v1_7_R4.Items;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.NBTTagList;
import net.minecraft.server.v1_7_R4.NBTTagString;
import net.minecraft.server.v1_7_R4.PacketPlayOutCollect;
import net.minecraft.server.v1_7_R4.PacketPlayOutMultiBlockChange;
import net.minecraft.server.v1_7_R4.PlayerInventory;
import net.minecraft.server.v1_7_R4.World;

import net.minecraft.server.v1_7_R4.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NetherWartsState;
import org.bukkit.craftbukkit.v1_7_R4.CraftChunk;
import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.block.CraftBlock;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftItem;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftInventoryPlayer;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;

import org.bukkit.CropState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.material.CocoaPlant;
import org.bukkit.material.Crops;
import org.bukkit.material.NetherWarts;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings({"unused", "deprecation"})
public final class NMSAdapter_v1_7_R4 implements NMSAdapter {

    @Override
    public String getVersion() {
        return "v1_7_R4";
    }

    @Override
    public List<org.bukkit.inventory.ItemStack> getBlockDrops(Player pl, org.bukkit.block.Block bl, boolean silkTouch) {
        List<org.bukkit.inventory.ItemStack> drops = new ArrayList<>();

        EntityPlayer player = ((CraftPlayer) pl).getHandle();
        World world = player.world;
        Block block = world.getType(bl.getX(), bl.getY(), bl.getZ());

        //Checks if player cannot break the block or player in creative mode
        if(!player.a(block) || player.playerInteractManager.isCreative())
            return drops;

        //Checks if player has silk touch
        if ((block.d() && !block.isTileEntity()) && (silkTouch || EnchantmentManager.hasSilkTouchEnchantment(player))) {
            int data = 0;
            Item item = Item.getItemOf(block);
            //Checks if item not null and something else?
            if (item != null && item.n()) {
                data = Block.getId(block);
            }
            //Adds item to drops
            drops.add(CraftItemStack.asBukkitCopy(new ItemStack(item, 1, data)));
        }

        else{
            int fortuneLevel = getItemInHand(pl).getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS),
                    dropCount = block.getDropCount(fortuneLevel, world.random),
                    blockId = Block.getId(block);

            Item item = block.getDropType(blockId, world.random, fortuneLevel);
            if (item != null) {
                drops.add(CraftItemStack.asBukkitCopy(new ItemStack(item, dropCount, block.getDropData(blockId))));
            }
        }

        return drops;
    }

    @Override
    public List<org.bukkit.inventory.ItemStack> getCropDrops(Player pl, org.bukkit.block.Block bl) {
        List<org.bukkit.inventory.ItemStack> drops = new ArrayList<>();

        EntityPlayer player = ((CraftPlayer) pl).getHandle();
        World world = player.world;
        Block block = world.getType(bl.getX(), bl.getY(), bl.getZ());

        int age = ((CraftBlock) bl).getData();
        int fortuneLevel = getItemInHand(pl).getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);

        if(block instanceof BlockCrops){
            if (age >= 7) {
                //Give the item itself to the player
                if(block instanceof BlockCarrots) {
                    drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.CARROT, 1, 0)));
                }else if(block instanceof BlockPotatoes){
                    drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.POTATO, 1, 0)));
                }else{
                    drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.WHEAT, 1, 0)));
                }
                //Give the "seeds" to the player. I run -1 iteration for "replant"
                for(int i = 0; i < (fortuneLevel + 3) - 1; i++) {
                    if (world.random.nextInt(15) <= age) {
                        if(block instanceof BlockCarrots) {
                            drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.CARROT, 1, 0)));
                        }else if(block instanceof BlockPotatoes){
                            drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.POTATO, 1, 0)));
                            if (world.random.nextInt(50) == 0) {
                                drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.POTATO_POISON, 1, 0)));
                            }
                        }
                        else{
                            drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.SEEDS, 1, 0)));
                        }
                    }
                }
            }
        }
        else if(block instanceof BlockCocoa){
            if(age >= 2) {
                drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.INK_SACK, 3, 3)));
            }
        }
        else if(block instanceof BlockNetherWart){
            if (age >= 3) {
                int amount = 2 + world.random.nextInt(3);
                if (fortuneLevel > 0) {
                    amount += world.random.nextInt(fortuneLevel + 1);
                }
                drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.NETHER_STALK, amount)));
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
                String tagString = currentTaskIds.getString(i);
                if(!tagString.equals(taskId.toString()) && ToolTaskManager.isTaskActive(UUID.fromString(tagString))) {
                    nbtTagList.add(new NBTTagString(tagString));
                }
            }
        }
        else{
            if(tag.hasKeyOfType("task-id", 8)) {
                NBTTagString tagString = (NBTTagString) tag.get("task-id");
                if(!tagString.a_().equals(taskId.toString()) && ToolTaskManager.isTaskActive(UUID.fromString(tagString.a_())))
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
        if(block.getState().getData() instanceof Crops)
            return ((Crops) block.getState().getData()).getState() == CropState.RIPE;
        else if(block.getState().getData() instanceof CocoaPlant)
            return ((CocoaPlant) block.getState().getData()).getSize() == CocoaPlant.CocoaPlantSize.LARGE;
        else if(block.getState().getData() instanceof NetherWarts)
            return ((NetherWarts) block.getState().getData()).getState() == NetherWartsState.RIPE;
        else if(block.getType() == Material.CARROT || block.getType() == Material.POTATO)
            return ((CraftBlock) block).getData() == CropState.RIPE.getData();

        return true;
    }

    @Override
    public void setCropState(org.bukkit.block.Block block, CropState cropState) {
        if(block.getType() == Material.COCOA){
            CocoaPlant cocoaPlant = (CocoaPlant) block.getState().getData();
            switch (cropState){
                case SEEDED:
                case GERMINATED:
                case VERY_SMALL:
                case SMALL:
                    cocoaPlant.setSize(CocoaPlant.CocoaPlantSize.SMALL);
                    break;
                case MEDIUM:
                    cocoaPlant.setSize(CocoaPlant.CocoaPlantSize.MEDIUM);
                    break;
                case TALL:
                case VERY_TALL:
                case RIPE:
                    cocoaPlant.setSize(CocoaPlant.CocoaPlantSize.LARGE);
                    break;
            }
            ((CraftBlock) block).setData(cocoaPlant.getData());
        }else if(block.getType() == Material.MELON_BLOCK || block.getType() == Material.PUMPKIN){
            block.setType(Material.AIR);
        }else {
            ((CraftBlock) block).setData(cropState.getData());
        }
    }

    @Override
    public void copyBlock(org.bukkit.block.Block from, org.bukkit.block.Block to) {
        CraftBlock fromBlock = (CraftBlock) from, toBlock = (CraftBlock) to;
        toBlock.setType(fromBlock.getType());
        toBlock.setData(fromBlock.getData());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<Player> getOnlinePlayers() {
        CraftServer craftServer = (CraftServer) Bukkit.getServer();
        List<CraftPlayer> players = new ArrayList<>();
        try{
            Field playersField = craftServer.getClass().getDeclaredField("playerView");
            playersField.setAccessible(true);
            players = (List<CraftPlayer>) playersField.get(craftServer);
            playersField.setAccessible(false);
        }catch(Exception ignored){}
        return Collections.unmodifiableCollection(players);
    }

    @Override
    public void setBlockFast(Location location, int combinedId) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        Chunk chunk = world.getChunkAt(location.getChunk().getX(), location.getChunk().getZ());
        chunk.a(location.getBlockX() & 0x0f, location.getBlockY(), location.getBlockZ() & 0x0f, Block.getById(combinedId), 2);
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

        assert firstLocation != null;
        AxisAlignedBB bb = AxisAlignedBB.a(firstLocation.getX() - 60, firstLocation.getY() - 200, firstLocation.getZ() - 60,
                firstLocation.getX() + 60, firstLocation.getY() + 200, firstLocation.getZ() + 60);

        //noinspection unchecked
        for(Entity entity : (List<Entity>) ((CraftWorld) bukkitChunk.getWorld()).getHandle().getEntities(null, bb)){
            if(entity instanceof EntityPlayer)
                ((EntityPlayer) entity).playerConnection.sendPacket(multiBlockChange);
        }
    }

    @Override
    public int getCombinedId(Location location) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        return Block.getId(world.getType(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }

    @Override
    public int getFarmlandId() {
        return Block.getId(Blocks.SOIL);
    }

    @Override
    public void setCombinedId(Location location, int combinedId) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        world.setTypeAndData(location.getBlockX(), location.getBlockY(), location.getBlockZ(), Block.getById(combinedId), 2,18);
    }

    @Override
    public Enchantment getGlowEnchant() {
        return new Enchantment(101) {
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
        };
    }

    @Override
    public boolean isOutsideWorldborder(Location location) {
        return false;
    }

    @Override
    public Object getBlockData(Material type, byte data) {
        throw new UnsupportedOperationException("That's not possible to create block data in 1.7");
    }

    @Override
    public void playPickupAnimation(LivingEntity livingEntity, org.bukkit.entity.Item item) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        EntityItem entityItem = (EntityItem) ((CraftItem) item).getHandle();
        ((WorldServer) entityLiving.world).getTracker().a(entityItem, new PacketPlayOutCollect(entityItem.getId(), entityLiving.getId()));
    }

}
