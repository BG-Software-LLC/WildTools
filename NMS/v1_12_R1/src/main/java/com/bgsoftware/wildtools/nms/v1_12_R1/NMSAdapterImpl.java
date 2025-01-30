package com.bgsoftware.wildtools.nms.v1_12_R1;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.wildtools.nms.NMSAdapter;
import com.bgsoftware.wildtools.nms.v1_12_R1.tool.ToolItemStackImpl;
import com.bgsoftware.wildtools.nms.v1_12_R1.world.FakeCraftBlock;
import com.bgsoftware.wildtools.utils.items.DestroySpeedCategory;
import com.bgsoftware.wildtools.utils.items.ToolItemStack;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.Blocks;
import net.minecraft.server.v1_12_R1.ContainerAnvil;
import net.minecraft.server.v1_12_R1.EntityItem;
import net.minecraft.server.v1_12_R1.EntityLiving;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.Items;
import net.minecraft.server.v1_12_R1.PacketPlayOutCollect;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftLivingEntity;
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
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

public class NMSAdapterImpl implements NMSAdapter {

    private static final ReflectField<ItemStack> ITEM_STACK_HANDLE = new ReflectField<>(CraftItemStack.class, ItemStack.class, "handle");

    private static final Enchantment GLOW_ENCHANT = initializeGlowEnchantment();

    @Override
    public ToolItemStack createToolItemStack(org.bukkit.inventory.ItemStack bukkitItem) {
        ItemStack nmsItem;

        if (bukkitItem instanceof CraftItemStack) {
            nmsItem = ITEM_STACK_HANDLE.get(bukkitItem);
        } else {
            nmsItem = CraftItemStack.asNMSCopy(bukkitItem);
        }

        return new ToolItemStackImpl(nmsItem);
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
    public Collection<Player> getOnlinePlayers() {
        return new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    @Override
    public void makeItemGlow(ItemMeta itemMeta) {
        itemMeta.addEnchant(GLOW_ENCHANT, 1, true);
    }

    @Override
    public int getFarmlandId() {
        return Block.getCombinedId(Blocks.FARMLAND.getBlockData());
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
        ((WorldServer) entityLiving.world).getTracker().a(entityItem, new PacketPlayOutCollect(
                entityItem.getId(), entityLiving.getId(), item.getItemStack().getAmount()));
    }

    @Override
    public DestroySpeedCategory getDestroySpeedCategory(Material material) {
        IBlockData blockData = CraftMagicNumbers.getBlock(material).getBlockData();

        if (Items.DIAMOND_AXE.getDestroySpeed(new ItemStack(Items.DIAMOND_AXE), blockData) == 8f)
            return DestroySpeedCategory.AXE;

        if (Items.DIAMOND_SHOVEL.getDestroySpeed(new ItemStack(Items.DIAMOND_SHOVEL), blockData) == 8f)
            return DestroySpeedCategory.SHOVEL;

        return DestroySpeedCategory.PICKAXE;
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

    private static Enchantment initializeGlowEnchantment() {
        int enchantId = 100;
        while (Enchantment.getById(enchantId) != null)
            ++enchantId;

        Enchantment glowEnchant = new Enchantment(enchantId) {
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
            public boolean isTreasure() {
                return false;
            }

            @Override
            public boolean isCursed() {
                return false;
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

        try {
            Field field = Enchantment.class.getDeclaredField("acceptingNew");
            field.setAccessible(true);
            field.set(null, true);
            field.setAccessible(false);
        } catch (Exception ignored) {
        }

        try {
            Enchantment.registerEnchantment(glowEnchant);
        } catch (Exception ignored) {
        }

        return glowEnchant;
    }

}
