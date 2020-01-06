package com.bgsoftware.wildtools.hooks;

import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

public interface BlocksProvider {

    boolean canBreak(Player player, Block block, boolean onlyInClaim);

    default boolean canInteract(Player player, Block block, boolean onlyInClaim){
        return canBreak(player, block, onlyInClaim);
    }

    default boolean canPickupItem(Player player, Item item){
        return true;
    }

}
