package xyz.wildseries.wildtools.api.objects.tools;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import xyz.wildseries.wildtools.api.objects.ToolMode;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface Tool {

    void setDisplayName(String name);

    void setLore(List<String> lore);

    void setOnlySameType(boolean onlySameType);

    void setOnlyInsideClaim(boolean onlyInsideClaim);

    void setAutoCollect(boolean autoCollect);

    void setSilkTouch(boolean silkTouch);

    void setUnbreakable(boolean unbreakable);

    void setUsesLeft(int usesLeft);

    void setCooldown(long cooldown);

    void setKeepInventory(boolean keepInventory);

    void addEnchantment(Enchantment ench, int level);

    void addBlacklistedMaterial(String type);

    void addWhitelistedMaterial(String type);

    void addBlacklistedDrop(String drop);

    void addWhitelistedDrop(String drop);

    ItemStack getItemStack();

    ItemStack getFormattedItemStack();

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

    void useOnBlock(Player pl, Block bl);

    void reduceDurablility(Player pl);

    boolean canBreakBlock(Block firstBlock, Block targetBlock);

    List<ItemStack> filterDrops(List<ItemStack> drops);

    boolean isSimilar(ItemStack is);

    void setLastUse(UUID uuid);

    boolean canUse(UUID uuid);

    long getTimeLeft(UUID uuid);

}
