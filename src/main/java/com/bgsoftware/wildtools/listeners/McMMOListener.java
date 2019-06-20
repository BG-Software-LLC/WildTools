package com.bgsoftware.wildtools.listeners;

import com.bgsoftware.wildtools.Locale;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.gmail.nossr50.events.skills.abilities.McMMOPlayerAbilityActivateEvent;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused")
public final class McMMOListener implements Listener {

    private Set<UUID> messageCooldowns = new HashSet<>();
    private WildToolsPlugin plugin;

    public McMMOListener(WildToolsPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerAbilityActive(McMMOPlayerAbilityActivateEvent e){
        if(!getAbilityName(e).equals("SUPER_BREAKER"))
            return;

        if(plugin.getToolsManager().getTool(plugin.getNMSAdapter().getItemInHand(e.getPlayer())) == null)
            return;

        e.setCancelled(true);

        if(!messageCooldowns.contains(e.getPlayer().getUniqueId())) {
            Locale.MCMMO_TOOL_SUPER_BREAKER.send(e.getPlayer());
            messageCooldowns.add(e.getPlayer().getUniqueId());
            Bukkit.getScheduler().runTaskLater(plugin, () -> messageCooldowns.remove(e.getPlayer().getUniqueId()), 100L);
        }
    }

    private String getAbilityName(McMMOPlayerAbilityActivateEvent event){
        try{
            return event.getAbility().name();
        }catch(Throwable ex){
            try{
                Object ability = McMMOPlayerAbilityActivateEvent.class.getMethod("getAbility").invoke(event);
                return ability.toString().toUpperCase();
            }catch(Throwable ex1){
                ex1.printStackTrace();
                return "";
            }
        }
    }

}
