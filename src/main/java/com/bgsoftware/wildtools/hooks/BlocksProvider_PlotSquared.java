package com.bgsoftware.wildtools.hooks;

import com.intellectualcrafters.plot.api.PlotAPI;
import com.intellectualcrafters.plot.object.Plot;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class BlocksProvider_PlotSquared implements BlocksProvider {

    private PlotAPI API = new PlotAPI();

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        Plot plot = API.getPlot(block.getLocation());
        if(plot == null && onlyInClaim) return false;
        return plot == null || player.hasPermission("plots.admin.build.other") ||
                plot.isOwner(player.getUniqueId()) || plot.isAdded(player.getUniqueId());
    }
}
