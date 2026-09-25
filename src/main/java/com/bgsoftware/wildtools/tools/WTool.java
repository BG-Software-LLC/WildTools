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
        ItemMeta itemMeta = this.toolItemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        this.toolItemStack.setItemMeta(itemMeta);
    }

    @Override
    public void setLore(List<String> lore) {
        ItemMeta itemMeta = this.toolItemStack.getItemMeta();
        List<String> _lore = new ArrayList<>();

        for (String line : lore) {
            _lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        itemMeta.setLore(_lore);
        this.toolItemStack.setItemMeta(itemMeta);
    }

    @Override
    public void setSpigotUnbreakable(boolean spigotUnbreakable) {
        if (SET_SPIGOT_UNBREAKABLE.isValid()) {
            ItemMeta itemMeta = this.toolItemStack.getItemMeta();
            SET_SPIGOT_UNBREAKABLE.invoke(itemMeta, spigotUnbreakable);
            this.toolItemStack.setItemMeta(itemMeta);
        }
    }

    @Override
    public void setCustomModel(int customModel) {
        if (SET_CUSTOM_MODEL_DATA.isValid()) {
            ItemMeta itemMeta = this.toolItemStack.getItemMeta();
            SET_CUSTOM_MODEL_DATA.invoke(itemMeta, customModel);
            this.toolItemStack.setItemMeta(itemMeta);
        }
    }

    @Override
    public void setItemModel(String itemModel) {
        ItemMeta itemMeta = this.toolItemStack.getItemMeta();
        plugin.getNMSAdapter().setItemModel(itemMeta, itemModel);
        this.toolItemStack.setItemMeta(itemMeta);
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
    public void addEnchantment(Enchantment enchantment, int level) {
        ItemMeta itemMeta = this.toolItemStack.getItemMeta();
        itemMeta.addEnchant(enchantment, level, true);
        this.toolItemStack.setItemMeta(itemMeta);
    }

    @Override
    public void addItemFlag(String itemFlagName) {
        ItemMeta itemMeta = this.toolItemStack.getItemMeta();
        plugin.getNMSAdapter().addItemFlag(itemMeta, itemFlagName);
        this.toolItemStack.setItemMeta(itemMeta);
    }

    public void makeToolGlow() {
        ItemMeta itemMeta = this.toolItemStack.getItemMeta();
        plugin.getNMSAdapter().makeItemGlow(itemMeta);
        this.toolItemStack.setItemMeta(itemMeta);
    }

    @Override
    public void addBlacklistedMaterial(String type) {
        this.blacklistedMaterials.add(type);
    }

    @Override
    public void addWhitelistedMaterial(String type) {
        this.whitelistedMaterials.add(type);
    }

    @Override
    public void addBlacklistedDrop(String drop) {
        this.blacklistedDrops.add(drop);
    }

    @Override
    public void addWhitelistedDrop(String drop) {
        this.whitelistedDrops.add(drop);
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
        return this.toolItemStack.getItem().clone();
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
        return this.name;
    }

    @Override
    public boolean isUnbreakable() {
        return this.isUnbreakable;
    }

    @Override
    public boolean hasVanillaDamage() {
        return this.isVanillaDamage;
    }

    @Override
    public boolean isAutoCollect() {
        return this.isAutoCollect;
    }

    @Override
    public boolean isInstantBreak() {
        return this.isInstantBreak;
    }

    @Override
    public boolean hasSilkTouch() {
        return this.isSilkTouch;
    }

    @Override
    public int getDefaultUses() {
        return this.usesLeft;
    }

    @Override
    public boolean isUsingDurability() {
        return this.usesLeft < 0;
    }

    @Override
    public boolean isOnlySameType() {
        return this.isOnlySameType;
    }

    @Override
    public boolean isOnlyInsideClaim() {
        return this.isOnlyInsideClaim;
    }

    @Override
    public long getCooldown() {
        return this.cooldown;
    }

    @Override
    public boolean hasKeepInventory() {
        return this.isKeepInventory;
    }

    @Override
    public boolean isOmni() {
        return this.isOmni;
    }

    @Override
    public boolean isPrivate() {
        return this.isPrivateTool;
    }

    @Override
    public boolean isUsesProgress() {
        return this.isUsingProgress;
    }

    @Override
    public boolean isAnvilCombine() {
        return this.anvilCombineExp > 0;
    }

    @Override
    public boolean hasAnvilCombineLimit() {
        return this.anvilCombineLimit > 0;
    }

    @Override
    public Set<String> getBlacklistedMaterials() {
        return this.blacklistedMaterials.isEmpty() ? Collections.emptySet() :
                Collections.unmodifiableSet(this.blacklistedMaterials);
    }

    @Override
    public Set<String> getWhitelistedMaterials() {
        return this.whitelistedMaterials.isEmpty() ? Collections.emptySet() :
                Collections.unmodifiableSet(this.whitelistedMaterials);
    }

    @Override
    public Set<String> getBlacklistedDrops() {
        return this.blacklistedDrops.isEmpty() ? Collections.emptySet() :
                Collections.unmodifiableSet(this.blacklistedDrops);
    }

    @Override
    public Set<String> getWhitelistedDrops() {
        return this.whitelistedDrops.isEmpty() ? Collections.emptySet() :
                Collections.unmodifiableSet(this.whitelistedDrops);
    }

    @Override
    public Set<String> getNotifiedPlugins() {
        return this.notifiedPlugins.isEmpty() ? Collections.emptySet() :
                Collections.unmodifiableSet(this.notifiedPlugins);
    }

    @Override
    public boolean hasBlacklistedMaterials() {
        return !this.blacklistedMaterials.isEmpty();
    }

    @Override
    public boolean hasWhitelistedMaterials() {
        return !this.whitelistedMaterials.isEmpty();
    }

    @Override
    public boolean hasBlacklistedDrops() {
        return !this.blacklistedDrops.isEmpty();
    }

    @Override
    public boolean hasWhitelistedDrops() {
        return !this.whitelistedDrops.isEmpty();
    }

    @Override
    public boolean isBlacklistedMaterial(Material type, short data) {
        return isMaterialInList(type, data, this.blacklistedMaterials);
    }

    @Override
    public boolean isWhitelistedMaterial(Material type, short data) {
        return isMaterialInList(type, data, this.whitelistedMaterials);
    }

    @Override
    public boolean isBlacklistedDrop(Material type, short data) {
        return isMaterialInList(type, data, this.blacklistedDrops);
    }

    @Override
    public boolean isWhitelistedDrop(Material type, short data) {
        return isMaterialInList(type, data, this.whitelistedDrops);
    }

    @Override
    public double getMultiplier() {
        return this.multiplier;
    }

    @Override
    public int getAnvilCombineExp() {
        return this.anvilCombineExp;
    }

    @Override
    public int getAnvilCombineLimit() {
        return this.anvilCombineLimit;
    }

    @Override
    public boolean isBlacklistedWorld(String world) {
        return this.blacklistedWorlds.contains(world);
    }

    @Override
    public boolean isWhitelistedWorld(String world) {
        return this.whitelistedWorlds.isEmpty() || this.whitelistedWorlds.contains(world);
    }

    @Override
    public boolean hasStatistics() {
        return this.isStatistics;
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
    @SuppressWarnings("all")
    public boolean canBreakBlock(Block block, Material firstType, short firstData) {
        BlockMaterial blockMaterial = BlockMaterial.of(block);
        BlockMaterial firstBlockType = BlockMaterial.of(firstType, firstData);

        if (blockMaterial.getType() == null || blockMaterial.getType() == Material.AIR) {
            return false;
        }
        if (this.isOnlySameType && !firstBlockType.equals(blockMaterial)) {
            return false;
        }
        if (hasBlacklistedMaterials() && isBlacklistedMaterial(blockMaterial.getType(), blockMaterial.getData())) {
            return false;
        }
        if (hasWhitelistedMaterials() && !isWhitelistedMaterial(blockMaterial.getType(), blockMaterial.getData())) {
            return false;
        }

        return true;
    }

    @Override
    public List<ItemStack> filterDrops(List<ItemStack> drops) {
        if (!hasBlacklistedDrops() && !hasWhitelistedDrops()) {
            return drops.stream().filter(is -> is.getType() != Material.AIR).collect(Collectors.toList());
        }

        List<ItemStack> filteredDrops = new ArrayList<>();

        for (ItemStack item : drops) {
            if (hasBlacklistedDrops() && !isBlacklistedDrop(item.getType(), item.getDurability())) {
                filteredDrops.add(item);
            }
            if (hasWhitelistedDrops() && isWhitelistedDrop(item.getType(), item.getDurability())) {
                filteredDrops.add(item);
            }
        }

        return filteredDrops.stream().filter(item -> item.getType() != Material.AIR).collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("all")
    public boolean isSimilar(ItemStack item) {
        ToolItemStack other = ToolItemStack.of(item);

        if (other.getToolType().equals(getName().toLowerCase())) {
            return true;
        }

        if (this.toolItemStack.getType() != item.getType() || this.toolItemStack.hasItemMeta() != item.hasItemMeta()) {
            return false;
        }

        if (this.toolItemStack.hasItemMeta()) {
            int usesLeft = other.getUses();

            if (this.toolItemStack.getItemMeta().hasDisplayName()) {
                if (!item.getItemMeta().hasDisplayName()) {
                    return false;
                }
                if (!item.getItemMeta().getDisplayName().equals(this.toolItemStack.getItemMeta().getDisplayName()
                        .replace("{}", usesLeft + ""))) {
                    return false;
                }
            }

            if (this.toolItemStack.getItemMeta().hasLore()) {
                if (!item.getItemMeta().hasLore()) {
                    return false;
                }
                if (!item.getItemMeta().getLore().toString().equals(this.toolItemStack.getItemMeta().getLore()
                        .toString().replace("{}", usesLeft + ""))) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void setLastUse(UUID uuid) {
        if (this.cooldown <= 0) {
            return;
        }

        this.lastUses.put(uuid, System.currentTimeMillis());
    }

    @Override
    public boolean canUse(UUID uuid) {
        return getTimeLeft(uuid) <= 0;
    }

    @Override
    public long getTimeLeft(UUID uuid) {
        long lastUseTime = this.lastUses.getOrDefault(uuid, -1L);

        if (lastUseTime < 0) {
            return 0;
        }

        long timeLeft = lastUseTime + this.cooldown - System.currentTimeMillis();

        if (timeLeft <= 0) {
            this.lastUses.remove(uuid);
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
        if (isInstantBreak() && !Materials.isBlacklisted(e.getClickedBlock().getType())) {
            return onBlockBreak(new BlockBreakEvent(e.getClickedBlock(), e.getPlayer()));
        }

        return false;
    }

    @Override
    public boolean onAirInteract(PlayerInteractEvent e) {
        return false;
    }

    /***********************************************************************************/

    private boolean isMaterialInList(Material type, short data, Set<String> list) {
        for (String material : list) {
            if (material.contains(":")) {
                if (material.split(":")[0].equalsIgnoreCase(type.name())
                        && material.split(":")[1].equalsIgnoreCase(data + "")) {
                    return true;
                }
            } else {
                if (material.equalsIgnoreCase(type.name())) {
                    return true;
                }
            }
        }

        return false;
    }

}
