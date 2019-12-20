package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.WildToolsPlugin;
import net.brcdev.shopgui.ShopGuiPlugin;
import net.brcdev.shopgui.ShopGuiPlusApi;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class PricesProvider_ShopGUIPlus implements PricesProvider {

    private ShopGuiPlugin plugin;

    public PricesProvider_ShopGUIPlus(){
        WildToolsPlugin.log(" - Using ShopGUIPlus as PricesProvider.");
        plugin = ShopGuiPlugin.getInstance();
    }

    @Override
    public double getPrice(Player player, ItemStack itemStack) {
        try {
            return ShopGuiPlusApi.getItemStackPriceSell(player, itemStack);
        }catch(Exception ex){
            plugin.getPlayerManager().registerPlayer(player);
            return ShopGuiPlusApi.getItemStackPriceSell(player, itemStack);
        }
    }

}
