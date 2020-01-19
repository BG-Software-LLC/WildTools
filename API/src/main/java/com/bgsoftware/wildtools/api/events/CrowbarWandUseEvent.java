package com.bgsoftware.wildtools.api.events;

import com.bgsoftware.wildtools.api.objects.tools.CrowbarTool;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

@SuppressWarnings("unused")
public final class CrowbarWandUseEvent extends ToolUseEvent<CrowbarTool> {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Block affectedBlock;

    public CrowbarWandUseEvent(Player player, CrowbarTool tool, Block affectedBlock){
        super(player, tool);
        this.affectedBlock = affectedBlock;
    }

    public Block getBlock() {
        return affectedBlock;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
