package com.bgsoftware.wildtools.hooks;

import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import org.bukkit.entity.Player;

public final class AntiCheatProvider_NoCheatPlus implements AntiCheatProvider {

    @Override
    public void enableBypass(Player player) {
        NCPExemptionManager.exemptPermanently(player);
    }

    @Override
    public void disableBypass(Player player) {
        NCPExemptionManager.unexempt(player);
    }

}
