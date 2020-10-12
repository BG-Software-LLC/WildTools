package com.bgsoftware.wildtools.api.hooks;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface ClaimsProvider {

    /**
     * Check if a location is inside a claim of the player.
     * This is used when having only-inside-claim is enabled.
     * @param player The player to check.
     * @param location The location to check.
     */
    boolean isPlayerClaim(Player player, Location location);

}
