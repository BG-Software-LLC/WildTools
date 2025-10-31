package com.bgsoftware.wildtools.api.events;

import com.bgsoftware.wildtools.api.WildTools;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class PluginInitializeEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final WildTools plugin;

    public PluginInitializeEvent(WildTools plugin) {
        this.plugin = plugin;
    }

    public WildTools getPlugin() {
        return plugin;
    }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}

