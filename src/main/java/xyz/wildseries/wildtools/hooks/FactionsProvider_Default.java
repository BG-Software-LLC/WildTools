package xyz.wildseries.wildtools.hooks;

import org.bukkit.entity.Player;
import xyz.wildseries.wildtools.WildToolsPlugin;

public final class FactionsProvider_Default implements FactionsProvider {

    public FactionsProvider_Default(){
        WildToolsPlugin.log(" - Couldn't find any factions providers, using default one.");
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
