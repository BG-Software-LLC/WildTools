package com.bgsoftware.wildtools.api.events;

import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

@SuppressWarnings("unused")
public final class SellWandUseEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Chest chest;

    private boolean cancelled;
    private String sellMessage;
    private double price;

    public SellWandUseEvent(Player player, Chest chest, double price, String sellMessage){
        super(player);
        this.chest = chest;
        this.sellMessage = sellMessage;
        this.price = price;
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
