package com.bgsoftware.wildtools.api.events;

import com.bgsoftware.wildtools.api.objects.tools.CannonTool;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.Collections;
import java.util.List;

/**
 * CannonWandUseEvent is called when a cannon wand is used.
 */
public class CannonWandUseEvent extends ToolUseEvent<CannonTool> {

    private static final HandlerList HANDLERS = new HandlerList();

    private final List<Location> blocks;

    /**
     * The constructor of the event.
     * @param player The player who used the wand.
     * @param tool The wand that was used.
     * @param blocks All the affected blocks by the wand.
     */
    public CannonWandUseEvent(Player player, CannonTool tool, List<Location> blocks){
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
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
