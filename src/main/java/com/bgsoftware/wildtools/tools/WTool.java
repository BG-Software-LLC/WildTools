package com.bgsoftware.wildtools.tools;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import com.bgsoftware.wildtools.utils.Materials;
import com.bgsoftware.wildtools.utils.items.ItemUtils;
import com.bgsoftware.wildtools.utils.items.ToolItemStack;
import com.bgsoftware.wildtools.world.BlockMaterial;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("all")
public abstract class WTool implements Tool {

    private static final ReflectMethod<Void> SET_SPIGOT_UNBREAKABLE = new ReflectMethod<>(
            ItemMeta.class, "setUnbreakable", boolean.class);

    private static final ReflectMethod<Void> SET_CUSTOM_MODEL_DATA = new ReflectMethod<>(
            ItemMeta.class, "setCustomModelData", Integer.class);

    protected static WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    private final Map<UUID, Long> lastUses = new HashMap<>();
    private final Set<String> blacklistedMaterials = new LinkedHashSet<>();
    private final Set<String> whitelistedMaterials = new LinkedHashSet<>();
    private final Set<String> blacklistedDrops = new LinkedHashSet<>();
    private final Set<String> whitelistedDrops = new LinkedHashSet<>();
    private final Set<String> blacklistedWorlds = new HashSet<>();
    private final Set<String> whitelistedWorlds = new HashSet<>();
    private final Set<String> notifiedPlugins = new LinkedHashSet<>();

    private final ToolItemStack toolItemStack;
    private final String name;
    private final ToolMode toolMode;

    private boolean isOnlySameType = false;
    private boolean isOnlyInsideClaim = false;
    private boolean isUnbreakable = false;
    private boolean isVanillaDamage = false;
    private boolean isAutoCollect = false;
    private boolean isInstantBreak = false;
    private boolean isSilkTouch = false;
    private boolean isKeepInventory = false;
    private boolean isOmni = false;
    private boolean isPrivateTool = false;
    private boolean isUsingProgress = false;
    private boolean isStatistics = true;
    private long cooldown = 0;
    private double multiplier = 1D;
    private int usesLeft = -1;
    private int anvilCombineExp = -1;
    private int anvilCombineLimit = 0;

    public WTool(Material type, String name, ToolMode toolMode) {
        this.toolItemStack = ToolItemStack.of(type);
        this.toolMode = toolMode;
        this.name = name;
    }

    @Override
    public void setDisplayName(String name) {
        ItemMeta im = toolItemStack.getItemMeta();
        im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        toolItemStack.setItemMeta(im);
    }

    @Override
    public void setLore(List<String> lore) {
        ItemMeta im = toolItemStack.getItemMeta();
        List<String> _lore = new ArrayList<>();

        for (String line : lore)
            _lore.add(ChatColor.translateAlternateColorCodes('&', line));

        im.setLore(_lore);
        toolItemStack.setItemMeta(im);
    }

    @Override
    public void setSpigotUnbreakable(boolean spigotUnbreakable) {
        if (SET_SPIGOT_UNBREAKABLE.isValid()) {
            ItemMeta itemMeta = toolItemStack.getItemMeta();
            SET_SPIGOT_UNBREAKABLE.invoke(itemMeta, spigotUnbreakable);
            toolItemStack.setItemMeta(itemMeta);
        }
    }

    @Override
    public void setCustomModel(int customModel) {
        if (SET_CUSTOM_MODEL_DATA.isValid()) {
            ItemMeta itemMeta = toolItemStack.getItemMeta();
            SET_CUSTOM_MODEL_DATA.invoke(itemMeta, customModel);
            toolItemStack.setItemMeta(itemMeta);
        }
    }

    @Override
    public void setOnlySameType(boolean onlySameType) {
        this.isOnlySameType = onlySameType;
    }

    @Override
    public void setOnlyInsideClaim(boolean onlyInsideClaim) {
        this.isOnlyInsideClaim = onlyInsideClaim;
    }

    @Override
    public void setAutoCollect(boolean autoCollect) {
        this.isAutoCollect = autoCollect;
    }

    @Override
    public void setInstantBreak(boolean instantBreak) {
        this.isInstantBreak = instantBreak;
    }

