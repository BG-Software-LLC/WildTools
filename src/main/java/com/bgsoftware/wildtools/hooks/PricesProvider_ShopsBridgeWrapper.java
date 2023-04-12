package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.common.shopsbridge.IShopsBridge;
import com.bgsoftware.common.shopsbridge.ShopsProvider;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.hooks.PricesProvider;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PricesProvider_ShopsBridgeWrapper implements PricesProvider {

    private final IShopsBridge shopsBridge;

    public PricesProvider_ShopsBridgeWrapper(ShopsProvider shopsProvider, IShopsBridge shopsBridge) {
        WildToolsPlugin.log(" - Using " + shopsProvider.getPluginName() + " as PricesProvider.");
        this.shopsBridge = shopsBridge;
    }

    @Override
    public double getPrice(Player player, ItemStack itemStack) {
        return this.shopsBridge.getSellPrice(player, itemStack).doubleValue();
    }

}
