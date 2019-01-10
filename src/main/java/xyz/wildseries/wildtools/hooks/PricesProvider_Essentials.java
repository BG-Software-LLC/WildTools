package xyz.wildseries.wildtools.hooks;

import com.earth2me.essentials.Essentials;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import xyz.wildseries.wildtools.WildToolsPlugin;

import java.math.BigDecimal;

public final class PricesProvider_Essentials implements PricesProvider {

    public PricesProvider_Essentials(){
        WildToolsPlugin.log(" - Using Essentials as PricesProvider.");
    }

    @Override
    public double getPrice(Player player, ItemStack itemStack) {
        BigDecimal price = Essentials.getPlugin(Essentials.class).getWorth().getPrice(itemStack);
        return price == null ? -1 : price.doubleValue() * itemStack.getAmount();
    }
}
