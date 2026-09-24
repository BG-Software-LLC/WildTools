package com.bgsoftware.wildtools.listeners;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.handlers.EditorHandler;
import com.bgsoftware.wildtools.scheduler.Scheduler;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class EditorListener implements Listener {

    private final WildToolsPlugin plugin;

    private final Set<UUID> noResetClose = new HashSet<>();
    private final Map<UUID, String> toolTypes = new HashMap<>();
    private final Map<UUID, String> toolValues = new HashMap<>();

    private final String[] integerValues = new String[] {"length", "break-level", "radius", "farmland-radius", "tnt-amount",
            "cooldown", "uses", "anvil-combine-exp", "anvil-combine-limit"};
    private final String[] doubleValues = new String[] {"multiplier"};
    private final String[] booleanValues = new String[] {"auto-collect", "silk-touch", "only-same-type", "only-inside-claim",
            "unbreakable", "keep-inventory", "glow", "omni-tool", "spigot-unbreakable", "instant-break"};
    private final String[] listValues = new String[] {"prices-list", "craftings", "enchants", "whitelisted-blocks", "blacklisted-blocks",
            "whitelisted-drops", "blacklisted-drops"};

    public EditorListener(WildToolsPlugin plugin){
        this.plugin = plugin;
    }

    /**
     * The following two events are here for patching a dupe glitch caused
     * by shift clicking and closing the inventory in the same time.
     */

    private final Map<UUID, ItemStack> latestClickedItem = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClickMonitor(InventoryClickEvent e){
        if (e.getCurrentItem() != null && e.isCancelled() && isEditorMenu(e.getView())) {
            this.latestClickedItem.put(e.getWhoClicked().getUniqueId(), e.getCurrentItem());
            Scheduler.runTask(() -> this.latestClickedItem.remove(e.getWhoClicked().getUniqueId()), 20L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    @SuppressWarnings("deprecation")
    public void onInventoryCloseMonitor(InventoryCloseEvent e){
        if (this.latestClickedItem.containsKey(e.getPlayer().getUniqueId())){
            ItemStack clickedItem = this.latestClickedItem.get(e.getPlayer().getUniqueId());
            Scheduler.runTask(e.getPlayer(), () -> {
                e.getPlayer().getInventory().removeItem(clickedItem);
                ((Player) e.getPlayer()).updateInventory();
            }, 1L);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        if (e.getInventory() == null || !isEditorMenu(e.getView()) || !(e.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) e.getWhoClicked();

        EditorHandler.EditorMenu editorMenu = (EditorHandler.EditorMenu) e.getView().getTopInventory().getHolder();
        EditorHandler.EditorMenuType menuType = editorMenu.getMenuType();

        if (menuType == EditorHandler.EditorMenuType.SETTINGS) {
            e.setCancelled(true);

            switch (e.getRawSlot()) {
                case 21:
                    this.toolValues.put(player.getUniqueId(), "prices-list");
                    player.sendMessage("" + ChatColor.AQUA + ChatColor.BOLD + "WildTools" + ChatColor.GRAY
                            + " Please enter a new value (-cancel to cancel):");
                    player.sendMessage("" + ChatColor.AQUA + ChatColor.BOLD + "WildTools" + ChatColor.GRAY
                            + " If you enter a value that is already in the list, it will be removed.");
                    Scheduler.runTask(player, player::closeInventory, 1L);
                    break;
                case 23:
                    this.noResetClose.add(player.getUniqueId());
                    player.openInventory(this.plugin.getEditor().getToolsEditor());
                    break;
                case 40:
                    Scheduler.runTaskAsync(() -> {
                        this.plugin.getEditor().saveConfiguration();
                        player.sendMessage("" + ChatColor.AQUA + ChatColor.BOLD + "WildTools " + ChatColor.GRAY
                                + "Saved configuration successfully.");
                    });
                    break;
            }

        } else if (menuType == EditorHandler.EditorMenuType.TOOLS) {
            e.setCancelled(true);

            if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) {
                return;
            }

            List<String> tools = new ArrayList<>(this.plugin.getEditor().config.getConfigurationSection("tools").getKeys(false));
            tools.sort(Comparator.naturalOrder());

            if (e.getRawSlot() < tools.size()) {
                this.toolTypes.put(player.getUniqueId(), tools.get(e.getRawSlot()));
                player.openInventory(this.plugin.getEditor().getToolEditor(tools.get(e.getRawSlot())));
            } else {
                this.toolTypes.put(player.getUniqueId(), "$new_tool");
                player.closeInventory();
                player.sendMessage("" + ChatColor.AQUA + ChatColor.BOLD + "WildTools " + ChatColor.GRAY
                        + "Enter a name for your tool (-cancel to cancel):");
            }

        } else if(menuType == EditorHandler.EditorMenuType.TOOL_EDITOR) {
            e.setCancelled(true);

            String toolName = this.toolTypes.get(player.getUniqueId());
            ToolMode toolMode = ToolMode.valueOf(this.plugin.getEditor().config.getString("tools." + toolName + ".tool-mode"));

            switch (e.getRawSlot()){
                case 10:
                    this.toolValues.put(player.getUniqueId(), "cooldown");
                    break;
                case 11:
                    this.toolValues.put(player.getUniqueId(), "unbreakable");
                    break;
                case 12:
                    this.toolValues.put(player.getUniqueId(), "keep-inventory");
                    break;
                case 13:
                    this.toolValues.put(player.getUniqueId(), "whitelisted-blocks");
                    break;
                case 14:
                    this.toolValues.put(player.getUniqueId(), "auto-collect");
                    break;
                case 15:
                    this.toolValues.put(player.getUniqueId(), "uses");
                    break;
                case 16:
                    this.toolValues.put(player.getUniqueId(), "enchants");
                    break;
                case 19:
                    this.toolValues.put(player.getUniqueId(), "whitelisted-drops");
                    break;
                case 20:
                    this.toolValues.put(player.getUniqueId(), "silk-touch");
                    break;
                case 21:
                    this.toolValues.put(player.getUniqueId(), "only-inside-claim");
                    break;
                case 22:
                    this.toolValues.put(player.getUniqueId(), "omni-tool");
                    break;
                case 23:
                    this.toolValues.put(player.getUniqueId(), "blacklisted-blocks");
                    break;
                case 24:
                    this.toolValues.put(player.getUniqueId(), "only-same-type");
                    break;
                case 25:
                    this.toolValues.put(player.getUniqueId(), "glow");
                    break;
                case 28:
                    this.toolValues.put(player.getUniqueId(), "spigot-unbreakable");
                    break;
                case 29:
                    this.toolValues.put(player.getUniqueId(), "blacklisted-drops");
                    break;
                case 30:
                    this.toolValues.put(player.getUniqueId(), "instant-break");
                    break;
                case 31:
                    this.toolValues.put(player.getUniqueId(), "anvil-combine-exp");
                    break;
                case 32:
                    this.toolValues.put(player.getUniqueId(), "anvil-combine-limit");
                    break;
                case 46:
                    this.toolValues.put(player.getUniqueId(), "type");
                    break;
                case 47:
                    this.toolValues.put(player.getUniqueId(), "name");
                    break;
                case 48:
                    this.toolValues.put(player.getUniqueId(), "lore");
                    break;
                case 50:
                    switch (toolMode) {
                        case BUILDER:
                            this.toolValues.put(player.getUniqueId(), "length");
                            break;
                        case CANNON:
                            this.toolValues.put(player.getUniqueId(), "tnt-amount");
                            break;
                        case CRAFTING:
                            this.toolValues.put(player.getUniqueId(), "craftings");
                            break;
                        case CUBOID:
                            this.toolValues.put(player.getUniqueId(), "break-level");
                            break;
                        case HARVESTER:
                        case ICE:
                            this.toolValues.put(player.getUniqueId(), "radius");
                            break;
                        case SELL:
                            this.toolValues.put(player.getUniqueId(), "multiplier");
                            break;
                        default:
                            return;
                    }
                    break;
                case 51:
                    if (toolMode == ToolMode.HARVESTER) {
                        this.toolValues.put(player.getUniqueId(), "farmland-radius");
                    } else {
                        return;
                    }
                    break;
                case 52:
                    if (toolMode == ToolMode.HARVESTER) {
                        toolValues.put(player.getUniqueId(), "active-action");
                    } else {
                        return;
                    }
                    break;
                case 53:
                    if (toolMode == ToolMode.HARVESTER) {
                        toolValues.put(player.getUniqueId(), "multiplier");
                    } else {
                        return;
                    }
                    break;
                default:
                    return;
            }

            Scheduler.runTask(player, player::closeInventory, 1L);
            player.sendMessage("" + ChatColor.AQUA + ChatColor.BOLD + "WildTools" + ChatColor.GRAY
                    + " Please enter a new value (-cancel to cancel):");

            if (Arrays.asList(this.listValues).contains(this.toolValues.get(player.getUniqueId()))){
                player.sendMessage("" + ChatColor.AQUA + ChatColor.BOLD + "WildTools" + ChatColor.GRAY
                        + " If you enter a value that is already in the list, it will be removed.");
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e){
        if (e.getInventory() == null || !isEditorMenu(e.getView()) || !(e.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) e.getPlayer();

        EditorHandler.EditorMenu editorMenu = (EditorHandler.EditorMenu) player.getOpenInventory().getTopInventory().getHolder();
        EditorHandler.EditorMenuType menuType = editorMenu.getMenuType();

        Scheduler.runTask(player, () -> {
            if (!isEditorMenu(player.getOpenInventory())) {
                return;
            }

            if (menuType == EditorHandler.EditorMenuType.SETTINGS) {
                if (!this.noResetClose.contains(player.getUniqueId())) {
                    Scheduler.runTaskAsync(() -> this.plugin.getEditor().reloadConfiguration());
                }
            } else if (menuType == EditorHandler.EditorMenuType.TOOLS) {
                if (this.toolTypes.containsKey(player.getUniqueId())) {
                    return;
                }

                this.noResetClose.remove(player.getUniqueId());
                player.openInventory(this.plugin.getEditor().getSettingsEditor());
            } else if (menuType == EditorHandler.EditorMenuType.TOOL_EDITOR) {
                if (this.toolValues.containsKey(player.getUniqueId())) {
                    return;
                }

                this.toolTypes.remove(player.getUniqueId());
                player.openInventory(this.plugin.getEditor().getToolsEditor());
            }

        }, 1L);
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e){
        if (!this.toolValues.containsKey(e.getPlayer().getUniqueId())
                && !this.toolTypes.getOrDefault(e.getPlayer().getUniqueId(), "g").equals("$new_tool")) {
            return;
        }

        e.setCancelled(true);

        String toolName = this.toolTypes.getOrDefault(e.getPlayer().getUniqueId(), "");
        String toolValue = this.toolValues.getOrDefault(e.getPlayer().getUniqueId(), "");
        String path = "tools." + toolName + "." + toolValue;
        Object value = e.getMessage();

        if (!value.toString().equalsIgnoreCase("-cancel")) {
            if (toolName.equals("$new_tool")) {
                if (toolValue.isEmpty()) {
                    if (this.plugin.getEditor().config.contains("tools." + e.getMessage())) {
                        e.getPlayer().sendMessage(ChatColor.RED + "Please enter a unique name for your tool.");
                    } else {
                        this.toolValues.put(e.getPlayer().getUniqueId(), e.getMessage());
                        e.getPlayer().sendMessage("" + ChatColor.AQUA + ChatColor.BOLD + "WildTools" + ChatColor.GRAY
                                + " Please enter a mode for your tool (-cancel for cancel):");
                    }

                    return;
                } else {
                    try {
                        ToolMode toolMode = ToolMode.valueOf(e.getMessage().toUpperCase());
                        this.plugin.getEditor().createTool(this.toolValues.get(e.getPlayer().getUniqueId()), toolMode);
                        this.toolTypes.put(e.getPlayer().getUniqueId(), this.toolValues.get(e.getPlayer().getUniqueId()));
                        Scheduler.runTask(() -> this.toolTypes.remove(e.getPlayer().getUniqueId()), 1L);
                    } catch (IllegalArgumentException ex) {
                        e.getPlayer().sendMessage(ChatColor.RED + "Please enter a valid tool mode.");
                        return;
                    }
                }
            } else if (Arrays.asList(this.listValues).contains(toolValue)) {
                if (toolValue.equals("prices-list")) {
                    path = "prices-list";
                }

                List<String> list = this.plugin.getEditor().config.getStringList(path);

                if (list.contains(value.toString())) {
                    list.remove(value.toString());
                    e.getPlayer().sendMessage("" + ChatColor.AQUA + ChatColor.BOLD + "WildTools" + ChatColor.GRAY
                            + " Removed the value " + value + " from " + path);
                } else {
                    list.add(value.toString());
                    e.getPlayer().sendMessage("" + ChatColor.AQUA + ChatColor.BOLD + "WildTools" + ChatColor.GRAY
                            + " Added the value " + value + " to " + path);
                }

                this.plugin.getEditor().config.set(path, list);

                if (toolValue.equals("prices-list")) {
                    this.toolValues.remove(e.getPlayer().getUniqueId());
                    e.getPlayer().openInventory(this.plugin.getEditor().getSettingsEditor());
                    return;
                }
            } else {
                boolean valid = true;

                if (Arrays.asList(this.integerValues).contains(toolValue)) {
                    try {
                        value = Integer.valueOf(value.toString());
                    } catch(IllegalArgumentException ex) {
                        e.getPlayer().sendMessage(ChatColor.RED + "Please specify a valid number");
                        valid = false;
                    }
                } else if (Arrays.asList(this.doubleValues).contains(toolValue)) {
                    try {
                        value = Double.valueOf(value.toString());
                    } catch (IllegalArgumentException ex) {
                        e.getPlayer().sendMessage(ChatColor.RED + "Please specify a valid number");
                        valid = false;
                    }
                } else if (Arrays.asList(this.booleanValues).contains(toolValue)) {
                    if (value.toString().equalsIgnoreCase("true") || value.toString().equalsIgnoreCase("false")) {
                        value = Boolean.valueOf(value.toString());
                    } else {
                        e.getPlayer().sendMessage(ChatColor.RED + "Please specify a valid boolean");
                        valid = false;
                    }
                }

                if (valid) {
                    this.plugin.getEditor().config.set(path, value);
                    e.getPlayer().sendMessage("" + ChatColor.AQUA + ChatColor.BOLD + "WildTools" + ChatColor.GRAY
                            + " Changed value of " + path + " to " + value);
                }
            }
        }

        Scheduler.runTask(e.getPlayer(), () -> e.getPlayer().openInventory(this.plugin.getEditor().getToolEditor(toolName)));
        this.toolValues.remove(e.getPlayer().getUniqueId());
    }

    private static boolean isEditorMenu(InventoryView inventoryView) {
        Inventory topInventory = inventoryView.getTopInventory();
        return topInventory != null && topInventory.getHolder() instanceof EditorHandler.EditorMenu;
    }

}
