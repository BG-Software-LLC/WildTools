package xyz.wildseries.wildtools.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import xyz.wildseries.wildtools.Updater;
import xyz.wildseries.wildtools.WildToolsPlugin;
import xyz.wildseries.wildtools.api.objects.tools.Tool;
import xyz.wildseries.wildtools.objects.WSelection;
import xyz.wildseries.wildtools.objects.tools.WCannonTool;
import xyz.wildseries.wildtools.utils.ItemUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public final class PlayerListener implements Listener {

    /*
    Just notifies me if the server is using WildBuster
     */

    private WildToolsPlugin instance;

    public PlayerListener(WildToolsPlugin instance){
        this.instance = instance;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        if(e.getPlayer().getUniqueId().toString().equals("45713654-41bf-45a1-aa6f-00fe6598703b")){
            Bukkit.getScheduler().runTaskLater(instance, () ->
                    sendMessage(e.getPlayer(), "&8[&fWildSeries&8] &7This server is using WildTools v" + instance.getDescription().getVersion()), 5L);
        }

        if(e.getPlayer().isOp() && Updater.isOutdated()){
            Bukkit.getScheduler().runTaskLater(instance, () ->
                sendMessage(e.getPlayer(), "&b&lWildTools &7A new version is available (v" + Updater.getLatestVersion() + ")!"), 20L);
        }

    }

    //Remove the players selection and cancel task
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        WSelection selection = WCannonTool.getSelection(e.getPlayer());

        if(selection != null)
            selection.remove();
    }

    private Map<UUID, List<ItemStack>> keepInventoryTools = new HashMap<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent e){
        Inventory inventory = e.getEntity().getInventory();
        for(int slot = 0; slot < inventory.getSize(); slot++){
            ItemStack itemStack = inventory.getItem(slot);

            if(itemStack == null)
                continue;

            Tool tool = instance.getToolsManager().getTool(itemStack);

            if(tool == null || !tool.hasKeepInventory())
                continue;

            e.getDrops().remove(itemStack);
            inventory.setItem(slot, new ItemStack(Material.AIR));

            if(!keepInventoryTools.containsKey(e.getEntity().getUniqueId()))
                keepInventoryTools.put(e.getEntity().getUniqueId(), new ArrayList<>());

            keepInventoryTools.get(e.getEntity().getUniqueId()).add(itemStack);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e){
        if(keepInventoryTools.containsKey(e.getPlayer().getUniqueId())){
            List<ItemStack> toAdd = keepInventoryTools.get(e.getPlayer().getUniqueId());
            keepInventoryTools.remove(e.getPlayer().getUniqueId());
            toAdd.forEach(itemStack -> ItemUtil.addItem(itemStack, e.getPlayer().getInventory(), e.getPlayer().getLocation()));
        }
    }

    private void sendMessage(Player player, String message){
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

}
