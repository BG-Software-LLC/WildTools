package com.bgsoftware.wildtools.api.hooks;

import java.util.HashMap;
import java.util.Map;

public class SellInfo {

    private final Map<Integer, SoldItem> soldItems = new HashMap<>();
    private final double totalEarnings;

    /**
     * Constructor for the sell info object.
     * @param soldItems The sold items of the transaction.
     * @param totalEarnings The total earnings that were made from this transaction.
     */
    public SellInfo(Map<Integer, SoldItem> soldItems, double totalEarnings){
        this.soldItems.putAll(soldItems);
        this.totalEarnings = totalEarnings;
    }

    /**
     * Get all the sold items of the transaction.
     */
    public Map<Integer, SoldItem> getSoldItems() {
        return soldItems;
    }

    /**
     * Get the total earnings from this transaction.
     */
    public double getTotalEarnings() {
        return totalEarnings;
    }
}
