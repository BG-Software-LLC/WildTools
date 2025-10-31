package com.bgsoftware.wildtools.handlers;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.Selection;
import com.bgsoftware.wildtools.api.objects.ToolKind;
import com.bgsoftware.wildtools.api.objects.ToolKindFactory;
import com.bgsoftware.wildtools.api.objects.ToolSectionView;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.api.objects.tools.*;
import com.bgsoftware.wildtools.api.handlers.ToolsManager;
import com.bgsoftware.wildtools.handlers.kinds.BuiltinKind;
import com.bgsoftware.wildtools.handlers.legacy.LegacyKindMapper;
import com.bgsoftware.wildtools.handlers.legacy.ToolSectionViewAdapters;
import com.bgsoftware.wildtools.handlers.loaders.CommonToolLoader;
import com.bgsoftware.wildtools.tools.*;
import com.bgsoftware.wildtools.utils.items.ToolItemStack;
import com.bgsoftware.wildtools.api.objects.tools.BaseTool;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@SuppressWarnings("unchecked")
public class ToolsHandler implements ToolsManager {

    private final WildToolsPlugin plugin;

    private final Map<String, Tool> toolsByName = new HashMap<>();
    private final List<Tool> tools = new ArrayList<>();

    private final Map<String, ToolKind> kinds = new HashMap<>();
    private final Map<String, ToolKindFactory> kindFactories = new HashMap<>();

    public ToolsHandler(WildToolsPlugin plugin){
        this.plugin = plugin;
    }

    private static int sortOf(Tool t) {
        ToolKind k = t.getKind();
        return k != null ? k.sortOrder() : Integer.MAX_VALUE;
    }

    private final Comparator<Tool> toolComparator = (a, b) -> {
        int c = Integer.compare(sortOf(a), sortOf(b));
        return c == 0 ? a.getName().compareToIgnoreCase(b.getName()) : c;
    };

    @Override public BuilderTool getBuilderTool(ItemStack itemStack) { return getToolByKind(itemStack, ToolMode.BUILDER, BuilderTool.class); }
    @Override public CannonTool getCannonTool(ItemStack itemStack){ return getToolByKind(itemStack, ToolMode.CANNON, CannonTool.class); }
    @Override public CraftingTool getCraftingTool(ItemStack itemStack){ return getToolByKind(itemStack, ToolMode.CRAFTING, CraftingTool.class); }
    @Override public CuboidTool getCuboidTool(ItemStack itemStack){ return getToolByKind(itemStack, ToolMode.CUBOID, CuboidTool.class); }

    @Override public DrainTool getDrainTool(ItemStack itemStack) { return getToolByKind(itemStack, ToolMode.DRAIN, DrainTool.class); }

    @Override public HarvesterTool getHarvesterTool(ItemStack itemStack){ return getToolByKind(itemStack, ToolMode.HARVESTER, HarvesterTool.class); }
    @Override public IceTool getIceTool(ItemStack itemStack) { return getToolByKind(itemStack, ToolMode.ICE, IceTool.class); }
    @Override public LightningTool getLightningTool(ItemStack itemStack){ return getToolByKind(itemStack, ToolMode.LIGHTNING, LightningTool.class); }
    @Override public PillarTool getPillarTool(ItemStack itemStack){ return getToolByKind(itemStack, ToolMode.PILLAR, PillarTool.class); }
    @Override public SellTool getSellTool(ItemStack itemStack){ return getToolByKind(itemStack, ToolMode.SELL, SellTool.class); }
    @Override public SortTool getSortTool(ItemStack itemStack) { return getToolByKind(itemStack, ToolMode.SORT, SortTool.class); }

    @Override
    public Tool getTool(String name){ return name == null ? null : toolsByName.get(name.toLowerCase()); }

    @Override
    public Tool getTool(ItemStack itemStack){
        if(itemStack == null) return null;

        ToolItemStack tis = ToolItemStack.of(itemStack);
        String toolName = tis.getToolType();
        Tool byName = getTool(toolName);
        if(byName != null) return byName;

        for (Tool t : tools) {
            ToolKind k = t.getKind();
            boolean similar = (k != null && k.isSimilar(itemStack, t)) || t.isSimilar(itemStack);
            if (similar) return t;
        }
        return null;
    }

    @Override public List<Tool> getTools(){ return Collections.unmodifiableList(tools); }
    public void clearTools(){ tools.clear(); toolsByName.clear(); }

    @Override public Selection getSelection(Player player){ return WCannonTool.getSelection(player); }

    @Override public double getPrice(Player player, ItemStack itemStack){ return plugin.getProviders().getPrice(player, itemStack); }

    @Override
    public boolean isOwningTool(ItemStack itemStack, Player player) {
        ToolItemStack tis = ToolItemStack.of(itemStack);
        Tool tool = tis.getTool();
        if (tool == null || !tool.isPrivate()) return true;
        String uuid = tis.getOwner();
        return uuid.isEmpty() || player.getUniqueId().toString().equals(uuid);
    }

    public static void reload(){
        ToolsHandler tm = WildToolsPlugin.getPlugin().getToolsManager();
        tm.clearTools();
    }

    @Override
    public void registerToolKind(ToolKind kind, ToolKindFactory factory) {
        kinds.put(kind.id().toUpperCase(), kind);
        kindFactories.put(kind.id().toUpperCase(), factory);
    }

    @Override
    public ToolKind getKind(String id) {
        return id == null ? null : kinds.get(id.toUpperCase());
    }

