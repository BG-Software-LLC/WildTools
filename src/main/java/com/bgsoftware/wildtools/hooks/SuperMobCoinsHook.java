package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import me.swanis.mobcoins.events.MobCoinsReceiveEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SuperMobCoinsHook {

    private static WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    private static class MobCoinsListener implements Listener{

        @EventHandler
        public void onMobCoinReceive(MobCoinsReceiveEvent e){
            Player killer = e.getProfile().getPlayer();
            Tool tool = plugin.getToolsManager().getTool(plugin.getNMSAdapter().getItemInHand(killer));

            if(tool != null)
                e.setAmount((int) (e.getAmount() * tool.getMultiplier()));
        }

    }

    public static void register(){
        plugin.getServer().getPluginManager().registerEvents(new MobCoinsListener(), plugin);
    }

}
