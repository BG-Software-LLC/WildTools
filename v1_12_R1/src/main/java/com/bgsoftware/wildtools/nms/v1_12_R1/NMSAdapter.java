package com.bgsoftware.wildtools.nms.v1_12_R1;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.wildtools.nms.v1_12_R1.world.FakeCraftBlock;
import com.bgsoftware.wildtools.recipes.AdvancedShapedRecipe;
import com.bgsoftware.wildtools.utils.items.ToolItemStack;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.BlockBeetroot;
import net.minecraft.server.v1_12_R1.BlockCarrots;
import net.minecraft.server.v1_12_R1.BlockCocoa;
import net.minecraft.server.v1_12_R1.BlockCrops;
import net.minecraft.server.v1_12_R1.BlockNetherWart;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.BlockPotatoes;
import net.minecraft.server.v1_12_R1.Blocks;
import net.minecraft.server.v1_12_R1.Chunk;
import net.minecraft.server.v1_12_R1.ContainerAnvil;
import net.minecraft.server.v1_12_R1.EnchantmentManager;
import net.minecraft.server.v1_12_R1.Enchantments;
import net.minecraft.server.v1_12_R1.EntityHuman;
import net.minecraft.server.v1_12_R1.EntityItem;
import net.minecraft.server.v1_12_R1.EntityLiving;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.EnumColor;
import net.minecraft.server.v1_12_R1.GameProfileSerializer;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.Item;
import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.Items;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.Packet;
import net.minecraft.server.v1_12_R1.PacketPlayOutCollect;
import net.minecraft.server.v1_12_R1.PacketPlayOutMultiBlockChange;
import net.minecraft.server.v1_12_R1.PlayerChunkMap;
import net.minecraft.server.v1_12_R1.StatisticList;
import net.minecraft.server.v1_12_R1.TileEntity;
import net.minecraft.server.v1_12_R1.TileEntityShulkerBox;
import net.minecraft.server.v1_12_R1.TileEntitySkull;
import net.minecraft.server.v1_12_R1.World;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.CropState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NetherWartsState;
import org.bukkit.WorldBorder;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftMagicNumbers;
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
import org.bukkit.material.CocoaPlant;
import org.bukkit.material.Crops;
import org.bukkit.material.NetherWarts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@SuppressWarnings({"unused", "deprecation", "ConstantConditions"})
public final class NMSAdapter implements com.bgsoftware.wildtools.nms.NMSAdapter {

    private static final ReflectField<ItemStack> ITEM_STACK_HANDLE = new ReflectField<>(CraftItemStack.class, ItemStack.class, "handle");

    @Override
    public String getVersion() {
        return "v1_12_R1";
    }

