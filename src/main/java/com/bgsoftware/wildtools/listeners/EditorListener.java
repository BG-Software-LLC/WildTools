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

@SuppressWarnings({"unused", "all"})
public class EditorListener implements Listener {

    private WildToolsPlugin plugin;

    private Set<UUID> noResetClose = new HashSet<>();
    private Map<UUID, String> toolTypes = new HashMap<>();
    private Map<UUID, String> toolValues = new HashMap<>();

    private String[] integerValues = new String[] {"length", "break-level", "radius", "farmland-radius", "tnt-amount",
            "cooldown", "uses", "anvil-combine-exp", "anvil-combine-limit"};
    private String[] doubleValues = new String[] {"multiplier"};
    private String[] booleanValues = new String[] {"auto-collect", "silk-touch", "only-same-type", "only-inside-claim",
            "unbreakable", "keep-inventory", "glow", "omni-tool", "spigot-unbreakable", "instant-break"};
    private String[] listValues = new String[] {"prices-list", "craftings", "enchants", "whitelisted-blocks", "blacklisted-blocks",
            "whitelisted-drops", "blacklisted-drops"};

    public EditorListener(WildToolsPlugin plugin){
        this.plugin = plugin;
    }

    /**
     * The following two events are here for patching a dupe glitch caused
     * by shift clicking and closing the inventory in the same time.
     */

