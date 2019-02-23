package com.bgsoftware.wildtools.listeners;

import com.bgsoftware.wildtools.Locale;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.objects.tools.WTool;
import org.bukkit.GameMode;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import xyz.wildseries.wildtools.api.objects.tools.Tool;

@SuppressWarnings("unused")
public final class BlocksListener implements Listener {

    private WildToolsPlugin instance;

    public BlocksListener(WildToolsPlugin instance){
        this.instance = instance;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e){
        //One of the blocks that were broken by a tool
        if(WTool.toolBlockBreak.contains(e.getPlayer().getUniqueId()))
            return;

        if(!e.getPlayer().hasPermission("wildtools.use"))
            return;

        Tool tool = instance.getToolsManager().getTool(instance.getNMSAdapter().getItemInHand(e.getPlayer()));

        if(tool == null)
            return;

        if(tool.isOnlyInsideClaim() && !instance.getProviders().inClaim(e.getPlayer(), e.getBlock().getLocation())) {
            e.setCancelled(true);
            return;
        }

        if(!tool.canUse(e.getPlayer().getUniqueId())){
            e.setCancelled(true);
            Locale.COOLDOWN_TIME.send(e.getPlayer(), getTime(tool.getTimeLeft(e.getPlayer().getUniqueId())));
            return;
        }

        WTool.toolBlockBreak.add(e.getPlayer().getUniqueId());

        if(tool.onBlockBreak(e)){
            e.setCancelled(true);
            tool.setLastUse(e.getPlayer().getUniqueId());
            if(!tool.isUnbreakable() && e.getPlayer().getGameMode() != GameMode.CREATIVE)
                tool.reduceDurablility(e.getPlayer());
        }

        WTool.toolBlockBreak.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockInteract(PlayerInteractEvent e){
        //One of the blocks that were broken by a tool
        if(WTool.toolBlockBreak.contains(e.getPlayer().getUniqueId()) || e.getItem() == null)
            return;

        if(!e.getPlayer().hasPermission("wildtools.use"))
            return;

        Tool tool = instance.getToolsManager().getTool(instance.getNMSAdapter().getItemInHand(e.getPlayer()));

        if(tool == null)
            return;

        if(tool.isOnlyInsideClaim() && !instance.getProviders().inClaim(e.getPlayer(), e.getClickedBlock().getLocation())) {
            e.setCancelled(true);
            return;
        }

        if(!tool.canUse(e.getPlayer().getUniqueId())){
            e.setCancelled(true);
            Locale.COOLDOWN_TIME.send(e.getPlayer(), getTime(tool.getTimeLeft(e.getPlayer().getUniqueId())));
            return;
        }

        WTool.toolBlockBreak.add(e.getPlayer().getUniqueId());

        boolean toolInteract = false;

        switch (e.getAction()){
            case RIGHT_CLICK_AIR:
                toolInteract = tool.onAirInteract(e);
                break;
            case RIGHT_CLICK_BLOCK:
                toolInteract = tool.onBlockInteract(e);
                break;
            case LEFT_CLICK_BLOCK:
                toolInteract = tool.onBlockHit(e);
                break;
        }

        if(toolInteract){
            e.setCancelled(true);
            tool.setLastUse(e.getPlayer().getUniqueId());
            if(!tool.isUnbreakable() && e.getPlayer().getGameMode() != GameMode.CREATIVE)
                tool.reduceDurablility(e.getPlayer());
        }

        WTool.toolBlockBreak.remove(e.getPlayer().getUniqueId());
    }

    private String getTime(long timeLeft){
        String time = "";

        // Get rid of miliseconds
        timeLeft = timeLeft / 1000;

        if(timeLeft >= 3600) {
            if (timeLeft / 3600 == 1)
                time += "1 hour, ";
            else time += (timeLeft / 3600) + " hours, ";
            timeLeft %= 3600;
        }

        if(timeLeft >= 60){
            if (timeLeft / 60 == 1)
                time += "1 minute, ";
            else time += (timeLeft / 60) + " minutes, ";
            timeLeft %= 60;
        }

        if(timeLeft != 0) {
            if (timeLeft == 1)
                time += timeLeft + " second";
            else time += timeLeft + " seconds";
            return time;
        }

        return time.substring(0, time.length() - 2);
    }

}
