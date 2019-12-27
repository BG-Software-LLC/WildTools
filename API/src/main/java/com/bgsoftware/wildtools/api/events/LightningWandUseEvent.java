package com.bgsoftware.wildtools.api.events;

import com.bgsoftware.wildtools.api.objects.tools.LightningTool;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public final class LightningWandUseEvent extends ToolUseEvent<LightningTool> {

    private static final HandlerList HANDLERS = new HandlerList();

    private final List<Creeper> creepers;

    public LightningWandUseEvent(Player player, LightningTool tool, List<Creeper> creepers){
        super(player, tool);
        this.creepers = Collections.unmodifiableList(creepers);
    }

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
