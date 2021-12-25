package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.hooks.ClaimsProvider;
import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.land.LandArea;
import me.angeschossen.lands.api.role.enums.RoleSetting;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class ClaimsProvider_Lands implements ClaimsProvider {

    private final LandsIntegration landsIntegration;

    public ClaimsProvider_Lands(WildToolsPlugin plugin) {
        landsIntegration = new LandsIntegration(plugin, false);
        landsIntegration.initialize();
    }

    @Override
    public boolean isPlayerClaim(Player player, Location location) {
        LandArea landArea = landsIntegration.getArea(location);
        return landArea != null && landArea.canSetting(player.getUniqueId(), RoleSetting.BLOCK_BREAK);
    }

}
