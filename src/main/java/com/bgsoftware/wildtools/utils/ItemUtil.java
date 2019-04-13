package com.bgsoftware.wildtools.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.tools.Tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class ItemUtil {

    private static WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    public static void addItem(ItemStack itemStack, Inventory inventory, Location location){
        if(!Bukkit.isPrimaryThread()){
            Bukkit.getScheduler().runTask(plugin, () -> addItem(itemStack, inventory, location));
            return;
        }

        HashMap<Integer, ItemStack> additionalItems = inventory.addItem(itemStack);
        if(location != null && !additionalItems.isEmpty()){
            for(ItemStack additional : additionalItems.values())
                location.getWorld().dropItemNaturally(location, additional);
        }
    }

    public static void formatItemStack(Tool tool, ItemStack itemStack){
        ItemMeta meta = itemStack.getItemMeta();
        int usesLeft = plugin.getNMSAdapter().getTag(itemStack, "tool-uses", tool.getDefaultUses());

        if(meta.hasDisplayName()){
            if(tool.getItemStack().getItemMeta().getDisplayName().equals(meta.getDisplayName()) ||
                    tool.getItemStack().getItemMeta().getDisplayName().replace("{}", (usesLeft + 1) + "").equals(meta.getDisplayName()))
                meta.setDisplayName(tool.getItemStack().getItemMeta().getDisplayName().replace("{}", usesLeft + ""));
        }

        if(meta.hasLore()){
            List<String> wantedLore = new ArrayList<>();
            List<String> lore = new ArrayList<>();

            for(String line : tool.getItemStack().getItemMeta().getLore()) {
                lore.add(line.replace("{}", usesLeft + ""));
                wantedLore.add(line.replace("{}", (usesLeft + 1) + ""));
            }

            if(meta.getLore().equals(tool.getItemStack().getItemMeta().getLore()) || meta.getLore().equals(wantedLore))
                meta.setLore(lore);
        }

        itemStack.setItemMeta(meta);
    }

}
