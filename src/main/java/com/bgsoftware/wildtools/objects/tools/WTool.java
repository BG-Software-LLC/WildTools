package com.bgsoftware.wildtools.objects.tools;

import com.bgsoftware.wildtools.api.objects.tools.HarvesterTool;
import com.bgsoftware.wildtools.utils.items.ItemUtils;
import com.bgsoftware.wildtools.utils.items.ToolTaskManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
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
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("all")
public abstract class WTool implements Tool {

    protected static WildToolsPlugin plugin = WildToolsPlugin.getPlugin();
    private final Map<Location, Object> toolMutexes = new HashMap<>();

    public static Set<UUID> toolBlockBreak;

    protected Map<UUID, Integer> heldItemsTracker = new HashMap<>();

    private Map<UUID, Long> lastUses;

    private ItemStack is;
    private int usesLeft;
    private String name, toolMode;
    private boolean onlySameType, onlyInsideClaim, unbreakable, autoCollect, instantBreak, silkTouch, keepInventory, omni, privateTool;
    private long cooldown;
    private double multiplier;
    private int anvilCombineExp, anvilCombineLimit;

    private Set<String> blacklistedMaterials, whitelistedMaterials, blacklistedDrops, whitelistedDrops;

    /***********************************************************************************/

    public WTool(Material type, String name, ToolMode toolMode){
        this.is = new ItemStack(type, 1);
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
        this.lastUses = new HashMap<>();
        this.multiplier = 1;
        this.anvilCombineExp = -1;
        toolBlockBreak = new HashSet<>();
    }

    @Override
    public void setDisplayName(String name){
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        is.setItemMeta(im);
    }

    @Override
    public void setLore(List<String> lore){
        ItemMeta im = is.getItemMeta();
        List<String> _lore = new ArrayList<>();

        for(String line : lore)
            _lore.add(ChatColor.translateAlternateColorCodes('&', line));

        im.setLore(_lore);
        is.setItemMeta(im);
    }

    @Override
    public void setSpigotUnbreakable(boolean spigotUnbreakable) {
        try {
            ItemMeta itemMeta = is.getItemMeta();
            ItemMeta.class.getMethod("setUnbreakable", boolean.class).invoke(itemMeta, spigotUnbreakable);
            is.setItemMeta(itemMeta);
        }catch(Throwable ignored){}
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
    public void addEnchantment(Enchantment ench, int level){
        ItemMeta im = is.getItemMeta();
        im.addEnchant(ench, level, true);
        is.setItemMeta(im);
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

    /***********************************************************************************/

    @Override
    public ItemStack getItemStack(){
        return is.clone();
    }

    @Override
    public ItemStack getFormattedItemStack(){
        return getFormattedItemStack(getDefaultUses());
    }

    @Override
    public ItemStack getFormattedItemStack(int uses) {
        ItemStack is = this.is.clone();

        ItemUtils.formatItemStack(this, is, uses, false, false, null);

        is = plugin.getNMSAdapter().setTag(is, "tool-type", getName().toLowerCase());

        return is;
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

    /***********************************************************************************/

    @Override
    public void reduceDurablility(Player pl, int amount, UUID taskId) {
        ItemStack is = ToolTaskManager.getItemFromTask(taskId);

        if(isUnbreakable() || pl.getGameMode() == GameMode.CREATIVE)
            return;

        ItemStack originalItem = is.clone();
        boolean giveOriginal = is.getAmount() > 1;

        if(is.getAmount() > 1){
            is.setAmount(1);
            originalItem.setAmount(originalItem.getAmount() - 1);
        }

        if(isUsingDurability()){
            int unbLevel = is.getEnchantmentLevel(Enchantment.DURABILITY);

            // Durability Reduce Chance: (100/(Level+1))%
            if (unbLevel != 0) {
                int chance = new Random().nextInt(100);
                if (chance > (100 / (unbLevel + 1)))
                    return;
            }

            is.setDurability((short) (is.getDurability() + amount));

            if(is.getDurability() > is.getType().getMaxDurability()) {
                is = new ItemStack(Material.AIR);
            }
        }

        else{
            int usesLeft = plugin.getNMSAdapter().getTag(is, "tool-uses", getDefaultUses());
            is = plugin.getNMSAdapter().setTag(is, "tool-uses", (usesLeft -= amount));

            if (usesLeft <= 0) {
                is = new ItemStack(Material.AIR);
            }

            //Update name and lore
            else if(is.hasItemMeta()){
                final ItemStack ITEM_STACK = is;
                ItemUtils.formatItemStack(
                        this,
                        ITEM_STACK,
                        getDefaultUses(),
                        this instanceof HarvesterTool && ((WHarvesterTool) this).hasSellMode(is),
                        () -> {
                            ToolTaskManager.setItemOfTask(taskId, ITEM_STACK);
                            if(giveOriginal)
                                ItemUtils.addItem(originalItem, pl.getInventory(), pl.getLocation());
                            ToolTaskManager.removeTask(taskId);
                        }
                );
                return;
            }
        }

        ToolTaskManager.setItemOfTask(taskId, is);

        if(giveOriginal)
            ItemUtils.addItem(originalItem, pl.getInventory(), pl.getLocation());

        ToolTaskManager.removeTask(taskId);
    }

    @Override
    public int getDurability(Player player, UUID taskId) {
        if(isUnbreakable() || player.getGameMode() == GameMode.CREATIVE)
            return Integer.MAX_VALUE;

        ItemStack is = ToolTaskManager.getItemFromTask(taskId);

        return isUsingDurability() ? is.getType().getMaxDurability() - is.getDurability() + 1 : plugin.getNMSAdapter().getTag(is, "tool-uses", getDefaultUses());
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
        if(plugin.getNMSAdapter().getTag(is, "tool-type", "").equals(getName().toLowerCase()))
            return true;

        if(this.is.getType() != is.getType() || this.is.hasItemMeta() != is.hasItemMeta())
            return false;

        if(this.is.hasItemMeta()){
            int usesLeft = plugin.getNMSAdapter().getTag(is, "tool-uses", getDefaultUses());
            if(this.is.getItemMeta().hasDisplayName()) {
                if(!is.getItemMeta().hasDisplayName())
                    return false;
                if (!is.getItemMeta().getDisplayName().equals(this.is.getItemMeta().getDisplayName().replace("{}", usesLeft + "")))
                    return false;
            }
            if(this.is.getItemMeta().hasLore()) {
                if(!is.getItemMeta().hasLore())
                    return false;
                if (!is.getItemMeta().getLore().toString().equals(this.is.getItemMeta().getLore().toString().replace("{}", usesLeft + "")))
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
        if(isInstantBreak() && e.getClickedBlock().getType() != Material.BEDROCK)
            return onBlockBreak(new BlockBreakEvent(e.getClickedBlock(), e.getPlayer()));

        return false;
    }

    @Override
    public boolean onAirInteract(PlayerInteractEvent e){
        return false;
    }

    /***********************************************************************************/

    protected Object getToolMutex(Block block){
        Location location = block.getLocation();
        if(!toolMutexes.containsKey(location))
            toolMutexes.put(location, new Object());
        return toolMutexes.get(location);
    }

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
