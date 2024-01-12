package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildtools.api.hooks.ClaimsProvider;
import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.listeners.FactionsBlockListener;
import com.massivecraft.factions.perms.PermissibleAction;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ClaimsProvider_FactionsUUID implements ClaimsProvider {

    private static final ReflectMethod<Boolean> PLAYER_CAN_BUILD_DESTROY_BLOCK_OLD = new ReflectMethod<>(
            FactionsBlockListener.class, "playerCanBuildDestroyBlock",
            Player.class, Location.class, String.class, boolean.class);

    private static final ReflectField<PermissibleAction> PERMISSIBLE_ACTIONS_DESTROY = new ReflectField<>(
            "com.massivecraft.factions.perms.PermissibleActions", PermissibleAction.class, "DESTROY");

    @Override
    public boolean isPlayerClaim(Player player, Location location) {
        Faction faction = Board.getInstance().getFactionAt(new FLocation(location));
        return !faction.isWilderness() && playerCanBuildDestroyBlock(player, location);
    }

    private static boolean playerCanBuildDestroyBlock(Player player, Location location) {
        if (PLAYER_CAN_BUILD_DESTROY_BLOCK_OLD.isValid()) {
            return PLAYER_CAN_BUILD_DESTROY_BLOCK_OLD.invoke(null, player, location, "DESTROY", true);
        }

        PermissibleAction permissibleAction = PERMISSIBLE_ACTIONS_DESTROY.isValid() ?
                PERMISSIBLE_ACTIONS_DESTROY.get(null) : PermissibleAction.DESTROY;

        return FactionsBlockListener.playerCanBuildDestroyBlock(player, location, permissibleAction, true);
    }

}
