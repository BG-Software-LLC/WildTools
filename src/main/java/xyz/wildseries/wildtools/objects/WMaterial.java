package xyz.wildseries.wildtools.objects;

import org.bukkit.Bukkit;
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
    CRAFTING_TABLE("WORKBENCH");

    private final String legacy;

    WMaterial(String legacy){
        this.legacy = legacy;
    }

    public static ItemStack getBlackGlassPane(){
        return Bukkit.getVersion().contains("1.13") ? new ItemStack(Material.matchMaterial("BLACK_STAINED_GLASS_PANE")) :
                new ItemStack(Material.matchMaterial("STAINED_GLASS_PANE"), 1, (short) 15);
    }

    public static ItemStack getBlueGlassPane(){
        return Bukkit.getVersion().contains("1.13") ? new ItemStack(Material.matchMaterial("LIGHT_BLUE_STAINED_GLASS_PANE")) :
                new ItemStack(Material.matchMaterial("STAINED_GLASS_PANE"), 1, (short) 3);
    }

    public static ItemStack getGodApple(){
        return Bukkit.getVersion().contains("1.13") ? new ItemStack(Material.matchMaterial("ENCHANTED_GOLDEN_APPLE")) :
                new ItemStack(Material.matchMaterial("GOLDEN_APPLE"), 1, (short) 1);
    }

    public Material parseMaterial(){
        return Bukkit.getBukkitVersion().contains("1.13") ? Material.matchMaterial(toString()) : Material.matchMaterial(legacy);
    }

}
