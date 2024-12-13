package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.common.shopsbridge.BulkTransaction;
import com.bgsoftware.common.shopsbridge.IShopsBridge;
import com.bgsoftware.common.shopsbridge.ShopsProvider;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.hooks.PricesProvider;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PricesProvider_ShopsBridgeWrapper implements PricesProvider {

    private final IShopsBridge shopsBridge;
    private BulkTransaction bulkTransaction;

    public PricesProvider_ShopsBridgeWrapper(ShopsProvider shopsProvider, IShopsBridge shopsBridge) {
        WildToolsPlugin.log(" - Using " + shopsProvider.getPluginName() + " as PricesProvider.");
        this.shopsBridge = shopsBridge;
    }

    @Override
    public double getPrice(Player player, ItemStack itemStack) {
        return (this.bulkTransaction == null ? this.shopsBridge : this.bulkTransaction)
                .getSellPrice(player, itemStack).getPrice().doubleValue();
    }

    public void startBulkTransaction() {
        this.bulkTransaction = this.shopsBridge.startBulkTransaction();
    }

    public void stopBulkTransaction() {
        this.bulkTransaction = null;
    }

}
