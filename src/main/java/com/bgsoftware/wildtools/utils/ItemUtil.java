package com.bgsoftware.wildtools.utils;

import com.bgsoftware.wildtools.Locale;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.tools.Tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class ItemUtil {

    private static WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    public static void addItem(ItemStack itemStack, Inventory inventory, Location location){
        HashMap<Integer, ItemStack> additionalItems = inventory.addItem(itemStack);
        if(location != null && !additionalItems.isEmpty()){
            Bukkit.getScheduler().runTask(plugin, () -> {
                for(ItemStack additional : additionalItems.values()) {
                    if (additional != null && additional.getType() != Material.AIR) {
                        location.getWorld().dropItemNaturally(location, additional);
                    }
                }
            });
        }
    }

    public static void formatItemStack(Tool tool, ItemStack itemStack, int defaultUses, boolean sellMode, Runnable callback){
        formatItemStack(tool, itemStack, defaultUses, sellMode, true, callback);
    }

    public static void formatItemStack(Tool tool, ItemStack itemStack, int defaultUses, boolean sellMode, boolean async, Runnable callback){
        if(async && Bukkit.isPrimaryThread()){
            Executor.async(() -> formatItemStack(tool, itemStack, defaultUses, sellMode, callback));
            return;
        }

        ItemMeta meta = itemStack.getItemMeta();
        int usesLeft = plugin.getNMSAdapter().getTag(itemStack, "tool-uses", defaultUses);
        String ownerName = "None", ownerUUID = plugin.getNMSAdapter().getTag(itemStack, "tool-owner", ""),
                enabled = Locale.HARVESTER_SELL_ENABLED.getMessage(), disabled = Locale.HARVESTER_SELL_DISABLED.getMessage();

        if(enabled == null) enabled = "";
        if(disabled == null) disabled = "";

        try {
            ownerName = Bukkit.getOfflinePlayer(UUID.fromString(ownerUUID)).getName();
        }catch(Exception ignored){}

        if(meta.hasDisplayName()){
            meta.setDisplayName(tool.getItemStack().getItemMeta().getDisplayName()
                    .replace("{}", usesLeft + "")
                    .replace("{owner}", ownerName)
                    .replace("{sell-mode}", sellMode ? enabled : disabled));
        }

        if(meta.hasLore()){
            List<String> lore = new ArrayList<>();

            for(String line : tool.getItemStack().getItemMeta().getLore())
                lore.add(line
                        .replace("{}", usesLeft + "")
                        .replace("{owner}", ownerName)
                        .replace("{sell-mode}", sellMode ? enabled : disabled));

            meta.setLore(lore);
        }

        itemStack.setItemMeta(meta);

        if(callback != null)
            callback.run();
    }

}