    @Override
    public void setSilkTouch(boolean silkTouch) {
        this.isSilkTouch = silkTouch;
    }

    @Override
    public void setUnbreakable(boolean unbreakable) {
        this.isUnbreakable = unbreakable;
    }

    @Override
    public void setVanillaDamage(boolean vanillaDamage) {
        this.isVanillaDamage = vanillaDamage;
    }

    @Override
    public void setUsesLeft(int usesLeft) {
        this.usesLeft = usesLeft;
    }

    @Override
    public void setCooldown(long cooldown) {
        this.cooldown = cooldown;
    }

    @Override
    public void setKeepInventory(boolean keepInventory) {
        this.isKeepInventory = keepInventory;
    }

    @Override
    public void setOmni(boolean omni) {
        this.isOmni = omni;
    }

    @Override
    public void setPrivate(boolean privateTool) {
        this.isPrivateTool = privateTool;
    }

    @Override
    public void setUsesProgress(boolean usesProgress) {
        this.isUsingProgress = usesProgress;
    }

    @Override
    public void addEnchantment(Enchantment ench, int level) {
        ItemMeta im = toolItemStack.getItemMeta();
        im.addEnchant(ench, level, true);
        toolItemStack.setItemMeta(im);
    }

    public void makeToolGlow() {
        ItemMeta itemMeta = toolItemStack.getItemMeta();
        plugin.getNMSAdapter().makeItemGlow(itemMeta);
        toolItemStack.setItemMeta(itemMeta);
    }

    @Override
    public void addBlacklistedMaterial(String type) {
        blacklistedMaterials.add(type);
    }

    @Override
    public void addWhitelistedMaterial(String type) {
        whitelistedMaterials.add(type);
    }

    @Override
    public void addBlacklistedDrop(String drop) {
        blacklistedDrops.add(drop);
    }

    @Override
    public void addWhitelistedDrop(String drop) {
        whitelistedDrops.add(drop);
    }

    @Override
    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    public void setAnvilCombineExp(int anvilCombineExp) {
        this.anvilCombineExp = anvilCombineExp;
    }

    @Override
    public void setAnvilCombineLimit(int anvilCombineLimit) {
        this.anvilCombineLimit = anvilCombineLimit;
    }

    @Override
    public void setBlacklistedWorlds(List<String> worlds) {
        this.blacklistedWorlds.addAll(worlds);
    }

    @Override
    public void setWhitelistedWorlds(List<String> worlds) {
        this.whitelistedWorlds.addAll(worlds);
    }

    @Override
    public void setStatistics(boolean statistics) {
        this.isStatistics = statistics;
    }

    @Override
    public void setNotifiedPlugins(List<String> notifiedPlugins) {
        this.notifiedPlugins.addAll(notifiedPlugins);
    }

    /***********************************************************************************/

    @Override
    public ItemStack getItemStack() {
        return toolItemStack.getItem().clone();
    }

    @Override
    public ItemStack getFormattedItemStack() {
        return getFormattedItemStack(getDefaultUses());
    }

    @Override
    public ItemStack getFormattedItemStack(int uses) {
        ToolItemStack toolItemStack = this.toolItemStack.copy();
        toolItemStack.setToolType(getName());
        toolItemStack.setUses(uses);
        ItemUtils.formatItemStack(toolItemStack);
        return toolItemStack.getItem();
    }

