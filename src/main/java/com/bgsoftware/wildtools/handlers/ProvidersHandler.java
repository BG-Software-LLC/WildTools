package com.bgsoftware.wildtools.handlers;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import com.bgsoftware.wildtools.hooks.BlockActionProvider;
import com.bgsoftware.wildtools.hooks.BlockActionProvider_CoreProtect;
import com.bgsoftware.wildtools.hooks.BlockActionProvider_Jobs;
import com.bgsoftware.wildtools.hooks.BlockActionProvider_WildStacker;
import com.bgsoftware.wildtools.hooks.BlocksProvider;
import com.bgsoftware.wildtools.hooks.BlocksProvider_ASkyblock;
import com.bgsoftware.wildtools.hooks.BlocksProvider_AcidIsland;
import com.bgsoftware.wildtools.hooks.BlocksProvider_BentoBox;
import com.bgsoftware.wildtools.hooks.BlocksProvider_ChunkHoppers;
import com.bgsoftware.wildtools.hooks.BlocksProvider_FabledSkyblock;
import com.bgsoftware.wildtools.hooks.BlocksProvider_FactionsOne;
import com.bgsoftware.wildtools.hooks.BlocksProvider_FactionsUUID;
import com.bgsoftware.wildtools.hooks.BlocksProvider_FactionsX;
import com.bgsoftware.wildtools.hooks.BlocksProvider_GriefPrevention;
import com.bgsoftware.wildtools.hooks.BlocksProvider_Lands;
import com.bgsoftware.wildtools.hooks.BlocksProvider_MassiveFactions;
import com.bgsoftware.wildtools.hooks.BlocksProvider_PlotSquared;
import com.bgsoftware.wildtools.hooks.BlocksProvider_PlotSquaredLegacy;
import com.bgsoftware.wildtools.hooks.BlocksProvider_Residence;
import com.bgsoftware.wildtools.hooks.BlocksProvider_SnowGearsShops;
import com.bgsoftware.wildtools.hooks.BlocksProvider_SuperiorSkyblock;
import com.bgsoftware.wildtools.hooks.BlocksProvider_Towny;
import com.bgsoftware.wildtools.hooks.BlocksProvider_Villages;
import com.bgsoftware.wildtools.hooks.BlocksProvider_WorldGuard;
import com.bgsoftware.wildtools.hooks.ContainerProvider;
import com.bgsoftware.wildtools.hooks.ContainerProvider_ChunkCollectors;
import com.bgsoftware.wildtools.hooks.ContainerProvider_Default;
import com.bgsoftware.wildtools.hooks.ContainerProvider_WildChests;
import com.bgsoftware.wildtools.hooks.DropsProvider;
import com.bgsoftware.wildtools.hooks.DropsProvider_ChunkHoppers;
import com.bgsoftware.wildtools.hooks.DropsProvider_SilkSpawners;
import com.bgsoftware.wildtools.hooks.DropsProvider_VoidChest;
import com.bgsoftware.wildtools.hooks.DropsProvider_WildStacker;
import com.bgsoftware.wildtools.hooks.DropsProviders_WildToolsSpawners;
import com.bgsoftware.wildtools.hooks.BlockActionProvider_mcMMO;
import com.bgsoftware.wildtools.hooks.PerWorldPluginsHook;
import com.bgsoftware.wildtools.hooks.PricesProvider_CMI;
import com.bgsoftware.wildtools.hooks.FactionsProvider;
import com.bgsoftware.wildtools.hooks.FactionsProvider_Default;
import com.bgsoftware.wildtools.hooks.FactionsProvider_SavageFactions;
import com.bgsoftware.wildtools.hooks.PricesProvider;
import com.bgsoftware.wildtools.hooks.PricesProvider_Default;
import com.bgsoftware.wildtools.hooks.PricesProvider_Essentials;
import com.bgsoftware.wildtools.hooks.PricesProvider_GUIShop;
import com.bgsoftware.wildtools.hooks.PricesProvider_NewtShop;
import com.bgsoftware.wildtools.hooks.PricesProvider_ShopGUIPlus;

