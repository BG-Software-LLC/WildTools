package com.bgsoftware.wildtools.api.handlers;

import com.bgsoftware.wildtools.api.objects.Selection;
import com.bgsoftware.wildtools.api.objects.tools.BuilderTool;
import com.bgsoftware.wildtools.api.objects.tools.CannonTool;
import com.bgsoftware.wildtools.api.objects.tools.CraftingTool;
import com.bgsoftware.wildtools.api.objects.tools.CuboidTool;
import com.bgsoftware.wildtools.api.objects.tools.DrainTool;
import com.bgsoftware.wildtools.api.objects.tools.HarvesterTool;
import com.bgsoftware.wildtools.api.objects.tools.LightningTool;
import com.bgsoftware.wildtools.api.objects.tools.PillarTool;
import com.bgsoftware.wildtools.api.objects.tools.SortTool;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.bgsoftware.wildtools.api.objects.tools.IceTool;
import com.bgsoftware.wildtools.api.objects.tools.SellTool;

import java.util.List;

@SuppressWarnings("unused")
public interface ToolsManager {

    BuilderTool getBuilderTool(ItemStack itemStack);

    CannonTool getCannonTool(ItemStack itemStack);

    CraftingTool getCraftingTool(ItemStack itemStack);

    CuboidTool getCuboidTool(ItemStack itemStack);

    DrainTool getDrainTool(ItemStack itemStack);

    HarvesterTool getHarvesterTool(ItemStack itemStack);

    IceTool getIceTool(ItemStack itemStack);

    LightningTool getLightningTool(ItemStack itemStack);

    PillarTool getPillarTool(ItemStack itemStack);

    SellTool getSellTool(ItemStack itemStack);

    SortTool getSortTool(ItemStack itemStack);

    Tool getTool(String name);

    Tool getTool(ItemStack itemStack);

    List<Tool> getTools();

    Selection getSelection(Player player);

    double getPrice(Player player, ItemStack itemStack);

    <T extends Tool> T registerTool(Material type, String name, Class<T> toolClass, Object arg);

    boolean isOwningTool(ItemStack itemStack, Player player);

}
