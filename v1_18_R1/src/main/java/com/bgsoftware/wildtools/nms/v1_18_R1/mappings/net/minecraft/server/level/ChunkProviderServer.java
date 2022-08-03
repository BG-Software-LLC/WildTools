package com.bgsoftware.wildtools.nms.v1_18_R1.mappings.net.minecraft.server.level;

import com.bgsoftware.wildtools.nms.mapping.Remap;
import com.bgsoftware.wildtools.nms.v1_18_R1.mappings.MappedObject;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.PlayerChunkMap;
import net.minecraft.world.entity.Entity;

public class ChunkProviderServer extends MappedObject<net.minecraft.server.level.ChunkProviderServer> {

    public ChunkProviderServer(net.minecraft.server.level.ChunkProviderServer handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.server.level.ServerChunkCache",
            name = "broadcast",
            type = Remap.Type.METHOD,
            remappedName = "b")
    public void broadcast(Entity entity, Packet<?> packet) {
        handle.b(entity, packet);
    }

    @Remap(classPath = "net.minecraft.server.level.ServerChunkCache",
            name = "blockChanged",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void blockChanged(BlockPosition blockPosition) {
        handle.a(blockPosition);
    }

    @Remap(classPath = "net.minecraft.server.level.ServerChunkCache",
            name = "chunkMap",
            type = Remap.Type.FIELD,
            remappedName = "a")
    public PlayerChunkMap getPlayerChunkMap() {
        return handle.a;
    }

}