import com.bgsoftware.wildtools.utils.container.SellInfo;
import com.google.common.collect.Lists;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public final class ProvidersHandler {

    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    static String pricesPlugin;

    private boolean isVaultEnabled = false;
    private Economy economy;

    private final List<BlocksProvider> blocksProviders = Lists.newArrayList();
    private final List<BlockActionProvider> blockActionProviders = Lists.newArrayList();
    private final List<DropsProvider> dropsProviders = Lists.newArrayList();
    private final List<ContainerProvider> containerProviders = Lists.newArrayList();
    private PricesProvider pricesProvider;
    private FactionsProvider factionsProvider;

    public ProvidersHandler(){
        loadData();
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
            if (PerWorldPluginsHook.isPluginEnabled(blocksProvider.getPlugin(), block.getWorld()) &&
                    !blocksProvider.canBreak(player, block, tool.isOnlyInsideClaim()))
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
            if (PerWorldPluginsHook.isPluginEnabled(blocksProvider.getPlugin(), block.getWorld()) &&
                    !blocksProvider.canInteract(player, block, tool.isOnlyInsideClaim()))
                return false;
        }
        return true;
    }

    public boolean canPickupItem(Player player, Item item){
        for(BlocksProvider blocksProvider : blocksProviders) {
            if (PerWorldPluginsHook.isPluginEnabled(blocksProvider.getPlugin(), item.getWorld()) &&
                    !blocksProvider.canPickupItem(player, item))
                return false;
        }
        return true;
    }

    public List<ItemStack> getBlockDrops(Block block, boolean onlySpawner){
        List<ItemStack> drops = new ArrayList<>();
        dropsProviders.stream().filter(dropsProvider -> onlySpawner == dropsProvider.isSpawnersOnly())
                .forEach(dropsProvider -> drops.addAll(dropsProvider.getBlockDrops(block)));
        return drops;
    }

    public boolean callEvent(){
        return dropsProviders.stream().allMatch(DropsProvider::callEvent);
    }

    public boolean isContainer(BlockState blockState){
        for(ContainerProvider containerProvider : containerProviders){
            if(containerProvider.isContainer(blockState))
                return true;
        }

        return false;
    }

    public SellInfo sellContainer(BlockState blockState, Player player){
        for(ContainerProvider containerProvider : containerProviders){
            if(containerProvider.isContainer(blockState))
                return containerProvider.sellContainer(blockState, player);
        }

        return SellInfo.EMPTY;
    }

    public void removeContainer(BlockState blockState, SellInfo sellInfo){
        for(ContainerProvider containerProvider : containerProviders){
            if(containerProvider.isContainer(blockState)) {
                containerProvider.removeContainer(blockState, sellInfo);
                break;
            }
        }
    }

    public void onBlockBreak(Player player, Block block, ItemStack usedItem){
        blockActionProviders.forEach(blockActionProvider -> blockActionProvider.onBlockBreak(player, block, usedItem));
    }

    /*
     * Handler' methods
     */

    public void enableVault(){
        isVaultEnabled = true;
        economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
    }

    public void depositPlayer(Player player, double price){
        economy.depositPlayer(player, price);
    }

    public boolean canSellItem(Player player, ItemStack itemStack){
        return isVaultEnabled && getPrice(player, itemStack) > 0;
    }

    public boolean isVaultEnabled(){
        return isVaultEnabled;
    }

    private void loadData(){
        //Prices Plugin Hookup
        if(pricesPlugin.equalsIgnoreCase("ShopGUIPlus") && Bukkit.getPluginManager().isPluginEnabled("ShopGUIPlus"))
            pricesProvider = new PricesProvider_ShopGUIPlus();
        else if(pricesPlugin.equalsIgnoreCase("GUIShop") && Bukkit.getPluginManager().isPluginEnabled("GUIShop"))
            pricesProvider = new PricesProvider_GUIShop();
        else if(pricesPlugin.equalsIgnoreCase("Essentials") && Bukkit.getPluginManager().isPluginEnabled("Essentials"))
            pricesProvider = new PricesProvider_Essentials();
        else if(pricesPlugin.equals("CMI") && Bukkit.getPluginManager().isPluginEnabled("CMI"))
            pricesProvider = new PricesProvider_CMI();
        if(pricesPlugin.equalsIgnoreCase("newtShop") && Bukkit.getPluginManager().isPluginEnabled("newtShop"))
            pricesProvider = new PricesProvider_NewtShop();
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
            Plugin factionsPlugin = Bukkit.getPluginManager().getPlugin("Factions");
            if(factionsPlugin.getDescription().getAuthors().contains("Daniel Saukel"))
                blocksProviders.add(new BlocksProvider_FactionsOne());
            else if(factionsPlugin.getDescription().getAuthors().contains("drtshock"))
                blocksProviders.add(new BlocksProvider_FactionsUUID());
            else
                blocksProviders.add(new BlocksProvider_MassiveFactions());
        }
        if(Bukkit.getPluginManager().isPluginEnabled("FactionsX"))
            blocksProviders.add(new BlocksProvider_FactionsX());
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
        if(Bukkit.getPluginManager().isPluginEnabled("PlotSquared")) {
            try {
                Class.forName("com.intellectualcrafters.plot.api.PlotAPI");
                blocksProviders.add(new BlocksProvider_PlotSquaredLegacy());
            }catch(ClassNotFoundException ex){
                blocksProviders.add(new BlocksProvider_PlotSquared());
            }
        }
        if(Bukkit.getPluginManager().isPluginEnabled("Residence"))
            blocksProviders.add(new BlocksProvider_Residence());
        if(Bukkit.getPluginManager().isPluginEnabled("Shop") && Bukkit.getPluginManager().getPlugin("Shop")
                .getDescription().getAuthors().stream().anyMatch(line -> line.contains("SnowGears")))
            blocksProviders.add(new BlocksProvider_SnowGearsShops());
        //Drops Plugin hook
        if(Bukkit.getPluginManager().isPluginEnabled("VoidChest"))
            dropsProviders.add(new DropsProvider_VoidChest());
        if(Bukkit.getPluginManager().isPluginEnabled("ChunkHoppers")) {
            dropsProviders.add(new DropsProvider_ChunkHoppers());
            blocksProviders.add(new BlocksProvider_ChunkHoppers());
        }
        //Drops for spawners
        if(Bukkit.getPluginManager().isPluginEnabled("WildStacker"))
            dropsProviders.add(new DropsProvider_WildStacker());
        else if(Bukkit.getPluginManager().isPluginEnabled("SilkSpawners"))
            dropsProviders.add(new DropsProvider_SilkSpawners());
        else
            dropsProviders.add(new DropsProviders_WildToolsSpawners());
        //Containers
        if(Bukkit.getPluginManager().isPluginEnabled("ChunkCollectors")){
            containerProviders.add(new ContainerProvider_ChunkCollectors());
        }
        if(Bukkit.getPluginManager().isPluginEnabled("WildChests")){
            containerProviders.add(new ContainerProvider_WildChests(plugin));
        }
        containerProviders.add(new ContainerProvider_Default(plugin));
        //Block Actions
        if(Bukkit.getPluginManager().isPluginEnabled("CoreProtect"))
            blockActionProviders.add(new BlockActionProvider_CoreProtect());
        if(Bukkit.getPluginManager().isPluginEnabled("Jobs"))
            blockActionProviders.add(new BlockActionProvider_Jobs());
        if(Bukkit.getPluginManager().isPluginEnabled("mcMMO"))
            blockActionProviders.add(new BlockActionProvider_mcMMO());
        if(Bukkit.getPluginManager().isPluginEnabled("WildStacker"))
            blockActionProviders.add(new BlockActionProvider_WildStacker());
    }

    public static void reload(){
        plugin.getProviders().loadData();
    }

}
