package com.bgsoftware.wildtools.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerPickupItemEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class EventsSimulation {

    private static final List<Function<PickupItemEventParams, Event>> pickupItemEventCalls = new LinkedList<>();

    private EventsSimulation() {

    }

    public static void init() {
        registerPickupItemEventCalls();
    }

    public static boolean simulateItemPickupEvent(Player player, Item item, boolean checkPickupEventStatus) {
        PickupItemEventParams params = new PickupItemEventParams(player, item, item.getItemStack().getAmount());
        for (Function<PickupItemEventParams, Event> eventCreator : pickupItemEventCalls) {
            Event event = eventCreator.apply(params);
            Bukkit.getPluginManager().callEvent(event);
            if (checkPickupEventStatus && event instanceof Cancellable && ((Cancellable) event).isCancelled())
                return true;
        }

        return false;
    }

    private static void registerPickupItemEventCalls() {
        // TODO: Fix PlayerAttemptPickupItemEvent
//        try {
//            Class.forName("org.bukkit.event.player.PlayerAttemptPickupItemEvent");
//            pickupItemEventCalls.add(params -> new org.bukkit.event.player.PlayerAttemptPickupItemEvent(
//                    params.player,
//                    params.item,
//                    params.remaining
//            ));
//        } catch (ClassNotFoundException ignored) {
//        }

        pickupItemEventCalls.add(params -> new PlayerPickupItemEvent(
                params.player,
                params.item,
                params.remaining
        ));

        try {
            Class.forName("org.bukkit.event.entity.EntityPickupItemEvent");
            pickupItemEventCalls.add(params -> new org.bukkit.event.entity.EntityPickupItemEvent(
                    params.player,
                    params.item,
                    params.remaining
            ));
        } catch (ClassNotFoundException ignored) {
        }
    }

    private static class PickupItemEventParams {

        private final Player player;
        private final Item item;
        private final int remaining;

        PickupItemEventParams(Player player, Item item, int remaining) {
            this.player = player;
            this.item = item;
            this.remaining = remaining;
        }

    }

}
