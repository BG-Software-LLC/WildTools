package com.bgsoftware.wildtools.handlers.legacy;

import com.bgsoftware.wildtools.api.objects.tools.*;

public final class LegacyKindMapper {
    private LegacyKindMapper() {
    }

    public static String toKindId(Class<? extends Tool> toolClass) {
        if (BuilderTool.class.isAssignableFrom(toolClass)) return "BUILDER";
        if (CannonTool.class.isAssignableFrom(toolClass)) return "CANNON";
        if (CraftingTool.class.isAssignableFrom(toolClass)) return "CRAFTING";
        if (CrowbarTool.class.isAssignableFrom(toolClass)) return "CROWBAR";
        if (CuboidTool.class.isAssignableFrom(toolClass)) return "CUBOID";
        if (DrainTool.class.isAssignableFrom(toolClass)) return "DRAIN";
        if (HarvesterTool.class.isAssignableFrom(toolClass)) return "HARVESTER";
        if (IceTool.class.isAssignableFrom(toolClass)) return "ICE";
        if (LightningTool.class.isAssignableFrom(toolClass)) return "LIGHTNING";
        if (MagnetTool.class.isAssignableFrom(toolClass)) return "MAGNET";
        if (PillarTool.class.isAssignableFrom(toolClass)) return "PILLAR";
        if (SellTool.class.isAssignableFrom(toolClass)) return "SELL";
        if (SortTool.class.isAssignableFrom(toolClass)) return "SORT";
        throw new IllegalArgumentException("Unknown legacy tool class " + toolClass.getName());
    }
}
