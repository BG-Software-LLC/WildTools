package com.bgsoftware.wildtools.nms;

import org.bukkit.Chunk;
import org.bukkit.CropState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.Collection;
import java.util.List;

public interface NMSAdapter {

    String getVersion();

    default boolean isLegacy(){
        return true;
    }

    List<ItemStack> getBlockDrops(Player pl, Block bl, boolean silkTouch);

    List<ItemStack> getCropDrops(Player pl, Block bl);

    int getTag(ItemStack is, String key, int def);

    ItemStack setTag(ItemStack is, String key, int value);

    String getTag(ItemStack is, String key, String def);

    ItemStack setTag(ItemStack is, String key, String value);

    ItemStack getItemInHand(Player player);

    void setItemInHand(Player player, ItemStack itemStack);

    boolean isFullyGrown(Block block);

    void setCropState(Block block, CropState cropState);

    void copyBlock(Block from, Block to);

    Collection<Player> getOnlinePlayers();

    void setBlockFast(Location location, int combinedId);

    void refreshChunks(List<Chunk> chunksList);

    int getCombinedId(Location location);

    void setCombinedId(Location location, int combinedId);

    Enchantment getGlowEnchant();

    boolean isOutsideWorldborder(Location location);

    Object getBlockData(Material type, byte data);

    void playPickupAnimation(LivingEntity livingEntity, Item item);

    default ItemStack[] parseChoice(Recipe recipe, ItemStack itemStack){
        return new ItemStack[] {itemStack};
    }
}
