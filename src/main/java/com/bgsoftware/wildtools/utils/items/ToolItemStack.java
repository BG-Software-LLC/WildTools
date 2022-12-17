package com.bgsoftware.wildtools.utils.items;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class ToolItemStack {

    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    public static ToolItemStack of(Material type) {
        return of(type, 1);
    }

    public static ToolItemStack of(Material type, int amount) {
        return of(type, amount, (short) 0);
    }

    public static ToolItemStack of(Material type, int amount, short damage) {
        return of(new ItemStack(type, amount, damage));
    }

    public static ToolItemStack of(ItemStack itemStack) {
        return plugin.getNMSAdapter().createToolItemStack(itemStack);
    }

    protected ItemStack handle;
    private Tool tool;

    protected ToolItemStack() {
    }

    protected void setItem(ItemStack handle) {
        this.handle = handle;
        this.tool = plugin.getToolsManager().getTool(getToolType());
    }

    public ItemStack getItem() {
        return handle;
    }

    public Tool getTool() {
        return tool;
    }

    public boolean hasSellMode() {
        return getTag("sell-mode", 0) == 1;
    }

    public void setSellMode(boolean sellMode) {
        setTag("sell-mode", sellMode ? 1 : 0);
    }

    public void setUses(int uses) {
        setTag("tool-uses", uses);
    }

    public int getUses() {
        if (tool != null)
            return getTag("tool-uses", tool.getDefaultUses());

        return 0;
    }

    public String getToolType() {
        return getTag("tool-type", "");
    }

    public void setToolType(String toolType) {
        toolType = toolType.toLowerCase();
        setTag("tool-type", toolType);
        tool = plugin.getToolsManager().getTool(toolType);
    }

    public String getOwner() {
        return getTag("tool-owner", "");
    }

    public void setOwner(String owner) {
        setTag("tool-owner", owner);
        ItemUtils.formatItemStack(this);
    }

    public ItemMeta getItemMeta() {
        return handle.getItemMeta();
    }

    public void setItemMeta(ItemMeta itemMeta) {
        handle.setItemMeta(itemMeta);
    }

    public boolean hasItemMeta() {
        return handle.hasItemMeta();
    }

    public Material getType() {
        return handle.getType();
    }

    public void setType(Material type) {
        handle.setType(type);
    }

    public int getAmount() {
        return handle.getAmount();
    }

    public void setAmount(int amount) {
        handle.setAmount(amount);
    }

    public int getEnchantmentLevel(Enchantment enchantment) {
        return handle.getEnchantmentLevel(enchantment);
    }

    public void setDurability(short durability) {
        handle.setDurability(durability);
    }

    public short getDurability() {
        return handle.getDurability();
    }

    public short getMaxDurability() {
        return handle.getType().getMaxDurability();
    }

    public abstract ToolItemStack copy();

    public abstract void setTag(String key, int value);

    public abstract void setTag(String key, String value);

    public abstract int getTag(String key, int def);

    public abstract String getTag(String key, String def);

    public abstract void breakTool(Player player);

}
