package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.utils.Executor;
import net.coreprotect.CoreProtect;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;

@SuppressWarnings("deprecation")
public final class CoreProtectHook {

    private static WildToolsPlugin plugin = WildToolsPlugin.getPlugin();
    private static CoreProtect coreProtect;

    static {
        coreProtect = (CoreProtect) Bukkit.getPluginManager().getPlugin("CoreProtect");
    }

    public static void recordBlockChange(OfflinePlayer offlinePlayer, Block block) {
        if(!Bukkit.isPrimaryThread()){
            Executor.sync(() -> recordBlockChange(offlinePlayer, block));
            return;
        }

        Location location = block.getLocation();
        Material type = block.getType();
        byte data = block.getData();

        if(coreProtect.getAPI().APIVersion() == 5) {
            coreProtect.getAPI().logRemoval(offlinePlayer.getName(), location, type, data);
        }
        else if(coreProtect.getAPI().APIVersion() == 6) {
            coreProtect.getAPI().logRemoval(offlinePlayer.getName(), location, type,
                    (org.bukkit.block.data.BlockData) plugin.getNMSAdapter().getBlockData(type, data));
        }
    }

}
