package xyz.wildseries.wildtools.hooks;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildtools.WildToolsPlugin;

import java.util.HashMap;
import java.util.Map;

public final class PricesProvider_Default implements PricesProvider {

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static Map<String, Double> prices = new HashMap<>();

    public PricesProvider_Default(){
        WildToolsPlugin.log(" - Couldn't find any prices providers, using default one.");
    }

    @Override
    public double getPrice(Player player, ItemStack itemStack) {
        //Checks for 'TYPE' item
        if(prices.containsKey(itemStack.getType().name()))
            return prices.get(itemStack.getType().name()) * itemStack.getAmount();
        //Checks for 'TYPE:DATA' item
        if(prices.containsKey(itemStack.getType().name() + ":" + itemStack.getDurability()))
            return prices.get(itemStack.getType().name() + ":" + itemStack.getDurability()) * itemStack.getAmount();
        //Couldn't find a price for this item
        return -1;
    }
}
