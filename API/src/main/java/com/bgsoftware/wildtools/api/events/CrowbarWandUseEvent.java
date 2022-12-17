package com.bgsoftware.wildtools.api.events;

import com.bgsoftware.wildtools.api.objects.tools.CrowbarTool;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * CrowbarWandUseEvent is called when a crowbar wand is used.
 */
public class CrowbarWandUseEvent extends ToolUseEvent<CrowbarTool> implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Block affectedBlock;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     * @param player The player who used the wand.
     * @param tool The wand that was used.
     * @param affectedBlock The affected block by the wand.
     */
    public CrowbarWandUseEvent(Player player, CrowbarTool tool, Block affectedBlock){
        super(player, tool);
        this.affectedBlock = affectedBlock;
    }

    /**
     * Get the affected block by the wand.
     */
    public Block getBlock() {
        return affectedBlock;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
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
