package com.bgsoftware.wildtools.objects.tools;

import com.bgsoftware.wildtools.utils.BukkitUtils;
import com.bgsoftware.wildtools.utils.blocks.BlocksController;
import org.bukkit.event.block.BlockBreakEvent;
import com.bgsoftware.wildtools.api.objects.tools.CuboidTool;
import com.bgsoftware.wildtools.api.objects.ToolMode;

import org.bukkit.block.Block;
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

        Material firstType = e.getBlock().getType();
        short firstData = e.getBlock().getState().getData().toItemStack().getDurability();

        boolean reduceDurability = false;

        outerLoop:
        for(int x = min.getBlockX(); x <= max.getBlockX(); x++){
            for(int z = min.getBlockZ(); z <= max.getBlockZ(); z++){
                for(int y = min.getBlockY(); y <= max.getBlockY(); y++){
                    Block targetBlock = e.getPlayer().getWorld().getBlockAt(x, y, z);
                    if(!plugin.getProviders().canBreak(e.getPlayer(), targetBlock, firstType, firstData, this))
                        continue;
                    BukkitUtils.breakNaturally(e.getPlayer(), targetBlock, this);
                    //Tool is using durability, reduces every block
                    if(isUsingDurability())
                        reduceDurablility(e.getPlayer());
                    if(plugin.getNMSAdapter().getItemInHand(e.getPlayer()).getType() == Material.AIR)
                        break outerLoop;
                    reduceDurability = true;
                }
            }
        }

        BlocksController.updateSession();

        if(reduceDurability && !isUsingDurability())
            reduceDurablility(e.getPlayer());

        return true;
    }
}
