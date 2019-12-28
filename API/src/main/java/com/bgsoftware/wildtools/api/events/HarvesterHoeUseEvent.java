package com.bgsoftware.wildtools.api.events;

import com.bgsoftware.wildtools.api.objects.tools.HarvesterTool;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public final class HarvesterHoeUseEvent extends ToolUseEvent<HarvesterTool> {

    private static final HandlerList HANDLERS = new HandlerList();

    private final List<Location> blocks;

    public HarvesterHoeUseEvent(Player player, HarvesterTool tool, List<Location> blocks){
        super(player, tool);
        this.blocks = Collections.unmodifiableList(blocks);
    }

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