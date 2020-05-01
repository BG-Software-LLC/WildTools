package com.bgsoftware.wildtools.hooks;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

public final class PerWorldPluginsHook {

    private static final boolean isEnabled = Bukkit.getPluginManager().isPluginEnabled("");

    public static boolean isPluginEnabled(Plugin plugin, World world){
        return !isEnabled || me.incomprehendable.dev.pwp.PerWorldPlugins.instance.checkWorld(plugin, world);
    }

}
