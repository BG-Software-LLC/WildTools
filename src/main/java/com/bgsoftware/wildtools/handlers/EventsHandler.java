package com.bgsoftware.wildtools.handlers;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import com.bgsoftware.wildtools.utils.items.ToolItemStack;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.RegisteredListener;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EventsHandler {

    private static final List<String> PRE_DEFINED_CLAIMING_PLUGINS = Arrays.asList("AcidIsland", "ASkyBlock",
            "BentoBox", "FabledSkyBlock", "Factions", "FactionsX", "GriefPrevention", "IslandWorld", "Lands",
            "PlotSquared", "Residence", "SuperiorSkyblock2", "Villages", "WorldGuard");

    private final List<RegisteredListener> claimingPluginsBreakMethods = new LinkedList<>();
    private final List<RegisteredListener> claimingPluginsPlaceMethods = new LinkedList<>();
    private final List<RegisteredListener> claimingPluginsInteractMethods = new LinkedList<>();
    private final List<RegisteredListener> globalNotifiedPluginsBreakMethods = new LinkedList<>();
    private final Map<Tool, List<RegisteredListener>> notifiedPluginsBreakMethodsTools = new HashMap<>();

    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    public EventsHandler() {

    }

    public void callBreakEvent(BlockBreakEvent blockBreakEvent, boolean claimingCheck) {
        if (claimingCheck) {
            callMethods(claimingPluginsBreakMethods, blockBreakEvent);
            return;
        }

        callMethods(globalNotifiedPluginsBreakMethods, blockBreakEvent);

        ToolItemStack toolItemStack = ToolItemStack.of(plugin.getNMSAdapter().getItemInHand(blockBreakEvent.getPlayer()));
        Tool tool = toolItemStack.getTool();
        List<RegisteredListener> getOtherPluginsMethods = notifiedPluginsBreakMethodsTools.get(tool);
        if (getOtherPluginsMethods != null)
            callMethods(getOtherPluginsMethods, blockBreakEvent);
    }

    public void callPlaceEvent(BlockPlaceEvent blockPlaceEvent) {
        callMethods(claimingPluginsPlaceMethods, blockPlaceEvent);
    }

    public void callInteractEvent(PlayerInteractEvent playerInteractEvent) {
        callMethods(claimingPluginsInteractMethods, playerInteractEvent);
    }

    private static void setEventExecutors(HandlerList handlerList,
                                          Collection<String> whitelistedPlugins,
                                          List<RegisteredListener> cachedEventExecutors) {
        cachedEventExecutors.clear();
        collectEventExecutors(handlerList, whitelistedPlugins, cachedEventExecutors);
        cachedEventExecutors.sort(COMPARATOR);
    }

    private static void collectEventExecutors(HandlerList handlerList,
                                              Collection<String> whitelistedPlugins,
                                              Collection<RegisteredListener> cachedEventExecutors) {
        for (RegisteredListener registeredListener : handlerList.getRegisteredListeners()) {
            if (whitelistedPlugins.contains(registeredListener.getPlugin().getName())) {
                cachedEventExecutors.add(registeredListener);
            }
        }
    }

    private static final Comparator<RegisteredListener> COMPARATOR = Comparator.comparing(RegisteredListener::getPriority);

    public void loadClaimingPlugins(List<String> claimingPlugins) {
        // We want to initialize some well-known plugins for claims.
        claimingPlugins.addAll(PRE_DEFINED_CLAIMING_PLUGINS);

        setEventExecutors(BlockBreakEvent.getHandlerList(), claimingPlugins, claimingPluginsBreakMethods);
        setEventExecutors(BlockPlaceEvent.getHandlerList(), claimingPlugins, claimingPluginsPlaceMethods);
        setEventExecutors(PlayerInteractEvent.getHandlerList(), claimingPlugins, claimingPluginsInteractMethods);
    }

    public void loadNotifiedPlugins(List<String> otherPlugins) {
        loadNotifiedPluginListeners0(otherPlugins, globalNotifiedPluginsBreakMethods);
        loadNotifiedForTools();
    }

    public void loadNotifiedForTools() {
        plugin.getToolsManager().getTools().stream()
                .filter(tool -> !tool.getNotifiedPlugins().isEmpty())
                .forEach(tool -> {
                    List<RegisteredListener> notifiedPlugins = notifiedPluginsBreakMethodsTools
                            .computeIfAbsent(tool, t -> new LinkedList<>());
                    loadNotifiedPluginListeners0(tool.getNotifiedPlugins(), notifiedPlugins);
                    if (notifiedPlugins.isEmpty())
                        notifiedPluginsBreakMethodsTools.remove(tool);
                });
    }

    private void loadNotifiedPluginListeners0(Collection<String> notifiedPlugins, List<RegisteredListener> cachedListenerMethods) {
        Set<RegisteredListener> cachedListenerMethodsSet = new LinkedHashSet<>();
        collectEventExecutors(BlockBreakEvent.getHandlerList(), notifiedPlugins, cachedListenerMethodsSet);

        cachedListenerMethods.clear();
        cachedListenerMethods.addAll(cachedListenerMethodsSet);
        cachedListenerMethods.sort(COMPARATOR);
    }

    private static void callMethods(List<RegisteredListener> registeredListeners, Event event) {
        registeredListeners.forEach(registeredListener -> {
            try {
                registeredListener.callEvent(event);
            } catch (EventException error) {
                // TODO
            }
        });
    }

}
