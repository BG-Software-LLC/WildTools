package com.bgsoftware.wildtools.hooks;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Util.Util;

public final class BlocksProvider_QuickShopOld implements BlocksProvider {

    private final QuickShop plugin;

    public BlocksProvider_QuickShopOld(){
        plugin = QuickShop.instance;
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        Shop shop = null;

        if (block.getState() instanceof InventoryHolder) {
            shop = plugin.getShopManager().getShop(block.getLocation());
        }
        else if (block.getType() == Material.WALL_SIGN) {
            Block attached = Util.getAttached(block);
            shop = attached == null ? null : plugin.getShopManager().getShop(attached.getLocation());
        }

        if(shop != null)
            shop.delete();

        return shop == null;
    }

}
