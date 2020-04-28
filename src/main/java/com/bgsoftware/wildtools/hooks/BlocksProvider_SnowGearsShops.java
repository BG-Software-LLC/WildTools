package com.bgsoftware.wildtools.hooks;

import com.snowgears.shop.listener.MiscListener;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.RegisteredListener;

public final class BlocksProvider_SnowGearsShops implements BlocksProvider {

    private MiscListener listener = null;

    public BlocksProvider_SnowGearsShops(){
        for(RegisteredListener listener : BlockBreakEvent.getHandlerList().getRegisteredListeners()){
            if(listener.getListener() instanceof MiscListener)
                this.listener = (MiscListener) listener.getListener();
        }
    }

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        listener.shopDestroy(blockBreakEvent);
        return !blockBreakEvent.isCancelled();
    }

}
