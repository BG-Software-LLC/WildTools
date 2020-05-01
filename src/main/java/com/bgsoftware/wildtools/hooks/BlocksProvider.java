package com.bgsoftware.wildtools.hooks;

import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public interface BlocksProvider {

    Plugin getPlugin();

    boolean canBreak(Player player, Block block, boolean onlyInClaim);

    default boolean canInteract(Player player, Block block, boolean onlyInClaim){
        return canBreak(player, block, onlyInClaim);
    }

    default boolean canPickupItem(Player player, Item item){
        return true;
    }

}
