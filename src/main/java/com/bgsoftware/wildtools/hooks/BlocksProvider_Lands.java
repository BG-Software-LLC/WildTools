package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.WildToolsPlugin;

import me.angeschossen.lands.api.enums.LandsAction;
import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.landsaddons.LandsAddon;
import me.angeschossen.lands.api.objects.LandChunk;
import me.angeschossen.lands.api.role.enums.RoleSetting;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

public final class BlocksProvider_Lands implements BlocksProvider {

    private BlocksProvider landsProvider;

    public BlocksProvider_Lands(){
        try{
            Class.forName("me.angeschossen.lands.api.land.LandChunk");
            landsProvider = new LandsNew();
        }catch(Throwable ex){
            landsProvider = new LandsOld();
        }
    }

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        return landsProvider.canBreak(player, block, onlyInClaim);
    }

    @Override
    public boolean canInteract(Player player, Block block, boolean onlyInClaim) {
        return landsProvider.canInteract(player, block, onlyInClaim);
    }

    private static final class LandsOld implements BlocksProvider {

        private LandsAddon landsAddon;

        LandsOld(){
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

    private static final class LandsNew implements BlocksProvider {

        private LandsIntegration landsIntegration;

        LandsNew(){
            landsIntegration = new LandsIntegration(WildToolsPlugin.getPlugin(), false);
            landsIntegration.initialize();
        }

        @Override
        public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
            me.angeschossen.lands.api.land.LandChunk landChunk = landsIntegration.getLandChunk(block.getLocation());
            if(onlyInClaim && landChunk == null) return false;
            return landChunk == null || landChunk.canAction(player.getUniqueId(), RoleSetting.BLOCK_PLACE);
        }

        @Override
        public boolean canInteract(Player player, Block block, boolean onlyInClaim) {
            me.angeschossen.lands.api.land.LandChunk landChunk = landsIntegration.getLandChunk(block.getLocation());
            if(onlyInClaim && landChunk == null) return false;

            RoleSetting roleSetting = block.getState() instanceof InventoryHolder ? RoleSetting.INTERACT_CONTAINER : RoleSetting.INTERACT_GENERAL;

            return landChunk == null || landChunk.canAction(player.getUniqueId(), roleSetting);
        }

    }

}
