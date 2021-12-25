package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import me.swanis.mobcoins.events.MobCoinsReceiveEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@SuppressWarnings("unused")
public final class SuperMobCoinsHook {

    public static void register(WildToolsPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onMobCoinReceive(MobCoinsReceiveEvent e) {
                Player killer = e.getProfile().getPlayer();
                Tool tool = plugin.getToolsManager().getTool(plugin.getNMSAdapter().getItemInHand(killer));

                if (tool != null)
                    e.setAmount((int) (e.getAmount() * tool.getMultiplier()));
            }
        }, plugin);
    }

}
