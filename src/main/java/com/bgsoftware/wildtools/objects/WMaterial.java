package com.bgsoftware.wildtools.objects;

import com.bgsoftware.wildtools.WildToolsPlugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum WMaterial {

    SUGAR_CANE("SUGAR_CANE_BLOCK"),
    MELON("MELON_BLOCK"),
    FARMLAND("SOIL"),
    GRASS_BLOCK("GRASS"),
    MAP("EMPTY_MAP"),
    CLOCK("WATCH"),
    REDSTONE_TORCH("REDSTONE_TORCH_ON"),
    IRON_BARS("IRON_FENCE"),
    CRAFTING_TABLE("WORKBENCH"),
    CAULDRON("CAULDRON_ITEM"),
    EXPERIENCE_BOTTLE("EXP_BOTTLE");

    private static final boolean isLegacy = WildToolsPlugin.getPlugin().getNMSAdapter().isLegacy();
    private final String legacy;

    WMaterial(String legacy){
        this.legacy = legacy;
    }

    public static ItemStack getBlackGlassPane(){
        return !isLegacy ? new ItemStack(Material.matchMaterial("BLACK_STAINED_GLASS_PANE")) :
                new ItemStack(Material.matchMaterial("STAINED_GLASS_PANE"), 1, (short) 15);
    }

    public static ItemStack getBlueGlassPane(){
        return !isLegacy ? new ItemStack(Material.matchMaterial("LIGHT_BLUE_STAINED_GLASS_PANE")) :
                new ItemStack(Material.matchMaterial("STAINED_GLASS_PANE"), 1, (short) 3);
    }

    public static ItemStack getGodApple(){
        return !isLegacy ? new ItemStack(Material.matchMaterial("ENCHANTED_GOLDEN_APPLE")) :
                new ItemStack(Material.matchMaterial("GOLDEN_APPLE"), 1, (short) 1);
    }

    public Material parseMaterial(){
        return !isLegacy ? Material.matchMaterial(toString()) : Material.matchMaterial(legacy);
    }

}
