package com.bgsoftware.wildtools.nms.v26_1;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.entity.Entity;
import org.bukkit.inventory.meta.ItemMeta;

public class NMSAdapterImpl extends com.bgsoftware.wildtools.nms.v26_1.AbstractNMSAdapter {

    @Override
    protected void sendPacket(ServerChunkCache serverChunkCache, Entity entity, Packet<? super ClientGamePacketListener> packet) {
        serverChunkCache.sendToTrackingPlayers(entity, packet);
    }

    @Override
    public void makeItemGlow(ItemMeta itemMeta) {
        itemMeta.setEnchantmentGlintOverride(true);
    }

}
