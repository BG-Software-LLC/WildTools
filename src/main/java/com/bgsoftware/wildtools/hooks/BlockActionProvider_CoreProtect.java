package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.utils.Executor;
import net.coreprotect.CoreProtect;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("deprecation")
public final class BlockActionProvider_CoreProtect implements BlockActionProvider {

    private final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();
    private final CoreProtect coreProtect = CoreProtect.getInstance();

    @Override
    public void onBlockBreak(Player player, Block block, ItemStack usedItem) {
        Location location = block.getLocation();
        Material type = block.getType();
        byte data = block.getData();

        if(coreProtect.getAPI().APIVersion() == 5) {
            coreProtect.getAPI().logRemoval(player.getName(), location, type, data);
        }
        else if(coreProtect.getAPI().APIVersion() == 6) {
            coreProtect.getAPI().logRemoval(player.getName(), location, type,
                    (org.bukkit.block.data.BlockData) plugin.getNMSAdapter().getBlockData(type, data));
        }
    }

}
