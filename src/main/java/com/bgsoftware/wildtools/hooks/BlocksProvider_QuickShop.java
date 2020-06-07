package com.bgsoftware.wildtools.hooks;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.shop.Shop;
import org.maxgamer.quickshop.util.Util;

public final class BlocksProvider_QuickShop implements BlocksProvider {

    private final QuickShop plugin;

    public BlocksProvider_QuickShop(){
        plugin = QuickShop.getInstance();
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        Shop shop = null;

        if(Util.canBeShop(block)){
            shop = plugin.getShopManager().getShop(block.getLocation());
        }
        else if(Util.isWallSign(block.getType())){
            if (block.getState() instanceof Sign) {
                Sign sign = (Sign) block.getState();
                if (sign.getLine(0).equals(plugin.getConfig().getString("lockette.private")) ||
                        sign.getLine(0).equals(plugin.getConfig().getString("lockette.more_users"))) {
                    return true;
                }
            }

            Block attache = Util.getAttached(block);
            shop = attache == null ? null : plugin.getShopManager().getShop(attache.getLocation());
        }

        if(shop != null)
            shop.delete();

        return shop != null;
    }

}
