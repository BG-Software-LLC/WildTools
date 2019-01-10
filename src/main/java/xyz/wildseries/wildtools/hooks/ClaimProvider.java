package xyz.wildseries.wildtools.hooks;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface ClaimProvider {

    boolean inClaim(Player player, Location location);

}