    @Override
    public ToolMode getToolMode() {
        return this.toolMode;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isUnbreakable() {
        return isUnbreakable;
    }

    @Override
    public boolean hasVanillaDamage() {
        return isVanillaDamage;
    }

    @Override
    public boolean isAutoCollect() {
        return isAutoCollect;
    }

    @Override
    public boolean isInstantBreak() {
        return isInstantBreak;
    }

    @Override
    public boolean hasSilkTouch() {
        return isSilkTouch;
    }

    @Override
    public int getDefaultUses() {
        return usesLeft;
    }

    @Override
    public boolean isUsingDurability() {
        return usesLeft < 0;
    }

    @Override
    public boolean isOnlySameType() {
        return isOnlySameType;
    }

    @Override
    public boolean isOnlyInsideClaim() {
        return isOnlyInsideClaim;
    }

    @Override
    public long getCooldown() {
        return cooldown;
    }

    @Override
    public boolean hasKeepInventory() {
        return isKeepInventory;
    }

    @Override
    public boolean isOmni() {
        return isOmni;
    }

    @Override
    public boolean isPrivate() {
        return isPrivateTool;
    }

    @Override
    public boolean isUsesProgress() {
        return isUsingProgress;
    }

    @Override
    public boolean isAnvilCombine() {
        return anvilCombineExp > 0;
    }

    @Override
    public boolean hasAnvilCombineLimit() {
        return anvilCombineLimit > 0;
    }

    @Override
    public Set<String> getBlacklistedMaterials() {
        return blacklistedMaterials.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(blacklistedMaterials);
    }

    @Override
    public Set<String> getWhitelistedMaterials() {
        return whitelistedMaterials.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(whitelistedMaterials);
    }

    @Override
    public Set<String> getBlacklistedDrops() {
        return blacklistedDrops.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(blacklistedDrops);
    }

    @Override
    public Set<String> getWhitelistedDrops() {
        return whitelistedDrops.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(whitelistedDrops);
    }

    @Override
    public Set<String> getNotifiedPlugins() {
        return notifiedPlugins.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(notifiedPlugins);
    }

    @Override
    public boolean hasBlacklistedMaterials() {
        return !blacklistedMaterials.isEmpty();
    }

    @Override
    public boolean hasWhitelistedMaterials() {
        return !whitelistedMaterials.isEmpty();
    }

    @Override
    public boolean hasBlacklistedDrops() {
        return !blacklistedDrops.isEmpty();
    }

    @Override
    public boolean hasWhitelistedDrops() {
        return !whitelistedDrops.isEmpty();
    }

    @Override
    public boolean isBlacklistedMaterial(Material type, short data) {
        return isMaterialInList(type, data, blacklistedMaterials);
    }

    @Override
    public boolean isWhitelistedMaterial(Material type, short data) {
        return isMaterialInList(type, data, whitelistedMaterials);
    }

    @Override
    public boolean isBlacklistedDrop(Material type, short data) {
        return isMaterialInList(type, data, blacklistedDrops);
    }

    @Override
    public boolean isWhitelistedDrop(Material type, short data) {
        return isMaterialInList(type, data, whitelistedDrops);
    }

    @Override
    public double getMultiplier() {
        return multiplier;
    }

    @Override
    public int getAnvilCombineExp() {
        return anvilCombineExp;
    }

    @Override
    public int getAnvilCombineLimit() {
        return anvilCombineLimit;
    }

    @Override
    public boolean isBlacklistedWorld(String world) {
        return blacklistedWorlds.contains(world);
    }

    @Override
    public boolean isWhitelistedWorld(String world) {
        return whitelistedWorlds.isEmpty() || whitelistedWorlds.contains(world);
    }

    @Override
    public boolean hasStatistics() {
        return isStatistics;
    }

    /***********************************************************************************/

    @Override
    public void reduceDurablility(Player player, int amount, ItemStack toolItem) {
        ItemUtils.reduceDurability(ToolItemStack.of(toolItem), player, amount);
    }

    @Override
    public int getDurability(Player player, ItemStack itemStack) {
        return ItemUtils.getDurability(player, ToolItemStack.of(itemStack));
    }

    @Override
    public boolean canBreakBlock(Block block, Material firstType, short firstData) {
        BlockMaterial blockMaterial = BlockMaterial.of(block);
        BlockMaterial firstBlockType = BlockMaterial.of(firstType, firstData);

        if (blockMaterial.getType() == null || blockMaterial.getType() == Material.AIR)
            return false;
        if (isOnlySameType && !firstBlockType.equals(blockMaterial))
            return false;
        if (hasBlacklistedMaterials() && isBlacklistedMaterial(blockMaterial.getType(), blockMaterial.getData()))
            return false;
        if (hasWhitelistedMaterials() && !isWhitelistedMaterial(blockMaterial.getType(), blockMaterial.getData()))
            return false;

        return true;
    }

    @Override
    public List<ItemStack> filterDrops(List<ItemStack> drops) {
        if (!hasBlacklistedDrops() && !hasWhitelistedDrops())
            return drops.stream().filter(is -> is.getType() != Material.AIR).collect(Collectors.toList());

        List<ItemStack> filteredDrops = new ArrayList<>();

        for (ItemStack is : drops) {
            if (hasBlacklistedDrops() && !isBlacklistedDrop(is.getType(), is.getDurability()))
                filteredDrops.add(is);
            if (hasWhitelistedDrops() && isWhitelistedDrop(is.getType(), is.getDurability()))
                filteredDrops.add(is);
        }

        return filteredDrops.stream().filter(is -> is.getType() != Material.AIR).collect(Collectors.toList());
    }

    @Override
    public boolean isSimilar(ItemStack is) {
        ToolItemStack other = ToolItemStack.of(is);

        if (other.getToolType().equals(getName().toLowerCase()))
            return true;

        if (this.toolItemStack.getType() != is.getType() || this.toolItemStack.hasItemMeta() != is.hasItemMeta())
            return false;

        if (this.toolItemStack.hasItemMeta()) {
            int usesLeft = other.getUses();
            if (this.toolItemStack.getItemMeta().hasDisplayName()) {
                if (!is.getItemMeta().hasDisplayName())
                    return false;
                if (!is.getItemMeta().getDisplayName().equals(this.toolItemStack.getItemMeta().getDisplayName().replace("{}", usesLeft + "")))
                    return false;
            }
            if (this.toolItemStack.getItemMeta().hasLore()) {
                if (!is.getItemMeta().hasLore())
                    return false;
                if (!is.getItemMeta().getLore().toString().equals(this.toolItemStack.getItemMeta().getLore().toString().replace("{}", usesLeft + "")))
                    return false;
            }
        }

        return true;
    }

    @Override
    public void setLastUse(UUID uuid) {
        if (cooldown <= 0)
            return;

        lastUses.put(uuid, System.currentTimeMillis());
    }

    @Override
    public boolean canUse(UUID uuid) {
        return getTimeLeft(uuid) <= 0;
    }

    @Override
    public long getTimeLeft(UUID uuid) {
        long lastUseTime = lastUses.getOrDefault(uuid, -1L);

        if (lastUseTime < 0)
            return 0;

        long timeLeft = lastUseTime + cooldown - System.currentTimeMillis();

        if (timeLeft <= 0) {
            lastUses.remove(uuid);
            return 0;
        }

        return timeLeft;
    }

    @Override
    public boolean onBlockBreak(BlockBreakEvent e) {
        return false;
    }

    @Override
    public boolean onBlockInteract(PlayerInteractEvent e) {
        return false;
    }

    @Override
    public boolean onBlockHit(PlayerInteractEvent e) {
        if (isInstantBreak() && !Materials.isBlacklisted(e.getClickedBlock().getType()))
            return onBlockBreak(new BlockBreakEvent(e.getClickedBlock(), e.getPlayer()));

        return false;
    }

    @Override
    public boolean onAirInteract(PlayerInteractEvent e) {
        return false;
    }

    /***********************************************************************************/

    private boolean isMaterialInList(Material type, short data, Set<String> list) {
        for (String mat : list) {
            if (mat.contains(":")) {
                if (mat.split(":")[0].equalsIgnoreCase(type.name()) && mat.split(":")[1].equalsIgnoreCase(data + ""))
                    return true;
            } else {
                if (mat.equalsIgnoreCase(type.name()))
                    return true;
            }
        }
        return false;
    }

    protected String getTime(long timeLeft) {
        String time = "";

        // Get rid of miliseconds
        timeLeft = timeLeft / 1000;

        if (timeLeft >= 3600) {
            if (timeLeft / 3600 == 1)
                time += "1 hour, ";
            else time += (timeLeft / 3600) + " hours, ";
            timeLeft %= 3600;
        }

        if (timeLeft >= 60) {
            if (timeLeft / 60 == 1)
                time += "1 minute, ";
            else time += (timeLeft / 60) + " minutes, ";
            timeLeft %= 60;
        }

        if (timeLeft != 0) {
            if (timeLeft == 1)
                time += timeLeft + " second";
            else time += timeLeft + " seconds";
            return time;
        }

        return time.substring(0, time.length() - 2);
    }

}
