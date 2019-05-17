package com.bgsoftware.wildtools.api.objects.tools;

import com.bgsoftware.wildtools.api.objects.ToolMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface Tool {

    void setDisplayName(String name);

    void setLore(List<String> lore);

    void setSpigotUnbreakable(boolean spigotUnbreakable);

    void setOnlySameType(boolean onlySameType);

    void setOnlyInsideClaim(boolean onlyInsideClaim);

    void setAutoCollect(boolean autoCollect);

    void setSilkTouch(boolean silkTouch);

    void setUnbreakable(boolean unbreakable);

    void setUsesLeft(int usesLeft);

    void setCooldown(long cooldown);

    void setKeepInventory(boolean keepInventory);

    void setOmni(boolean omni);

    void setPrivate(boolean privateTool);

    void addEnchantment(Enchantment ench, int level);

    void addBlacklistedMaterial(String type);

    void addWhitelistedMaterial(String type);

    void addBlacklistedDrop(String drop);

    void addWhitelistedDrop(String drop);

    void setMultiplier(double multiplier);

    ItemStack getItemStack();

    ItemStack getFormattedItemStack();

    ItemStack getFormattedItemStack(int uses);

    ToolMode getToolMode();

    String getName();

    boolean isUnbreakable();

    boolean isAutoCollect();

    boolean hasSilkTouch();

    int getDefaultUses();

    boolean isUsingDurability();

    boolean isOnlySameType();

    boolean isOnlyInsideClaim();

    long getCooldown();

    boolean hasKeepInventory();

    boolean isOmni();

    boolean isPrivate();

    Set<String> getBlacklistedMaterials();

    Set<String> getWhitelistedMaterials();

    Set<String> getBlacklistedDrops();

    Set<String> getWhitelistedDrops();

    boolean hasBlacklistedMaterials();

    boolean hasWhitelistedMaterials();

    boolean hasBlacklistedDrops();

    boolean hasWhitelistedDrops();

    boolean isBlacklistedMaterial(Material type, short data);

    boolean isWhitelistedMaterial(Material type, short data);

    boolean isBlacklistedDrop(Material type, short data);

    boolean isWhitelistedDrop(Material type, short data);

    double getMultiplier();

    void reduceDurablility(Player pl);

    boolean canBreakBlock(Block block, Material firstType, short data);

    List<ItemStack> filterDrops(List<ItemStack> drops);

    boolean isSimilar(ItemStack is);

    void setLastUse(UUID uuid);

    boolean canUse(UUID uuid);

    long getTimeLeft(UUID uuid);

    boolean onBlockBreak(BlockBreakEvent e);

    boolean onBlockInteract(PlayerInteractEvent e);

    boolean onBlockHit(PlayerInteractEvent e);

    boolean onAirInteract(PlayerInteractEvent e);

}
