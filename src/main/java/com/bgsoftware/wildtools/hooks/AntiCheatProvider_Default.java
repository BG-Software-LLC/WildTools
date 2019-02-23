package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.WildToolsPlugin;
import org.bukkit.entity.Player;

public final class AntiCheatProvider_Default implements AntiCheatProvider {

    public AntiCheatProvider_Default(){
        WildToolsPlugin.log(" - Couldn't find any anti-cheat providers, using default one.");
    }

    @Override
    public void enableBypass(Player player) {
        //Nothing to do :>
    }

    @Override
    public void disableBypass(Player player) {
        //Nothing to do :>
    }
}
