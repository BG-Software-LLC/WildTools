package com.bgsoftware.wildtools.listeners;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import com.bgsoftware.wildtools.tools.ToolBreaksTracker;
import com.bgsoftware.wildtools.tools.WCannonTool;
import com.bgsoftware.wildtools.utils.WSelection;
import com.bgsoftware.wildtools.utils.items.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {

    private final Map<UUID, LinkedList<ItemStack>> keepInventoryTools = new HashMap<>();

    private final WildToolsPlugin plugin;

    public PlayerListener(WildToolsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Just notifies me if the server is using WildTools
     */

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (e.getPlayer().getUniqueId().toString().equals("45713654-41bf-45a1-aa6f-00fe6598703b")) {
            Bukkit.getScheduler().runTaskLater(plugin, () ->
                    sendMessage(e.getPlayer(), "&8[&fWildSeries&8] &7This server is using WildTools v" + plugin.getDescription().getVersion()), 5L);
        }

        if (e.getPlayer().isOp() && plugin.getUpdater().isOutdated()) {
            Bukkit.getScheduler().runTaskLater(plugin, () ->
                    sendMessage(e.getPlayer(), "&b&lWildTools &7A new version is available (v" + plugin.getUpdater().getLatestVersion() + ")!"), 20L);
        }

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        ToolBreaksTracker.removePlayer(e.getPlayer());

        WSelection selection = WCannonTool.getSelection(e.getPlayer());
        if (selection != null)
            selection.remove();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Inventory inventory = e.getEntity().getInventory();

        LinkedList<ItemStack> keepInventoryItems = null;

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack itemStack = inventory.getItem(slot);

            if (itemStack == null)
                continue;

            Tool tool = plugin.getToolsManager().getTool(itemStack);

            if (tool == null || !tool.hasKeepInventory())
                continue;

            e.getDrops().remove(itemStack);
            inventory.setItem(slot, new ItemStack(Material.AIR));

            if (keepInventoryItems == null) {
                keepInventoryItems = keepInventoryTools.computeIfAbsent(e.getEntity().getUniqueId(), x -> new LinkedList<>());
            }

            keepInventoryItems.add(itemStack);
        }

    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        LinkedList<ItemStack> keepInventoryItems = keepInventoryTools.remove(e.getPlayer().getUniqueId());

        if (keepInventoryItems == null)
            return;

        PlayerInventory inventory = e.getPlayer().getInventory();
        Location location = e.getPlayer().getLocation();

        keepInventoryItems.forEach(itemStack -> ItemUtils.addItem(itemStack, inventory, location, null));
    }

    private void sendMessage(Player player, String message) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

}
