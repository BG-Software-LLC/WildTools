package xyz.wildseries.wildtools.listeners;

import com.gmail.nossr50.datatypes.skills.AbilityType;
import com.gmail.nossr50.events.skills.abilities.McMMOPlayerAbilityActivateEvent;

import xyz.wildseries.wildtools.Locale;
import xyz.wildseries.wildtools.WildToolsPlugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@SuppressWarnings("unused")
public final class McMMOListener implements Listener {

    private WildToolsPlugin instance;

    public McMMOListener(WildToolsPlugin instance){
        this.instance = instance;
    }

    @EventHandler
    public void onPlayerAbilityActive(McMMOPlayerAbilityActivateEvent e){
        if(e.getAbility() != AbilityType.SUPER_BREAKER)
            return;


        if(instance.getToolsManager().getTool(instance.getNMSAdapter().getItemInHand(e.getPlayer())) == null)
            return;

        e.setCancelled(true);
        Locale.MCMMO_TOOL_SUPER_BREAKER.send(e.getPlayer());
    }

}
