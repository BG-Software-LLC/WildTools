package com.bgsoftware.wildtools.handlers;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class EventsHandler {

    private static final List<String> PRE_DEFINED_CLAIMING_PLUGINS = Arrays.asList("AcidIsland", "ASkyBlock",
            "BentoBox", "FabledSkyBlock", "Factions", "FactionsX", "GriefPrevention", "IslandWorld", "Lands",
            "PlotSquared", "Residence", "SuperiorSkyblock2", "Villages", "WorldGuard");

    private final List<CachedListenerMethod> claimingPluginsBreakMethods = new ArrayList<>();
    private final List<CachedListenerMethod> claimingPluginsPlaceMethods = new ArrayList<>();
    private final List<CachedListenerMethod> claimingPluginsInteractMethods = new ArrayList<>();
    private final List<CachedListenerMethod> otherPluginsBreakMethods = new ArrayList<>();

    public EventsHandler(){

    }

    public void callBreakEvent(BlockBreakEvent blockBreakEvent, boolean claimingCheck){
        callMethods(claimingCheck ? claimingPluginsBreakMethods : otherPluginsBreakMethods, blockBreakEvent);
    }

    public void callPlaceEvent(BlockPlaceEvent blockPlaceEvent){
        callMethods(claimingPluginsPlaceMethods, blockPlaceEvent);
    }

    public void callInteractEvent(PlayerInteractEvent playerInteractEvent){
        callMethods(claimingPluginsInteractMethods, playerInteractEvent);
    }

    public void loadClaimingPlugins(List<String> claimingPlugins){
        // We want to initialize some well-known plugins for claims.
        claimingPlugins.addAll(PRE_DEFINED_CLAIMING_PLUGINS);

        claimingPluginsBreakMethods.clear();
        for(RegisteredListener registeredListener : BlockBreakEvent.getHandlerList().getRegisteredListeners()){
            if(claimingPlugins.contains(registeredListener.getPlugin().getName()))
                addAllMethods(claimingPluginsBreakMethods, registeredListener.getListener(), BlockBreakEvent.class);
        }
        claimingPluginsBreakMethods.sort(CachedListenerMethod::compareTo);

        claimingPluginsPlaceMethods.clear();
        for(RegisteredListener registeredListener : BlockPlaceEvent.getHandlerList().getRegisteredListeners()){
            if(claimingPlugins.contains(registeredListener.getPlugin().getName()))
                addAllMethods(claimingPluginsPlaceMethods, registeredListener.getListener(), BlockPlaceEvent.class);
        }
        claimingPluginsPlaceMethods.sort(CachedListenerMethod::compareTo);

        claimingPluginsInteractMethods.clear();
        for(RegisteredListener registeredListener : PlayerInteractEvent.getHandlerList().getRegisteredListeners()){
            if(claimingPlugins.contains(registeredListener.getPlugin().getName()))
                addAllMethods(claimingPluginsInteractMethods, registeredListener.getListener(), PlayerInteractEvent.class);
        }
        claimingPluginsInteractMethods.sort(CachedListenerMethod::compareTo);
    }

    public void loadOtherPlugins(List<String> otherPlugins){
        otherPluginsBreakMethods.clear();
        for(RegisteredListener registeredListener : BlockBreakEvent.getHandlerList().getRegisteredListeners()){
            if(otherPlugins.contains(registeredListener.getPlugin().getName()))
                addAllMethods(otherPluginsBreakMethods, registeredListener.getListener(), BlockBreakEvent.class);
        }
        otherPluginsBreakMethods.sort(CachedListenerMethod::compareTo);
    }

    private static void callMethods(List<CachedListenerMethod> methodList, Event event){
        methodList.forEach(method -> method.invoke(event));
    }

    private static void addAllMethods(List<CachedListenerMethod> methodsList, Listener listener, Class<?> eventClass){
        for(Method method : listener.getClass().getDeclaredMethods()){
            EventHandler eventHandler = method.getAnnotation(EventHandler.class);
            if(eventHandler != null && method.getParameterCount() == 1 && method.getParameterTypes()[0].equals(eventClass)){
                method.setAccessible(true);
                methodsList.add(new CachedListenerMethod(listener, method, eventHandler));
            }
        }
    }

    private static final class CachedListenerMethod implements Comparable<CachedListenerMethod> {

        private final Listener listener;
        private final Method method;
        private final EventHandler eventHandler;

        CachedListenerMethod(Listener listener, Method method, EventHandler eventHandler){
            this.listener = listener;
            this.method = method;
            this.eventHandler = eventHandler;
        }

        void invoke(Event event){
            try{
                if(!eventHandler.ignoreCancelled() || !(event instanceof Cancellable) || !((Cancellable) event).isCancelled())
                    method.invoke(listener, event);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        @Override
        public int compareTo(@NotNull CachedListenerMethod o) {
            return eventHandler.priority().compareTo(o.eventHandler.priority());
        }
    }

}
