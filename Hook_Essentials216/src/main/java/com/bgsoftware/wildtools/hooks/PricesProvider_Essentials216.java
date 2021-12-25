package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.hooks.PricesProvider;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.Worth;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;

public final class PricesProvider_Essentials216 implements PricesProvider {


    public PricesProvider_Essentials216() {
        WildToolsPlugin.log(" - Using Essentials as PricesProvider.");
    }

    @Override
    public double getPrice(Player player, ItemStack itemStack) {
        Essentials plugin = Essentials.getPlugin(Essentials.class);
        Worth worth = plugin.getWorth();
        BigDecimal price = worth.getPrice(plugin, itemStack);
        return price == null ? -1 : price.doubleValue() * itemStack.getAmount();
    }

}
