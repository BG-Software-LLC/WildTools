package com.bgsoftware.wildtools.utils.items;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.utils.Materials;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnusedReturnValue")
public class ItemBuilder {

    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    private final ItemStack itemStack;
    @Nullable
    private ItemMeta itemMeta;

    public ItemBuilder(ItemStack itemStack) {
        this(itemStack.getType(), itemStack.getDurability());
        this.itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder(Materials type) {
        this(type.toItemStack(1));
    }

    public ItemBuilder(Material type) {
        this(type, 0);
    }

    public ItemBuilder(Material type, int damage) {
        this.itemStack = new ItemStack(type, 1, (short) damage);
        this.itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder(ConfigurationSection section) {
        Material type;
        int damage = section.getInt("data", 0);

        try {
            type = Material.valueOf(section.getString("type"));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Couldn't find valid type for " + section.getCurrentPath() + "...");
        }

        this.itemStack = new ItemStack(type, 1, (short) damage);
        this.itemMeta = itemStack.getItemMeta();

        if (section.contains("name")) {
            withName(section.getString("name"));
        }

        if (section.contains("lore")) {
            withLore(section.getStringList("lore"));
        }

        if (section.getBoolean("glow", false)) {
            plugin.getNMSAdapter().makeItemGlow(itemMeta);
        }

        if (section.contains("enchants")) {
            List<String> enchants = section.getStringList("enchants");

            for (String line : enchants) {
                try {
                    Enchantment enchantment = Enchantment.getByName(line.split(":")[0]);
                    int level = Integer.parseInt(line.split(":")[1]);
                    withEnchant(enchantment, level);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    public ItemBuilder withName(String name) {
        if (this.itemMeta != null && name != null && !name.isEmpty()) {
            this.itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        }

        return this;
    }

    public ItemBuilder withLore(String firstLine, List<String> lore) {
        if (this.itemMeta != null && firstLine != null && lore != null && !firstLine.isEmpty()) {
            List<String> formattedLore = new ArrayList<>();

            firstLine = ChatColor.translateAlternateColorCodes('&', firstLine);
            formattedLore.add(firstLine);

            for (String line : lore) {
                formattedLore.add(ChatColor.getLastColors(firstLine) + ChatColor.translateAlternateColorCodes('&', line));
            }

            if (formattedLore.size() > 10) {
                for (int i = 10; i < formattedLore.size(); i++) {
                    formattedLore.remove(formattedLore.get(i));
                }

                formattedLore.add(ChatColor.getLastColors(firstLine) + "...");
            }

            this.itemMeta.setLore(formattedLore);
        }

        return this;
    }

    public ItemBuilder withLore(List<String> lore) {
        if (this.itemMeta != null && lore != null && !lore.isEmpty()) {
            List<String> formattedLore = new ArrayList<>();

            for (String line : lore) {
                formattedLore.add(ChatColor.translateAlternateColorCodes('&', line));
            }

            this.itemMeta.setLore(formattedLore);
        }

        return this;
    }

    public ItemBuilder withLore(String... lore) {
        if (this.itemMeta != null && lore != null && lore.length > 0) {
            List<String> formattedLore = new ArrayList<>();

            for (String line : lore) {
                formattedLore.add(ChatColor.translateAlternateColorCodes('&', line));
            }

            this.itemMeta.setLore(formattedLore);
        }

        return this;
    }

    public ItemBuilder withEnchant(Enchantment enchantment, int level) {
        if (this.itemMeta != null) {
            this.itemMeta.addEnchant(enchantment, level, true);
        }

        return this;
    }

    public ItemStack build() {
        if (this.itemMeta != null) {
            this.itemStack.setItemMeta(itemMeta);
        }

        return this.itemStack;
    }

}
