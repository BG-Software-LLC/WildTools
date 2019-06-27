package com.bgsoftware.wildtools.nms;

import net.minecraft.server.v1_9_R2.Block;
import net.minecraft.server.v1_9_R2.BlockBeetroot;
import net.minecraft.server.v1_9_R2.BlockCarrots;
import net.minecraft.server.v1_9_R2.BlockCocoa;
import net.minecraft.server.v1_9_R2.BlockCrops;
import net.minecraft.server.v1_9_R2.BlockNetherWart;
import net.minecraft.server.v1_9_R2.BlockPosition;
import net.minecraft.server.v1_9_R2.BlockPotatoes;
import net.minecraft.server.v1_9_R2.EnchantmentManager;
import net.minecraft.server.v1_9_R2.Enchantments;
import net.minecraft.server.v1_9_R2.EntityPlayer;
import net.minecraft.server.v1_9_R2.EnumColor;
import net.minecraft.server.v1_9_R2.IBlockData;
import net.minecraft.server.v1_9_R2.Item;
import net.minecraft.server.v1_9_R2.ItemStack;
import net.minecraft.server.v1_9_R2.Items;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.PlayerInventory;
import net.minecraft.server.v1_9_R2.World;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NetherWartsState;
import org.bukkit.WorldBorder;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.block.CraftBlock;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftInventoryPlayer;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;

import org.bukkit.CropState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;

import org.bukkit.material.CocoaPlant;
import org.bukkit.material.Crops;
import org.bukkit.material.NetherWarts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings({"unused", "deprecation", "ConstantConditions"})
public final class NMSAdapter_v1_9_R2 implements NMSAdapter {

    @Override
    public String getVersion() {
        return "v1_9_R2";
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

        // Has silk touch enchant
        if (block.n() && !block.isTileEntity() && (silkTouch || EnchantmentManager.a(Enchantments.SILK_TOUCH, player) > 0)) {
            int i = 0;
            Item item = Item.getItemOf(block);
            if (item != null && item.k()) {
                i = block.toLegacyData(blockData);
            }

            ItemStack itemStack = new ItemStack(item, 1, i);
            drops.add(CraftItemStack.asBukkitCopy(itemStack));
        }

        else if (!world.isClientSide) {
            int fortuneLevel = EnchantmentManager.a(Enchantments.LOOT_BONUS_BLOCKS, player),
                    dropCount = block.getDropCount(fortuneLevel, world.random);

            for(int i = 0; i < dropCount; i++) {
                if (world.random.nextFloat() < 1.0F) {
                    Item item = block.getDropType(blockData, world.random, fortuneLevel);
                    if (item != null) {
                        ItemStack itemStack = new ItemStack(item, 1, block.getDropData(blockData));
                        drops.add(CraftItemStack.asBukkitCopy(itemStack));
                    }
                }
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

        int age = ((CraftBlock) bl).getData();
        int fortuneLevel = EnchantmentManager.a(Enchantments.LOOT_BONUS_BLOCKS, player);

        if(block instanceof BlockCrops){
            int growthAge = 7;

            if(block instanceof BlockBeetroot)
                growthAge = 3;

            if (age >= growthAge) {
                //Give the item itself to the player
                if(block instanceof BlockCarrots) {
                    drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.CARROT, 1, 0)));
                }else if(block instanceof BlockPotatoes){
                    drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.POTATO, 1, 0)));
                }else if(block instanceof BlockBeetroot) {
                    drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.BEETROOT, 1, 0)));
                }else{
                    drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.WHEAT, 1, 0)));
                }
                //Give the "seeds" to the player. I run -1 iteration for "replant"
                for(int i = 0; i < (fortuneLevel + 3) - 1; i++) {
                    if (world.random.nextInt(2 * growthAge) <= age) {
                        if(block instanceof BlockCarrots) {
                            drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.CARROT, 1, 0)));
                        }else if(block instanceof BlockPotatoes){
                            drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.POTATO, 1, 0)));
                            if (world.random.nextInt(50) == 0) {
                                drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.POISONOUS_POTATO, 1, 0)));
                            }
                        }else if(block instanceof BlockBeetroot) {
                            drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.BEETROOT_SEEDS, 1, 0)));
                        }else{
                            drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.WHEAT_SEEDS, 1, 0)));
                        }
                    }
                }
            }
        }
        else if(block instanceof BlockCocoa){
            if(age >= 2) {
                drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.DYE, 3, EnumColor.BROWN.getInvColorIndex())));
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
    public Collection<Player> getOnlinePlayers() {
        return new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    @Override
    public void setAirFast(org.bukkit.block.Block block) {
        World world = ((CraftWorld) block.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        world.setTypeAndData(blockPosition, Block.getByCombinedId(0), 18);
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
        WorldBorder worldBorder = location.getWorld().getWorldBorder();
        int radius = (int) worldBorder.getSize() / 2;
        return location.getBlockX() > (worldBorder.getCenter().getBlockX() + radius) || location.getBlockX() < (worldBorder.getCenter().getBlockX() - radius) ||
                location.getBlockZ() > (worldBorder.getCenter().getBlockZ() + radius) || location.getBlockZ() < (worldBorder.getCenter().getBlockZ() - radius);
    }

}
