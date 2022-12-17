package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.WildToolsPlugin;
import org.bukkit.entity.Player;

public class FactionsProvider_Default implements FactionsProvider {

    public FactionsProvider_Default(){
        WildToolsPlugin.log(" - Couldn't find any factions providers for tnt banks, using default one.");
    }

    @Override
    public int getTNTAmountFromBank(Player player) {
        return 0;
    }

    @Override
    public void takeTNTFromBank(Player player, int amount) {
        //Nothing to do :>
    }
}
