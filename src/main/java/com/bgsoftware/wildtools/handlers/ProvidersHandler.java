package com.bgsoftware.wildtools.handlers;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import com.bgsoftware.wildtools.hooks.BlocksProvider;
import com.bgsoftware.wildtools.hooks.BlocksProvider_ASkyblock;
import com.bgsoftware.wildtools.hooks.BlocksProvider_AcidIsland;
import com.bgsoftware.wildtools.hooks.BlocksProvider_BentoBox;
import com.bgsoftware.wildtools.hooks.BlocksProvider_FabledSkyblock;
import com.bgsoftware.wildtools.hooks.BlocksProvider_FactionsOne;
import com.bgsoftware.wildtools.hooks.BlocksProvider_FactionsUUID;
import com.bgsoftware.wildtools.hooks.BlocksProvider_GriefPrevention;
import com.bgsoftware.wildtools.hooks.BlocksProvider_Lands;
import com.bgsoftware.wildtools.hooks.BlocksProvider_MassiveFactions;
import com.bgsoftware.wildtools.hooks.BlocksProvider_PlotSquared;
import com.bgsoftware.wildtools.hooks.BlocksProvider_SuperiorSkyblock;
import com.bgsoftware.wildtools.hooks.BlocksProvider_Towny;
import com.bgsoftware.wildtools.hooks.BlocksProvider_Villages;
import com.bgsoftware.wildtools.hooks.BlocksProvider_WorldGuard;

import com.bgsoftware.wildtools.hooks.PricesProvider_CMI;
import com.google.common.collect.Lists;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.bgsoftware.wildtools.hooks.FactionsProvider;
import com.bgsoftware.wildtools.hooks.FactionsProvider_Default;
import com.bgsoftware.wildtools.hooks.FactionsProvider_SavageFactions;
import com.bgsoftware.wildtools.hooks.PricesProvider;
import com.bgsoftware.wildtools.hooks.PricesProvider_Default;
import com.bgsoftware.wildtools.hooks.PricesProvider_Essentials;
import com.bgsoftware.wildtools.hooks.PricesProvider_ShopGUIPlus;

import java.util.List;

public final class ProvidersHandler {

    private static WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    static String pricesPlugin;

    private boolean isVaultEnabled = false;
    private Economy economy;

    private List<BlocksProvider> blocksProviders = Lists.newArrayList();
    private PricesProvider pricesProvider;
    private FactionsProvider factionsProvider;

    public ProvidersHandler(){
        //Prices Plugin Hookup
        if(pricesPlugin.equalsIgnoreCase("ShopGUIPlus") && Bukkit.getPluginManager().isPluginEnabled("ShopGUIPlus"))
            pricesProvider = new PricesProvider_ShopGUIPlus();
        else if(pricesPlugin.equalsIgnoreCase("Essentials") && Bukkit.getPluginManager().isPluginEnabled("Essentials"))
            pricesProvider = new PricesProvider_Essentials();
        else if(pricesPlugin.equals("CMI") && Bukkit.getPluginManager().isPluginEnabled("CMI"))
            pricesProvider = new PricesProvider_CMI();
        else pricesProvider = new PricesProvider_Default();
        //Factions Hookup
        if(Bukkit.getPluginManager().isPluginEnabled("Factions") &&
                Bukkit.getPluginManager().getPlugin("Factions").getDescription().getAuthors().contains("ProSavage"))
            factionsProvider = new FactionsProvider_SavageFactions();
        else factionsProvider = new FactionsProvider_Default();
        //Claim Hookup
        if(Bukkit.getPluginManager().isPluginEnabled("AcidIsland"))
            blocksProviders.add(new BlocksProvider_AcidIsland());
        if(Bukkit.getPluginManager().isPluginEnabled("ASkyBlock"))
            blocksProviders.add(new BlocksProvider_ASkyblock());
        if(Bukkit.getPluginManager().isPluginEnabled("BentoBox"))
            blocksProviders.add(new BlocksProvider_BentoBox());
        if(Bukkit.getPluginManager().isPluginEnabled("FabledSkyBlock"))
            blocksProviders.add(new BlocksProvider_FabledSkyblock());
        if(Bukkit.getPluginManager().isPluginEnabled("Factions")){
            if(Bukkit.getPluginManager().getPlugin("Factions").getDescription().getAuthors().contains("Daniel Saukel")){
                blocksProviders.add(new BlocksProvider_FactionsOne());
            }
            else if(Bukkit.getPluginManager().isPluginEnabled("MassiveCore")){
                blocksProviders.add(new BlocksProvider_MassiveFactions());
            }else {
                blocksProviders.add(new BlocksProvider_FactionsUUID());
            }
        }
        if(Bukkit.getPluginManager().isPluginEnabled("GriefPrevention"))
            blocksProviders.add(new BlocksProvider_GriefPrevention());
        if(Bukkit.getPluginManager().isPluginEnabled("SuperiorSkyblock2"))
            blocksProviders.add(new BlocksProvider_SuperiorSkyblock());
        if(Bukkit.getPluginManager().isPluginEnabled("Towny"))
            blocksProviders.add(new BlocksProvider_Towny());
        if(Bukkit.getPluginManager().isPluginEnabled("Villages"))
            blocksProviders.add(new BlocksProvider_Villages());
        if(Bukkit.getPluginManager().isPluginEnabled("WorldGuard"))
            blocksProviders.add(new BlocksProvider_WorldGuard());
        if(Bukkit.getPluginManager().isPluginEnabled("Lands"))
            blocksProviders.add(new BlocksProvider_Lands());
        if(Bukkit.getPluginManager().isPluginEnabled("PlotSquared"))
            blocksProviders.add(new BlocksProvider_PlotSquared());

    }

