package com.bgsoftware.wildtools.nms.v1_17;

import com.bgsoftware.wildtools.nms.alogrithms.PaperGlowEnchantment;
import com.bgsoftware.wildtools.nms.alogrithms.SpigotGlowEnchantment;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.entity.Entity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;

public class NMSAdapterImpl extends com.bgsoftware.wildtools.nms.v1_17.AbstractNMSAdapter {

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

        try {
            Field field = Enchantment.class.getDeclaredField("acceptingNew");
            field.setAccessible(true);
            field.set(null, true);
            field.setAccessible(false);
        } catch (Exception ignored) {
        }

        try {
            Enchantment.registerEnchantment(glowEnchant);
        } catch (Exception ignored) {
        }

        return glowEnchant;
    }

}
