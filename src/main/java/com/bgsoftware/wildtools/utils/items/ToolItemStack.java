package com.bgsoftware.wildtools.utils.items;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class ToolItemStack{

    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    private final ItemStack original;
    private final Object nmsItem;

    private Tool tool;

    private ToolItemStack(ItemStack original, Object nmsItem) {
        this.original = original;
        this.nmsItem = nmsItem;
        this.tool = plugin.getToolsManager().getTool(getToolType());
        if(!isEmpty())
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
        Object[] items = plugin.getNMSAdapter().createSyncedItem(itemStack);
        return new ToolItemStack((ItemStack) items[0], items[1]);
    }

    public ItemStack getItem() {
        return original;
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

    public ItemMeta getItemMeta(){
        return original.getItemMeta();
    }

    public void setItemMeta(ItemMeta itemMeta){
        original.setItemMeta(itemMeta);
    }

    public boolean hasItemMeta(){
        return original.hasItemMeta();
    }

    public Material getType(){
        return original.getType();
    }

    public void setType(Material type){
        original.setType(type);
    }

    public int getAmount(){
        return original.getAmount();
    }

    public void setAmount(int amount){
        original.setAmount(amount);
    }

    public int getEnchantmentLevel(Enchantment enchantment){
        return original.getEnchantmentLevel(enchantment);
    }

    public void setDurability(short durability){
        original.setDurability(durability);
    }

    public short getDurability(){
        return original.getDurability();
    }

    public short getMaxDurability(){
        return original.getType().getMaxDurability();
    }

    @Override
    public ToolItemStack clone() {
        ItemStack cloned = original.clone();
        return of(cloned);
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
