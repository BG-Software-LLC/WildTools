package com.bgsoftware.wildtools.hooks;


import com.plotsquared.bukkit.BukkitMain;
import com.plotsquared.core.api.PlotAPI;
import com.plotsquared.core.plot.Plot;
import com.sk89q.worldedit.math.BlockVector2;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class BlocksProvider_PlotSquared5 implements BlocksProvider {

    private final BukkitMain instance = JavaPlugin.getPlugin(BukkitMain.class);
    private final PlotAPI API = new PlotAPI();

    @Override
    public Plugin getPlugin() {
        return instance;
    }

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        Plot plot = API.getChunkManager().hasPlot(block.getWorld().getName(), BlockVector2.at(block.getChunk().getX(), block.getChunk().getZ()));
        if(plot == null && onlyInClaim) return false;
        return plot == null || player.hasPermission("plots.admin.build.other") ||
                plot.isOwner(player.getUniqueId()) || plot.isAdded(player.getUniqueId());
    }
}
