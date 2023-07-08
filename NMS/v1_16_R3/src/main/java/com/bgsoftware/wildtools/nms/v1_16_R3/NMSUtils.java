package com.bgsoftware.wildtools.nms.v1_16_R3;

import com.bgsoftware.common.reflection.ReflectField;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PlayerChunkMap;
import net.minecraft.server.v1_16_R3.PlayerMap;
import net.minecraft.server.v1_16_R3.WorldServer;

public class NMSUtils {

    private static final ReflectField<PlayerMap> PLAYER_MAP_FIELD = new ReflectField<>(PlayerChunkMap.class, PlayerMap.class, "playerMap");

    private NMSUtils() {

    }

    public static void sendPacketToRelevantPlayers(WorldServer worldServer, int chunkX, int chunkZ, Packet<?> packet) {
        PlayerChunkMap playerChunkMap = worldServer.getChunkProvider().playerChunkMap;
        PLAYER_MAP_FIELD.get(playerChunkMap).a(1)
                .forEach(entityPlayer -> entityPlayer.playerConnection.sendPacket(packet));
    }

}
