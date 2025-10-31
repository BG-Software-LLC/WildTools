package com.bgsoftware.wildtools.api.objects.tools;

import com.bgsoftware.wildtools.api.objects.ToolKind;
import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class BaseTool implements Tool {

    private final Material type;
    private final String name;
    private final ToolKind kind;

    private Tool delegate;

    protected BaseTool(Material type, String name, ToolKind kind) {
        this.type = type;
        this.name = name;
        this.kind = kind;
    }

    public final void bindDelegate(Tool delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ToolKind getKind() {
        return kind;
    }

    @Override
    public void setDisplayName(String n) {
        delegate.setDisplayName(n);
    }

    @Override
    public void setLore(List<String> lore) {
        delegate.setLore(lore);
    }

    @Override
    public void setSpigotUnbreakable(boolean b) {
        delegate.setSpigotUnbreakable(b);
    }

    @Override
    public void setCustomModel(int i) {
        delegate.setCustomModel(i);
    }

    @Override
    public void setOnlySameType(boolean b) {
        delegate.setOnlySameType(b);
    }

    @Override
    public void setOnlyInsideClaim(boolean b) {
        delegate.setOnlyInsideClaim(b);
    }

    @Override
    public void setAutoCollect(boolean b) {
        delegate.setAutoCollect(b);
    }

    @Override
    public void setInstantBreak(boolean b) {
        delegate.setInstantBreak(b);
    }

    @Override
    public void setSilkTouch(boolean b) {
        delegate.setSilkTouch(b);
    }

    @Override
    public void setUnbreakable(boolean b) {
        delegate.setUnbreakable(b);
    }

    @Override
    public void setVanillaDamage(boolean b) {
        delegate.setVanillaDamage(b);
    }

    @Override
    public void setUsesLeft(int i) {
        delegate.setUsesLeft(i);
    }

    @Override
    public void setCooldown(long l) {
        delegate.setCooldown(l);
    }

    @Override
    public void setKeepInventory(boolean b) {
        delegate.setKeepInventory(b);
    }

    @Override
    public void setOmni(boolean b) {
        delegate.setOmni(b);
    }

    @Override
    public void setPrivate(boolean b) {
        delegate.setPrivate(b);
    }

    @Override
    public void setUsesProgress(boolean b) {
        delegate.setUsesProgress(b);
    }

    @Override
    public void addEnchantment(org.bukkit.enchantments.Enchantment e, int lvl) {
        delegate.addEnchantment(e, lvl);
    }

    @Override
    public void addBlacklistedMaterial(String t) {
        delegate.addBlacklistedMaterial(t);
    }

    @Override
    public void addWhitelistedMaterial(String t) {
        delegate.addWhitelistedMaterial(t);
    }

    @Override
    public void addBlacklistedDrop(String t) {
        delegate.addBlacklistedDrop(t);
    }

    @Override
    public void addWhitelistedDrop(String t) {
        delegate.addWhitelistedDrop(t);
    }

    @Override
    public void setMultiplier(double m) {
        delegate.setMultiplier(m);
    }

    @Override
    public void setAnvilCombineExp(int i) {
        delegate.setAnvilCombineExp(i);
    }

    @Override
    public void setAnvilCombineLimit(int i) {
        delegate.setAnvilCombineLimit(i);
    }

    @Override
    public void setBlacklistedWorlds(List<String> w) {
        delegate.setBlacklistedWorlds(w);
    }

    @Override
    public void setWhitelistedWorlds(List<String> w) {
        delegate.setWhitelistedWorlds(w);
    }

    @Override
    public void setStatistics(boolean b) {
        delegate.setStatistics(b);
    }

    @Override
    public void setNotifiedPlugins(List<String> p) {
        delegate.setNotifiedPlugins(p);
    }

    @Override
    public org.bukkit.inventory.ItemStack getItemStack() {
        return delegate.getItemStack();
    }

    @Override
    public ItemStack getFormattedItemStack() {
        return delegate.getFormattedItemStack();
    }

    @Override
    public ItemStack getFormattedItemStack(int uses) {
        return delegate.getFormattedItemStack(uses);
    }

    @Override
    public com.bgsoftware.wildtools.api.objects.ToolMode getToolMode() {
        return delegate.getToolMode();
    }

    @Override
    public boolean isUnbreakable() {
        return delegate.isUnbreakable();
    }

    @Override
    public boolean hasVanillaDamage() {
        return delegate.hasVanillaDamage();
    }

    @Override
    public boolean isAutoCollect() {
        return delegate.isAutoCollect();
    }

    @Override
    public boolean isInstantBreak() {
        return delegate.isInstantBreak();
    }

    @Override
    public boolean hasSilkTouch() {
        return delegate.hasSilkTouch();
    }

    @Override
    public int getDefaultUses() {
        return delegate.getDefaultUses();
    }

    @Override
    public boolean isUsingDurability() {
        return delegate.isUsingDurability();
    }

    @Override
    public boolean isOnlySameType() {
        return delegate.isOnlySameType();
    }

    @Override
    public boolean isOnlyInsideClaim() {
        return delegate.isOnlyInsideClaim();
    }

    @Override
    public long getCooldown() {
        return delegate.getCooldown();
    }

    @Override
    public boolean hasKeepInventory() {
        return delegate.hasKeepInventory();
    }

    @Override
    public boolean isOmni() {
        return delegate.isOmni();
    }

    @Override
    public boolean isPrivate() {
        return delegate.isPrivate();
    }

    @Override
    public boolean isUsesProgress() {
        return delegate.isUsesProgress();
    }

    @Override
    public boolean isAnvilCombine() {
        return delegate.isAnvilCombine();
    }

    @Override
    public boolean hasAnvilCombineLimit() {
        return delegate.hasAnvilCombineLimit();
    }

    @Override
    public Set<String> getBlacklistedMaterials() {
        return delegate.getBlacklistedMaterials();
    }

    @Override
    public Set<String> getWhitelistedMaterials() {
        return delegate.getWhitelistedMaterials();
    }

    @Override
    public Set<String> getBlacklistedDrops() {
        return delegate.getBlacklistedDrops();
    }

    @Override
    public Set<String> getWhitelistedDrops() {
        return delegate.getWhitelistedDrops();
    }

    @Override
    public Set<String> getNotifiedPlugins() {
        return delegate.getNotifiedPlugins();
    }

    @Override
    public boolean hasBlacklistedMaterials() {
        return delegate.hasBlacklistedMaterials();
    }

    @Override
    public boolean hasWhitelistedMaterials() {
        return delegate.hasWhitelistedMaterials();
    }

    @Override
    public boolean hasBlacklistedDrops() {
        return delegate.hasBlacklistedDrops();
    }

    @Override
    public boolean hasWhitelistedDrops() {
        return delegate.hasWhitelistedDrops();
    }

    @Override
    public boolean isBlacklistedMaterial(org.bukkit.Material t, short d) {
        return delegate.isBlacklistedMaterial(t, d);
    }

    @Override
    public boolean isWhitelistedMaterial(org.bukkit.Material t, short d) {
        return delegate.isWhitelistedMaterial(t, d);
    }

    @Override
    public boolean isBlacklistedDrop(org.bukkit.Material t, short d) {
        return delegate.isBlacklistedDrop(t, d);
    }

    @Override
    public boolean isWhitelistedDrop(org.bukkit.Material t, short d) {
        return delegate.isWhitelistedDrop(t, d);
    }

    @Override
    public double getMultiplier() {
        return delegate.getMultiplier();
    }

    @Override
    public int getAnvilCombineExp() {
        return delegate.getAnvilCombineExp();
    }

    @Override
    public int getAnvilCombineLimit() {
        return delegate.getAnvilCombineLimit();
    }

    @Override
    public boolean isBlacklistedWorld(String w) {
        return delegate.isBlacklistedWorld(w);
    }

    @Override
    public boolean isWhitelistedWorld(String w) {
        return delegate.isWhitelistedWorld(w);
    }

    @Override
    public boolean hasStatistics() {
        return delegate.hasStatistics();
    }

    @Override
    public void reduceDurablility(org.bukkit.entity.Player p, int amt, ItemStack tool) {
        delegate.reduceDurablility(p, amt, tool);
    }

    @Override
    public int getDurability(org.bukkit.entity.Player p, ItemStack is) {
        return delegate.getDurability(p, is);
    }

    @Override
    public boolean canBreakBlock(org.bukkit.block.Block b, Material ft, short fd) {
        return delegate.canBreakBlock(b, ft, fd);
    }

    @Override
    public List<ItemStack> filterDrops(List<ItemStack> drops) {
        return delegate.filterDrops(drops);
    }

    @Override
    public boolean isSimilar(ItemStack is) {
        return delegate.isSimilar(is);
    }

    @Override
    public void setLastUse(UUID u) {
        delegate.setLastUse(u);
    }

    @Override
    public boolean canUse(UUID u) {
        return delegate.canUse(u);
    }

    @Override
    public long getTimeLeft(UUID u) {
        return delegate.getTimeLeft(u);
    }

    @Override
    public boolean onBlockBreak(BlockBreakEvent e) {
        return delegate.onBlockBreak(e);
    }

    @Override
    public boolean onBlockInteract(PlayerInteractEvent e) {
        return delegate.onBlockInteract(e);
    }

    @Override
    public boolean onBlockHit(PlayerInteractEvent e) {
        return delegate.onBlockHit(e);
    }

    @Override
    public boolean onAirInteract(PlayerInteractEvent e) {
        return delegate.onAirInteract(e);
    }

    public Material getType() {
        return type;
    }
}
