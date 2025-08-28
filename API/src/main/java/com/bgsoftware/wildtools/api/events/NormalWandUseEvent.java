package com.bgsoftware.wildtools.api.events;

import com.bgsoftware.wildtools.api.objects.tools.NormalTool;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * NormalWandUseEvent is called when a normal wand is used.
 */
public class NormalWandUseEvent extends ToolUseEvent<NormalTool> implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Block block;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     * @param player The player who used the wand.
     * @param tool The wand that was used.
     * @param block The affected block by the wand.
     */
    public NormalWandUseEvent(Player player, NormalTool tool, Block block){
        super(player, tool);
        this.block = block;
    }

    /**
     * Get the affected block by the wand.
     */
    public Block getBlock() {
        return block;
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