    /*
     * Hooks' methods
     */

    public double getPrice(Player player, ItemStack itemStack){
        try {
            return pricesProvider.getPrice(player, itemStack);
        }catch(Exception ex){
            return -1;
        }
    }

    public int getTNTAmountFromBank(Player player){
        return factionsProvider.getTNTAmountFromBank(player);
    }

    public void takeTNTFromBank(Player player, int amount){
        factionsProvider.takeTNTFromBank(player, amount);
    }

    public boolean canBreak(Player player, Block block, Tool tool){
        return canBreak(player, block, block.getType(), block.getState().getData().toItemStack().getDurability(), tool);
    }

    public boolean canBreak(Player player, Block block, Material firstType, short firstData, Tool tool){
        if(!tool.canBreakBlock(block, firstType, firstData) || plugin.getNMSAdapter().isOutsideWorldborder(block.getLocation()) ||
            block.getType() == Material.BEDROCK)
            return false;

        for(BlocksProvider blocksProvider : blocksProviders) {
            if (!blocksProvider.canBreak(player, block, tool.isOnlyInsideClaim()))
                return false;
        }
        return true;
    }

    public boolean canInteract(Player player, Block block, Tool tool){
        return canInteract(player, block, block.getType(), block.getState().getData().toItemStack().getDurability(), tool);
    }

    @SuppressWarnings("WeakerAccess")
    public boolean canInteract(Player player, Block block, Material firstType, short firstData, Tool tool){
        if(!tool.canBreakBlock(block, firstType, firstData) || plugin.getNMSAdapter().isOutsideWorldborder(block.getLocation()))
            return false;

        for(BlocksProvider blocksProvider : blocksProviders) {
            if (!blocksProvider.canInteract(player, block, tool.isOnlyInsideClaim()))
                return false;
        }
        return true;
    }

    /*
     * Handler' methods
     */

    public void enableVault(){
        isVaultEnabled = true;
        economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean trySellingItem(Player player, ItemStack itemStack, double multiplier){
        if(!canSellItem(player, itemStack))
            return false;

        double price = getPrice(player, itemStack) * multiplier;

        if(!economy.hasAccount(player))
            economy.createPlayerAccount(player);

        economy.depositPlayer(player, price);

        return true;
    }

    public boolean canSellItem(Player player, ItemStack itemStack){
        return isVaultEnabled && getPrice(player, itemStack) > 0;
    }

    public boolean isVaultEnabled(){
        return isVaultEnabled;
    }

}
