package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.WildToolsPlugin;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Modules.Worth.WorthItem;

import com.bgsoftware.wildtools.api.hooks.PricesProvider;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class PricesProvider_CMI implements PricesProvider {

    public PricesProvider_CMI(){
        WildToolsPlugin.log(" - Using CMI as PricesProvider.");
    }

    @Override
    public double getPrice(Player player, ItemStack itemStack) {
        WorthItem worth = CMI.getInstance().getWorthManager().getWorth(itemStack);
        return worth == null ? 0 : worth.getSellPrice() * itemStack.getAmount();
    }

}
