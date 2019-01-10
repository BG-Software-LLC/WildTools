package xyz.wildseries.wildtools.objects.tools;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildtools.Locale;
import xyz.wildseries.wildtools.api.objects.ToolMode;
import xyz.wildseries.wildtools.api.objects.tools.BuilderTool;
import xyz.wildseries.wildtools.utils.BukkitUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class WBuilderTool extends WTool implements BuilderTool {

    public static Map<UUID, BlockFace> blockFaces = new HashMap<>();

    private int length;

    public WBuilderTool(Material type, String name, int length){
        super(type, name, ToolMode.BUILDER);
        this.length = length;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public void useOnBlock(Player pl, Block bl) {
        if(!canUse(pl.getUniqueId())){
            Locale.COOLDOWN_TIME.send(pl, getTime(getTimeLeft(pl.getUniqueId())));
            return;
        }

        setLastUse(pl.getUniqueId());

        if(!isUnbreakable() && pl.getGameMode() != GameMode.CREATIVE){
            reduceDurablility(pl);
        }

        toolBlockBreak.add(pl.getUniqueId());

        Block nextBlock = bl;
        for(int i = 0; i < length; i++){
            nextBlock = nextBlock.getRelative(blockFaces.get(pl.getUniqueId()));

            if(nextBlock.getType() != Material.AIR || !BukkitUtil.canBreak(pl, nextBlock))
                break;

            if(isOnlyInsideClaim() && !plugin.getProviders().inClaim(pl, nextBlock.getLocation()))
                break;

            ItemStack blockItemStack = bl.getState().getData().toItemStack(1);

            if(!pl.getInventory().containsAtLeast(blockItemStack, 1)){
                Locale.BUILDER_NO_BLOCK.send(pl, bl.getType().name());
                break;
            }

            pl.getInventory().removeItem(blockItemStack);
            plugin.getNMSAdapter().copyBlock(bl, nextBlock);
        }

        blockFaces.remove(pl.getUniqueId());
        toolBlockBreak.remove(pl.getUniqueId());
    }
}
