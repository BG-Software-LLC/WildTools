package com.bgsoftware.wildtools.handlers;

import com.bgsoftware.wildtools.objects.tools.WBuilderTool;
import com.bgsoftware.wildtools.objects.tools.WCannonTool;
import com.bgsoftware.wildtools.objects.tools.WCraftingTool;
import com.bgsoftware.wildtools.objects.tools.WCuboidTool;
import com.bgsoftware.wildtools.objects.tools.WHarvesterTool;
import com.bgsoftware.wildtools.objects.tools.WIceTool;
import com.bgsoftware.wildtools.objects.tools.WLightningTool;
import com.bgsoftware.wildtools.objects.tools.WPillarTool;
import com.bgsoftware.wildtools.objects.tools.WSellTool;
import com.bgsoftware.wildtools.objects.tools.WSortTool;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.handlers.ToolsManager;
import com.bgsoftware.wildtools.api.objects.Selection;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.api.objects.tools.BuilderTool;
import com.bgsoftware.wildtools.api.objects.tools.CannonTool;
import com.bgsoftware.wildtools.api.objects.tools.CraftingTool;
import com.bgsoftware.wildtools.api.objects.tools.CuboidTool;
import com.bgsoftware.wildtools.api.objects.tools.DrainTool;
import com.bgsoftware.wildtools.api.objects.tools.HarvesterTool;
import com.bgsoftware.wildtools.api.objects.tools.IceTool;
import com.bgsoftware.wildtools.api.objects.tools.LightningTool;
import com.bgsoftware.wildtools.api.objects.tools.PillarTool;
import com.bgsoftware.wildtools.api.objects.tools.SellTool;
import com.bgsoftware.wildtools.api.objects.tools.SortTool;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import com.bgsoftware.wildtools.objects.tools.WDrainTool;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unchecked")
public final class ToolsHandler implements ToolsManager {

    private WildToolsPlugin plugin;

    private Set<Tool> tools = new HashSet<>();

    public ToolsHandler(WildToolsPlugin plugin){
        this.plugin = plugin;
    }

    @Override
    public BuilderTool getBuilderTool(ItemStack itemStack) {
        return getTool(itemStack, ToolMode.BUILDER, BuilderTool.class);
    }

    @Override
    public CannonTool getCannonTool(ItemStack itemStack){
        return getTool(itemStack, ToolMode.CANNON, CannonTool.class);
    }

    @Override
    public CraftingTool getCraftingTool(ItemStack itemStack){
        return getTool(itemStack, ToolMode.CRAFTING, CraftingTool.class);
    }

    @Override
    public CuboidTool getCuboidTool(ItemStack itemStack){
        return getTool(itemStack, ToolMode.CUBOID, CuboidTool.class);
    }

    @Override
    public DrainTool getDrainTool(ItemStack itemStack) {
        return getTool(itemStack, ToolMode.CUBOID, DrainTool.class);
    }

    @Override
    public HarvesterTool getHarvesterTool(ItemStack itemStack){
        return getTool(itemStack, ToolMode.HARVESTER, HarvesterTool.class);
    }

    @Override
    public IceTool getIceTool(ItemStack itemStack) {
        return getTool(itemStack, ToolMode.ICE, IceTool.class);
    }

    @Override
    public LightningTool getLightningTool(ItemStack itemStack){
        return getTool(itemStack, ToolMode.LIGHTNING, LightningTool.class);
    }

    @Override
    public PillarTool getPillarTool(ItemStack itemStack){
        return getTool(itemStack, ToolMode.PILLAR, PillarTool.class);
    }

    @Override
    public SellTool getSellTool(ItemStack itemStack){
        return getTool(itemStack, ToolMode.SELL, SellTool.class);
    }

    @Override
    public SortTool getSortTool(ItemStack itemStack) {
        return getTool(itemStack, ToolMode.SORT, SortTool.class);
    }

    @Override
    public Tool getTool(String name){
        for(Tool tool : tools)
            if (tool.getName().equalsIgnoreCase(name))
                return tool;

        return null;
    }

    @Override
    public Tool getTool(ItemStack itemStack){
        for(Tool tool : tools)
            if (tool.isSimilar(itemStack))
                return tool;

        return null;
    }

    @Override
    public List<Tool> getTools(){
        return new ArrayList<>(tools);
    }

    @Override
    public Selection getSelection(Player player){
        return WCannonTool.getSelection(player);
    }

    @Override
    public double getPrice(Player player, ItemStack itemStack){
        return plugin.getProviders().getPrice(player, itemStack);
    }

    @Override
    public <T extends Tool> T registerTool(Material type, String name, Class<T> toolClass, Object arg) {
        Tool tool;
        if(toolClass.isAssignableFrom(BuilderTool.class)){
            tool = new WBuilderTool(type, name, (int) arg);
        }else if(toolClass.isAssignableFrom(CannonTool.class)){
            tool = new WCannonTool(type, name, (int) arg);
        }else if(toolClass.isAssignableFrom(CraftingTool.class)){
            tool = new WCraftingTool(type, name, (List<String>) arg);
        }else if(toolClass.isAssignableFrom(CuboidTool.class)){
            tool = new WCuboidTool(type, name, (int) arg);
        }else if(toolClass.isAssignableFrom(DrainTool.class)){
            tool = new WDrainTool(type, name, (int) arg);
        }else if(toolClass.isAssignableFrom(HarvesterTool.class)){
            tool = new WHarvesterTool(type, name, (int) arg);
        }else if(toolClass.isAssignableFrom(IceTool.class)){
            tool = new WIceTool(type, name, (int) arg);
        }else if(toolClass.isAssignableFrom(LightningTool.class)){
            tool = new WLightningTool(type, name);
        }else if(toolClass.isAssignableFrom(PillarTool.class)){
            tool = new WPillarTool(type, name);
        }else if(toolClass.isAssignableFrom(SellTool.class)){
            tool = new WSellTool(type, name);
        }else if(toolClass.isAssignableFrom(SortTool.class)){
            tool = new WSortTool(type, name);
        }else throw new IllegalArgumentException("Couldn't find tool class " + toolClass.getName());

        for(Tool _tool : new ArrayList<>(tools)){
            if(_tool.getName().equals(tool.getName()))
                tools.remove(_tool);
        }

        tools.add(tool);

        return toolClass.cast(tool);
    }

    public static void reload(){
        try{
            Field field = ToolsHandler.class.getDeclaredField("tools");
            field.setAccessible(true);
            ((HashSet<Tool>) field.get(WildToolsPlugin.getPlugin().getToolsManager())).clear();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private <T extends Tool> T getTool(ItemStack itemStack, ToolMode mode, Class<T> clazz){
        for(Tool tool : tools)
            if (tool.getToolMode() == mode && tool.isSimilar(itemStack))
                return clazz.cast(tool);

        return null;
    }

}
