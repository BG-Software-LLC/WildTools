package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.WildToolsPlugin;
import net.prosavage.factionsx.core.FPlayer;
import net.prosavage.factionsx.core.Faction;
import net.prosavage.factionsx.core.FactionTNTData;
import net.prosavage.factionsx.manager.PlayerManager;
import net.prosavage.factionsx.persist.TNTAddonData;
import org.bukkit.entity.Player;

public final class FactionsProvider_FactionsX implements FactionsProvider {

    public FactionsProvider_FactionsX(){
        WildToolsPlugin.log(" - Couldn't find any factions providers for tnt banks, using default one.");
    }

    @Override
    public int getTNTAmountFromBank(Player player) {
        FPlayer fPlayer = PlayerManager.INSTANCE.getFPlayer(player);
        Faction faction = fPlayer.getFaction();
        return TNTAddonData.INSTANCE.getTntData().getTNTData(faction).getTntAmt();
    }

    @Override
    public void takeTNTFromBank(Player player, int amount) {
        FPlayer fPlayer = PlayerManager.INSTANCE.getFPlayer(player);
        Faction faction = fPlayer.getFaction();
        FactionTNTData.TNTData tntData = TNTAddonData.INSTANCE.getTntData().getTNTData(faction);
        tntData.setTntAmt(tntData.getTntAmt() - amount);
    }

}