    private Map<UUID, ItemStack> latestClickedItem = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClickMonitor(InventoryClickEvent e){
        if(e.getCurrentItem() != null && e.isCancelled() && isEditorMenu(e.getView())) {
            latestClickedItem.put(e.getWhoClicked().getUniqueId(), e.getCurrentItem());
            Scheduler.runTask(() -> latestClickedItem.remove(e.getWhoClicked().getUniqueId()), 20L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryCloseMonitor(InventoryCloseEvent e){
        if(latestClickedItem.containsKey(e.getPlayer().getUniqueId())){
            ItemStack clickedItem = latestClickedItem.get(e.getPlayer().getUniqueId());
            Scheduler.runTask(e.getPlayer(), () -> {
                e.getPlayer().getInventory().removeItem(clickedItem);
                ((Player) e.getPlayer()).updateInventory();
            }, 1L);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        if(e.getInventory() == null || !isEditorMenu(e.getView()) || !(e.getWhoClicked() instanceof Player))
            return;

        Player player = (Player) e.getWhoClicked();

        EditorHandler.EditorMenu editorMenu = (EditorHandler.EditorMenu) e.getView().getTopInventory().getHolder();
        EditorHandler.EditorMenuType menuType = editorMenu.getMenuType();

        if(menuType == EditorHandler.EditorMenuType.SETTINGS){
            e.setCancelled(true);

            switch (e.getRawSlot()){
                case 21:
                    toolValues.put(player.getUniqueId(), "prices-list");
                    player.sendMessage("" + ChatColor.AQUA + ChatColor.BOLD + "WildTools" + ChatColor.GRAY + " Please enter a new value (-cancel to cancel):");
                    player.sendMessage("" + ChatColor.AQUA + ChatColor.BOLD + "WildTools" + ChatColor.GRAY + " If you enter a value that is already in the list, it will be removed.");
                    Scheduler.runTask(player, () -> player.closeInventory(), 1L);
                    break;
                case 23:
                    noResetClose.add(player.getUniqueId());
                    player.openInventory(plugin.getEditor().getToolsEditor());
                    break;
                case 40:
                    Scheduler.runTaskAsync(() -> {
                        plugin.getEditor().saveConfiguration();
                        player.sendMessage("" + ChatColor.AQUA + ChatColor.BOLD + "WildTools " + ChatColor.GRAY + "Saved configuration successfully.");
                    });
                    break;
            }

        }

        else if(menuType == EditorHandler.EditorMenuType.TOOLS){
            e.setCancelled(true);

            if(e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
                return;

            List<String> tools = new ArrayList<>(plugin.getEditor().config.getConfigurationSection("tools").getKeys(false));
            tools.sort(Comparator.naturalOrder());

            if(e.getRawSlot() < tools.size()){
                toolTypes.put(player.getUniqueId(), tools.get(e.getRawSlot()));
                player.openInventory(plugin.getEditor().getToolEditor(tools.get(e.getRawSlot())));
            }
            else{
                toolTypes.put(player.getUniqueId(), "$new_tool");
                player.closeInventory();
                player.sendMessage("" + ChatColor.AQUA + ChatColor.BOLD + "WildTools " + ChatColor.GRAY + "Enter a name for your tool (-cancel to cancel):");
            }

        }

        else if(menuType == EditorHandler.EditorMenuType.TOOL_EDITOR){
            e.setCancelled(true);

            String toolName = toolTypes.get(player.getUniqueId());
            ToolMode toolMode = ToolMode.valueOf(plugin.getEditor().config.getString("tools." + toolName + ".tool-mode"));

            switch (e.getRawSlot()){
                case 10:
                    toolValues.put(player.getUniqueId(), "cooldown");
                    break;
                case 11:
                    toolValues.put(player.getUniqueId(), "unbreakable");
                    break;
                case 12:
                    toolValues.put(player.getUniqueId(), "keep-inventory");
                    break;
                case 13:
                    toolValues.put(player.getUniqueId(), "whitelisted-blocks");
                    break;
                case 14:
                    toolValues.put(player.getUniqueId(), "auto-collect");
                    break;
                case 15:
                    toolValues.put(player.getUniqueId(), "uses");
                    break;
                case 16:
                    toolValues.put(player.getUniqueId(), "enchants");
                    break;
                case 19:
                    toolValues.put(player.getUniqueId(), "whitelisted-drops");
                    break;
                case 20:
                    toolValues.put(player.getUniqueId(), "silk-touch");
                    break;
                case 21:
                    toolValues.put(player.getUniqueId(), "only-inside-claim");
                    break;
                case 22:
                    toolValues.put(player.getUniqueId(), "omni-tool");
                    break;
                case 23:
                    toolValues.put(player.getUniqueId(), "blacklisted-blocks");
                    break;
                case 24:
                    toolValues.put(player.getUniqueId(), "only-same-type");
                    break;
                case 25:
                    toolValues.put(player.getUniqueId(), "glow");
                    break;
                case 28:
                    toolValues.put(player.getUniqueId(), "spigot-unbreakable");
                    break;
                case 29:
                    toolValues.put(player.getUniqueId(), "blacklisted-drops");
                    break;
                case 30:
                    toolValues.put(player.getUniqueId(), "instant-break");
                    break;
                case 31:
                    toolValues.put(player.getUniqueId(), "anvil-combine-exp");
                    break;
                case 32:
                    toolValues.put(player.getUniqueId(), "anvil-combine-limit");
                    break;
                case 46:
                    toolValues.put(player.getUniqueId(), "type");
                    break;
                case 47:
                    toolValues.put(player.getUniqueId(), "name");
                    break;
                case 48:
                    toolValues.put(player.getUniqueId(), "lore");
                    break;
                case 50:
                    if(toolMode == ToolMode.BUILDER)
                        toolValues.put(player.getUniqueId(), "length");
                    else if(toolMode == ToolMode.CUBOID)
                        toolValues.put(player.getUniqueId(), "break-level");
                    else if(toolMode == ToolMode.HARVESTER)
                        toolValues.put(player.getUniqueId(), "radius");
                    else if(toolMode == ToolMode.ICE)
                        toolValues.put(player.getUniqueId(), "radius");
                    else if(toolMode == ToolMode.CANNON)
                        toolValues.put(player.getUniqueId(), "tnt-amount");
                    else if(toolMode == ToolMode.CRAFTING)
                        toolValues.put(player.getUniqueId(), "craftings");
                    else if(toolMode == ToolMode.SELL)
                        toolValues.put(player.getUniqueId(), "multiplier");
                    else return;
                    break;
                case 51:
                    if(toolMode == ToolMode.HARVESTER)
                        toolValues.put(player.getUniqueId(), "farmland-radius");
                    else return;
                    break;
                case 52:
                    if(toolMode == ToolMode.HARVESTER)
                        toolValues.put(player.getUniqueId(), "active-action");
                    else return;
                    break;
                case 53:
                    if(toolMode == ToolMode.HARVESTER)
                        toolValues.put(player.getUniqueId(), "multiplier");
                    else return;
                    break;
                default:
                    return;
            }

            Scheduler.runTask(player, () -> player.closeInventory(), 1L);
            player.sendMessage("" + ChatColor.AQUA + ChatColor.BOLD + "WildTools" + ChatColor.GRAY + " Please enter a new value (-cancel to cancel):");

            if(Arrays.asList(listValues).contains(toolValues.get(player.getUniqueId()))){
                player.sendMessage("" + ChatColor.AQUA + ChatColor.BOLD + "WildTools" + ChatColor.GRAY + " If you enter a value that is already in the list, it will be removed.");
            }


        }

    }

    @EventHandler
    public void onEditorClose(InventoryCloseEvent e){
        if(e.getInventory() == null || !isEditorMenu(e.getView()) || !(e.getPlayer() instanceof Player))
            return;

        Player player = (Player) e.getPlayer();

        Scheduler.runTask(player, () -> {
            if(!isEditorMenu(player.getOpenInventory()))
                return;

            EditorHandler.EditorMenu editorMenu = (EditorHandler.EditorMenu) player.getOpenInventory().getTopInventory().getHolder();
            EditorHandler.EditorMenuType menuType = editorMenu.getMenuType();

            if(menuType == EditorHandler.EditorMenuType.SETTINGS){
                if(!noResetClose.contains(player.getUniqueId())) {
                    Scheduler.runTaskAsync(() -> plugin.getEditor().reloadConfiguration());
                }
            }

            else if(menuType == EditorHandler.EditorMenuType.TOOLS){
                if(toolTypes.containsKey(player.getUniqueId()))
                    return;
                noResetClose.remove(player.getUniqueId());
                player.openInventory(plugin.getEditor().getSettingsEditor());
            }

            else if(menuType == EditorHandler.EditorMenuType.TOOL_EDITOR){
                if(toolValues.containsKey(player.getUniqueId()))
                    return;
                toolTypes.remove(player.getUniqueId());
                player.openInventory(plugin.getEditor().getToolsEditor());
            }

        }, 1L);
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e){
        if(!toolValues.containsKey(e.getPlayer().getUniqueId()) && !toolTypes.getOrDefault(e.getPlayer().getUniqueId(), "g").equals("$new_tool"))
            return;

        e.setCancelled(true);

        String toolName = toolTypes.getOrDefault(e.getPlayer().getUniqueId(), "");
        String toolValue = toolValues.getOrDefault(e.getPlayer().getUniqueId(), "");
        String path = "tools." + toolName + "." + toolValue;
        Object value = e.getMessage();

        if(!value.toString().equalsIgnoreCase("-cancel")){
            if(toolName.equals("$new_tool")){
                if(toolValue.isEmpty()) {
                    if (plugin.getEditor().config.contains("tools." + e.getMessage())) {
                        e.getPlayer().sendMessage(ChatColor.RED + "Please enter a unique name for your tool.");
                    } else {
                        toolValues.put(e.getPlayer().getUniqueId(), e.getMessage());
                        e.getPlayer().sendMessage("" + ChatColor.AQUA + ChatColor.BOLD + "WildTools" + ChatColor.GRAY + " Please enter a mode for your tool (-cancel for cancel):");
                    }
                    return;
                }else{
                    try{
                        ToolMode toolMode = ToolMode.valueOf(e.getMessage().toUpperCase());
                        plugin.getEditor().createTool(toolValues.get(e.getPlayer().getUniqueId()), toolMode);
                        toolTypes.put(e.getPlayer().getUniqueId(), toolValues.get(e.getPlayer().getUniqueId()));
                        Scheduler.runTask(() -> toolTypes.remove(e.getPlayer().getName()), 1L);
                    }catch(IllegalArgumentException ex){
                        e.getPlayer().sendMessage(ChatColor.RED + "Please enter a valid tool mode.");
                        return;
                    }
                }
            } else if(Arrays.asList(listValues).contains(toolValue)){
                if(toolValue.equals("prices-list"))
                    path = "prices-list";

                List<String> list = plugin.getEditor().config.getStringList(path);

                if (list.contains(value.toString())) {
                    list.remove(value.toString());
                    e.getPlayer().sendMessage("" + ChatColor.AQUA + ChatColor.BOLD + "WildTools" + ChatColor.GRAY + " Removed the value " + value.toString() + " from " + path);
                } else {
                    list.add(value.toString());
                    e.getPlayer().sendMessage("" + ChatColor.AQUA + ChatColor.BOLD + "WildTools" + ChatColor.GRAY + " Added the value " + value.toString() + " to " + path);
                }

                plugin.getEditor().config.set(path, list);

                if(toolValue.equals("prices-list")){
                    toolValues.remove(e.getPlayer().getName());
                    e.getPlayer().openInventory(plugin.getEditor().getSettingsEditor());
                    return;
                }
            }

            else{
                boolean valid = true;
                if(Arrays.asList(integerValues).contains(toolValue)){
                    try{
                        value = Integer.valueOf(value.toString());
                    }catch(IllegalArgumentException ex){
                        e.getPlayer().sendMessage(ChatColor.RED + "Please specify a valid number");
                        valid = false;
                    }
                }

                else if(Arrays.asList(doubleValues).contains(toolValue)){
                    try{
                        value = Double.valueOf(value.toString());
                    }catch(IllegalArgumentException ex){
                        e.getPlayer().sendMessage(ChatColor.RED + "Please specify a valid number");
                        valid = false;
                    }
                }

                else if(Arrays.asList(booleanValues).contains(toolValue)){
                    if(value.toString().equalsIgnoreCase("true") || value.toString().equalsIgnoreCase("false")){
                        value = Boolean.valueOf(value.toString());
                    }else{
                        e.getPlayer().sendMessage(ChatColor.RED + "Please specify a valid boolean");
                        valid = false;
                    }
                }

                if(valid) {
                    plugin.getEditor().config.set(path, value);
                    e.getPlayer().sendMessage("" + ChatColor.AQUA + ChatColor.BOLD + "WildTools" + ChatColor.GRAY + " Changed value of " + path + " to " + value.toString());
                }
            }
        }

        Scheduler.runTask(e.getPlayer(), () -> e.getPlayer().openInventory(plugin.getEditor().getToolEditor(toolName)));
        toolValues.remove(e.getPlayer().getUniqueId());
    }

    private static boolean isEditorMenu(InventoryView inventoryView) {
        Inventory topInventory = inventoryView.getTopInventory();
        return topInventory != null && topInventory.getHolder() instanceof EditorHandler.EditorMenu;
    }

}
