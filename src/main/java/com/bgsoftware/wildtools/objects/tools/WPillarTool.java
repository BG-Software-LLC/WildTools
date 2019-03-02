package com.bgsoftware.wildtools.objects.tools;

import com.bgsoftware.wildtools.utils.BukkitUtil;
import org.bukkit.event.player.PlayerInteractEvent;
import com.bgsoftware.wildtools.api.objects.tools.PillarTool;
import com.bgsoftware.wildtools.api.objects.ToolMode;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.Location;
import org.bukkit.Material;

public final class WPillarTool extends WTool implements PillarTool {

    public WPillarTool(Material type, String name){
        super(type, name, ToolMode.PILLAR);
    }

    @Override
    public boolean onBlockInteract(PlayerInteractEvent e) {
        int maxY = getPoint(e.getClickedBlock(), true), minY = getPoint(e.getClickedBlock(), false),
                x = e.getClickedBlock().getLocation().getBlockX(), z = e.getClickedBlock().getLocation().getBlockZ();

        for(int y = maxY; y >= minY; y--){
            Block targetBlock = e.getPlayer().getWorld().getBlockAt(x, y, z);
            if(canBreakBlock(e.getClickedBlock(), targetBlock)) {
                if(!plugin.getProviders().canBreak(e.getPlayer(), targetBlock, this))
                    continue;
                BukkitUtil.breakNaturally(e.getPlayer(), targetBlock, this);
                //Tool is using durability, reduces every block
                if(!isUnbreakable() && isUsingDurability() && e.getPlayer().getGameMode() != GameMode.CREATIVE){
                    reduceDurablility(e.getPlayer());
                }
                if(plugin.getNMSAdapter().getItemInHand(e.getPlayer()) == null) {
                    break;
                }
            }
        }

        return true;
    }

    private int getPoint(Block bl, boolean max){
        Location loc = bl.getLocation().clone();
        boolean isSameBlock = true;

        //Find max block
        if(max) {
            while (isSameBlock) {
                loc = loc.clone().add(0, 1, 0);
                isSameBlock = canBreakBlock(bl, loc.getBlock()) && loc.getBlockY() <= 256;
            }
        }

        //Find min block
        else{
            while(isSameBlock){
                loc = loc.clone().subtract(0, 1, 0);
                isSameBlock = canBreakBlock(bl, loc.getBlock()) && loc.getBlockY() >= 0;
            }
        }

        return loc.getBlockY();
    }

}
