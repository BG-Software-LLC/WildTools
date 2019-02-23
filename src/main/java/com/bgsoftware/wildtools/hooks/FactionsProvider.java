package com.bgsoftware.wildtools.hooks;

import org.bukkit.entity.Player;

public interface FactionsProvider {

    int getTNTAmountFromBank(Player player);

    void takeTNTFromBank(Player player, int amount);

}
