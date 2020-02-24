package com.bgsoftware.wildtools.api.events;

import com.bgsoftware.wildtools.api.objects.tools.IceTool;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
/**
 * IceWandUseEvent is called when an ice wand is used.
 */
public final class IceWandUseEvent extends ToolUseEvent<IceTool> {

    private static final HandlerList HANDLERS = new HandlerList();

    private final List<Location> blocks;

    /**
     * The constructor of the event.
     * @param player The player who used the wand.
     * @param tool The wand that was used.
     * @param blocks All the affected blocks by the wand.
     */
    public IceWandUseEvent(Player player, IceTool tool, List<Location> blocks){
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
