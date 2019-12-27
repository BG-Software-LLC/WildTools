package com.bgsoftware.wildtools.api.events;

import com.bgsoftware.wildtools.api.objects.tools.MagnetTool;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public final class MagnetWandUseEvent extends ToolUseEvent<MagnetTool> {

    private static final HandlerList HANDLERS = new HandlerList();

    private final List<Item> items;

    public MagnetWandUseEvent(Player player, MagnetTool tool, List<Item> items){
        super(player, tool);
        this.items = Collections.unmodifiableList(items);
    }

    public List<Item> getItems() {
        return items;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
