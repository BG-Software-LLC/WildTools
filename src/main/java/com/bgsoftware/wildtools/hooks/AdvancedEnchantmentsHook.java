package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import n3kas.ae.api.EnchantApplyEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class AdvancedEnchantmentsHook implements Listener {

    private static WildToolsPlugin plugin;

    public static void register(WildToolsPlugin plugin){
        AdvancedEnchantmentsHook.plugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(new AdvancedEnchantmentsHook(), plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onItemEnchant(EnchantApplyEvent e){
        if(!plugin.getProviders().hasAdvancedEnchantmentsEnabled())
            return;

        Tool tool = plugin.getToolsManager().getTool(e.getItem());
        if(tool != null)
            e.setCancelled(true);
    }

}
