package com.bgsoftware.wildtools.api.events;

import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

@SuppressWarnings("unused")
public final class SellWandUseEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Chest chest;
    private final Player player;

    private boolean cancelled;
    private String sellMessage;
    private double price, multiplier;

    @Deprecated
    public SellWandUseEvent(Player player, Chest chest, double price, String sellMessage){
        this(player, chest, price, 1, sellMessage);
    }

    public SellWandUseEvent(Player player, Chest chest, double price, double multiplier, String sellMessage){
        super(true);
        this.player = player;
        this.chest = chest;
        this.sellMessage = sellMessage;
        this.price = price;
        this.multiplier = multiplier;
        this.cancelled = false;
    }

    public Chest getChest(){
        return chest;
    }

    public double getPrice(){
        return price;
    }

    public void setPrice(double price){
        this.price = price;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public String getMessage(){
        return sellMessage;
    }

    public void setMessage(String sellMessage){
        this.sellMessage = sellMessage;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
