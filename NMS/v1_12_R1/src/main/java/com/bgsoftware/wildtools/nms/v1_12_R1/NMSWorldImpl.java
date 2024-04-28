package com.bgsoftware.wildtools.nms.v1_12_R1;

import com.bgsoftware.wildtools.nms.NMSWorld;
import com.bgsoftware.wildtools.utils.math.Vector3;
import com.bgsoftware.wildtools.utils.world.WorldEditSession;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.BlockBeetroot;
import net.minecraft.server.v1_12_R1.BlockCarrots;
import net.minecraft.server.v1_12_R1.BlockCocoa;
import net.minecraft.server.v1_12_R1.BlockCrops;
import net.minecraft.server.v1_12_R1.BlockNetherWart;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.BlockPotatoes;
import net.minecraft.server.v1_12_R1.Chunk;
import net.minecraft.server.v1_12_R1.EnchantmentManager;
import net.minecraft.server.v1_12_R1.Enchantments;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.EnumColor;
import net.minecraft.server.v1_12_R1.GameProfileSerializer;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.Item;
import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.Items;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.PacketPlayOutMultiBlockChange;
import net.minecraft.server.v1_12_R1.TileEntity;
import net.minecraft.server.v1_12_R1.TileEntityShulkerBox;
import net.minecraft.server.v1_12_R1.TileEntitySkull;
import net.minecraft.server.v1_12_R1.World;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.CropState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NetherWartsState;
import org.bukkit.WorldBorder;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.material.CocoaPlant;
import org.bukkit.material.Crops;
import org.bukkit.material.NetherWarts;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class NMSWorldImpl implements NMSWorld {

    @Override
    public List<org.bukkit.inventory.ItemStack> getBlockDrops(Player bukkitPlayer, org.bukkit.block.Block bukkitBlock, boolean silkTouch) {
        List<org.bukkit.inventory.ItemStack> drops = new LinkedList<>();

        EntityPlayer entityPlayer = ((CraftPlayer) bukkitPlayer).getHandle();
        BlockPosition blockPosition = new BlockPosition(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());
        World world = entityPlayer.world;
        IBlockData blockData = world.getType(blockPosition);
        Block block = world.getType(blockPosition).getBlock();

        int fortuneLevel = EnchantmentManager.a(Enchantments.LOOT_BONUS_BLOCKS, entityPlayer);

        if (block instanceof BlockCrops) {
            int growthAge = block instanceof BlockBeetroot ? 3 : 7;
            int age = ((CraftBlock) bukkitBlock).getData();

            if (age >= growthAge) {
                //Give the item itself to the player
                if (block instanceof BlockCarrots) {
                    drops.add(new org.bukkit.inventory.ItemStack(Material.CARROT_ITEM, 1));
                } else if (block instanceof BlockPotatoes) {
                    drops.add(new org.bukkit.inventory.ItemStack(Material.POTATO_ITEM, 1));
                } else if (block instanceof BlockBeetroot) {
                    drops.add(new org.bukkit.inventory.ItemStack(Material.BEETROOT, 1));
                } else {
                    drops.add(new org.bukkit.inventory.ItemStack(Material.WHEAT, 1));
                }

                //Give the "seeds" to the player. I run -1 iteration for "replant"
                for (int i = 0; i < (fortuneLevel + 3) - 1; i++) {
                    if (world.random.nextInt(2 * growthAge) <= age) {
                        if (block instanceof BlockCarrots) {
                            drops.add(new org.bukkit.inventory.ItemStack(Material.CARROT_ITEM, 1));
                        } else if (block instanceof BlockPotatoes) {
                            drops.add(new org.bukkit.inventory.ItemStack(Material.POTATO_ITEM, 1));
                            if (world.random.nextInt(50) == 0)
                                drops.add(new org.bukkit.inventory.ItemStack(Material.POISONOUS_POTATO, 1));
                        } else if (block instanceof BlockBeetroot) {
                            drops.add(new org.bukkit.inventory.ItemStack(Material.BEETROOT_SEEDS, 1));
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
                drops.add(new org.bukkit.inventory.ItemStack(Material.INK_SACK, 3, (short) EnumColor.BROWN.getInvColorIndex()));

            return drops;
        }

        if (block instanceof BlockNetherWart) {
            int age = ((CraftBlock) bukkitBlock).getData();

            if (age >= 3) {
                int amount = 2 + world.random.nextInt(3);
                if (fortuneLevel > 0) {
                    amount += world.random.nextInt(fortuneLevel + 1);
                }
                drops.add(new org.bukkit.inventory.ItemStack(Material.NETHER_STALK, amount));
            }

            return drops;
        }

        if (!entityPlayer.hasBlock(blockData) || entityPlayer.playerInteractManager.isCreative())
            return drops;

        TileEntity tileEntity = world.getTileEntity(blockPosition);

        if (tileEntity instanceof TileEntityShulkerBox) {
            TileEntityShulkerBox tileEntityShulkerBox = (TileEntityShulkerBox) tileEntity;
            if (!tileEntityShulkerBox.r() && tileEntityShulkerBox.F()) {
                ItemStack itemStack = new ItemStack(Item.getItemOf(block));
                NBTTagCompound nbtTagCompound = new NBTTagCompound();
                nbtTagCompound.set("BlockEntityTag", tileEntityShulkerBox.f(new NBTTagCompound()));
                itemStack.setTag(nbtTagCompound);
                if (tileEntityShulkerBox.hasCustomName()) {
                    itemStack.g(tileEntityShulkerBox.getName());
                    tileEntityShulkerBox.setCustomName("");
                }

                drops.add(CraftItemStack.asCraftMirror(itemStack));
            }
            return drops;
        }

        if (tileEntity instanceof TileEntitySkull) {
            TileEntitySkull tileEntitySkull = (TileEntitySkull) tileEntity;
            ItemStack itemStack = new ItemStack(Items.SKULL, 1, tileEntitySkull.getSkullType());

            if (tileEntitySkull.getSkullType() == 3) {
                Optional.ofNullable(tileEntitySkull.getGameProfile()).ifPresent(gameProfile -> {
                    NBTTagCompound nbtTagCompound = itemStack.getTag();

                    if (nbtTagCompound == null) {
                        nbtTagCompound = new NBTTagCompound();
                        itemStack.setTag(nbtTagCompound);
                    }

                    NBTTagCompound skullOwnerTag = new NBTTagCompound();
                    GameProfileSerializer.serialize(skullOwnerTag, gameProfile);
                    nbtTagCompound.set("SkullOwner", skullOwnerTag);
                    itemStack.setTag(nbtTagCompound);
                });
            }

            drops.add(CraftItemStack.asCraftMirror(itemStack));
            return drops;
        }

        // Has silk touch enchant
        if ((block.r() && !block.isTileEntity()) && (silkTouch || EnchantmentManager.a(Enchantments.SILK_TOUCH, entityPlayer) > 0)) {
            Item item = Item.getItemOf(block);
            if (item != null) {
                int data = item.k() ? block.toLegacyData(blockData) : 0;
                drops.add(CraftItemStack.asCraftMirror(new ItemStack(item, 1, data)));
            }
        } else if (!world.isClientSide) {
            int dropCount = block.getDropCount(fortuneLevel, world.random);
            Item item = block.getDropType(blockData, world.random, fortuneLevel);
            if (item != null)
                drops.add(CraftItemStack.asCraftMirror(new ItemStack(item, dropCount, block.getDropData(blockData))));
        }

        return drops;
    }

    @Override
    public int getExpFromBlock(org.bukkit.block.Block block, Player player) {
        World world = ((CraftWorld) block.getWorld()).getHandle();
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        IBlockData blockData = world.getType(new BlockPosition(block.getX(), block.getY(), block.getZ()));
        return blockData.getBlock().getExpDrop(world, blockData,
                EnchantmentManager.getEnchantmentLevel(Enchantments.LOOT_BONUS_BLOCKS, entityPlayer.getItemInMainHand()));
    }

    @Override
    public boolean isFullyGrown(org.bukkit.block.Block block) {
        if (block.getState().getData() instanceof Crops)
            return ((Crops) block.getState().getData()).getState() == CropState.RIPE;
        else if (block.getState().getData() instanceof CocoaPlant)
            return ((CocoaPlant) block.getState().getData()).getSize() == CocoaPlant.CocoaPlantSize.LARGE;
        else if (block.getState().getData() instanceof NetherWarts)
            return ((NetherWarts) block.getState().getData()).getState() == NetherWartsState.RIPE;

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
        } else if (block.getType() == Material.CHORUS_PLANT) {
            block.setType(Material.CHORUS_FLOWER);
        } else if (block.getType() == Material.MELON_BLOCK || block.getType() == Material.PUMPKIN) {
            block.setType(Material.AIR);
        } else {
            ((CraftBlock) block).setData(cropState.getData());
        }
    }

    @Override
    public void setBlockFast(org.bukkit.World bukkitWorld, Vector3 location, int combinedId, boolean sendUpdate) {
        World world = ((CraftWorld) bukkitWorld).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());

        if (sendUpdate) {
            world.setTypeAndData(blockPosition, Block.getByCombinedId(combinedId), 18);
            return;
        }

        Chunk chunk = world.getChunkAt(location.getX() >> 4, location.getZ() >> 4);

        if (combinedId == 0)
            world.a(null, 2001, blockPosition, Block.getCombinedId(world.getType(blockPosition)));

        chunk.a(blockPosition, Block.getByCombinedId(combinedId));

        try {
            // Paper method to update anti-xray blocks
            world.chunkPacketBlockController.updateNearbyBlocks(world, blockPosition);
        } catch (Throwable ignored) {
        }
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
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        return Block.getCombinedId(world.getType(blockPosition));
    }

    @Override
    public boolean isOutsideWorldBorder(Location location) {
        WorldBorder worldBorder = location.getWorld().getWorldBorder();
        int radius = (int) worldBorder.getSize() / 2;
        return location.getBlockX() > (worldBorder.getCenter().getBlockX() + radius) ||
                location.getBlockX() < (worldBorder.getCenter().getBlockX() - radius) ||
                location.getBlockZ() > (worldBorder.getCenter().getBlockZ() + radius) ||
                location.getBlockZ() < (worldBorder.getCenter().getBlockZ() - radius);
    }

    @Override
    public int getMinHeight(org.bukkit.World world) {
        return 0;
    }

}
