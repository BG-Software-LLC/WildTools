package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.WildToolsPlugin;

import me.angeschossen.lands.Lands;
import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.land.LandArea;
import me.angeschossen.lands.api.role.enums.RoleSetting;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class BlocksProvider_Lands implements BlocksProvider {

    private final Lands instance;
    private final LandsIntegration landsIntegration;

    public BlocksProvider_Lands(){
        instance = JavaPlugin.getPlugin(Lands.class);
        landsIntegration = new LandsIntegration(WildToolsPlugin.getPlugin(), false);
        landsIntegration.initialize();
    }

    @Override
    public Plugin getPlugin() {
        return instance;
    }

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        LandArea landArea = landsIntegration.getArea(block.getLocation());
        if(onlyInClaim && landArea == null) return false;
        return landArea == null || landArea.canSetting(player.getUniqueId(), RoleSetting.BLOCK_PLACE);
    }

    @Override
    public boolean canInteract(Player player, Block block, boolean onlyInClaim) {
        LandArea landArea = landsIntegration.getArea(block.getLocation());
        if(onlyInClaim && landArea == null) return false;

        RoleSetting roleSetting = block.getState() instanceof InventoryHolder ? RoleSetting.INTERACT_CONTAINER : RoleSetting.INTERACT_GENERAL;

        return landArea == null || landArea.canSetting(player.getUniqueId(), roleSetting);
    }

}
