package com.bgsoftware.wildtools.api.events;

import com.bgsoftware.wildtools.api.objects.tools.Tool;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;

public abstract class ToolUseEvent<T extends Tool> extends PlayerEvent {

    protected final T tool;

    public ToolUseEvent(Player player, T tool) {
        super(player);
        this.tool = tool;
    }

    public T getTool() {
        return this.tool;
    }

}
