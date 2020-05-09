package com.bgsoftware.wildtools.hooks;

import me.crafter.mc.lockettepro.LockettePro;
import me.crafter.mc.lockettepro.LocketteProAPI;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class BlocksProvider_LockettePro implements BlocksProvider {

    @Override
    public Plugin getPlugin() {
        return LockettePro.getPlugin();
    }

    @Override
    public boolean canBreak(Player player, Block block, boolean onlyInClaim) {
        if(player.hasPermission("lockettepro.admin.break"))
            return true;

        return LocketteProAPI.isLockSign(block) ? LocketteProAPI.isOwnerOfSign(block, player) :
                !LocketteProAPI.isAdditionalSign(block) || (!LocketteProAPI.isLocked(LocketteProAPI.getAttachedBlock(block)) ||
                        LocketteProAPI.isOwnerOfSign(block, player) ||
                        !LocketteProAPI.isProtected(LocketteProAPI.getAttachedBlock(block)));
    }

}
