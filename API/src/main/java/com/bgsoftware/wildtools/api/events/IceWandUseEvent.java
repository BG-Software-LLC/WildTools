package com.bgsoftware.wildtools.api.events;

import com.bgsoftware.wildtools.api.objects.tools.IceTool;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import java.util.Collections;
import java.util.List;

/**
 * IceWandUseEvent is called when an ice wand is used.
 */
public class IceWandUseEvent extends ToolUseEvent<IceTool> implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final List<Location> blocks;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param player The player who used the wand.
     * @param tool   The wand that was used.
     * @param blocks All the affected blocks by the wand.
     */
    public IceWandUseEvent(Player player, IceTool tool, List<Location> blocks) {
        super(player, tool);
        this.blocks = Collections.unmodifiableList(blocks);
    }

    /**
     * Get all the affected blocks by the wand.
     */
    public List<Location> getBlocks() {
        return blocks;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
