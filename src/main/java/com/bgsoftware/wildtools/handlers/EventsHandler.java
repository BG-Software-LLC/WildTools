package com.bgsoftware.wildtools.handlers;

import com.bgsoftware.wildtools.utils.Pair;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.RegisteredListener;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class EventsHandler {

    private static final List<String> PRE_DEFINED_CLAIMING_PLUGINS = Arrays.asList("AcidIsland", "ASkyBlock",
            "BentoBox", "FabledSkyBlock", "Factions", "FactionsX", "GriefPrevention", "IslandWorld", "Lands",
            "PlotSquared", "Residence", "SuperiorSkyblock2", "Villages", "WorldGuard");

    private final List<Pair<Method, Listener>> claimingPluginsBreakMethods = new ArrayList<>();
    private final List<Pair<Method, Listener>> claimingPluginsPlaceMethods = new ArrayList<>();
    private final List<Pair<Method, Listener>> claimingPluginsInteractMethods = new ArrayList<>();
    private final List<Pair<Method, Listener>> otherPluginsBreakMethods = new ArrayList<>();

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
        claimingPluginsPlaceMethods.clear();
        for(RegisteredListener registeredListener : BlockPlaceEvent.getHandlerList().getRegisteredListeners()){
            if(claimingPlugins.contains(registeredListener.getPlugin().getName()))
                addAllMethods(claimingPluginsPlaceMethods, registeredListener.getListener(), BlockPlaceEvent.class);
        }
        claimingPluginsInteractMethods.clear();
        for(RegisteredListener registeredListener : PlayerInteractEvent.getHandlerList().getRegisteredListeners()){
            if(claimingPlugins.contains(registeredListener.getPlugin().getName()))
                addAllMethods(claimingPluginsInteractMethods, registeredListener.getListener(), PlayerInteractEvent.class);
        }
    }

    public void loadOtherPlugins(List<String> otherPlugins){
        for(RegisteredListener registeredListener : BlockBreakEvent.getHandlerList().getRegisteredListeners()){
            if(otherPlugins.contains(registeredListener.getPlugin().getName()))
                addAllMethods(otherPluginsBreakMethods, registeredListener.getListener(), BlockBreakEvent.class);
        }
    }

    private static void callMethods(List<Pair<Method, Listener>> methodList, Event event){
        methodList.forEach(pair -> {
            try{
                pair.getX().invoke(pair.getY(), event);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        });
    }

    private static void addAllMethods(List<Pair<Method, Listener>> methodsList, Listener listener, Class<?> eventClass){
        for(Method method : listener.getClass().getDeclaredMethods()){
            if(method.getParameterCount() == 1 && Arrays.asList(method.getParameterTypes()).contains(eventClass)){
                method.setAccessible(true);
                methodsList.add(new Pair<>(method, listener));
            }
        }
    }

}
