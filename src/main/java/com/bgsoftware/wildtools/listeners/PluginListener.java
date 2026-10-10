package com.bgsoftware.wildtools.listeners;

import com.bgsoftware.wildtools.WildToolsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;

public class PluginListener implements Listener {

    private final WildToolsPlugin plugin;

    public PluginListener(WildToolsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent e) {
        plugin.getToolsManager().unregisterToolModes(e.getPlugin());
    }

}
