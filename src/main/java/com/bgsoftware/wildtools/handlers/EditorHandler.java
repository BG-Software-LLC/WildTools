package com.bgsoftware.wildtools.handlers;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.utils.Materials;
import com.bgsoftware.wildtools.utils.items.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class EditorHandler {

    private WildToolsPlugin plugin;
    private ItemStack[] settingsEditor;

    public CommentedConfiguration config;

    public EditorHandler(WildToolsPlugin plugin) {
        this.plugin = plugin;

        File file = new File(plugin.getDataFolder(), "config.yml");

        this.config = CommentedConfiguration.loadConfiguration(file);

        try {
            this.config.syncWithConfig(file, plugin.getResource("config.yml"), "tools");
        } catch (IOException error) {
            error.printStackTrace();
            return;
        }

        loadSettingsEditor();
    }

    public void saveConfiguration() {
        try {
            config.save(new File(plugin.getDataFolder(), "config.yml"));
        } catch (IOException error) {
            error.printStackTrace();
            return;
        }
        ToolsHandler.reload();
        DataHandler.reload();
    }

    public void reloadConfiguration() {
        try {
            config.load(new File(plugin.getDataFolder(), "config.yml"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Inventory getSettingsEditor() {
        EditorMenu editorMenu = new EditorMenu(EditorMenuType.SETTINGS);
        Inventory inventory = Bukkit.createInventory(editorMenu, 9 * 5, "" + ChatColor.AQUA + ChatColor.BOLD + "WildTools");
        editorMenu.setInventory(inventory);
        inventory.setContents(settingsEditor);
        return inventory;
    }

    public Inventory getToolsEditor() {
        List<String> toolNames = new ArrayList<>(config.getConfigurationSection("tools").getKeys(false));
        int size = (toolNames.size() / 9) + 1;

        if (toolNames.size() % 9 != 0)
            size++;

        EditorMenu editorMenu = new EditorMenu(EditorMenuType.TOOLS);
        Inventory editor = Bukkit.createInventory(editorMenu, size * 9, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Tools Editor");
        editorMenu.setInventory(editor);

        toolNames.sort(Comparator.naturalOrder());

        for (int i = 0; i < toolNames.size(); i++) {
            editor.setItem(i, new ItemBuilder(Material.valueOf(getFromConfig(toolNames.get(i), "type", String.class, "")))
                    .withName(getFromConfig(toolNames.get(i), "name", String.class, ""))
                    .withLore("&7Click here to edit " + toolNames.get(i)).build());
        }

        editor.setItem(editor.getSize() - 1, new ItemBuilder(Materials.MAP)
                .withName("&bNew tool").withLore("&7Click to create a new tool.").build());

        return editor;
    }

    @SuppressWarnings("unchecked")
    public Inventory getToolEditor(String toolName) {
        if (plugin.getToolsManager().getTool(toolName) == null)
            return getSettingsEditor();

        EditorMenu editorMenu = new EditorMenu(EditorMenuType.TOOL_EDITOR);
        Inventory editor = Bukkit.createInventory(editorMenu, 9 * 6, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Tool Editor");
        editorMenu.setInventory(editor);

        ItemStack bluePane = new ItemBuilder(Materials.LIGHT_BLUE_STAINED_GLASS_PANE).withName("&6").build();

        for (int i = 0; i < editor.getSize(); i++)
            editor.setItem(i, bluePane);

        editor.setItem(46, new ItemBuilder(Material.valueOf(getFromConfig(toolName, "type", String.class, "")))
                .withName("&bTool Type").withLore("&7Type: " + getFromConfig(toolName, "type", String.class, "")).build());
        editor.setItem(47, new ItemBuilder(Material.NAME_TAG)
                .withName("&bTool Name").withLore("&7Name: " + getFromConfig(toolName, "name", String.class, "")).build());
        editor.setItem(48, new ItemBuilder(Materials.MAP)
                .withName("&bTool Lore").withLore("&7Lore:", getFromConfig(toolName, "lore", List.class, new ArrayList<>())).build());

        editor.setItem(10, new ItemBuilder(Materials.CLOCK)
                .withName("&bTool Cooldown").withLore("&7Cooldown: " + getFromConfig(toolName, "cooldown", Integer.class, 0)).build());
        editor.setItem(11, new ItemBuilder(Materials.IRON_BARS)
                .withName("&bTool Unbreakable").withLore("&7Unbreakable: " + getFromConfig(toolName, "unbreakable", Boolean.class, false)).build());
        editor.setItem(12, new ItemBuilder(Material.CHEST)
                .withName("&bTool Keep-Inventory").withLore("&7Keep-Inventory: " + getFromConfig(toolName, "keep-inventory", Boolean.class, false)).build());
        editor.setItem(13, new ItemBuilder(Material.QUARTZ_BLOCK)
                .withName("&bTool Whitelisted Blocks").withLore("&7Whitelisted Blocks: ",
                        getFromConfig(toolName, "whitelisted-blocks", List.class, new ArrayList<>())).build());
        editor.setItem(14, new ItemBuilder(Material.HOPPER)
                .withName("&bTool Auto-Collect").withLore("&7Auto-Collect: " + getFromConfig(toolName, "auto-collect", Boolean.class, false)).build());
        editor.setItem(15, new ItemBuilder(Material.ANVIL)
                .withName("&bTool Uses").withLore("&7Uses: " + getFromConfig(toolName, "uses", Integer.class, -1)).build());
        editor.setItem(16, new ItemBuilder(Materials.ENCHANTED_GOLDEN_APPLE)
                .withName("&bTool Enchants").withLore("&7Enchantments:", getEnchants(toolName)).build());
        editor.setItem(19, new ItemBuilder(Material.QUARTZ)
                .withName("&bTool Whitelisted Drops").withLore("&7Whitelisted Drops: ",
                        getFromConfig(toolName, "whitelisted-drops", List.class, new ArrayList<>())).build());
        editor.setItem(20, new ItemBuilder(Material.ENCHANTED_BOOK)
                .withName("&bTool Silktouch").withLore("&7Silktouch: " + getFromConfig(toolName, "silk-touch", Boolean.class, false)).build());
        editor.setItem(21, new ItemBuilder(Materials.REDSTONE_TORCH)
                .withName("&bTool Only-Inside-Claim").withLore("&7Only-Inside-Claim: " + getFromConfig(toolName, "only-inside-claim", Boolean.class, false)).build());
        editor.setItem(22, new ItemBuilder(Material.TRIPWIRE_HOOK)
                .withName("&bOmni Tool").withLore("&7Omni Tool: " + getFromConfig(toolName, "omni-tool", Boolean.class, false)).build());
        editor.setItem(23, new ItemBuilder(Material.COAL_BLOCK)
                .withName("&bTool Blacklisted Blocks").withLore("&7Blacklisted Blocks: ",
                        getFromConfig(toolName, "blacklisted-blocks", List.class, new ArrayList<>())).build());
        editor.setItem(24, new ItemBuilder(Material.ARROW)
                .withName("&bTool Only-Same-Type").withLore("&7Only-Same-Type: " + getFromConfig(toolName, "only-same-type", Boolean.class, false)).build());
        editor.setItem(25, new ItemBuilder(Material.NETHER_STAR)
                .withName("&bTool Glow").withLore("&7Glow: " + getFromConfig(toolName, "glow", Boolean.class, false)).build());
        editor.setItem(28, new ItemBuilder(Materials.CAULDRON)
                .withName("&bTool Spigot Unbreakable").withLore("&7Spigot Unbreakable: " + getFromConfig(toolName, "spigot-unbreakable", Boolean.class, false)).build());
        editor.setItem(29, new ItemBuilder(Material.COAL)
                .withName("&bTool Blacklisted Drops").withLore("&7Blacklisted Drops: ",
                        getFromConfig(toolName, "blacklisted-drops", List.class, new ArrayList<>())).build());
        editor.setItem(30, new ItemBuilder(Material.SUGAR)
                .withName("&bTool Instant-Break").withLore("&7Instant-Break: " + getFromConfig(toolName, "instant-break", Boolean.class, false)).build());
        editor.setItem(31, new ItemBuilder(Materials.EXPERIENCE_BOTTLE.toBukkitType())
                .withName("&bTool Anvil Combine Exp").withLore("&7Anvil Combine Exp: " + getFromConfig(toolName, "anvil-combine-exp", Integer.class, -1)).build());
        editor.setItem(32, new ItemBuilder(Material.GOLD_INGOT)
                .withName("&bTool Anvil Combine Limit").withLore("&7Anvil Combine Limit: " + getFromConfig(toolName, "anvil-combine-limit", Integer.class, -1)).build());

        editor.setItem(33, new ItemStack(Material.AIR));
        editor.setItem(34, new ItemStack(Material.AIR));

        ToolMode toolMode = ToolMode.valueOf(getFromConfig(toolName, "tool-mode", String.class, ""));

        if (toolMode == ToolMode.BUILDER) {
            editor.setItem(50, new ItemBuilder(Material.FEATHER)
                    .withName("&bTool Length").withLore("&7Length: " + getFromConfig(toolName, "length", Integer.class, 0)).build());
        } else if (toolMode == ToolMode.CUBOID) {
            editor.setItem(50, new ItemBuilder(Material.FEATHER)
                    .withName("&bTool Break-Level").withLore("&7Break-Level: " + getFromConfig(toolName, "break-level", Integer.class, 3)).build());
        } else if (toolMode == ToolMode.HARVESTER) {
            editor.setItem(50, new ItemBuilder(Material.FEATHER)
                    .withName("&bTool Radius").withLore("&7Radius: " + getFromConfig(toolName, "radius", Integer.class, 0)).build());
            editor.setItem(51, new ItemBuilder(Materials.FARMLAND)
                    .withName("&bTool Farmland Radius").withLore("&7Farmland Radius: " + getFromConfig(toolName, "farmland-radius", Integer.class, 0)).build());
            editor.setItem(52, new ItemBuilder(Material.REDSTONE)
                    .withName("&bTool Active Action").withLore("&7Active Action: " + getFromConfig(toolName, "active-action", String.class, "")).build());
            editor.setItem(53, new ItemBuilder(Material.GOLD_INGOT)
                    .withName("&bTool Multiplier").withLore("&7Multiplier: " + getFromConfig(toolName, "multiplier", Double.class, 1.0)).build());
        } else if (toolMode == ToolMode.ICE) {
            editor.setItem(50, new ItemBuilder(Material.FEATHER)
                    .withName("&bTool Radius").withLore("&7Radius: " + getFromConfig(toolName, "radius", Integer.class, 0)).build());
        } else if (toolMode == ToolMode.CANNON) {
            editor.setItem(50, new ItemBuilder(Material.TNT)
                    .withName("&bTool TNT-Amount").withLore("&7TNT-Amount: " + getFromConfig(toolName, "tnt-amount", Integer.class, 0)).build());
        } else if (toolMode == ToolMode.CRAFTING) {
            editor.setItem(50, new ItemBuilder(Materials.CRAFTING_TABLE)
                    .withName("&bTool Recipes").withLore("&7Recipes: ", getFromConfig(toolName, "craftings", List.class, new ArrayList<>())).build());
        } else if (toolMode == ToolMode.SELL) {
            editor.setItem(50, new ItemBuilder(Material.GOLD_INGOT)
                    .withName("&bTool Multiplier").withLore("&7Multiplier: " + getFromConfig(toolName, "multiplier", Double.class, 1.0)).build());
        }

        return editor;
    }

    public void createTool(String toolName, ToolMode toolMode) {
        config.set("tools." + toolName + ".type", "STICK");
        config.set("tools." + toolName + ".tool-mode", toolMode.name());

        switch (toolMode) {
            case BUILDER:
                config.set("tools." + toolName + ".length", 1);
                break;
            case CUBOID:
                config.set("tools." + toolName + ".break-level", 1);
                break;
            case HARVESTER:
                config.set("tools." + toolName + ".radius", 1);
                config.set("tools." + toolName + ".farmland-radius", 1);
                config.set("tools." + toolName + ".active-action", "RIGHT_CLICK");
                break;
            case ICE:
                config.set("tools." + toolName + ".radius", 1);
                break;
            case CANNON:
                config.set("tools." + toolName + ".tnt-amount", 1);
                break;
            case CRAFTING:
                config.set("tools." + toolName + ".craftings", Arrays.asList("TNT", "DIAMOND"));
                break;
        }
    }

    private <T> T getFromConfig(String toolName, String key, Class<T> type, T def) {
        return type.cast(config.get("tools." + toolName + "." + key, def));
    }

    private void loadSettingsEditor() {
        Inventory editor = Bukkit.createInventory(null, 9 * 5);

        ItemStack glassPane = new ItemBuilder(Materials.BLACK_STAINED_GLASS_PANE).withName("&6").build();

        for (int i = 0; i < 9; i++)
            editor.setItem(i, glassPane);

        for (int i = 36; i < 45; i++)
            editor.setItem(i, glassPane);

        editor.setItem(9, glassPane);
        editor.setItem(17, glassPane);
        editor.setItem(18, glassPane);
        editor.setItem(26, glassPane);
        editor.setItem(27, glassPane);
        editor.setItem(35, glassPane);

        editor.setItem(21, new ItemBuilder(Material.DIAMOND)
                .withName("&b&lPrices List").withLore("&7Click to edit prices list.").build());

        editor.setItem(23, new ItemBuilder(Material.DIAMOND_PICKAXE)
                .withName("&b&lTools Editor").withLore("&7Click to edit tools.").build());

        editor.setItem(40, new ItemBuilder(Material.EMERALD)
                .withName("&aSave Changes").withLore("&7Click to save all changes.").build());

        settingsEditor = editor.getContents();
    }

    private List<String> getEnchants(String toolName) {
        List<String> list = new ArrayList<>();

        if (config.contains("tools." + toolName + ".enchants")) {
            for (String enchantment : config.getStringList("tools." + toolName + ".enchants")) {
                try {
                    Enchantment ench = Enchantment.getByName(enchantment.split(":")[0]);
                    int level = Integer.parseInt(enchantment.split(":")[1]);
                    list.add(ChatColor.GRAY + " - " + ench.getName() + " " + getIntAsString(level));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        return list;
    }

    private String getIntAsString(int level) {
        switch (level) {
            case 1:
                return "I";
            case 2:
                return "II";
            case 3:
                return "III";
            case 4:
                return "IV";
            case 5:
                return "V";
            case 6:
                return "VI";
            case 7:
                return "VII";
            case 8:
                return "VIII";
            case 9:
                return "IX";
            case 10:
                return "X";
            default:
                return "" + level;
        }
    }

    public static class EditorMenu implements InventoryHolder {

        private final EditorMenuType menuType;

        private Inventory inventory;

        private EditorMenu(EditorMenuType menuType) {
            this.menuType = menuType;
        }

        public EditorMenuType getMenuType() {
            return menuType;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        @Override
        public Inventory getInventory() {
            return this.inventory;
        }

    }

    public enum EditorMenuType {

        SETTINGS,
        TOOLS,
        TOOL_EDITOR

    }

}
