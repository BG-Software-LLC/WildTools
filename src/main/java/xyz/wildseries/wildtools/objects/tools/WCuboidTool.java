package xyz.wildseries.wildtools.objects.tools;

import xyz.wildseries.wildtools.Locale;
import xyz.wildseries.wildtools.api.objects.tools.CuboidTool;
import xyz.wildseries.wildtools.api.objects.ToolMode;

import org.bukkit.block.Block;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.wildseries.wildtools.utils.BukkitUtil;

public final class WCuboidTool extends WTool implements CuboidTool {

    private int breakLevel;

    public WCuboidTool(Material type, String name, int breakLevel){
        super(type, name, ToolMode.CUBOID);
        this.breakLevel = breakLevel;
    }

    public int getBreakLevel(){
        return breakLevel;
    }

    @Override
    public void useOnBlock(Player pl, Block block) {
        if(!canUse(pl.getUniqueId())){
            Locale.COOLDOWN_TIME.send(pl, getTime(getTimeLeft(pl.getUniqueId())));
            return;
        }

        int radius = breakLevel / 2;

        Location max = block.getLocation().clone().add(radius, radius, radius),
                min = block.getLocation().clone().subtract(radius, radius, radius);

        setLastUse(pl.getUniqueId());

        //Use is per player break, and not per each block...
        if(!isUnbreakable() && !isUsingDurability() && pl.getGameMode() != GameMode.CREATIVE){
            reduceDurablility(pl);
        }

        toolBlockBreak.add(pl.getUniqueId());

        for(int x = min.getBlockX(); x <= max.getBlockX(); x++){
            for(int z = min.getBlockZ(); z <= max.getBlockZ(); z++){
                for(int y = min.getBlockY(); y <= max.getBlockY(); y++){
                    Block targetBlock = pl.getWorld().getBlockAt(x, y, z);
                    if(canBreakBlock(block, targetBlock)) {
                        if(isOnlyInsideClaim() && !plugin.getProviders().inClaim(pl, targetBlock.getLocation()))
                            continue;
                        BukkitUtil.breakNaturally(pl, targetBlock);
                        //Tool is using durability, reduces every block
                        if(!isUnbreakable() && isUsingDurability() && pl.getGameMode() != GameMode.CREATIVE)
                            reduceDurablility(pl);
                        if(plugin.getNMSAdapter().getItemInHand(pl) == null) {
                            break;
                        }
                    }
                }
            }
        }

        toolBlockBreak.remove(pl.getUniqueId());
    }

}
