package com.bgsoftware.wildtools.utils.container;

import com.bgsoftware.wildtools.objects.tools.WSellTool;

import java.util.HashMap;
import java.util.Map;

public final class SellInfo {

    public static final SellInfo EMPTY = new SellInfo(new HashMap<>(), 0.0);

    private final Map<Integer, WSellTool.SoldItem> soldItems = new HashMap<>();
    private final double totalEarnings;

    public SellInfo(Map<Integer, WSellTool.SoldItem> soldItems, double totalEarnings){
        this.soldItems.putAll(soldItems);
        this.totalEarnings = totalEarnings;
    }

    public Map<Integer, WSellTool.SoldItem> getSoldItems() {
        return soldItems;
    }

    public double getTotalEarnings() {
        return totalEarnings;
    }
}
