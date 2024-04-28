package com.bgsoftware.wildtools.nms.v1_7_R4;

import com.bgsoftware.wildtools.nms.NMSWorld;
import com.bgsoftware.wildtools.utils.math.Vector3;
import com.bgsoftware.wildtools.utils.world.WorldEditSession;
import net.minecraft.server.v1_7_R4.Block;
import net.minecraft.server.v1_7_R4.BlockCarrots;
import net.minecraft.server.v1_7_R4.BlockCocoa;
import net.minecraft.server.v1_7_R4.BlockCrops;
import net.minecraft.server.v1_7_R4.BlockNetherWart;
import net.minecraft.server.v1_7_R4.BlockPotatoes;
import net.minecraft.server.v1_7_R4.Chunk;
import net.minecraft.server.v1_7_R4.EnchantmentManager;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.GameProfileSerializer;
import net.minecraft.server.v1_7_R4.Item;
import net.minecraft.server.v1_7_R4.ItemStack;
import net.minecraft.server.v1_7_R4.Items;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.PacketPlayOutMultiBlockChange;
import net.minecraft.server.v1_7_R4.TileEntity;
import net.minecraft.server.v1_7_R4.TileEntitySkull;
import net.minecraft.server.v1_7_R4.World;
import net.minecraft.server.v1_7_R4.WorldServer;
import org.bukkit.CropState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NetherWartsState;
import org.bukkit.craftbukkit.v1_7_R4.CraftChunk;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.block.CraftBlock;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.material.CocoaPlant;
import org.bukkit.material.Crops;
import org.bukkit.material.NetherWarts;

import java.util.LinkedList;
import java.util.List;

public class NMSWorldImpl implements NMSWorld {

