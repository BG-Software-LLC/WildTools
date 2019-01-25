package xyz.wildseries.wildtools.objects.tools;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import xyz.wildseries.wildtools.Locale;
import xyz.wildseries.wildtools.api.objects.ToolMode;
import xyz.wildseries.wildtools.api.objects.tools.DrainTool;
import xyz.wildseries.wildtools.api.objects.tools.IceTool;
import xyz.wildseries.wildtools.utils.BukkitUtil;

public final class WDrainTool extends WTool implements DrainTool {

    private int radius;

    public WDrainTool(Material type, String name, int radius){
        super(type, name, ToolMode.DRAIN);
        this.radius = radius;
    }

    @Override
    public int getRadius() {
        return radius;
    }

    @Override
    public void useOnBlock(Player pl, Block bl) {
        if(Bukkit.isPrimaryThread()){
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> useOnBlock(pl, bl));
            return;
        }

        if(!canUse(pl.getUniqueId())){
            Locale.COOLDOWN_TIME.send(pl, getTime(getTimeLeft(pl.getUniqueId())));
            return;
        }

        setLastUse(pl.getUniqueId());

        //Use is per player break, and not per each block...
        if(!isUnbreakable() && pl.getGameMode() != GameMode.CREATIVE){
            reduceDurablility(pl);
        }

        toolBlockBreak.add(pl.getUniqueId());

        Location max = bl.getLocation().clone().add(radius, radius, radius),
                min = bl.getLocation().clone().subtract(radius, radius, radius);

        for(int x = min.getBlockX(); x <= max.getBlockX(); x++){
            for(int z = min.getBlockZ(); z <= max.getBlockZ(); z++){
                for(int y = max.getBlockY(); y >= min.getBlockY(); y--){
                    Block targetBlock = bl.getWorld().getBlockAt(x, y, z);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if(isOnlyInsideClaim() && !plugin.getProviders().inClaim(pl, targetBlock.getLocation()))
                            return;
                        if(targetBlock.getType() == Material.ICE && BukkitUtil.canBreak(pl, targetBlock)){
                            targetBlock.setType(Material.AIR);
                        }
                    });
                }
            }
        }

        toolBlockBreak.remove(pl.getUniqueId());
    }
}
