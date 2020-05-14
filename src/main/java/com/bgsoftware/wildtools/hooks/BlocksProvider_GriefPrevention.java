package com.bgsoftware.wildtools.hooks;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class BlocksProvider_GriefPrevention implements BlocksProvider {

    @Override
    public Plugin getPlugin() {
        return GriefPrevention.instance;
    }

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        return checkAccess(player, block.getLocation(), onlyInClaim, ClaimAction.BREAK, block.getType());
    }

    @Override
    public boolean canInteract(Player player, Block block, boolean onlyInClaim) {
        return checkAccess(player, block.getLocation(), onlyInClaim, ClaimAction.CONTAINER);
    }

    @Override
    public boolean canPickupItem(Player player, Item item) {
        return checkAccess(player, item.getLocation(), false, ClaimAction.GENERAL);
    }

    private boolean checkAccess(Player player, Location location, boolean onlyInClaim, ClaimAction claimAction, Object... args){
        DataStore dataStore = GriefPrevention.instance.dataStore;
        PlayerData playerData = dataStore.getPlayerData(player.getUniqueId());
        Claim claim = dataStore.getClaimAt(location, false, playerData.lastClaim);

        if(onlyInClaim && claim == null)
            return false;

        if(claim == null || playerData.ignoreClaims)
            return true;

        switch (claimAction){
            case BREAK:
                return claim.allowBreak(player, (Material) args[0]) == null;
            case CONTAINER:
                return claim.allowContainers(player) == null;
            default:
                return claim.allowAccess(player) == null;
        }
    }

    private enum ClaimAction {

        BREAK,
        CONTAINER,
        GENERAL

    }

}
