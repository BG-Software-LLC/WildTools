package com.bgsoftware.wildtools.objects.tools;

import com.bgsoftware.wildtools.utils.BukkitUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.api.objects.tools.IceTool;

public final class WIceTool extends WTool implements IceTool {

    private int radius;

    public WIceTool(Material type, String name, int radius){
        super(type, name, ToolMode.ICE);
        this.radius = radius;
    }

    @Override
    public int getRadius() {
        return radius;
    }

    @Override
    public boolean onBlockInteract(PlayerInteractEvent e) {
        return handleUse(e.getPlayer(), e.getPlayer().getLocation().getBlock());
    }

    @Override
    public boolean onAirInteract(PlayerInteractEvent e) {
        return handleUse(e.getPlayer(), e.getPlayer().getLocation().getBlock());
    }

    private boolean handleUse(Player player, Block block){
        Location max = block.getLocation().clone().add(radius, radius, radius),
                min = block.getLocation().clone().subtract(radius, radius, radius);

        for(int x = min.getBlockX(); x <= max.getBlockX(); x++){
            for(int z = min.getBlockZ(); z <= max.getBlockZ(); z++){
                for(int y = max.getBlockY(); y >= min.getBlockY(); y--){
                    Block targetBlock = block.getWorld().getBlockAt(x, y, z);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if(isOnlyInsideClaim() && !plugin.getProviders().inClaim(player, targetBlock.getLocation()))
                            return;
                        if(targetBlock.getType() == Material.ICE && BukkitUtil.canBreak(player, targetBlock)){
                            targetBlock.setType(Material.WATER);
                        }
                    });
                }
            }
        }

        return true;
    }

}
