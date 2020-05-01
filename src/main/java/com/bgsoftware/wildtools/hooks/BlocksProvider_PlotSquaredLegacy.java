package com.bgsoftware.wildtools.hooks;

import com.intellectualcrafters.plot.api.PlotAPI;
import com.intellectualcrafters.plot.object.Plot;

import com.plotsquared.bukkit.BukkitMain;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class BlocksProvider_PlotSquaredLegacy implements BlocksProvider {

    private final BukkitMain instance = JavaPlugin.getPlugin(BukkitMain.class);
    private final PlotAPI API = new PlotAPI();

    @Override
    public Plugin getPlugin() {
        return instance;
    }

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        Plot plot = API.getPlot(block.getLocation());
        if(plot == null && onlyInClaim) return false;
        return plot == null || player.hasPermission("plots.admin.build.other") ||
                plot.isOwner(player.getUniqueId()) || plot.isAdded(player.getUniqueId());
    }
}
