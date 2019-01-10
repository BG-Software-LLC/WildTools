package xyz.wildseries.wildtools.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import xyz.wildseries.wildtools.Locale;
import xyz.wildseries.wildtools.WildToolsPlugin;
import xyz.wildseries.wildtools.api.objects.tools.BuilderTool;
import xyz.wildseries.wildtools.api.objects.tools.CraftingTool;
import xyz.wildseries.wildtools.api.objects.tools.CuboidTool;
import xyz.wildseries.wildtools.api.objects.tools.HarvesterTool;
import xyz.wildseries.wildtools.api.objects.tools.IceTool;
import xyz.wildseries.wildtools.api.objects.tools.LightningTool;
import xyz.wildseries.wildtools.api.objects.tools.PillarTool;
import xyz.wildseries.wildtools.api.objects.tools.SellTool;
import xyz.wildseries.wildtools.api.objects.tools.SortTool;
import xyz.wildseries.wildtools.api.objects.tools.Tool;
import xyz.wildseries.wildtools.objects.tools.WBuilderTool;
import xyz.wildseries.wildtools.objects.tools.WCannonTool;
import xyz.wildseries.wildtools.objects.tools.WHarvesterTool;
import xyz.wildseries.wildtools.objects.tools.WTool;

@SuppressWarnings("unused")
public final class BlocksListener implements Listener {

    private WildToolsPlugin instance;

    public BlocksListener(WildToolsPlugin instance){
        this.instance = instance;
    }

    @EventHandler
    public void onCuboidUse(BlockBreakEvent e){
        //One of the blocks that were broken by a tool
        if(WTool.toolBlockBreak.contains(e.getPlayer().getUniqueId()))
            return;

        if(!e.getPlayer().hasPermission("wildtools.use"))
            return;

        CuboidTool tool = instance.getToolsManager().getCuboidTool(instance.getNMSAdapter().getItemInHand(e.getPlayer()));

        if(tool == null)
            return;

        e.setCancelled(true);

        tool.useOnBlock(e.getPlayer(), e.getBlock());
    }

    @EventHandler
    public void onToolUse(PlayerInteractEvent e){
        if(WTool.toolBlockBreak.contains(e.getPlayer().getUniqueId()) ||
                e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getItem() == null)
            return;

        if(!e.getPlayer().hasPermission("wildtools.use"))
            return;

        Tool tool = instance.getToolsManager().getTool(e.getItem());

        if(tool == null)
            return;

        if(tool instanceof PillarTool || tool instanceof SellTool || tool instanceof LightningTool || tool instanceof CraftingTool ||
                tool instanceof BuilderTool || tool instanceof SortTool){
            if(tool instanceof BuilderTool)
                WBuilderTool.blockFaces.put(e.getPlayer().getUniqueId(), e.getBlockFace());
            e.setCancelled(true);
            tool.useOnBlock(e.getPlayer(), e.getClickedBlock());
        }
    }

    @EventHandler
    public void onLightningUse(PlayerInteractEvent e){
        if(e.getAction() != Action.RIGHT_CLICK_AIR || e.getItem() == null)
            return;

        if(!e.getPlayer().hasPermission("wildtools.use"))
            return;

        LightningTool tool = instance.getToolsManager().getLightningTool(e.getItem());

        if(tool == null)
            return;

        Location eye = e.getPlayer().getEyeLocation();

        for(Entity entity : e.getPlayer().getNearbyEntities(10, 10, 10)){
            if(entity instanceof LivingEntity){
                Vector toEntity = ((LivingEntity) entity).getEyeLocation().toVector().subtract(eye.toVector());
                double dot = toEntity.normalize().dot(eye.getDirection());
                if(dot > 0.99D){
                    e.setCancelled(true);
                    tool.useOnBlock(e.getPlayer(), entity.getLocation().getBlock());
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onIceUse(PlayerInteractEvent e){
        if(WTool.toolBlockBreak.contains(e.getPlayer().getUniqueId()) ||
                !e.getAction().name().contains("RIGHT_CLICK") || e.getItem() == null)
            return;

        if(!e.getPlayer().hasPermission("wildtools.use"))
            return;

        IceTool tool = instance.getToolsManager().getIceTool(e.getItem());

        if(tool == null)
            return;

        e.setCancelled(true);
        tool.useOnBlock(e.getPlayer(), e.getPlayer().getLocation().getBlock());
    }

    @EventHandler
    public void onHarvesterUse(PlayerInteractEvent e){
        if(WTool.toolBlockBreak.contains(e.getPlayer().getUniqueId()) || e.getItem() == null)
            return;

        if(!e.getPlayer().hasPermission("wildtools.use"))
            return;

        HarvesterTool tool = instance.getToolsManager().getHarvesterTool(e.getItem());

        if(tool == null)
            return;

        if(!e.getAction().name().equals(tool.getActivationAction() + "_BLOCK"))
            return;

        e.setCancelled(true);

        tool.useOnBlock(e.getPlayer(), e.getClickedBlock());
    }

    @EventHandler
    public void onSellModeActivate(PlayerInteractEvent e){
        if(e.getAction() != Action.RIGHT_CLICK_AIR || !e.getPlayer().isSneaking() || e.getItem() == null || !e.getPlayer().hasPermission("wildtools.sellmode"))
            return;

        WHarvesterTool tool = (WHarvesterTool) instance.getToolsManager().getHarvesterTool(e.getItem());

        if(tool == null)
            return;

        if(tool.sellModesPlayers.contains(e.getPlayer().getUniqueId())){
            tool.sellModesPlayers.remove(e.getPlayer().getUniqueId());
            Locale.SELL_MODE_DISABLED.send(e.getPlayer());
        }
        else{
            tool.sellModesPlayers.add(e.getPlayer().getUniqueId());
            Locale.SELL_MODE_ENABLED.send(e.getPlayer());
        }
    }

    @EventHandler
    public void onCannonUse(PlayerInteractEvent e){
        if(e.getItem() == null)
            return;
        //One of the blocks that were broken by a tool
        if(WTool.toolBlockBreak.contains(e.getPlayer().getUniqueId()))
            return;
        if(!e.getPlayer().hasPermission("wildtools.use"))
            return;

        WCannonTool tool = (WCannonTool) instance.getToolsManager().getCannonTool(e.getItem());

        if(tool == null)
            return;

        if(e.getAction() != Action.PHYSICAL)
            e.setCancelled(true);

        switch(e.getAction()){
            case RIGHT_CLICK_BLOCK:
                WCannonTool.addSelection(e.getPlayer(), e.getClickedBlock().getLocation(), null);
                Locale.SELECTION_RIGHT_CLICK.send(e.getPlayer());
                break;
            case LEFT_CLICK_BLOCK:
                WCannonTool.addSelection(e.getPlayer(),null, e.getClickedBlock().getLocation());
                Locale.SELECTION_LEFT_CLICK.send(e.getPlayer());
                break;
            case RIGHT_CLICK_AIR:
            case LEFT_CLICK_AIR:
                if(e.getPlayer().isSneaking())
                    tool.useOnBlock(e.getPlayer(), null);
                break;
        }

    }

}
