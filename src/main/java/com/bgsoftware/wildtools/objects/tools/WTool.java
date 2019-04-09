package com.bgsoftware.wildtools.objects.tools;

import com.bgsoftware.wildtools.utils.ItemUtil;
import org.bukkit.GameMode;
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

@SuppressWarnings("all")
public abstract class WTool implements Tool {

    protected WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    public static Set<UUID> toolBlockBreak;

    private Map<UUID, Long> lastUses;

    private ItemStack is;
    private int usesLeft;
    private String name, toolMode;
    private boolean onlySameType, onlyInsideClaim, unbreakable, autoCollect, silkTouch, keepInventory;
    private long cooldown;

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
        this.silkTouch = false;
        this.unbreakable = false;
        this.blacklistedMaterials = new HashSet<>();
        this.whitelistedMaterials = new HashSet<>();
        this.blacklistedDrops = new HashSet<>();
        this.whitelistedDrops = new HashSet<>();
        this.lastUses = new HashMap<>();
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

    /***********************************************************************************/

    @Override
    public ItemStack getItemStack(){
        return is.clone();
    }

    @Override
    public ItemStack getFormattedItemStack(){
        ItemStack is = this.is.clone();

        ItemUtil.formatItemStack(this, is);

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
    public boolean isUnbreakable(){
        return unbreakable;
    }

    @Override
    public boolean isAutoCollect(){
        return autoCollect;
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

    /***********************************************************************************/

    @Override
    public void reduceDurablility(Player pl){
        ItemStack is = plugin.getNMSAdapter().getItemInHand(pl).clone();

        if(isUnbreakable() || pl.getGameMode() == GameMode.CREATIVE)
            return;

        if(isUsingDurability()){
            int unbLevel = is.getEnchantmentLevel(Enchantment.DURABILITY);

            // Durability Reduce Chance: (100/(Level+1))%
            if (unbLevel != 0) {
                int chance = new Random().nextInt(100);
                if (chance > (100 / (unbLevel + 1)))
                    return;
            }

            is.setDurability((short) (is.getDurability() + 1));

            if(is.getDurability() > is.getType().getMaxDurability()) {
                is = new ItemStack(Material.AIR);
            }
        }

        else{
            int usesLeft = plugin.getNMSAdapter().getIntTag(plugin.getNMSAdapter().getItemInHand(pl), "tool-uses", getDefaultUses());
            is = plugin.getNMSAdapter().setIntTag(is, "tool-uses", --usesLeft);

            if (usesLeft <= 0) {
                is = new ItemStack(Material.AIR);
            }

            //Update name and lore
            else if(is.hasItemMeta()){
                ItemUtil.formatItemStack(this, is);
            }
        }

        plugin.getNMSAdapter().setItemInHand(pl, is);
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
            return drops;

        List<ItemStack> filteredDrops = new ArrayList<>();

        for(ItemStack is : drops){
            if(hasBlacklistedDrops() && !isBlacklistedDrop(is.getType(), is.getDurability()))
                filteredDrops.add(is);
            if(hasWhitelistedDrops() && isWhitelistedDrop(is.getType(), is.getDurability()))
                filteredDrops.add(is);
        }

        return filteredDrops;
    }

    @Override
    public boolean isSimilar(ItemStack is){
        if(this.is.getType() != is.getType() || this.is.hasItemMeta() != is.hasItemMeta())
            return false;

        if(this.is.hasItemMeta()){
            int usesLeft = plugin.getNMSAdapter().getIntTag(is, "tool-uses", getDefaultUses());
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
