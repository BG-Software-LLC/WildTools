package xyz.wildseries.wildtools.objects.tools;

import xyz.wildseries.wildtools.Locale;
import xyz.wildseries.wildtools.api.objects.tools.PillarTool;
import xyz.wildseries.wildtools.api.objects.ToolMode;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.wildseries.wildtools.utils.BukkitUtil;

public final class WPillarTool extends WTool implements PillarTool {

    public WPillarTool(Material type, String name){
        super(type, name, ToolMode.PILLAR);
    }

    @Override
    public void useOnBlock(Player pl, Block block) {
        if(!canUse(pl.getUniqueId())){
            Locale.COOLDOWN_TIME.send(pl, getTime(getTimeLeft(pl.getUniqueId())));
            return;
        }

        int maxY = getPoint(block, true), minY = getPoint(block, false),
                x = block.getLocation().getBlockX(), z = block.getLocation().getBlockZ();

        setLastUse(pl.getUniqueId());

        //Use is per player break, and not per each block...
        if(!isUnbreakable() && !isUsingDurability() && pl.getGameMode() != GameMode.CREATIVE){
            reduceDurablility(pl);
        }

        toolBlockBreak.add(pl.getUniqueId());

        for(int y = maxY; y >= minY; y--){
            Block targetBlock = pl.getWorld().getBlockAt(x, y, z);
            if(canBreakBlock(block, targetBlock)) {
                if(isOnlyInsideClaim() && !plugin.getProviders().inClaim(pl, block.getLocation()))
                    continue;
                BukkitUtil.breakNaturally(pl, targetBlock);
                //Tool is using durability, reduces every block
                if(!isUnbreakable() && isUsingDurability() && pl.getGameMode() != GameMode.CREATIVE){
                    reduceDurablility(pl);
                }
                if(plugin.getNMSAdapter().getItemInHand(pl) == null) {
                    break;
                }
            }
        }

        toolBlockBreak.remove(pl.getUniqueId());
    }

    private int getPoint(Block bl, boolean max){
        Location loc = bl.getLocation().clone();
        boolean isSameBlock = true;

        //Find max block
        if(max) {
            while (isSameBlock) {
                loc = loc.clone().add(0, 1, 0);
                isSameBlock = canBreakBlock(bl, loc.getBlock()) && loc.getBlockY() <= 256;
            }
        }

        //Find min block
        else{
            while(isSameBlock){
                loc = loc.clone().subtract(0, 1, 0);
                isSameBlock = canBreakBlock(bl, loc.getBlock()) && loc.getBlockY() >= 0;
            }
        }

        return loc.getBlockY();
    }

}
