package com.bgsoftware.wildtools.api.events;

import com.bgsoftware.wildtools.api.objects.tools.LightningTool;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
/**
 * LightningWandUseEvent is called when a lightning wand is used.
 */
public final class LightningWandUseEvent extends ToolUseEvent<LightningTool> {

    private static final HandlerList HANDLERS = new HandlerList();

    private final List<Creeper> creepers;

    /**
     * The constructor of the event.
     * @param player The player who used the wand.
     * @param tool The wand that was used.
     * @param creepers All the affected creepers by the wand.
     */
    public LightningWandUseEvent(Player player, LightningTool tool, List<Creeper> creepers){
        super(player, tool);
        this.creepers = Collections.unmodifiableList(creepers);
    }

    /**
     * Get all the affected creepers by the wand.
     */
    public List<Creeper> getCreepers() {
        return creepers;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
