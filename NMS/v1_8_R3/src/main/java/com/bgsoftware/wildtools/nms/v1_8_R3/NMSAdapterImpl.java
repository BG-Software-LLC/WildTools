package com.bgsoftware.wildtools.nms.v1_8_R3;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.wildtools.nms.NMSAdapter;
import com.bgsoftware.wildtools.nms.v1_8_R3.tool.ToolItemStackImpl;
import com.bgsoftware.wildtools.nms.v1_8_R3.world.FakeCraftBlock;
import com.bgsoftware.wildtools.utils.items.DestroySpeedCategory;
import com.bgsoftware.wildtools.utils.items.ToolItemStack;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.EntityItem;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.Items;
import net.minecraft.server.v1_8_R3.PacketPlayOutCollect;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftItem;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
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
        return player.getItemInHand();
    }

    @Override
    public org.bukkit.inventory.ItemStack getItemInHand(Player player, Event e) {
        return getItemInHand(player);
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
                true
        );
    }

    @Override
    public void playPickupAnimation(LivingEntity livingEntity, org.bukkit.entity.Item item) {
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        EntityItem entityItem = (EntityItem) ((CraftItem) item).getHandle();
        ((WorldServer) entityLiving.world).getTracker().a(entityItem, new PacketPlayOutCollect(entityItem.getId(), entityLiving.getId()));
    }

    @Override
    public DestroySpeedCategory getDestroySpeedCategory(Material material) {
        Block block = CraftMagicNumbers.getBlock(material);

        if (Items.DIAMOND_AXE.getDestroySpeed(new ItemStack(Items.DIAMOND_AXE), block) == 8f)
            return DestroySpeedCategory.AXE;

        if (Items.DIAMOND_SHOVEL.getDestroySpeed(new ItemStack(Items.DIAMOND_SHOVEL), block) == 8f)
            return DestroySpeedCategory.SHOVEL;

        return DestroySpeedCategory.PICKAXE;
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
