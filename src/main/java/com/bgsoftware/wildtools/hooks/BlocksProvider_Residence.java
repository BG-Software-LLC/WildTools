package com.bgsoftware.wildtools.hooks;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

public final class BlocksProvider_Residence implements BlocksProvider {

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        ClaimedResidence residence = ResidenceApi.getResidenceManager().getByLoc(block.getLocation());
        if(onlyInClaim && residence == null) return false;
        return residence == null || residence.getPermissions().playerHas(player, Flags.destroy, false);
    }

    @Override
    public boolean canInteract(Player player, Block block, boolean onlyInClaim) {
        ClaimedResidence residence = ResidenceApi.getResidenceManager().getByLoc(block.getLocation());
        if(onlyInClaim && residence == null) return false;
        return residence == null || residence.getPermissions().playerHas(player, Flags.use, false);
    }

    @Override
    public boolean canPickupItem(Player player, Item item) {
        ClaimedResidence residence = ResidenceApi.getResidenceManager().getByLoc(item.getLocation());
        return residence == null || residence.getPermissions().playerHas(player, Flags.itempickup, false);
    }
}
