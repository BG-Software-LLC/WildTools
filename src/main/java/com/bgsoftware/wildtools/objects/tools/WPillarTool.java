package com.bgsoftware.wildtools.objects.tools;

import com.bgsoftware.wildtools.api.events.PillarWandUseEvent;
import com.bgsoftware.wildtools.utils.BukkitUtils;
import com.bgsoftware.wildtools.utils.blocks.BlocksController;
import com.bgsoftware.wildtools.utils.items.ItemsDropper;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerInteractEvent;
import com.bgsoftware.wildtools.api.objects.tools.PillarTool;
import com.bgsoftware.wildtools.api.objects.ToolMode;

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

        Material firstType = e.getClickedBlock().getType();
        short firstData = e.getClickedBlock().getState().getData().toItemStack().getDurability();

        BlocksController blocksController = new BlocksController();
        ItemsDropper itemsDropper = new ItemsDropper();
        int toolDurability = getDurability(e.getPlayer(), e.getItem());
        boolean usingDurability = isUsingDurability();
        int toolUsages = 0;

        for(int y = maxY; y >= minY; y--){
            if(usingDurability && toolUsages >= toolDurability)
                break;

            Block targetBlock = e.getPlayer().getWorld().getBlockAt(x, y, z);

            if(!BukkitUtils.canBreakBlock(e.getPlayer(), targetBlock, firstType, firstData, this))
                continue;

            if(!BukkitUtils.breakBlock(e.getPlayer(), blocksController, itemsDropper, targetBlock, e.getItem(), this, itemStack -> itemStack))
                break;

            toolUsages++;
        }

        if(toolUsages > 0) {
            PillarWandUseEvent pillarWandUseEvent = new PillarWandUseEvent(e.getPlayer(), this, blocksController.getAffectedBlocks());
            Bukkit.getPluginManager().callEvent(pillarWandUseEvent);

            blocksController.updateSession();
            itemsDropper.dropItems();

            reduceDurablility(e.getPlayer(), usingDurability ? toolUsages : 1, e.getItem());
        }

        return true;
    }

    private int getPoint(Block bl, boolean max){
        Location loc = bl.getLocation().clone();
        boolean isSameBlock = true;

        //Find max block
        if(max) {
            while (isSameBlock) {
                loc.add(0, 1, 0);
                isSameBlock = canBreakBlock(bl, loc.getBlock().getType(), loc.getBlock().getState().getData().toItemStack().getDurability()) && loc.getBlockY() <= 256;
            }
        }

        //Find min block
        else{
            while(isSameBlock){
                loc = loc.clone().subtract(0, 1, 0);
                isSameBlock = canBreakBlock(bl, loc.getBlock().getType(), loc.getBlock().getState().getData().toItemStack().getDurability()) && loc.getBlockY() >= 0;
            }
        }

        return loc.getBlockY();
    }

}
