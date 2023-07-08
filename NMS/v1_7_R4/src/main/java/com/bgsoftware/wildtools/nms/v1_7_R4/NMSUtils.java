package com.bgsoftware.wildtools.nms.v1_7_R4;

import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PlayerChunkMap;
import net.minecraft.server.v1_7_R4.WorldServer;

public class NMSUtils {

    private NMSUtils() {

    }

    public static void sendPacketToRelevantPlayers(WorldServer worldServer, int chunkX, int chunkZ, Packet packet) {
        PlayerChunkMap playerChunkMap = worldServer.getPlayerChunkMap();
        for (Object entityHuman : worldServer.players) {
            if (entityHuman instanceof EntityPlayer && playerChunkMap.a((EntityPlayer) entityHuman, chunkX, chunkZ))
                ((EntityPlayer) entityHuman).playerConnection.sendPacket(packet);
        }
    }

}
