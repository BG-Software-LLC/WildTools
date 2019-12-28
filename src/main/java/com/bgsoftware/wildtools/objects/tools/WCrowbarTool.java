package com.bgsoftware.wildtools.objects.tools;

import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.api.objects.tools.CrowbarTool;
import com.bgsoftware.wildtools.utils.BukkitUtils;
import com.bgsoftware.wildtools.utils.items.ItemUtils;
import com.bgsoftware.wildtools.utils.items.ToolTaskManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public final class WCrowbarTool extends WTool implements CrowbarTool {

    public WCrowbarTool(Material type, String name){
        super(type, name, ToolMode.CROWBAR);
    }

    @Override
    public boolean onBlockInteract(PlayerInteractEvent e) {
        if(!e.getClickedBlock().getType().name().contains("SPAWNER"))
            return false;

        UUID taskId = ToolTaskManager.generateTaskId(e.getItem(), e.getPlayer().getInventory());

        if(!plugin.getProviders().canBreak(e.getPlayer(), e.getClickedBlock(), this))
            return true;

        CreatureSpawner creatureSpawner = (CreatureSpawner) e.getClickedBlock().getState();

        ItemStack dropItem = plugin.getProviders().getItem(creatureSpawner);

        BukkitUtils.breakNaturally(e.getPlayer(), e.getClickedBlock(), this, null, null);
        e.getClickedBlock().setType(Material.AIR);

        //We're telling all the other plugins that the block was broken.
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(e.getClickedBlock(), e.getPlayer());
        Bukkit.getPluginManager().callEvent(blockBreakEvent);

        if (isAutoCollect())
            ItemUtils.addItem(dropItem, e.getPlayer().getInventory(), e.getClickedBlock().getLocation());
        else
            e.getClickedBlock().getWorld().dropItemNaturally(e.getClickedBlock().getLocation(), dropItem);

        reduceDurablility(e.getPlayer(), 1, taskId);

        ToolTaskManager.removeTask(taskId);

        return true;
    }
}
