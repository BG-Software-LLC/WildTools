package com.bgsoftware.wildtools.handlers;

import com.bgsoftware.wildtools.api.objects.tools.CrowbarTool;
import com.bgsoftware.wildtools.api.objects.tools.MagnetTool;
import com.bgsoftware.wildtools.tools.WBuilderTool;
import com.bgsoftware.wildtools.tools.WCannonTool;
import com.bgsoftware.wildtools.tools.WCraftingTool;
import com.bgsoftware.wildtools.tools.WCrowbarTool;
import com.bgsoftware.wildtools.tools.WCuboidTool;
import com.bgsoftware.wildtools.tools.WHarvesterTool;
import com.bgsoftware.wildtools.tools.WIceTool;
import com.bgsoftware.wildtools.tools.WLightningTool;
import com.bgsoftware.wildtools.tools.WMagnetTool;
import com.bgsoftware.wildtools.tools.WPillarTool;
import com.bgsoftware.wildtools.tools.WSellTool;
import com.bgsoftware.wildtools.tools.WSortTool;
import com.bgsoftware.wildtools.utils.items.ToolItemStack;
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
import com.bgsoftware.wildtools.tools.WDrainTool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class ToolsHandler implements ToolsManager {

    private static final Comparator<Tool> toolComparator = (o1, o2) -> {
        int compare = Integer.compare(o1.getToolMode().ordinal(), o2.getToolMode().ordinal());
        return compare == 0 ? o1.getName().compareToIgnoreCase(o2.getName()) : compare;
    };

    private final WildToolsPlugin plugin;

    private final Map<String, Tool> toolsByName = new HashMap<>();
    private final List<Tool> tools = new ArrayList<>();

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
        return toolsByName.get(name.toLowerCase());
    }

    @Override
    public Tool getTool(ItemStack itemStack){
        if(itemStack == null)
            return null;

        ToolItemStack toolItemStack = ToolItemStack.of(itemStack);

        String toolName = toolItemStack.getToolType();

        Tool toolByName = getTool(toolName);

        if(toolByName != null)
            return toolByName;

        for(Tool tool : tools)
            if (tool.isSimilar(itemStack))
                return tool;

        return null;
    }

    @Override
    public List<Tool> getTools(){
        return Collections.unmodifiableList(tools);
    }

    public void clearTools(){
        tools.clear();
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
        }else if(toolClass.isAssignableFrom(CrowbarTool.class)){
            tool = new WCrowbarTool(type, name, (List<String>) arg);
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
        }else if(toolClass.isAssignableFrom(MagnetTool.class)){
            tool = new WMagnetTool(type, name, (int) arg);
        }else if(toolClass.isAssignableFrom(PillarTool.class)){
            tool = new WPillarTool(type, name);
        }else if(toolClass.isAssignableFrom(SellTool.class)){
            tool = new WSellTool(type, name);
        }else if(toolClass.isAssignableFrom(SortTool.class)) {
            tool = new WSortTool(type, name);
        }else throw new IllegalArgumentException("Couldn't find tool class " + toolClass.getName());

        if(toolsByName.containsKey(tool.getName().toLowerCase()))
            tools.remove(toolsByName.get(tool.getName().toLowerCase()));

        tools.add(tool);
        toolsByName.put(tool.getName().toLowerCase(), tool);

        tools.sort(toolComparator);

        return toolClass.cast(tool);
    }

    @Override
    public boolean isOwningTool(ItemStack itemStack, Player player) {
        ToolItemStack toolItemStack = ToolItemStack.of(itemStack);
        Tool tool = toolItemStack.getTool();

        if(tool == null || !tool.isPrivate())
            return true;

        String uuid = toolItemStack.getOwner();

        return uuid.isEmpty() || player.getUniqueId().toString().equals(uuid);
    }

    public static void reload(){
        WildToolsPlugin.getPlugin().getToolsManager().clearTools();
    }

    private <T extends Tool> T getTool(ItemStack itemStack, ToolMode mode, Class<T> clazz){
        for(Tool tool : tools)
            if (tool.getToolMode() == mode && tool.isSimilar(itemStack))
                return clazz.cast(tool);

        return null;
    }

}
