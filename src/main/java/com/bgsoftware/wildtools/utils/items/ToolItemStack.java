package com.bgsoftware.wildtools.utils.items;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.Map;

public final class ToolItemStack extends ItemStack {

    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    private final ItemStack original;
    private final Object nmsItem;

    private Tool tool;

    private ToolItemStack(ItemStack original, Object nmsItem) {
        this.original = original;
        this.nmsItem = nmsItem;
        this.tool = plugin.getToolsManager().getTool(this);
        plugin.getNMSAdapter().clearTasks(this);
    }

    public static ToolItemStack of(Material type){
        return of(type, 1);
    }

    public static ToolItemStack of(Material type, int amount){
        return of(type, amount, (short) 0);
    }

    public static ToolItemStack of(Material type, int amount, short damage) {
        return of(new ItemStack(type, amount, damage));
    }

    public static ToolItemStack of(ItemStack itemStack) {
        if(itemStack instanceof ToolItemStack)
            return (ToolItemStack) itemStack;

        Object[] items = plugin.getNMSAdapter().createSyncedItem(itemStack);
        return new ToolItemStack((ItemStack) items[0], items[1]);
    }

    public Tool getTool() {
        return tool;
    }

    public Object getNMSItem() {
        return nmsItem;
    }

    public boolean hasSellMode(){
        return getTag("sell-mode", 0) == 1;
    }

    public void setSellMode(boolean sellMode){
        setTag("sell-mode", sellMode ? 1 : 0);
    }

    public void setUses(int uses){
        setTag("tool-uses", uses);
    }

    public int getUses(){
        if(tool != null)
            return getTag("tool-uses", tool.getDefaultUses());

        return 0;
    }

    public String getToolType(){
        return getTag("tool-type", "");
    }

    public void setToolType(String toolType){
        toolType = toolType.toLowerCase();
        setTag("tool-type", toolType);
        tool = plugin.getToolsManager().getTool(toolType);
    }

    public String getOwner(){
        return getTag("tool-owner", "");
    }

    public void setOwner(String owner){
        setTag("tool-owner", owner);
        ItemUtils.formatItemStack(this);
    }

    @Override
    public Material getType() {
        return original.getType();
    }

    @Override
    public void setType(Material type) {
        original.setType(type);
    }

    @Override
    public int getAmount() {
        return original.getAmount();
    }

    @Override
    public void setAmount(int amount) {
        original.setAmount(amount);
    }

    @Override
    public MaterialData getData() {
        return original.getData();
    }

    @Override
    public void setData(MaterialData data) {
        original.setData(data);
    }

    @Override
    public void setDurability(short durability) {
        original.setDurability(durability);
    }

    @Override
    public short getDurability() {
        return original.getDurability();
    }

    public short getMaxDurability(){
        return getType().getMaxDurability();
    }

    @Override
    public int getMaxStackSize() {
        return original.getMaxStackSize();
    }

    @Override
    public String toString() {
        return original.toString();
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object obj) {
        return original.equals(obj);
    }

    @Override
    public boolean isSimilar(ItemStack stack) {
        return original.isSimilar(stack);
    }

    @Override
    public ToolItemStack clone() {
        ItemStack cloned = original.clone();
        return of(cloned);
    }

    @Override
    public boolean containsEnchantment(Enchantment ench) {
        return original.containsEnchantment(ench);
    }

    @Override
    public int getEnchantmentLevel(Enchantment ench) {
        return original.getEnchantmentLevel(ench);
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        return original.getEnchantments();
    }

    @Override
    public void addEnchantments(Map<Enchantment, Integer> enchantments) {
        original.addEnchantments(enchantments);
    }

    @Override
    public void addEnchantment(Enchantment ench, int level) {
        original.addEnchantment(ench, level);
    }

    @Override
    public void addUnsafeEnchantments(Map<Enchantment, Integer> enchantments) {
        original.addUnsafeEnchantments(enchantments);
    }

    @Override
    public void addUnsafeEnchantment(Enchantment ench, int level) {
        original.addUnsafeEnchantment(ench, level);
    }

    @Override
    public int removeEnchantment(Enchantment ench) {
        return original.removeEnchantment(ench);
    }

    @Override
    public Map<String, Object> serialize() {
        return original.serialize();
    }

    @Override
    public ItemMeta getItemMeta() {
        return original.getItemMeta();
    }

    @Override
    public boolean hasItemMeta() {
        return original.hasItemMeta();
    }

    @Override
    public boolean setItemMeta(ItemMeta itemMeta) {
        return original.setItemMeta(itemMeta);
    }

    private void setTag(String key, int value){
        if(!isEmpty())
            plugin.getNMSAdapter().setTag(this, key, value);
    }

    private void setTag(String key, String value){
        if(!isEmpty())
            plugin.getNMSAdapter().setTag(this, key, value);
    }

    private String getTag(String key, String def){
        return isEmpty() ? def : plugin.getNMSAdapter().getTag(this, key, def);
    }

    private int getTag(String key, int def){
        return isEmpty() ? def : plugin.getNMSAdapter().getTag(this, key, def);
    }

    private boolean isEmpty(){
        return original == null || original.getType() == Material.AIR;
    }

}
