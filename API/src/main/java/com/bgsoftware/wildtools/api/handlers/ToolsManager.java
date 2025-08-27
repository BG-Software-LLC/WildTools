package com.bgsoftware.wildtools.api.handlers;

import com.bgsoftware.wildtools.api.objects.Selection;
import com.bgsoftware.wildtools.api.objects.tools.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface ToolsManager {

    /**
     * Get a normal tool from an item.
     * @param itemStack The item to get a normal tool from.
     * @return If tool exists, the tool will be returned. Otherwise, null.
     */
    NormalTool getNormalTool(ItemStack itemStack);


    /**
     * Get a builder tool from an item.
     * @param itemStack The item to get a builder tool from.
     * @return If tool exists, the tool will be returned. Otherwise, null.
     */
    BuilderTool getBuilderTool(ItemStack itemStack);

    /**
     * Get a cannon tool from an item.
     * @param itemStack The item to get a cannon tool from.
     * @return If tool exists, the tool will be returned. Otherwise, null.
     */
    CannonTool getCannonTool(ItemStack itemStack);

    /**
     * Get a crafting tool from an item.
     * @param itemStack The item to get a crafting tool from.
     * @return If tool exists, the tool will be returned. Otherwise, null.
     */
    CraftingTool getCraftingTool(ItemStack itemStack);

    /**
     * Get a cuboid tool from an item.
     * @param itemStack The item to get a cuboid tool from.
     * @return If tool exists, the tool will be returned. Otherwise, null.
     */
    CuboidTool getCuboidTool(ItemStack itemStack);

    /**
     * Get a drain tool from an item.
     * @param itemStack The item to get a drain tool from.
     * @return If tool exists, the tool will be returned. Otherwise, null.
     */
    DrainTool getDrainTool(ItemStack itemStack);

    /**
     * Get a harvester tool from an item.
     * @param itemStack The item to get a harvester tool from.
     * @return If tool exists, the tool will be returned. Otherwise, null.
     */
    HarvesterTool getHarvesterTool(ItemStack itemStack);

    /**
     * Get an ice tool from an item.
     * @param itemStack The item to get an ice tool from.
     * @return If tool exists, the tool will be returned. Otherwise, null.
     */
    IceTool getIceTool(ItemStack itemStack);

    /**
     * Get a lightning tool from an item.
     * @param itemStack The item to get a lightning tool from.
     * @return If tool exists, the tool will be returned. Otherwise, null.
     */
    LightningTool getLightningTool(ItemStack itemStack);

    /**
     * Get a pillar tool from an item.
     * @param itemStack The item to get a pillar tool from.
     * @return If tool exists, the tool will be returned. Otherwise, null.
     */
    PillarTool getPillarTool(ItemStack itemStack);

    /**
     * Get a sell tool from an item.
     * @param itemStack The item to get a sell tool from.
     * @return If tool exists, the tool will be returned. Otherwise, null.
     */
    SellTool getSellTool(ItemStack itemStack);

    /**
     * Get a sort tool from an item.
     * @param itemStack The item to get a sort tool from.
     * @return If tool exists, the tool will be returned. Otherwise, null.
     */
    SortTool getSortTool(ItemStack itemStack);

    /**
     * Get a tool by its name.
     * @param name The name to check.
     * @return If tool exists, the tool will be returned. Otherwise, null.
     */
    Tool getTool(String name);

    /**
     * Get a tool from an item.
     * @param itemStack The item to get a tool from.
     * @return If tool exists, the tool will be returned. Otherwise, null.
     */
    Tool getTool(ItemStack itemStack);

    /**
     * Get all the tools available.
     */
    List<Tool> getTools();

    /**
     * Get a selection of a player for the cannon wand.
     * @param player The player to check.
     */
    Selection getSelection(Player player);

    /**
     * Get a price of an item for a player.
     * @param player The player to check.
     * @param itemStack The item to check.
     * @return If exists, the price. Otherwise, -1.
     */
    double getPrice(Player player, ItemStack itemStack);

    /**
     * Register a new tool.
     * @param type The type of the tool.
     * @param name The name of the tool.
     * @param toolClass The tool class.
     * @param arg Additional arguments for the item.
     * @return The new tool.
     */
    <T extends Tool> T registerTool(Material type, String name, Class<T> toolClass, Object arg);

    /**
     * Check whether or not a player is owning an item or not.
     * @param itemStack The item to check.
     * @param player The player to check.
     * @return True if player owns the item, otherwise false.
     */
    boolean isOwningTool(ItemStack itemStack, Player player);

}
