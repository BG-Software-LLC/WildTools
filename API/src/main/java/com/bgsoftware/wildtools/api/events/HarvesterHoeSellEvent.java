package com.bgsoftware.wildtools.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

@SuppressWarnings("unused")
public final class HarvesterHoeSellEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancelled;
    private String sellMessage;
    private double price, multiplier;

    @Deprecated
    public HarvesterHoeSellEvent(Player player, double price, String sellMessage){
        this(player, price, 1, sellMessage);
    }

    public HarvesterHoeSellEvent(Player player, double price, double multiplier, String sellMessage){
        super(player);
        this.price = price;
        this.multiplier = multiplier;
        this.sellMessage = sellMessage;
        this.cancelled = false;
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

    public String getMessage() {
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
