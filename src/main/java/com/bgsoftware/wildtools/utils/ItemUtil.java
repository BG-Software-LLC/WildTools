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
import java.util.UUID;

public final class ItemUtil {

    private static WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    public static void addItem(ItemStack itemStack, Inventory inventory, Location location){
        HashMap<Integer, ItemStack> additionalItems = inventory.addItem(itemStack);
        if(location != null && !additionalItems.isEmpty()){
            Bukkit.getScheduler().runTask(plugin, () -> {
                for(ItemStack additional : additionalItems.values())
                    location.getWorld().dropItemNaturally(location, additional);
            });
        }
    }

    public static void formatItemStack(Tool tool, ItemStack itemStack, int defaultUses){
        ItemMeta meta = itemStack.getItemMeta();
        int usesLeft = plugin.getNMSAdapter().getTag(itemStack, "tool-uses", defaultUses);
        String ownerName = "None", ownerUUID = plugin.getNMSAdapter().getTag(itemStack, "tool-owner", "");

        Bukkit.broadcastMessage(ownerUUID + "");

        try {
            ownerName = Bukkit.getOfflinePlayer(UUID.fromString(ownerUUID)).getName();
        }catch(Exception ignored){}

        if(meta.hasDisplayName()){
            if(tool.getItemStack().getItemMeta().getDisplayName().equals(meta.getDisplayName()) ||
                    tool.getItemStack().getItemMeta().getDisplayName().replace("{}", (usesLeft + 1) + "").equals(meta.getDisplayName()))
                meta.setDisplayName(tool.getItemStack().getItemMeta().getDisplayName()
                        .replace("{}", usesLeft + "").replace("{owner}", ownerName));
        }

        if(meta.hasLore()){
            List<String> lore = new ArrayList<>();

            for(String line : tool.getItemStack().getItemMeta().getLore())
                lore.add(line.replace("{}", usesLeft + "").replace("{owner}", ownerName));

            meta.setLore(lore);
        }

        itemStack.setItemMeta(meta);
    }

}