    @Override
    public List<org.bukkit.inventory.ItemStack> getBlockDrops(Player bukkitPlayer, org.bukkit.block.Block bukkitBlock, boolean silkTouch) {
        List<org.bukkit.inventory.ItemStack> drops = new LinkedList<>();

        EntityPlayer entityPlayer = ((CraftPlayer) bukkitPlayer).getHandle();
        World world = entityPlayer.world;
        Block block = world.getType(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());

        //Checks if player cannot break the block or player in creative mode
        if (!entityPlayer.a(block) || entityPlayer.playerInteractManager.isCreative())
            return drops;

        int fortuneLevel = EnchantmentManager.getBonusBlockLootEnchantmentLevel(entityPlayer);

        if (block instanceof BlockCrops) {
            int age = ((CraftBlock) bukkitBlock).getData();

            if (age >= 7) {
                //Give the item itself to the player
                if (block instanceof BlockCarrots) {
                    drops.add(new org.bukkit.inventory.ItemStack(Material.CARROT_ITEM, 1));
                } else if (block instanceof BlockPotatoes) {
                    drops.add(new org.bukkit.inventory.ItemStack(Material.POTATO_ITEM, 1));
                } else {
                    drops.add(new org.bukkit.inventory.ItemStack(Material.WHEAT, 1));
                }

                //Give the "seeds" to the player. I run -1 iteration for "replant"
                for (int i = 0; i < (fortuneLevel + 3) - 1; i++) {
                    if (world.random.nextInt(15) <= age) {
                        if (block instanceof BlockCarrots) {
                            drops.add(new org.bukkit.inventory.ItemStack(Material.CARROT_ITEM, 1));
                        } else if (block instanceof BlockPotatoes) {
                            drops.add(new org.bukkit.inventory.ItemStack(Material.POTATO_ITEM, 1));
                            if (world.random.nextInt(50) == 0)
                                drops.add(new org.bukkit.inventory.ItemStack(Material.POISONOUS_POTATO, 1));
                        } else {
                            drops.add(new org.bukkit.inventory.ItemStack(Material.SEEDS, 1));
                        }
                    }
                }
            }

            return drops;
        }

        if (block instanceof BlockCocoa) {
            int age = ((CraftBlock) bukkitBlock).getData();

            if (age >= 2)
                drops.add(new org.bukkit.inventory.ItemStack(Material.INK_SACK, 3, (short) 3));

            return drops;
        }

        if (block instanceof BlockNetherWart) {
            int age = ((CraftBlock) bukkitBlock).getData();

            if (age >= 3) {
                int amount = 2 + world.random.nextInt(3);
                if (fortuneLevel > 0) {
                    amount += world.random.nextInt(fortuneLevel + 1);
                }
                drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.NETHER_STALK, amount)));
            }

            return drops;
        }

        TileEntity tileEntity = world.getTileEntity(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());

        if (tileEntity instanceof TileEntitySkull) {
            TileEntitySkull tileEntitySkull = (TileEntitySkull) tileEntity;
            ItemStack itemStack = new ItemStack(Items.SKULL, 1, tileEntitySkull.getSkullType());

            if (tileEntitySkull.getSkullType() == 3) {
                NBTTagCompound nbtTagCompound = itemStack.getTag();

                if (nbtTagCompound == null) {
                    nbtTagCompound = new NBTTagCompound();
                    itemStack.setTag(nbtTagCompound);
                }

                NBTTagCompound skullOwnerTag = new NBTTagCompound();
                GameProfileSerializer.serialize(skullOwnerTag, tileEntitySkull.getGameProfile());
                nbtTagCompound.set("SkullOwner", skullOwnerTag);
                itemStack.setTag(nbtTagCompound);
            }

            drops.add(CraftItemStack.asCraftMirror(itemStack));
            return drops;
        }

        //Checks if player has silk touch
        if ((block.d() && !block.isTileEntity()) && (silkTouch || EnchantmentManager.hasSilkTouchEnchantment(entityPlayer))) {
            Item item = Item.getItemOf(block);
            if (item != null) {
                int data = item.n() ? Block.getId(block) : 0;
                drops.add(CraftItemStack.asCraftMirror(new ItemStack(item, 1, data)));
            }
        } else {
            int dropCount = block.getDropCount(fortuneLevel, world.random);
            int blockId = Block.getId(block);
            Item item = block.getDropType(blockId, world.random, fortuneLevel);
            if (item != null)
                drops.add(CraftItemStack.asCraftMirror(new ItemStack(item, dropCount, block.getDropData(blockId))));
        }

        return drops;
    }

    @Override
    public int getExpFromBlock(org.bukkit.block.Block block, Player player) {
        World world = ((CraftWorld) block.getWorld()).getHandle();
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        return world.getType(block.getX(), block.getY(), block.getZ()).getExpDrop(world,
                block.getData(), EnchantmentManager.getBonusBlockLootEnchantmentLevel(entityPlayer));
    }

    @Override
    public boolean isFullyGrown(org.bukkit.block.Block block) {
        if (block.getState().getData() instanceof Crops)
            return ((Crops) block.getState().getData()).getState() == CropState.RIPE;
        else if (block.getState().getData() instanceof CocoaPlant)
            return ((CocoaPlant) block.getState().getData()).getSize() == CocoaPlant.CocoaPlantSize.LARGE;
        else if (block.getState().getData() instanceof NetherWarts)
            return ((NetherWarts) block.getState().getData()).getState() == NetherWartsState.RIPE;
        else if (block.getType() == Material.CARROT || block.getType() == Material.POTATO)
            return ((CraftBlock) block).getData() == CropState.RIPE.getData();

        return true;
    }

    @Override
    public void setCropState(org.bukkit.block.Block block, CropState cropState) {
        if (block.getType() == Material.COCOA) {
            CocoaPlant cocoaPlant = (CocoaPlant) block.getState().getData();
            switch (cropState) {
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
        } else if (block.getType() == Material.MELON_BLOCK || block.getType() == Material.PUMPKIN) {
            block.setType(Material.AIR);
        } else {
            ((CraftBlock) block).setData(cropState.getData());
        }
    }

    @Override
    public void setBlockFast(org.bukkit.World bukkitWorld, Vector3 location, int combinedId, boolean sendUpdate) {
        World world = ((CraftWorld) bukkitWorld).getHandle();
        int x = location.getX();
        int y = location.getY();
        int z = location.getZ();

        if (sendUpdate) {
            world.setTypeAndData(x, y, z, Block.getById(combinedId), 2, 18);
            return;
        }

        Chunk chunk = world.getChunkAt(x >> 4, z >> 4);

        if (combinedId == 0)
            world.a(null, 2001, x, y, z, Block.getId(world.getType(x, y, z)) + (world.getData(x, y, z) << 12));

        chunk.a(x & 0x0f, y, z & 0x0f, Block.getById(combinedId), 2);
    }

    @Override
    public void refreshChunk(org.bukkit.Chunk bukkitChunk, List<WorldEditSession.BlockData> blocksList) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
        int blocksAmount = blocksList.size();
        short[] values = new short[blocksAmount];

        Vector3 firstLocation = null;

        int counter = 0;
        for (WorldEditSession.BlockData blockData : blocksList) {
            if (firstLocation == null)
                firstLocation = blockData.location;

            values[counter++] = (short) ((blockData.location.getX() & 15) << 12 | (blockData.location.getZ() & 15) << 8 | blockData.location.getY());
        }

        NMSUtils.sendPacketToRelevantPlayers((WorldServer) chunk.world, chunk.locX, chunk.locZ,
                new PacketPlayOutMultiBlockChange(blocksAmount, values, chunk));
    }

    @Override
    public int getCombinedId(org.bukkit.block.Block block) {
        World world = ((CraftWorld) block.getWorld()).getHandle();
        return Block.getId(world.getType(block.getX(), block.getY(), block.getZ()));
    }

    @Override
    public boolean isOutsideWorldBorder(Location location) {
        return false;
    }

    @Override
    public int getMinHeight(org.bukkit.World world) {
        return 0;
    }

}
