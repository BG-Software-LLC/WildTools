package com.bgsoftware.wildtools.api.events;

import com.bgsoftware.wildtools.api.objects.tools.CraftingTool;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

/**
 * CraftingWandUseEvent is called when a crafting wand is used.
 */
public class CraftingWandUseEvent extends ToolUseEvent<CraftingTool> {

    private static final HandlerList HANDLERS = new HandlerList();

    private final List<ItemStack> items;

    /**
     * The constructor of the event.
     * @param player The player who used the wand.
     * @param tool The wand that was used.
     * @param items All the affected items by the wand.
     */
    public CraftingWandUseEvent(Player player, CraftingTool tool, List<ItemStack> items){
        super(player, tool);
        this.items = Collections.unmodifiableList(items);
    }

    /**
     * Get all the affected items by the wand.
     */
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
