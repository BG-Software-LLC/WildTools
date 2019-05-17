package com.bgsoftware.wildtools.api;

import com.bgsoftware.wildtools.api.objects.Selection;
import com.bgsoftware.wildtools.api.objects.tools.CannonTool;
import com.bgsoftware.wildtools.api.objects.tools.CraftingTool;
import com.bgsoftware.wildtools.api.objects.tools.CuboidTool;
import com.bgsoftware.wildtools.api.objects.tools.HarvesterTool;
import com.bgsoftware.wildtools.api.objects.tools.LightningTool;
import com.bgsoftware.wildtools.api.objects.tools.PillarTool;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.bgsoftware.wildtools.api.objects.tools.SellTool;

@SuppressWarnings("unused")
public final class WildToolsAPI {

    private static WildTools instance;

    /**
     * Get a tool object by an item-stack
     *
     * @param itemStack an item-stack to check
     * @return tool object
     */
    public static Tool getTool(ItemStack itemStack){
        return instance.getToolsManager().getTool(itemStack);
    }

    /**
     * Get a tool object by a name
     *
     * @param name an name to check
     * @return tool object
     */
    public static Tool getTool(String name){
        return instance.getToolsManager().getTool(name);
    }

    /**
     * Get a cannon-tool object by an item-stack
     *
     * @param itemStack an item-stack to check
     * @return cannon-tool object
     */
    public static CannonTool getCannonTool(ItemStack itemStack){
        return instance.getToolsManager().getCannonTool(itemStack);
    }

    /**
     * Get a crafting-tool object by an item-stack
     *
     * @param itemStack an item-stack to check
     * @return crafting-tool object
     */
    public static CraftingTool getCraftingTool(ItemStack itemStack){
        return instance.getToolsManager().getCraftingTool(itemStack);
    }

    /**
     * Get a cuboid-tool object by an item-stack
     *
     * @param itemStack an item-stack to check
     * @return cuboid-tool object
     */
    public static CuboidTool getCuboidTool(ItemStack itemStack){
        return instance.getToolsManager().getCuboidTool(itemStack);
    }

    /**
     * Get a harvester-tool object by an item-stack
     *
     * @param itemStack an item-stack to check
     * @return harvester-tool object
     */
    public static HarvesterTool getHarvesterTool(ItemStack itemStack){
        return instance.getToolsManager().getHarvesterTool(itemStack);
    }

    /**
     * Get a lightning-tool object by an item-stack
     *
     * @param itemStack an item-stack to check
     * @return lightning-tool object
     */
    public static LightningTool getLightningTool(ItemStack itemStack){
        return instance.getToolsManager().getLightningTool(itemStack);
    }

    /**
     * Get a pillar-tool object by an item-stack
     *
     * @param itemStack an item-stack to check
     * @return pillar-tool object
     */
    public static PillarTool getPillarTool(ItemStack itemStack){
        return instance.getToolsManager().getPillarTool(itemStack);
    }

    /**
     * Get a sell-tool object by an item-stack
     *
     * @param itemStack an item-stack to check
     * @return sell-tool object
     */
    public static SellTool getSellTool(ItemStack itemStack){
        return instance.getToolsManager().getSellTool(itemStack);
    }

    /**
     * Get a players cannon-wand selection
     *
     * @param player a player to check
     * @return selection object, null if no selection exists
     */
    public static Selection getCannonWandSelection(Player player){
        return instance.getToolsManager().getSelection(player);
    }

    /**
     * Checks if a player is owning the private tool.
     * If there's no tool for the item or it's not private, true will be returned.
     *
     * @param itemStack The item to check
     * @param player The player to check
     * @return True if player is owning the tool, otherwise false.
     */
    public static boolean isOwningTool(ItemStack itemStack, Player player) {
        return instance.getToolsManager().isOwningTool(itemStack, player);
    }


    /**
     * Get the wildtools object
     *
     * @return wildtools object
     */
    public static WildTools getWildTools(){
        return instance;
    }

    /**
     * Get a price for an item-stack
     *
     * @param player a player to check
     * @param itemStack an item-stack to check
     * @return the price of the item-stack, -1 if no price exists
     */
    public static double getPrice(Player player, ItemStack itemStack){
        return instance.getToolsManager().getPrice(player, itemStack);
    }

}
