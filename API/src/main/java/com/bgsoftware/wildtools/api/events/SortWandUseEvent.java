package com.bgsoftware.wildtools.api.events;

import com.bgsoftware.wildtools.api.objects.tools.SortTool;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
/**
 * SortWandUseEvent is called when a sort wand is used.
 */
public final class SortWandUseEvent extends ToolUseEvent<SortTool> {

    private static final HandlerList HANDLERS = new HandlerList();

    private final List<ItemStack> items;

    /**
     * The constructor of the event.
     * @param player The player who used the wand.
     * @param tool The wand that was used.
     * @param items All the affected items by the wand.
     */
    public SortWandUseEvent(Player player, SortTool tool, List<ItemStack> items){
        super(player, tool);
        this.items = Collections.unmodifiableList(items);
    }

    /**
     * Get all the affected items by the wand.
     */
    public List<ItemStack> getItems() {
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
