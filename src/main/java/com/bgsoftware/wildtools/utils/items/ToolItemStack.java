package com.bgsoftware.wildtools.utils.items;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ToolItemStack {

    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    public static ToolItemStack of(Material type) {
        return type == Material.AIR ? EmptyToolItemStack.INSTANCE : createFromItemStack(new ItemStack(type, 1));
    }

    public static ToolItemStack of(@Nullable ItemStack itemStack) {
        return itemStack == null || itemStack.getType() == Material.AIR ? EmptyToolItemStack.INSTANCE :
                createFromItemStack(itemStack);
    }

    private static ToolItemStack createFromItemStack(@Nonnull ItemStack itemStack) {
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

    @Nullable
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

    private static class EmptyToolItemStack extends ToolItemStack {

        private static final EmptyToolItemStack INSTANCE = new EmptyToolItemStack();

        private final ItemStack EMPTY_ITEM = new ItemStack(Material.AIR);

        @Override
        protected void setItem(ItemStack handle) {
            // Do nothing.
        }

        @Override
        public ItemStack getItem() {
            return EMPTY_ITEM;
        }

        @Nullable
        @Override
        public Tool getTool() {
            return null;
        }

        @Override
        public boolean hasSellMode() {
            return false;
        }

        @Override
        public void setSellMode(boolean sellMode) {
            // Do nothing.
        }

        @Override
        public void setUses(int uses) {
            // Do nothing.
        }

        @Override
        public int getUses() {
            return 0;
        }

        @Override
        public String getToolType() {
            return "";
        }

        @Override
        public void setToolType(String toolType) {
            // Do nothing.
        }

        @Override
        public String getOwner() {
            return "";
        }

        @Override
        public void setOwner(String owner) {
            // Do nothing.
        }

        @Override
        public ItemMeta getItemMeta() {
            return null;
        }

        @Override
        public void setItemMeta(ItemMeta itemMeta) {
            // Do nothing.
        }

        @Override
        public boolean hasItemMeta() {
            return false;
        }

        @Override
        public Material getType() {
            return Material.AIR;
        }

        @Override
        public void setType(Material type) {
            // Do nothing.
        }

        @Override
        public int getAmount() {
            return 0;
        }

        @Override
        public void setAmount(int amount) {
            // Do nothing.
        }

        @Override
        public int getEnchantmentLevel(Enchantment enchantment) {
            return 0;
        }

        @Override
        public void setDurability(short durability) {
            // Do nothing.
        }

        @Override
        public short getDurability() {
            return 0;
        }

        @Override
        public short getMaxDurability() {
            return 0;
        }

        @Override
        public ToolItemStack copy() {
            return this;
        }

        @Override
        public void setTag(String key, int value) {
            // Do nothing
        }

        @Override
        public void setTag(String key, String value) {
            // Do nothing
        }

        @Override
        public int getTag(String key, int def) {
            return def;
        }

        @Override
        public String getTag(String key, String def) {
            return def;
        }

        @Override
        public void breakTool(Player player) {
            // Do nothing
        }

    }

}
