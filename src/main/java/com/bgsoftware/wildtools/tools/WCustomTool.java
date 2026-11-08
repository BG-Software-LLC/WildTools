package com.bgsoftware.wildtools.tools;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.factory.ToolBehaviorFactory;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.api.objects.tools.ToolBehavior;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

public class WCustomTool extends WTool {

    private final Plugin owner;
    private final ToolBehaviorFactory factory;
    private ToolBehavior behavior;

    public WCustomTool(Material type, String name, ToolMode toolMode, Plugin owner, ToolBehaviorFactory factory) {
        super(type, name, toolMode);
        this.owner = owner;
        this.factory = factory;
    }

    public boolean loadBehavior(ConfigurationSection config) {
        try {
            this.behavior = this.factory.create(this, config);
        } catch (RuntimeException error) {
            WildToolsPlugin.log("Failed to create tool '" + getName() + "' from plugin '" + this.owner.getName() + "'.");
            error.printStackTrace();
            return false;
        }
        return this.behavior != null;
    }

    @Override
    public boolean onBlockBreak(BlockBreakEvent e) {
        return this.owner.isEnabled() && this.behavior.onBlockBreak(this, e);
    }

    @Override
    public boolean onBlockInteract(PlayerInteractEvent e) {
        return this.owner.isEnabled() && this.behavior.onBlockInteract(this, e);
    }

    @Override
    public boolean onBlockHit(PlayerInteractEvent e) {
        if (!this.owner.isEnabled())
            return false;

        return this.behavior.onBlockHit(this, e) || super.onBlockHit(e);
    }

    @Override
    public boolean onAirInteract(PlayerInteractEvent e) {
        return this.owner.isEnabled() && this.behavior.onAirInteract(this, e);
    }

}
