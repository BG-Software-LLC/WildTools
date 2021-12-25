package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.hooks.PricesProvider;
import com.newtjam.newtShop.newtShop;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class PricesProvider_NewtShop implements PricesProvider {

    public PricesProvider_NewtShop() {
        WildToolsPlugin.log(" - Using NewtShop as PricesProvider.");
    }

    @Override
    public double getPrice(Player player, ItemStack itemStack) {
        return newtShop.shop.getItemFromItemStack(itemStack).getSellPrice() * itemStack.getAmount();
    }

}
