package com.bgsoftware.wildtools.handlers;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import com.bgsoftware.wildtools.utils.items.ToolItemStack;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
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

    private final List<CachedListenerMethod> claimingPluginsBreakMethods = new LinkedList<>();
    private final List<CachedListenerMethod> claimingPluginsPlaceMethods = new LinkedList<>();
    private final List<CachedListenerMethod> claimingPluginsInteractMethods = new LinkedList<>();
    private final List<CachedListenerMethod> globalNotifiedPluginsBreakMethods = new LinkedList<>();
    private final Map<Tool, List<CachedListenerMethod>> notifiedPluginsBreakMethodsTools = new HashMap<>();

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
        List<CachedListenerMethod> getOtherPluginsMethods = notifiedPluginsBreakMethodsTools.get(tool);
        if (getOtherPluginsMethods != null)
            callMethods(getOtherPluginsMethods, blockBreakEvent);
    }

    public void callPlaceEvent(BlockPlaceEvent blockPlaceEvent) {
        callMethods(claimingPluginsPlaceMethods, blockPlaceEvent);
    }

    public void callInteractEvent(PlayerInteractEvent playerInteractEvent) {
        callMethods(claimingPluginsInteractMethods, playerInteractEvent);
    }

    public void loadClaimingPlugins(List<String> claimingPlugins) {
        // We want to initialize some well-known plugins for claims.
        claimingPlugins.addAll(PRE_DEFINED_CLAIMING_PLUGINS);

        claimingPluginsBreakMethods.clear();
        for (RegisteredListener registeredListener : BlockBreakEvent.getHandlerList().getRegisteredListeners()) {
            if (claimingPlugins.contains(registeredListener.getPlugin().getName()))
                addAllMethods(claimingPluginsBreakMethods, registeredListener.getListener(), BlockBreakEvent.class);
        }
        claimingPluginsBreakMethods.sort(CachedListenerMethod::compareTo);

        claimingPluginsPlaceMethods.clear();
        for (RegisteredListener registeredListener : BlockPlaceEvent.getHandlerList().getRegisteredListeners()) {
            if (claimingPlugins.contains(registeredListener.getPlugin().getName()))
                addAllMethods(claimingPluginsPlaceMethods, registeredListener.getListener(), BlockPlaceEvent.class);
        }
        claimingPluginsPlaceMethods.sort(CachedListenerMethod::compareTo);

        claimingPluginsInteractMethods.clear();
        for (RegisteredListener registeredListener : PlayerInteractEvent.getHandlerList().getRegisteredListeners()) {
            if (claimingPlugins.contains(registeredListener.getPlugin().getName()))
                addAllMethods(claimingPluginsInteractMethods, registeredListener.getListener(), PlayerInteractEvent.class);
        }
        claimingPluginsInteractMethods.sort(CachedListenerMethod::compareTo);
    }

    public void loadNotifiedPlugins(List<String> otherPlugins) {
        loadNotifiedPluginListeners0(otherPlugins, globalNotifiedPluginsBreakMethods);
        loadNotifiedForTools();
    }

    public void loadNotifiedForTools() {
        plugin.getToolsManager().getTools().stream()
                .filter(tool -> !tool.getNotifiedPlugins().isEmpty())
                .forEach(tool -> {
                    List<CachedListenerMethod> notifiedPlugins = notifiedPluginsBreakMethodsTools
                            .computeIfAbsent(tool, t -> new LinkedList<>());
                    loadNotifiedPluginListeners0(tool.getNotifiedPlugins(), notifiedPlugins);
                    if (notifiedPlugins.isEmpty())
                        notifiedPluginsBreakMethodsTools.remove(tool);
                });
    }

    private void loadNotifiedPluginListeners0(Collection<String> notifiedPlugins, List<CachedListenerMethod> cachedListenerMethods) {
        cachedListenerMethods.clear();

        Set<CachedListenerMethod> cachedListenerMethodsSet = new LinkedHashSet<>();

        for (RegisteredListener registeredListener : BlockBreakEvent.getHandlerList().getRegisteredListeners()) {
            if (notifiedPlugins.contains(registeredListener.getPlugin().getName())) {
                addAllMethods0(cachedListenerMethodsSet, registeredListener.getListener(), BlockBreakEvent.class);
            }
        }

        cachedListenerMethods.addAll(cachedListenerMethodsSet);
        cachedListenerMethods.sort(CachedListenerMethod::compareTo);
    }

    private static void callMethods(List<CachedListenerMethod> methodList, Event event) {
        methodList.forEach(method -> method.invoke(event));
    }

    private static void addAllMethods(List<CachedListenerMethod> methodsList, Listener listener, Class<?> eventClass) {
        Set<CachedListenerMethod> methodsSet = new LinkedHashSet<>();
        addAllMethods0(methodsSet, listener, eventClass);
        methodsList.addAll(methodsSet);
    }

    private static void addAllMethods0(Set<CachedListenerMethod> methods, Listener listener, Class<?> eventClass) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            EventHandler eventHandler = method.getAnnotation(EventHandler.class);
            if (eventHandler != null && method.getParameterCount() == 1 && method.getParameterTypes()[0].equals(eventClass)) {
                method.setAccessible(true);
                methods.add(new CachedListenerMethod(listener, method, eventHandler));
            }
        }

        // Checking the super class
        Class<?> superClass = listener.getClass().getSuperclass();

        if (Listener.class.isAssignableFrom(superClass)) {
            for (Method method : superClass.getDeclaredMethods()) {
                EventHandler eventHandler = method.getAnnotation(EventHandler.class);
                if (eventHandler != null && method.getParameterCount() == 1 && method.getParameterTypes()[0].equals(eventClass)) {
                    method.setAccessible(true);
                    methods.add(new CachedListenerMethod(listener, method, eventHandler));
                }
            }
        }
    }

    private static class CachedListenerMethod implements Comparable<CachedListenerMethod> {

        private final Listener listener;
        private final Method method;
        private final EventHandler eventHandler;

        CachedListenerMethod(Listener listener, Method method, EventHandler eventHandler) {
            this.listener = listener;
            this.method = method;
            this.eventHandler = eventHandler;
        }

        void invoke(Event event) {
            try {
                if (!eventHandler.ignoreCancelled() || !(event instanceof Cancellable) || !((Cancellable) event).isCancelled())
                    method.invoke(listener, event);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public int compareTo(@NotNull CachedListenerMethod o) {
            return eventHandler.priority().compareTo(o.eventHandler.priority());
        }

        @Override
        public boolean equals(Object object) {
            if (object == null || getClass() != object.getClass()) return false;
            CachedListenerMethod that = (CachedListenerMethod) object;
            return this.method.equals(that.method);
        }

        @Override
        public int hashCode() {
            return this.method.hashCode();
        }

    }

}
