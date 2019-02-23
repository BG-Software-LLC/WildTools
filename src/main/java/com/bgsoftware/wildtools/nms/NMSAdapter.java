package com.bgsoftware.wildtools.nms;

import org.bukkit.CropState;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;

public interface NMSAdapter {

    String getVersion();

    List<ItemStack> getBlockDrops(Player pl, Block bl, boolean silkTouch);

    List<ItemStack> getCropDrops(Player pl, Block bl);

    int getIntTag(ItemStack is, String key, int def);

    ItemStack setIntTag(ItemStack is, String key, int value);

    ItemStack getItemInHand(Player player);

    void setItemInHand(Player player, ItemStack itemStack);

    boolean isFullyGrown(Block block);

    void setCropState(Block block, CropState cropState);

    void copyBlock(Block from, Block to);

    Collection<Player> getOnlinePlayers();

    void setAirFast(Block block);

    Enchantment getGlowEnchant();

}
