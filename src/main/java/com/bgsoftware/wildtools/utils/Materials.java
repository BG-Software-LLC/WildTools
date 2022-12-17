package com.bgsoftware.wildtools.utils;

import com.bgsoftware.wildtools.WildToolsPlugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public enum Materials {

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
    EXPERIENCE_BOTTLE("EXP_BOTTLE"),
    COBWEB("WEB");

    private static final boolean isLegacy = ServerVersion.isLegacy();
    private static int farmlandId = -1;
    private final String legacy;

    Materials(String legacy) {
        this.legacy = legacy;
    }

    public static ItemStack getBlackGlassPane() {
        return !isLegacy ? new ItemStack(Material.matchMaterial("BLACK_STAINED_GLASS_PANE")) :
                new ItemStack(Material.matchMaterial("STAINED_GLASS_PANE"), 1, (short) 15);
    }

    public static ItemStack getBlueGlassPane() {
        return !isLegacy ? new ItemStack(Material.matchMaterial("LIGHT_BLUE_STAINED_GLASS_PANE")) :
                new ItemStack(Material.matchMaterial("STAINED_GLASS_PANE"), 1, (short) 3);
    }

    public static ItemStack getGodApple() {
        return !isLegacy ? new ItemStack(Material.matchMaterial("ENCHANTED_GOLDEN_APPLE")) :
                new ItemStack(Material.matchMaterial("GOLDEN_APPLE"), 1, (short) 1);
    }

    public static int getFarmlandId() {
        if (farmlandId == -1)
            farmlandId = WildToolsPlugin.getPlugin().getNMSAdapter().getFarmlandId();

        return farmlandId;
    }

    public static Material getSafeMaterial(String name, @Nullable Material def) {
        try {
            return Material.valueOf(name);
        } catch (IllegalArgumentException error) {
            return def;
        }
    }

    public Material parseMaterial() {
        return !isLegacy ? Material.matchMaterial(toString()) : Material.matchMaterial(legacy);
    }

}
