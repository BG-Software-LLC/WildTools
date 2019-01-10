package xyz.wildseries.wildtools.api.handlers;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import xyz.wildseries.wildtools.api.objects.Selection;
import xyz.wildseries.wildtools.api.objects.tools.BuilderTool;
import xyz.wildseries.wildtools.api.objects.tools.CannonTool;
import xyz.wildseries.wildtools.api.objects.tools.CraftingTool;
import xyz.wildseries.wildtools.api.objects.tools.CuboidTool;
import xyz.wildseries.wildtools.api.objects.tools.HarvesterTool;
import xyz.wildseries.wildtools.api.objects.tools.IceTool;
import xyz.wildseries.wildtools.api.objects.tools.LightningTool;
import xyz.wildseries.wildtools.api.objects.tools.PillarTool;
import xyz.wildseries.wildtools.api.objects.tools.SellTool;
import xyz.wildseries.wildtools.api.objects.tools.SortTool;
import xyz.wildseries.wildtools.api.objects.tools.Tool;

import java.util.List;

@SuppressWarnings("unused")
public interface ToolsManager {

    BuilderTool getBuilderTool(ItemStack itemStack);

    CannonTool getCannonTool(ItemStack itemStack);

    CraftingTool getCraftingTool(ItemStack itemStack);

    CuboidTool getCuboidTool(ItemStack itemStack);

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

}
