package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.common.reflection.ClassInfo;
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

    private static final IFaction FACTION_HOOK = IFaction.create();

    @Override
    public boolean isPlayerClaim(Player player, Location location) {
        Faction faction = Board.getInstance().getFactionAt(new FLocation(location));
        return !faction.isWilderness() && FACTION_HOOK.playerCanBuildDestroyBlock(player, location);
    }

    interface IFaction {

        boolean playerCanBuildDestroyBlock(Player player, Location location);

        static IFaction create() {
            if (OldFactionsAPI.PLAYER_CAN_BUILD_DESTROY_BLOCK.isValid())
                return new OldFactionsAPI();

            return new NewFactionsAPI();
        }

    }

    private static class OldFactionsAPI implements IFaction {

        private static final ReflectMethod<Boolean> PLAYER_CAN_BUILD_DESTROY_BLOCK = new ReflectMethod<>(
                FactionsBlockListener.class, "playerCanBuildDestroyBlock",
                Player.class, Location.class, String.class, boolean.class);

        @Override
        public boolean playerCanBuildDestroyBlock(Player player, Location location) {
            return PLAYER_CAN_BUILD_DESTROY_BLOCK.invoke(null, player, location, "DESTROY", true);
        }

    }

    private static class NewFactionsAPI implements IFaction {

        private static final ReflectField<PermissibleAction> PERMISSIBLE_ACTIONS_DESTROY = new ReflectField<>(
                new ClassInfo("com.massivecraft.factions.perms.PermissibleActions", ClassInfo.PackageType.UNKNOWN),
                PermissibleAction.class, "DESTROY");

        @Override
        public boolean playerCanBuildDestroyBlock(Player player, Location location) {
            PermissibleAction permissibleAction = PERMISSIBLE_ACTIONS_DESTROY.isValid() ?
                    PERMISSIBLE_ACTIONS_DESTROY.get(null) : PermissibleAction.DESTROY;

            return FactionsBlockListener.playerCanBuildDestroyBlock(player, location, permissibleAction, true);
        }

    }

}
