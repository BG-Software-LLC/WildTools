package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.WildToolsPlugin;
import me.angeschossen.lands.api.enums.LandsAction;
import me.angeschossen.lands.api.landsaddons.LandsAddon;
import me.angeschossen.lands.api.objects.LandChunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

public final class BlocksProvider_Lands implements BlocksProvider {

    private LandsAddon landsAddon;

    public BlocksProvider_Lands(){
        landsAddon = new LandsAddon(WildToolsPlugin.getPlugin(), false);
        landsAddon.initialize();
    }

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        LandChunk landChunk = landsAddon.getLandChunkHard(block.getWorld().getName(), block.getChunk().getX(), block.getChunk().getZ());
        if(onlyInClaim && landChunk == null) return false;
        return landChunk == null || landChunk.canAction(player.getUniqueId().toString(), LandsAction.BLOCK_PLACE);
    }

    @Override
    public boolean canInteract(Player player, Block block, boolean onlyInClaim) {
        LandChunk landChunk = landsAddon.getLandChunkHard(block.getWorld().getName(), block.getChunk().getX(), block.getChunk().getZ());
        if(onlyInClaim && landChunk == null) return false;

        LandsAction landsAction = block.getState() instanceof InventoryHolder ? LandsAction.INTERACT_CONTAINER : LandsAction.INTERACT_GENERAL;

        return landChunk == null || landChunk.canAction(player.getUniqueId().toString(), landsAction);
    }
}
