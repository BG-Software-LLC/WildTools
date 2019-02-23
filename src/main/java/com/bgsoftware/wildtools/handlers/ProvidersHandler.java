package com.bgsoftware.wildtools.handlers;

import com.gmail.nossr50.util.player.UserManager;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.bgsoftware.wildtools.hooks.AntiCheatProvider;
import com.bgsoftware.wildtools.hooks.AntiCheatProvider_AAC;
import com.bgsoftware.wildtools.hooks.AntiCheatProvider_Default;
import com.bgsoftware.wildtools.hooks.AntiCheatProvider_NoCheatPlus;
import com.bgsoftware.wildtools.hooks.AntiCheatProvider_Spartan;
import com.bgsoftware.wildtools.hooks.ClaimProvider;
import com.bgsoftware.wildtools.hooks.ClaimProvider_Default;
import com.bgsoftware.wildtools.hooks.ClaimProvider_Factions;
import com.bgsoftware.wildtools.hooks.ClaimProvider_FactionsUUID;
import com.bgsoftware.wildtools.hooks.FactionsProvider;
import com.bgsoftware.wildtools.hooks.FactionsProvider_Default;
import com.bgsoftware.wildtools.hooks.FactionsProvider_SavageFactions;
import com.bgsoftware.wildtools.hooks.PricesProvider;
import com.bgsoftware.wildtools.hooks.PricesProvider_Default;
import com.bgsoftware.wildtools.hooks.PricesProvider_Essentials;
import com.bgsoftware.wildtools.hooks.PricesProvider_ShopGUIPlus;

public final class ProvidersHandler {

    static String pricesPlugin;

    private boolean isVaultEnabled = false;
    private Economy economy;

    private AntiCheatProvider antiCheatProvider;
    private PricesProvider pricesProvider;
    private FactionsProvider factionsProvider;
    private ClaimProvider claimProvider;

    public ProvidersHandler(){
        //AntiCheat Hookup
        if(Bukkit.getPluginManager().isPluginEnabled("NoCheatPlus"))
            antiCheatProvider = new AntiCheatProvider_NoCheatPlus();
        else if(Bukkit.getPluginManager().isPluginEnabled("Spartan"))
            antiCheatProvider = new AntiCheatProvider_Spartan();
        else if(Bukkit.getPluginManager().isPluginEnabled("AAC"))
            antiCheatProvider = new AntiCheatProvider_AAC();
        else antiCheatProvider = new AntiCheatProvider_Default();
        //Prices Plugin Hookup
        if(pricesPlugin.equalsIgnoreCase("ShopGUIPlus") && Bukkit.getPluginManager().isPluginEnabled("ShopGUIPlus"))
            pricesProvider = new PricesProvider_ShopGUIPlus();
        else if(pricesPlugin.equalsIgnoreCase("Essentials") && Bukkit.getPluginManager().isPluginEnabled("Essentials"))
            pricesProvider = new PricesProvider_Essentials();
        else pricesProvider = new PricesProvider_Default();
        //Factions Hookup
        if(Bukkit.getPluginManager().isPluginEnabled("Factions") &&
                Bukkit.getPluginManager().getPlugin("Factions").getDescription().getAuthors().contains("ProSavage"))
            factionsProvider = new FactionsProvider_SavageFactions();
        else factionsProvider = new FactionsProvider_Default();
        //Claim Hookup
        if(Bukkit.getPluginManager().isPluginEnabled("Factions")){
                claimProvider = Bukkit.getPluginManager().isPluginEnabled("MassiveCore") ? new ClaimProvider_Factions() : new ClaimProvider_FactionsUUID();
        }else claimProvider = new ClaimProvider_Default();
    }

    /*
     * Hooks' methods
     */

    public void enableAntiCheatBypass(Player player){
        antiCheatProvider.enableBypass(player);
    }

    public void disableAntiCheatBypass(Player player){
        antiCheatProvider.disableBypass(player);
    }

    public double getPrice(Player player, ItemStack itemStack){
        try {
            return pricesProvider.getPrice(player, itemStack);
        }catch(Exception ex){
            return -1;
        }
    }

    public int getTNTAmountFromBank(Player player){
        return factionsProvider.getTNTAmountFromBank(player);
    }

    public void takeTNTFromBank(Player player, int amount){
        factionsProvider.takeTNTFromBank(player, amount);
    }

    public boolean inClaim(Player player, Location location){
        return claimProvider.inClaim(player, location);
    }

    /*
     * Handler' methods
     */

    public void enableVault(){
        isVaultEnabled = true;
        economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
    }

    public void toggleChatNotifications(Player player){
        if(Bukkit.getPluginManager().isPluginEnabled("mcMMO")){
            UserManager.getPlayer(player).toggleChatNotifications();
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean trySellingItem(Player player, ItemStack itemStack){
        if(!canSellItem(player, itemStack))
            return false;

        double price = getPrice(player, itemStack);

        if(!economy.hasAccount(player))
            economy.createPlayerAccount(player);

        economy.depositPlayer(player, price);

        return true;
    }

    public boolean canSellItem(Player player, ItemStack itemStack){
        return isVaultEnabled && getPrice(player, itemStack) > 0;
    }

    public boolean isVaultEnabled(){
        return isVaultEnabled;
    }

}