    @Override
    public List<org.bukkit.inventory.ItemStack> getBlockDrops(Player pl, org.bukkit.block.Block bl, boolean silkTouch) {
        List<org.bukkit.inventory.ItemStack> drops = new ArrayList<>();

        EntityPlayer player = ((CraftPlayer) pl).getHandle();
        BlockPosition blockPosition = new BlockPosition(bl.getX(), bl.getY(), bl.getZ());
        World world = player.world;
        IBlockData blockData = world.getType(blockPosition);
        Block block = world.getType(blockPosition).getBlock();

        if (!player.hasBlock(blockData) || player.playerInteractManager.isCreative())
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

                drops.add(CraftItemStack.asBukkitCopy(itemStack));
            }
            return drops;
        } else if (tileEntity instanceof TileEntitySkull) {
            TileEntitySkull tileEntitySkull = (TileEntitySkull) tileEntity;
            if (tileEntitySkull.getSkullType() == 3) {
                ItemStack itemStack = new ItemStack(Items.SKULL, 1, 3);
                NBTTagCompound nbtTagCompound = itemStack.hasTag() ? itemStack.getTag() : new NBTTagCompound();
                assert nbtTagCompound != null;
                NBTTagCompound skullOwnerTag = new NBTTagCompound();
                GameProfileSerializer.serialize(skullOwnerTag, tileEntitySkull.getGameProfile());
                nbtTagCompound.set("SkullOwner", skullOwnerTag);
                itemStack.setTag(nbtTagCompound);
                drops.add(CraftItemStack.asBukkitCopy(itemStack));
                return drops;
            }
        }

        // Has silk touch enchant
        if ((block.r() && !block.isTileEntity()) && (silkTouch || EnchantmentManager.a(Enchantments.SILK_TOUCH, player) > 0)) {
            int i = 0;
            Item item = Item.getItemOf(block);
            if (item != null && item.k()) {
                i = block.toLegacyData(blockData);
            }

            ItemStack itemStack = new ItemStack(item, 1, i);
            drops.add(CraftItemStack.asBukkitCopy(itemStack));
        } else if (!world.isClientSide) {
            int fortuneLevel = EnchantmentManager.a(Enchantments.LOOT_BONUS_BLOCKS, player),
                    dropCount = block.getDropCount(fortuneLevel, world.random);

            Item item = block.getDropType(blockData, world.random, fortuneLevel);
            if (item != null) {
                drops.add(CraftItemStack.asBukkitCopy(new ItemStack(item, dropCount, block.getDropData(blockData))));
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

        if (block instanceof BlockCrops) {
            int growthAge = 7;

            if (block instanceof BlockBeetroot)
                growthAge = 3;

            if (age >= growthAge) {
                //Give the item itself to the player
                if (block instanceof BlockCarrots) {
                    drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.CARROT, 1, 0)));
                } else if (block instanceof BlockPotatoes) {
                    drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.POTATO, 1, 0)));
                } else if (block instanceof BlockBeetroot) {
                    drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.BEETROOT, 1, 0)));
                } else {
                    drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.WHEAT, 1, 0)));
                }
                //Give the "seeds" to the player. I run -1 iteration for "replant"
                for (int i = 0; i < (fortuneLevel + 3) - 1; i++) {
                    if (world.random.nextInt(2 * growthAge) <= age) {
                        if (block instanceof BlockCarrots) {
                            drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.CARROT, 1, 0)));
                        } else if (block instanceof BlockPotatoes) {
                            drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.POTATO, 1, 0)));
                            if (world.random.nextInt(50) == 0) {
                                drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.POISONOUS_POTATO, 1, 0)));
                            }
                        } else if (block instanceof BlockBeetroot) {
                            drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.BEETROOT_SEEDS, 1, 0)));
                        } else {
                            drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.WHEAT_SEEDS, 1, 0)));
                        }
                    }
                }
            }
        } else if (block instanceof BlockCocoa) {
            if (age >= 2) {
                drops.add(CraftItemStack.asBukkitCopy(new ItemStack(Items.DYE, 3, EnumColor.BROWN.getInvColorIndex())));
            }
        } else if (block instanceof BlockNetherWart) {
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
    public int getExpFromBlock(org.bukkit.block.Block block, Player player) {
        World world = ((CraftWorld) block.getWorld()).getHandle();
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        IBlockData blockData = world.getType(new BlockPosition(block.getX(), block.getY(), block.getZ()));
        return blockData.getBlock().getExpDrop(world, blockData,
                EnchantmentManager.getEnchantmentLevel(Enchantments.LOOT_BONUS_BLOCKS, entityPlayer.getItemInMainHand()));
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
        NBTTagCompound tagCompound = nmsStack.getTag();
        if (tagCompound == null) {
            nmsStack.setTag(new NBTTagCompound());
            tagCompound = nmsStack.getTag();
        }
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
        NBTTagCompound tagCompound = nmsStack.getTag();
        if (tagCompound == null) {
            nmsStack.setTag(new NBTTagCompound());
            tagCompound = nmsStack.getTag();
        }
        tagCompound.setString(key, value);
    }

    @Override
    public void clearTasks(ToolItemStack toolItemStack) {
        ItemStack nmsStack = (ItemStack) toolItemStack.getNMSItem();
        NBTTagCompound tagCompound = nmsStack.getTag();
        if (tagCompound != null)
            tagCompound.remove("task-id");
    }

    @Override
    public void breakTool(ToolItemStack toolItemStack, Player player) {
        ItemStack nmsStack = (ItemStack) toolItemStack.getNMSItem();
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        entityPlayer.b(nmsStack);
        Item item = nmsStack.getItem();

        if (nmsStack.getCount() == 1)
            CraftEventFactory.callPlayerItemBreakEvent(entityPlayer, nmsStack);

        nmsStack.subtract(1);

        entityPlayer.b(StatisticList.c(nmsStack.getItem()));

        nmsStack.setData(0);
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
    public Collection<Player> getOnlinePlayers() {
        return new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    @Override
    public void setBlockFast(Location location, int combinedId) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        Chunk chunk = world.getChunkAt(location.getChunk().getX(), location.getChunk().getZ());
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());

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
    public void refreshChunk(org.bukkit.Chunk bukkitChunk, Set<Location> blocksList) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
        int blocksAmount = blocksList.size();
        short[] values = new short[blocksAmount];

        Location firstLocation = null;

        int counter = 0;
        for (Location location : blocksList) {
            if (firstLocation == null)
                firstLocation = location;

            values[counter++] = (short) ((location.getBlockX() & 15) << 12 | (location.getBlockZ() & 15) << 8 | location.getBlockY());
        }

        sendPacketToRelevantPlayers((WorldServer) chunk.world, chunk.locX, chunk.locZ,
                new PacketPlayOutMultiBlockChange(blocksAmount, values, chunk));
    }

    @Override
    public int getCombinedId(org.bukkit.block.Block block) {
        World world = ((CraftWorld) block.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
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
    public BlockPlaceEvent getFakePlaceEvent(Player player, org.bukkit.block.Block block, org.bukkit.block.Block copyBlock) {
        FakeCraftBlock fakeBlock = new FakeCraftBlock(block, copyBlock.getType());
        return new BlockPlaceEvent(
                fakeBlock,
                block.getState(),
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
        ((WorldServer) entityLiving.world).getTracker().a(entityItem, new PacketPlayOutCollect(entityItem.getId(), entityLiving.getId(), item.getItemStack().getAmount()));
    }

    @Override
    public boolean isAxeType(Material material) {
        return Items.DIAMOND_AXE.getDestroySpeed(new ItemStack(Items.DIAMOND_AXE), CraftMagicNumbers.getBlock(material).getBlockData()) == 8.0F;
    }

    @Override
    public boolean isShovelType(Material material) {
        return Items.DIAMOND_SHOVEL.getDestroySpeed(new ItemStack(Items.DIAMOND_SHOVEL), CraftMagicNumbers.getBlock(material).getBlockData()) == 8.0F;
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

    @Override
    public AdvancedShapedRecipe createRecipe(String toolName, org.bukkit.inventory.ItemStack result) {
        return new com.bgsoftware.wildtools.nms.recipe.AdvancedRecipeClassImpl(toolName, result);
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

        for (Object entityItem : droppedItemsRaw) {
            if (canMerge((EntityItem) entityItem)) {
                for (Object otherEntityItem : droppedItemsRaw) {
                    if (entityItem != otherEntityItem && canMerge((EntityItem) otherEntityItem)) {
                        if (mergeEntityItems((EntityItem) entityItem, (EntityItem) otherEntityItem))
                            break;
                    }
                }
            }
        }

        droppedItemsRaw.forEach(droppedItemObject -> {
            EntityItem entityItem = (EntityItem) droppedItemObject;
            if (entityItem.isAlive()) {
                entityItem.world.addEntity(entityItem);
            }
        });
    }

    private static boolean canMerge(EntityItem entityItem) {
        ItemStack itemStack = entityItem.getItemStack();
        return !itemStack.isEmpty() && itemStack.getCount() < itemStack.getMaxStackSize();
    }

    private static boolean mergeEntityItems(EntityItem entityItem, EntityItem otherEntity) {
        ItemStack itemOfEntity = entityItem.getItemStack();
        ItemStack itemOfOtherEntity = otherEntity.getItemStack();
        if (canMergeTogether(itemOfEntity, itemOfOtherEntity)) {
            if (!CraftEventFactory.callItemMergeEvent(otherEntity, entityItem).isCancelled()) {
                mergeItems(entityItem, itemOfEntity, otherEntity, itemOfOtherEntity);
                entityItem.pickupDelay = Math.max(entityItem.pickupDelay, otherEntity.pickupDelay);
                if (itemOfOtherEntity.isEmpty()) {
                    otherEntity.die();
                }
            }
        }

        return entityItem.dead;
    }

    private static boolean canMergeTogether(ItemStack itemStack, ItemStack otherItem) {
        if (itemStack.getItem() != otherItem.getItem())
            return false;

        if (itemStack.getCount() + otherItem.getCount() > otherItem.getMaxStackSize())
            return false;

        if (itemStack.hasTag() ^ otherItem.hasTag())
            return false;

        return !otherItem.hasTag() || otherItem.getTag().equals(itemStack.getTag());
    }

    private static void mergeItems(EntityItem entityItem, ItemStack itemStack, EntityItem otherEntity, ItemStack otherItem) {
        int amountLeftUntilFullStack = Math.min(itemStack.getMaxStackSize() - itemStack.getCount(), otherItem.getCount());
        ItemStack itemStackClone = itemStack.cloneItemStack();
        itemStackClone.add(amountLeftUntilFullStack);
        entityItem.setItemStack(itemStackClone);
        otherItem.subtract(amountLeftUntilFullStack);
        if (!otherItem.isEmpty()) {
            otherEntity.setItemStack(otherItem);
        }
    }

    private void sendPacketToRelevantPlayers(WorldServer worldServer, int chunkX, int chunkZ, Packet<?> packet) {
        PlayerChunkMap playerChunkMap = worldServer.getPlayerChunkMap();
        for (EntityHuman entityHuman : worldServer.players) {
            if (entityHuman instanceof EntityPlayer && playerChunkMap.a((EntityPlayer) entityHuman, chunkX, chunkZ))
                ((EntityPlayer) entityHuman).playerConnection.sendPacket(packet);
        }
    }

}
