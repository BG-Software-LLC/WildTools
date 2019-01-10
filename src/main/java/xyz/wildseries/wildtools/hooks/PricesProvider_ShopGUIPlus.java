package xyz.wildseries.wildtools.hooks;

import net.brcdev.shopgui.ShopGuiPlugin;
import net.brcdev.shopgui.player.PlayerData;
import net.brcdev.shopgui.shop.WrappedShopItem;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import xyz.wildseries.wildtools.WildToolsPlugin;

public final class PricesProvider_ShopGUIPlus implements PricesProvider {

    private ShopGuiPlugin plugin;

    public PricesProvider_ShopGUIPlus(){
        WildToolsPlugin.log(" - Using ShopGUIPlus as PricesProvider.");
        //Loading database of online players
        plugin = ShopGuiPlugin.getInstance();
        for(Player player : WildToolsPlugin.getPlugin().getNMSAdapter().getOnlinePlayers())
           plugin.getPlayerManager().loadData(player);
    }

    @Override
    public double getPrice(Player player, ItemStack itemStack) {
        try {
            PlayerData playerData;

            try{
                playerData = plugin.getPlayerManager().getPlayerData(player);
            }catch(NullPointerException ex){
                plugin.getPlayerManager().registerPlayer(player);
                playerData = plugin.getPlayerManager().getPlayerData(player);
            }

            WrappedShopItem shopItem = plugin.getShopManager().findShopItemByItemStack(player, playerData, itemStack, false);
            return shopItem.getShopItem().getSellPriceForAmount(shopItem.getShop(), player, playerData, itemStack.getAmount());
        }catch(Exception ex){
            return -1;
        }
    }
}
