package com.bgsoftware.wildtools.api.events;

import com.bgsoftware.wildtools.api.objects.tools.SortTool;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public final class SortWandUseEvent extends ToolUseEvent<SortTool> {

    private static final HandlerList HANDLERS = new HandlerList();

    private final List<ItemStack> items;

    public SortWandUseEvent(Player player, SortTool tool, List<ItemStack> items){
        super(player, tool);
        this.items = Collections.unmodifiableList(items);
    }

    public List<ItemStack> getItems() {
        return items;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
