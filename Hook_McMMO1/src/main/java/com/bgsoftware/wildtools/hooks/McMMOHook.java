package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildtools.Locale;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.hooks.listener.IToolBlockListener;
import com.gmail.nossr50.events.skills.abilities.McMMOPlayerAbilityActivateEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused")
public final class McMMOHook {

    private static final ReflectMethod<Object> MCMMO_GET_PLACESTORE = new ReflectMethod<>(com.gmail.nossr50.mcMMO.class, "getPlaceStore");
    private static ReflectMethod<Void> MCMMO_PLACESTORE_SET = null;
    private static WildToolsPlugin plugin;

    static {
        try {
            Class<?> placeStoreRetClass = Class.forName("com.gmail.nossr50.util.blockmeta.chunkmeta.ChunkManager");
            MCMMO_PLACESTORE_SET = new ReflectMethod<>(placeStoreRetClass, "setTrue", Block.class);
        } catch (Throwable ignored) {
        }
    }

    public static void register(WildToolsPlugin plugin) {
        McMMOHook.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(new McMMOListener(), plugin);
        plugin.getProviders().registerToolBlockListener(McMMOHook::recordBlockChange);
    }

    private static void recordBlockChange(Location location, IToolBlockListener.Action action) {
        if (action != IToolBlockListener.Action.BLOCK_PLACE)
            return;

        Block block = location.getBlock();

        try {
            com.gmail.nossr50.mcMMO.getPlaceStore().setTrue(block);
        } catch (Throwable error) {
            Object placeStore = MCMMO_GET_PLACESTORE.invoke(null);
            MCMMO_PLACESTORE_SET.invoke(placeStore, block);
        }
    }

    private static final class McMMOListener implements Listener {

        private static final Set<UUID> messageCooldowns = new HashSet<>();

        @EventHandler
        public void onPlayerAbilityActive(McMMOPlayerAbilityActivateEvent e) {
            if (!getAbilityName(e).equals("SUPER_BREAKER"))
                return;

            if (plugin.getToolsManager().getTool(plugin.getNMSAdapter().getItemInHand(e.getPlayer())) == null)
                return;

            e.setCancelled(true);

            if (!messageCooldowns.contains(e.getPlayer().getUniqueId())) {
                Locale.MCMMO_TOOL_SUPER_BREAKER.send(e.getPlayer());
                messageCooldowns.add(e.getPlayer().getUniqueId());
                Bukkit.getScheduler().runTaskLater(plugin, () -> messageCooldowns.remove(e.getPlayer().getUniqueId()), 100L);
            }
        }

        private String getAbilityName(McMMOPlayerAbilityActivateEvent event) {
            try {
                return event.getAbility().name();
            } catch (Throwable ex) {
                try {
                    Object ability = McMMOPlayerAbilityActivateEvent.class.getMethod("getAbility").invoke(event);
                    return ability.toString().toUpperCase();
                } catch (Throwable ex1) {
                    ex1.printStackTrace();
                    return "";
                }
            }
        }

    }

}
