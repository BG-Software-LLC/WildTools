package com.bgsoftware.wildtools.api.events;

import com.bgsoftware.wildtools.api.objects.tools.MagnetTool;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
/**
 * MagnetWandUseEvent is called when a magnet wand is used.
 */
public final class MagnetWandUseEvent extends ToolUseEvent<MagnetTool> {

    private static final HandlerList HANDLERS = new HandlerList();

    private final List<Item> items;

    /**
     * The constructor of the event.
     * @param player The player who used the wand.
     * @param tool The wand that was used.
     * @param items All the affected items by the wand.
     */
    public MagnetWandUseEvent(Player player, MagnetTool tool, List<Item> items){
        super(player, tool);
        this.items = Collections.unmodifiableList(items);
    }

    /**
     * Get all the affected items by the wand.
     */
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
