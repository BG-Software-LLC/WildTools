package com.bgsoftware.wildtools.nms.v1_7_R4;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.wildtools.nms.v1_7_R4.tool.ToolItemStackImpl;
import com.bgsoftware.wildtools.nms.v1_7_R4.world.FakeCraftBlock;
import com.bgsoftware.wildtools.utils.items.DestroySpeedCategory;
import com.bgsoftware.wildtools.utils.items.ToolItemStack;
import net.minecraft.server.v1_7_R4.Block;
import net.minecraft.server.v1_7_R4.Blocks;
import net.minecraft.server.v1_7_R4.EntityItem;
import net.minecraft.server.v1_7_R4.EntityLiving;
import net.minecraft.server.v1_7_R4.ItemStack;
import net.minecraft.server.v1_7_R4.Items;
import net.minecraft.server.v1_7_R4.PacketPlayOutCollect;
import net.minecraft.server.v1_7_R4.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftItem;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_7_R4.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class NMSAdapter implements com.bgsoftware.wildtools.nms.NMSAdapter {

    private static final ReflectField<ItemStack> ITEM_STACK_HANDLE = new ReflectField<>(CraftItemStack.class, ItemStack.class, "handle");

    @Override
    public org.bukkit.inventory.ItemStack getItemInHand(Player player) {
        return player.getItemInHand();
    }

    @Override
    public org.bukkit.inventory.ItemStack getItemInHand(Player player, Event e) {
        return getItemInHand(player);
    }

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
    @SuppressWarnings("unchecked")
    public Collection<Player> getOnlinePlayers() {
        CraftServer craftServer = (CraftServer) Bukkit.getServer();
        List<CraftPlayer> players = new ArrayList<>();
        try {
            Field playersField = craftServer.getClass().getDeclaredField("playerView");
            playersField.setAccessible(true);
            players = (List<CraftPlayer>) playersField.get(craftServer);
            playersField.setAccessible(false);
        } catch (Exception ignored) {
        }
        return Collections.unmodifiableCollection(players);
    }

    @Override
    public int getFarmlandId() {
        return Block.getId(Blocks.SOIL);
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

        if (Items.DIAMOND_SPADE.getDestroySpeed(new ItemStack(Items.DIAMOND_SPADE), block) == 8f)
            return DestroySpeedCategory.SHOVEL;

        return DestroySpeedCategory.PICKAXE;
    }

}