    @Override
    public Map<String, ToolKind> getRegisteredKinds() {
        return Collections.unmodifiableMap(kinds);
    }

    @Override
    public <T extends Tool> T registerTool(String kindId, Material type, String name, ToolSectionView cfg) {
        ToolKindFactory factory = kindFactories.get(kindId.toUpperCase());
        if (factory == null) throw new IllegalArgumentException("Unknown tool kind: " + kindId);

        Tool tool = factory.create(type, name, cfg);
        if (tool == null) throw new IllegalArgumentException("Tool factory returned null for " + kindId);

        if (tool instanceof BaseTool) {
            WTool backend = new WTool(type, name, getKind(kindId)) {};
            ((BaseTool) tool).bindDelegate(backend);
            CommonToolLoader.applyCommonProperties(tool, cfg);
        } else {
            CommonToolLoader.applyCommonProperties(tool, cfg);
        }

        Tool prev = toolsByName.put(tool.getName().toLowerCase(), tool);
        if (prev != null) tools.remove(prev);
        tools.add(tool);
        tools.sort(toolComparator);
        return (T) tool;
    }

    @Deprecated
    @Override
    public <T extends Tool> T registerTool(Material type, String name, Class<T> toolClass, Object arg) {
        String kindId = LegacyKindMapper.toKindId(toolClass);
        return registerTool(kindId, type, name, ToolSectionViewAdapters.fromLegacyArg(arg));
    }

    private <T extends Tool> T getToolByKind(ItemStack stack, ToolMode mode, Class<T> clazz) {
        return getToolByKind(stack, mode.name(), clazz);
    }

    private <T extends Tool> T getToolByKind(ItemStack stack, String kindId, Class<T> clazz) {
        if (stack == null) return null;
        ToolKind k = getKind(kindId);
        for (Tool t : tools) {
            ToolKind tk = t.getKind();
            if (tk == null || !tk.id().equalsIgnoreCase(kindId)) continue;
            boolean similar = (k != null && k.isSimilar(stack, t)) || t.isSimilar(stack);
            if (similar) return clazz.cast(t);
        }
        return null;
    }

    public void registerBuiltinKindsPublic() {
        final ToolKind BUILDER = new BuiltinKind(ToolMode.BUILDER, 10);
        registerToolKind(BUILDER, (type, name, cfg) ->
                new WBuilderTool(type, name, cfg.getInt("length", 0), BUILDER));

        final ToolKind CANNON = new BuiltinKind(ToolMode.CANNON, 20);
        registerToolKind(CANNON, (type, name, cfg) ->
                new WCannonTool(type, name, cfg.getInt("tnt-amount", 0), CANNON));

        final ToolKind CRAFTING = new BuiltinKind(ToolMode.CRAFTING, 30);
        registerToolKind(CRAFTING, (type, name, cfg) ->
                new WCraftingTool(type, name, cfg.getStringList("craftings"), CRAFTING));

        final ToolKind CROWBAR = new BuiltinKind(ToolMode.CROWBAR, 40);
        registerToolKind(CROWBAR, (type, name, cfg) ->
                new WCrowbarTool(type, name, cfg.getStringList("commands-on-use"), CROWBAR));

        final ToolKind CUBOID = new BuiltinKind(ToolMode.CUBOID, 50);
        registerToolKind(CUBOID, (type, name, cfg) ->
                new WCuboidTool(type, name, cfg.getInt("break-level", 1), CUBOID));

        final ToolKind DRAIN = new BuiltinKind(ToolMode.DRAIN, 60);
        registerToolKind(DRAIN, (type, name, cfg) ->
                new WDrainTool(type, name, cfg.getInt("radius", 3), DRAIN));

        final ToolKind HARVESTER = new BuiltinKind(ToolMode.HARVESTER, 70);
        registerToolKind(HARVESTER, (type, name, cfg) -> {
            WHarvesterTool t = new WHarvesterTool(type, name, cfg.getInt("radius", 3), HARVESTER);
            t.setActivationAction(cfg.getString("active-action", "RIGHT_CLICK"));
            t.setFarmlandRadius(cfg.getInt("farmland-radius", 0));
            t.setOneLayerOnly(cfg.getBoolean("one-layer-only", false));
            return t;
        });

        final ToolKind ICE = new BuiltinKind(ToolMode.ICE, 80);
        registerToolKind(ICE, (type, name, cfg) ->
                new WIceTool(type, name, cfg.getInt("radius", 3), ICE));

        final ToolKind LIGHTNING = new BuiltinKind(ToolMode.LIGHTNING, 90);
        registerToolKind(LIGHTNING, (type, name, cfg) ->
                new WLightningTool(type, name, LIGHTNING));

        final ToolKind MAGNET = new BuiltinKind(ToolMode.MAGNET, 100);
        registerToolKind(MAGNET, (type, name, cfg) ->
                new WMagnetTool(type, name, cfg.getInt("radius", 3), MAGNET));

        final ToolKind PILLAR = new BuiltinKind(ToolMode.PILLAR, 110);
        registerToolKind(PILLAR, (type, name, cfg) ->
                new WPillarTool(type, name, PILLAR));

        final ToolKind SELL = new BuiltinKind(ToolMode.SELL, 120);
        registerToolKind(SELL, (type, name, cfg) ->
                new WSellTool(type, name, SELL));

        final ToolKind SORT = new BuiltinKind(ToolMode.SORT, 130);
        registerToolKind(SORT, (type, name, cfg) ->
                new WSortTool(type, name, SORT));
    }
}
