package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.WildToolsPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class ClaimProvider_Default implements ClaimProvider {

    public ClaimProvider_Default(){
        WildToolsPlugin.log(" - Couldn't find any claim providers, using default one.");
    }

    @Override
    public boolean inClaim(Player player, Location location) {
        return true;
    }
}
