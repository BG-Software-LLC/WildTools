package com.bgsoftware.wildtools.objects.tools;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildtools.utils.BukkitUtils;
import com.bgsoftware.wildtools.utils.items.ToolItemStack;
import com.bgsoftware.wildtools.utils.items.ItemUtils;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import com.bgsoftware.wildtools.api.objects.ToolMode;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

    public static Set<UUID> toolBlockBreak;

    protected Map<UUID, Integer> heldItemsTracker = new HashMap<>();

    private Map<UUID, Long> lastUses;

    private ToolItemStack toolItemStack;
    private int usesLeft;
    private String name, toolMode;
    private boolean onlySameType, onlyInsideClaim, unbreakable, vanillaDamage, autoCollect, instantBreak, silkTouch,
            keepInventory, omni, privateTool, usesProgress, statistics;
    private long cooldown;
    private double multiplier;
    private int anvilCombineExp, anvilCombineLimit;

    private Set<String> blacklistedMaterials, whitelistedMaterials, blacklistedDrops, whitelistedDrops,
            blacklistedWorlds, whitelistedWorlds, notifiedPlugins;

    /***********************************************************************************/

    public WTool(Material type, String name, ToolMode toolMode){
        this.toolItemStack = ToolItemStack.of(type);
        this.toolMode = toolMode.name();
        this.name = name;
        this.usesLeft = -1;
        this.cooldown = 0;
        this.onlySameType = false;
        this.onlyInsideClaim = false;
        this.autoCollect = false;
        this.instantBreak = false;
        this.silkTouch = false;
        this.unbreakable = false;
        this.omni = false;
        this.blacklistedMaterials = new HashSet<>();
        this.whitelistedMaterials = new HashSet<>();
        this.blacklistedDrops = new HashSet<>();
        this.whitelistedDrops = new HashSet<>();
        this.blacklistedWorlds = new HashSet<>();
        this.whitelistedWorlds = new HashSet<>();
        this.notifiedPlugins = new HashSet<>();
        this.lastUses = new HashMap<>();
        this.multiplier = 1;
        this.anvilCombineExp = -1;
        this.toolBlockBreak = new HashSet<>();
        this.statistics = true;
    }

    @Override
    public void setDisplayName(String name){
        ItemMeta im = toolItemStack.getItemMeta();
        im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        toolItemStack.setItemMeta(im);
    }

    @Override
    public void setLore(List<String> lore){
        ItemMeta im = toolItemStack.getItemMeta();
        List<String> _lore = new ArrayList<>();

        for(String line : lore)
            _lore.add(ChatColor.translateAlternateColorCodes('&', line));

        im.setLore(_lore);
        toolItemStack.setItemMeta(im);
    }

    @Override
    public void setSpigotUnbreakable(boolean spigotUnbreakable) {
        if(SET_SPIGOT_UNBREAKABLE.isValid()) {
            ItemMeta itemMeta = toolItemStack.getItemMeta();
            SET_SPIGOT_UNBREAKABLE.invoke(itemMeta, spigotUnbreakable);
            toolItemStack.setItemMeta(itemMeta);
        }
    }

    @Override
    public void setCustomModel(int customModel) {
        if(SET_CUSTOM_MODEL_DATA.isValid()) {
            ItemMeta itemMeta = toolItemStack.getItemMeta();
            SET_CUSTOM_MODEL_DATA.invoke(itemMeta, customModel);
            toolItemStack.setItemMeta(itemMeta);
        }
    }

    @Override
    public void setOnlySameType(boolean onlySameType){
        this.onlySameType = onlySameType;
    }

    @Override
    public void setOnlyInsideClaim(boolean onlyInsideClaim) {
        this.onlyInsideClaim = onlyInsideClaim;
    }

    @Override
    public void setAutoCollect(boolean autoCollect){
        this.autoCollect = autoCollect;
    }

    @Override
    public void setInstantBreak(boolean instantBreak) {
        this.instantBreak = instantBreak;
    }

    @Override
    public void setSilkTouch(boolean silkTouch){
        this.silkTouch = silkTouch;
    }

    @Override
    public void setUnbreakable(boolean unbreakable){
        this.unbreakable = unbreakable;
    }

    @Override
    public void setVanillaDamage(boolean vanillaDamage) {
        this.vanillaDamage = vanillaDamage;
    }

    @Override
    public void setUsesLeft(int usesLeft){
        this.usesLeft = usesLeft;
    }

    @Override
    public void setCooldown(long cooldown){
        this.cooldown = cooldown;
    }

    @Override
    public void setKeepInventory(boolean keepInventory) {
        this.keepInventory = keepInventory;
    }

    @Override
    public void setOmni(boolean omni) {
        this.omni = omni;
    }

    @Override
    public void setPrivate(boolean privateTool) {
        this.privateTool = privateTool;
    }

    @Override
    public void setUsesProgress(boolean usesProgress) {
        this.usesProgress = usesProgress;
    }

    @Override
    public void addEnchantment(Enchantment ench, int level){
        ItemMeta im = toolItemStack.getItemMeta();
        im.addEnchant(ench, level, true);
        toolItemStack.setItemMeta(im);
    }

    @Override
    public void addBlacklistedMaterial(String type){
        blacklistedMaterials.add(type);
    }

    @Override
    public void addWhitelistedMaterial(String type){
        whitelistedMaterials.add(type);
    }

    @Override
    public void addBlacklistedDrop(String drop){
        blacklistedDrops.add(drop);
    }

    @Override
    public void addWhitelistedDrop(String drop){
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
        this.statistics = statistics;
    }

    @Override
    public void setNotifiedPlugins(List<String> notifiedPlugins) {
        this.notifiedPlugins.addAll(notifiedPlugins);
    }

    /***********************************************************************************/

    @Override
    public ItemStack getItemStack(){
        return toolItemStack.clone().getItem();
    }

    @Override
    public ItemStack getFormattedItemStack(){
        return getFormattedItemStack(getDefaultUses());
    }

    @Override
    public ItemStack getFormattedItemStack(int uses) {
        ToolItemStack toolItemStack = this.toolItemStack.clone();
        toolItemStack.setToolType(getName());
        toolItemStack.setUses(uses);
        ItemUtils.formatItemStack(toolItemStack);
        return toolItemStack.getItem();
    }

    @Override
    public ToolMode getToolMode() {
        return ToolMode.valueOf(toolMode);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isUnbreakable() {
        return unbreakable;
    }

    @Override
    public boolean hasVanillaDamage() {
        return vanillaDamage;
    }

    @Override
    public boolean isAutoCollect(){
        return autoCollect;
    }

    @Override
    public boolean isInstantBreak() {
        return instantBreak;
    }

    @Override
    public boolean hasSilkTouch(){
        return silkTouch;
    }

    @Override
    public int getDefaultUses(){
        return usesLeft;
    }

    @Override
    public boolean isUsingDurability(){
        return usesLeft < 0;
    }

    @Override
    public boolean isOnlySameType(){
        return onlySameType;
    }

    @Override
    public boolean isOnlyInsideClaim() {
        return onlyInsideClaim;
    }

    @Override
    public long getCooldown(){
        return cooldown;
    }

    @Override
    public boolean hasKeepInventory() {
        return keepInventory;
    }

    @Override
    public boolean isOmni() {
        return omni;
    }

    @Override
    public boolean isPrivate() {
        return privateTool;
    }

    @Override
    public boolean isUsesProgress() {
        return usesProgress;
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
        return new HashSet<>(blacklistedMaterials);
    }

    @Override
    public Set<String> getWhitelistedMaterials() {
        return new HashSet<>(whitelistedMaterials);
    }

    @Override
    public Set<String> getBlacklistedDrops() {
        return new HashSet<>(blacklistedDrops);
    }

    @Override
    public Set<String> getWhitelistedDrops() {
        return new HashSet<>(whitelistedDrops);
    }

    @Override
    public Set<String> getNotifiedPlugins() {
        return notifiedPlugins;
    }

    @Override
    public boolean hasBlacklistedMaterials(){
        return !blacklistedMaterials.isEmpty();
    }

    @Override
    public boolean hasWhitelistedMaterials(){
        return !whitelistedMaterials.isEmpty();
    }

    @Override
    public boolean hasBlacklistedDrops(){
        return !blacklistedDrops.isEmpty();
    }

    @Override
    public boolean hasWhitelistedDrops(){
        return !whitelistedDrops.isEmpty();
    }

    @Override
    public boolean isBlacklistedMaterial(Material type, short data){
        return isMaterialInList(type, data, blacklistedMaterials);
    }

    @Override
    public boolean isWhitelistedMaterial(Material type, short data){
        return isMaterialInList(type, data, whitelistedMaterials);
    }

    @Override
    public boolean isBlacklistedDrop(Material type, short data){
        return isMaterialInList(type, data, blacklistedDrops);
    }

    @Override
    public boolean isWhitelistedDrop(Material type, short data){
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
        return statistics;
    }

    /***********************************************************************************/

    @Override
    public void reduceDurablility(Player pl, int amount, ItemStack toolItem) {
        ItemUtils.reduceDurability(ToolItemStack.of(toolItem), pl, amount);
    }

    @Override
    public int getDurability(Player player, ItemStack itemStack) {
        return ItemUtils.getDurability(player, ToolItemStack.of(itemStack));
    }

    @Override
    public boolean canBreakBlock(Block block, Material firstType, short firstData){
        if(block.getType() == null || block.getType() == Material.AIR)
            return false;
        if(onlySameType && (firstType != block.getType() || firstData != block.getData()))
            return false;
        if(hasBlacklistedMaterials() && isBlacklistedMaterial(block.getType(), block.getData()))
            return false;
        if(hasWhitelistedMaterials() && !isWhitelistedMaterial(block.getType(), block.getData()))
            return false;
        return true;
    }

    @Override
    public List<ItemStack> filterDrops(List<ItemStack> drops){
        if(!hasBlacklistedDrops() && !hasWhitelistedDrops())
            return drops.stream().filter(is -> is.getType() != Material.AIR).collect(Collectors.toList());

        List<ItemStack> filteredDrops = new ArrayList<>();

        for(ItemStack is : drops){
            if (hasBlacklistedDrops() && !isBlacklistedDrop(is.getType(), is.getDurability()))
                filteredDrops.add(is);
            if (hasWhitelistedDrops() && isWhitelistedDrop(is.getType(), is.getDurability()))
                filteredDrops.add(is);
        }

        return filteredDrops.stream().filter(is -> is.getType() != Material.AIR).collect(Collectors.toList());
    }

    @Override
    public boolean isSimilar(ItemStack is){
        ToolItemStack other = ToolItemStack.of(is);

        if(other.getToolType().equals(getName().toLowerCase()))
            return true;

        if(this.toolItemStack.getType() != is.getType() || this.toolItemStack.hasItemMeta() != is.hasItemMeta())
            return false;

        if(this.toolItemStack.hasItemMeta()){
            int usesLeft = other.getUses();
            if(this.toolItemStack.getItemMeta().hasDisplayName()) {
                if(!is.getItemMeta().hasDisplayName())
                    return false;
                if (!is.getItemMeta().getDisplayName().equals(this.toolItemStack.getItemMeta().getDisplayName().replace("{}", usesLeft + "")))
                    return false;
            }
            if(this.toolItemStack.getItemMeta().hasLore()) {
                if(!is.getItemMeta().hasLore())
                    return false;
                if (!is.getItemMeta().getLore().toString().equals(this.toolItemStack.getItemMeta().getLore().toString().replace("{}", usesLeft + "")))
                    return false;
            }
        }

        return true;
    }

    @Override
    public void setLastUse(UUID uuid){
        if(cooldown <= 0)
            return;
        lastUses.remove(uuid);
        lastUses.put(uuid, System.currentTimeMillis());
    }

    @Override
    public boolean canUse(UUID uuid){
        if(!lastUses.containsKey(uuid))
            return true;

        if(getTimeLeft(uuid) / 1000 > 0)
            return false;

        lastUses.remove(uuid);
        return true;
    }

    @Override
    public long getTimeLeft(UUID uuid){
        if(!lastUses.containsKey(uuid))
            return 0;

        return (lastUses.get(uuid) + cooldown) - System.currentTimeMillis();
    }

    @Override
    public boolean onBlockBreak(BlockBreakEvent e){
        return false;
    }

    @Override
    public boolean onBlockInteract(PlayerInteractEvent e){
        return false;
    }

    @Override
    public boolean onBlockHit(PlayerInteractEvent e){
        if(isInstantBreak() && !BukkitUtils.DISALLOWED_BLOCKS.contains(e.getClickedBlock().getType()))
            return onBlockBreak(new BlockBreakEvent(e.getClickedBlock(), e.getPlayer()));

        return false;
    }

    @Override
    public boolean onAirInteract(PlayerInteractEvent e){
        return false;
    }

    /***********************************************************************************/

    private boolean isMaterialInList(Material type, short data, Set<String> list){
        for(String mat : list) {
            if (mat.contains(":")) {
                if(mat.split(":")[0].equalsIgnoreCase(type.name()) && mat.split(":")[1].equalsIgnoreCase(data + ""))
                    return true;
            } else {
                if(mat.equalsIgnoreCase(type.name()))
                    return true;
            }
        }
        return false;
    }

    protected String getTime(long timeLeft){
        String time = "";

        // Get rid of miliseconds
        timeLeft = timeLeft / 1000;

        if(timeLeft >= 3600) {
            if (timeLeft / 3600 == 1)
                time += "1 hour, ";
            else time += (timeLeft / 3600) + " hours, ";
            timeLeft %= 3600;
        }

        if(timeLeft >= 60){
            if (timeLeft / 60 == 1)
                time += "1 minute, ";
            else time += (timeLeft / 60) + " minutes, ";
            timeLeft %= 60;
        }

        if(timeLeft != 0) {
            if (timeLeft == 1)
                time += timeLeft + " second";
            else time += timeLeft + " seconds";
            return time;
        }

        return time.substring(0, time.length() - 2);
    }

}
