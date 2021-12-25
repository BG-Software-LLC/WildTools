package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.hooks.PricesProvider;
import me.gypopo.economyshopgui.api.EconomyShopGUIHook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class PricesProvider_EconomyShopGUI implements PricesProvider {

    public PricesProvider_EconomyShopGUI(){
        WildToolsPlugin.log(" - Using EconomyShopGUI as PricesProvider.");
    }

    @Override
    public double getPrice(Player player, ItemStack itemStack) {
        return EconomyShopGUIHook.getItemSellPrice(player, itemStack);
    }

}

