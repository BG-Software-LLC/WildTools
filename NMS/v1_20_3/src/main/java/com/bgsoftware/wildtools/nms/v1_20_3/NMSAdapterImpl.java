package com.bgsoftware.wildtools.nms.v1_20_3;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.wildtools.nms.v1_20_3.alogrithms.PaperGlowEnchantment;
import com.bgsoftware.wildtools.nms.v1_20_3.alogrithms.SpigotGlowEnchantment;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.entity.Entity;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.v1_20_R3.CraftRegistry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class NMSAdapterImpl extends com.bgsoftware.wildtools.nms.v1_20_3.AbstractNMSAdapter {

    private static final ReflectField<Map<NamespacedKey, Enchantment>> REGISTRY_CACHE =
            new ReflectField<>(CraftRegistry.class, Map.class, "cache");

    private static final Enchantment GLOW_ENCHANT = initializeGlowEnchantment();

    @Override
    protected void sendPacket(ServerChunkCache serverChunkCache, Entity entity, Packet<? super ClientGamePacketListener> packet) {
        serverChunkCache.broadcast(entity, packet);
    }

    @Override
    public void makeItemGlow(ItemMeta itemMeta) {
        itemMeta.addEnchant(GLOW_ENCHANT, 1, true);
    }

    private static Enchantment initializeGlowEnchantment() {
        Enchantment glowEnchant;

        try {
            glowEnchant = new PaperGlowEnchantment("wildtools_glowing_enchant");
        } catch (Throwable error) {
            glowEnchant = new SpigotGlowEnchantment("wildtools_glowing_enchant");
        }

        Map<NamespacedKey, Enchantment> registryCache = REGISTRY_CACHE.get(Registry.ENCHANTMENT);

        registryCache.put(glowEnchant.getKey(), glowEnchant);

        return glowEnchant;
    }

}
