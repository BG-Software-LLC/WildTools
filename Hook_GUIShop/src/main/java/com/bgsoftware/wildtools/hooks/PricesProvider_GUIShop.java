package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.hooks.PricesProvider;
import com.pablo67340.guishop.Main;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class PricesProvider_GUIShop implements PricesProvider {

    private final Main plugin;

    public PricesProvider_GUIShop() {
        WildToolsPlugin.log(" - Using GUIShop as PricesProvider.");
        plugin = Main.getINSTANCE();
    }

    @Override
    public double getPrice(Player player, ItemStack itemStack) {
        return plugin.getPRICETABLE().get(itemStack.getType().toString()).getSellPrice() * itemStack.getAmount();
    }

}
