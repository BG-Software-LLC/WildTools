package com.bgsoftware.wildtools.objects.tools;

import com.bgsoftware.wildtools.utils.BukkitUtil;
import org.bukkit.event.block.BlockBreakEvent;
import xyz.wildseries.wildtools.api.objects.tools.CuboidTool;
import xyz.wildseries.wildtools.api.objects.ToolMode;

import org.bukkit.block.Block;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;

public final class WCuboidTool extends WTool implements CuboidTool {

    private int breakLevel;

    public WCuboidTool(Material type, String name, int breakLevel){
        super(type, name, ToolMode.CUBOID);
        this.breakLevel = breakLevel;
    }

    public int getBreakLevel(){
        return breakLevel;
    }

    @Override
    public boolean onBlockBreak(BlockBreakEvent e) {
        int radius = breakLevel / 2;

        Location max = e.getBlock().getLocation().add(radius, radius, radius),
                min = e.getBlock().getLocation().subtract(radius, radius, radius);

        for(int x = min.getBlockX(); x <= max.getBlockX(); x++){
            for(int z = min.getBlockZ(); z <= max.getBlockZ(); z++){
                for(int y = min.getBlockY(); y <= max.getBlockY(); y++){
                    Block targetBlock = e.getPlayer().getWorld().getBlockAt(x, y, z);
                    if(canBreakBlock(e.getBlock(), targetBlock)) {
                        if(isOnlyInsideClaim() && !plugin.getProviders().inClaim(e.getPlayer(), targetBlock.getLocation()))
                            continue;
                        if(!BukkitUtil.canBreak(e.getPlayer(), targetBlock))
                            continue;
                        BukkitUtil.breakNaturally(e.getPlayer(), targetBlock);
                        //Tool is using durability, reduces every block
                        if(!isUnbreakable() && isUsingDurability() && e.getPlayer().getGameMode() != GameMode.CREATIVE)
                            reduceDurablility(e.getPlayer());
                        if(plugin.getNMSAdapter().getItemInHand(e.getPlayer()) == null)
                            break;
                    }
                }
            }
        }

        return true;
    }
}
