package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.WildToolsPlugin;
import me.konsolas.aac.api.PlayerViolationEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class AntiCheatProvider_AAC implements AntiCheatProvider, Listener {

    private final Set<UUID> bypassUUIDs = new HashSet<>();

    public AntiCheatProvider_AAC(){
        Bukkit.getPluginManager().registerEvents(this, WildToolsPlugin.getPlugin());
    }

    @Override
    public void enableBypass(Player player) {
        bypassUUIDs.add(player.getUniqueId());
    }

    @Override
    public void disableBypass(Player player) {
        bypassUUIDs.remove(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerViolation(PlayerViolationEvent e) {
        if(bypassUUIDs.contains(e.getPlayer().getUniqueId()))
            e.setCancelled(true);
    }

}
