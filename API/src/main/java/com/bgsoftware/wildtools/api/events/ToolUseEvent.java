package com.bgsoftware.wildtools.api.events;

import com.bgsoftware.wildtools.api.objects.tools.Tool;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;

/**
 * ToolUseEvent is called when a wand is used.
 */
public abstract class ToolUseEvent<T extends Tool> extends PlayerEvent {

    protected final T tool;

    /**
     * The constructor of the event.
     * @param player The player who used the wand.
     * @param tool The wand that was used.
     */
    public ToolUseEvent(Player player, T tool) {
        super(player);
        this.tool = tool;
    }

    /**
     * Get the tool that was used.
     */
    public T getTool() {
        return this.tool;
    }

}
