package xyz.wildseries.wildtools.objects.tools;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import xyz.wildseries.wildtools.Locale;
import xyz.wildseries.wildtools.api.objects.tools.LightningTool;
import xyz.wildseries.wildtools.api.objects.ToolMode;

import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import xyz.wildseries.wildtools.utils.BukkitUtil;

public final class WLightningTool extends WTool implements LightningTool {

    public WLightningTool(Material type, String name){
        super(type, name, ToolMode.LIGHTNING);
    }

    @Override
    public void useOnBlock(Player pl, Block block) {
        if(Bukkit.isPrimaryThread()){
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> useOnBlock(pl, block));
            return;
        }

        if(!canUse(pl.getUniqueId())){
            Locale.COOLDOWN_TIME.send(pl, getTime(getTimeLeft(pl.getUniqueId())));
            return;
        }

        //Checks if the player has access to the block
        if(!BukkitUtil.canBreak(pl, block))
            return;

        setLastUse(pl.getUniqueId());

        if(pl.getGameMode() != GameMode.CREATIVE && !isUnbreakable())
            reduceDurablility(pl);

        pl.getWorld().strikeLightningEffect(block.getLocation());

        for(LivingEntity en : pl.getWorld().getLivingEntities()) {
            if (en instanceof Creeper) {
                if (en.getLocation().distance(block.getLocation()) <= 3) {
                    ((Creeper) en).setPowered(true);
                }
            }
        }
    }

}
