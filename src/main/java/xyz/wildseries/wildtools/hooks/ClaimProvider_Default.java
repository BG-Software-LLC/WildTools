package xyz.wildseries.wildtools.hooks;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import xyz.wildseries.wildtools.WildToolsPlugin;

public final class ClaimProvider_Default implements ClaimProvider {

    public ClaimProvider_Default(){
        WildToolsPlugin.log(" - Couldn't find any claim providers, using default one.");
    }

    @Override
    public boolean inClaim(Player player, Location location) {
        return true;
    }
}
