package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.WildToolsPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class EconomyProvider_Vault implements EconomyProvider {

    private static Economy econ;

    public static boolean isCompatible() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp != null)
            econ = rsp.getProvider();

        if (econ != null)
            WildToolsPlugin.log("Using Vault as an economy provider.");

        return econ != null;
    }

    @Override
    public void depositPlayer(Player player, double price) {
        if (!econ.hasAccount(player))
            econ.createPlayerAccount(player);

        econ.depositPlayer(player, price);
    }


}
