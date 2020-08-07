package com.bgsoftware.wildtools.handlers;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.handlers.ProvidersManager;
import com.bgsoftware.wildtools.hooks.AdvancedEnchantmentsHook;
import com.bgsoftware.wildtools.api.hooks.ContainerProvider;
import com.bgsoftware.wildtools.hooks.ContainerProvider_ChunkCollectors;
import com.bgsoftware.wildtools.hooks.ContainerProvider_Default;
import com.bgsoftware.wildtools.hooks.ContainerProvider_WildChests;
import com.bgsoftware.wildtools.hooks.DropsProvider;
import com.bgsoftware.wildtools.hooks.DropsProvider_ChunkHoppers;
import com.bgsoftware.wildtools.hooks.DropsProvider_MergedSpawner;
import com.bgsoftware.wildtools.hooks.DropsProvider_SilkSpawners;
import com.bgsoftware.wildtools.hooks.DropsProvider_SuperLuckyBlock;
import com.bgsoftware.wildtools.hooks.DropsProvider_VoidChest;
import com.bgsoftware.wildtools.hooks.DropsProvider_WildStacker;
import com.bgsoftware.wildtools.hooks.DropsProvider_mcMMO;
import com.bgsoftware.wildtools.hooks.DropsProviders_WildToolsSpawners;
import com.bgsoftware.wildtools.hooks.PricesProvider_CMI;
import com.bgsoftware.wildtools.hooks.FactionsProvider;
import com.bgsoftware.wildtools.hooks.FactionsProvider_Default;
import com.bgsoftware.wildtools.hooks.PricesProvider;
import com.bgsoftware.wildtools.hooks.PricesProvider_Default;
import com.bgsoftware.wildtools.hooks.PricesProvider_Essentials;
import com.bgsoftware.wildtools.hooks.PricesProvider_GUIShop;
import com.bgsoftware.wildtools.hooks.PricesProvider_NewtShop;
import com.bgsoftware.wildtools.hooks.PricesProvider_QuantumShop;
import com.bgsoftware.wildtools.hooks.PricesProvider_ShopGUIPlus;

import com.bgsoftware.wildtools.api.hooks.SellInfo;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.google.common.collect.Lists;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class ProvidersHandler implements ProvidersManager {

    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();
    private static final SellInfo EMPTY_INFO = new SellInfo(new HashMap<>(), 0.0);

    static String pricesPlugin;
    static boolean mcmmoHook = false, jobsHook = false, advancedEnchantmentsHook = false;

    private boolean isVaultEnabled = false;
    private Economy economy;

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
        if(plugin.getToolsManager().getTool(itemStack) != null)
            return -1;

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

    public List<ItemStack> getBlockDrops(Player player, Block block, boolean onlySpawner){
        List<ItemStack> drops = new ArrayList<>();
        dropsProviders.stream().filter(dropsProvider -> onlySpawner == dropsProvider.isSpawnersOnly())
                .forEach(dropsProvider -> drops.addAll(dropsProvider.getBlockDrops(player, block)));
        return drops;
    }

    public boolean isContainer(BlockState blockState){
        for(ContainerProvider containerProvider : containerProviders){
            if(containerProvider.isContainer(blockState))
                return true;
        }

        return false;
    }

    public SellInfo sellContainer(BlockState blockState, Inventory inventory, Player player){
        for(ContainerProvider containerProvider : containerProviders){
            if(containerProvider.isContainer(blockState))
                return containerProvider.sellContainer(blockState, inventory, player);
        }

        return EMPTY_INFO;
    }

    public void removeContainer(BlockState blockState, Inventory inventory, SellInfo sellInfo){
        for(ContainerProvider containerProvider : containerProviders){
            if(containerProvider.isContainer(blockState)) {
                containerProvider.removeContainer(blockState, inventory, sellInfo);
                break;
            }
        }
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
        else if(pricesPlugin.equalsIgnoreCase("Essentials") && Bukkit.getPluginManager().isPluginEnabled("Essentials")){
            try{
                pricesProvider = new PricesProvider_Essentials();
            }catch(Throwable ex){
                pricesProvider = (PricesProvider) getInstance("com.bgsoftware.wildtools.hooks.PricesProvider_EssentialsOld");
            }
        }
        else if(pricesPlugin.equals("CMI") && Bukkit.getPluginManager().isPluginEnabled("CMI"))
            pricesProvider = new PricesProvider_CMI();
        else if(pricesPlugin.equalsIgnoreCase("newtShop") && Bukkit.getPluginManager().isPluginEnabled("newtShop"))
            pricesProvider = new PricesProvider_NewtShop();
        else if(pricesPlugin.equalsIgnoreCase("QuantumShop") && Bukkit.getPluginManager().isPluginEnabled("QuantumShop"))
            pricesProvider = new PricesProvider_QuantumShop();
        else pricesProvider = new PricesProvider_Default();
        //Factions Hookup
        if(Bukkit.getPluginManager().isPluginEnabled("Factions") &&
                Bukkit.getPluginManager().getPlugin("Factions").getDescription().getAuthors().contains("ProSavage"))
            factionsProvider = (FactionsProvider) getInstance("com.bgsoftware.wildtools.hooks.FactionsProvider_SavageFactions");
        else factionsProvider = new FactionsProvider_Default();
        //Claim Hookup
//        if(Bukkit.getPluginManager().isPluginEnabled("AcidIsland"))
//            blocksProviders.add(new BlocksProvider_AcidIsland());
//        if(Bukkit.getPluginManager().isPluginEnabled("ASkyBlock"))
//            blocksProviders.add(new BlocksProvider_ASkyblock());
//        if(Bukkit.getPluginManager().isPluginEnabled("BentoBox"))
//            blocksProviders.add(new BlocksProvider_BentoBox());
//        if(Bukkit.getPluginManager().isPluginEnabled("FabledSkyBlock"))
//            blocksProviders.add(new BlocksProvider_FabledSkyblock());
//        if(Bukkit.getPluginManager().isPluginEnabled("Factions")){
//            Plugin factionsPlugin = Bukkit.getPluginManager().getPlugin("Factions");
//            if(factionsPlugin.getDescription().getAuthors().contains("Daniel Saukel"))
//                blocksProviders.add((BlocksProvider) getInstance("com.bgsoftware.wildtools.hooks.BlocksProvider_FactionsOne"));
//            else if(factionsPlugin.getDescription().getAuthors().contains("DroppingAnvil"))
//                blocksProviders.add((BlocksProvider) getInstance("com.bgsoftware.wildtools.hooks.BlocksProvider_SaberFactions"));
//            else if(factionsPlugin.getDescription().getAuthors().contains("drtshock"))
//                blocksProviders.add((BlocksProvider) getInstance("com.bgsoftware.wildtools.hooks.BlocksProvider_FactionsUUID"));
//            else
//                blocksProviders.add(new BlocksProvider_MassiveFactions());
//        }
//        if(Bukkit.getPluginManager().isPluginEnabled("FactionsX"))
//            blocksProviders.add(new BlocksProvider_FactionsX());
//        if(Bukkit.getPluginManager().isPluginEnabled("GriefPrevention"))
//            blocksProviders.add(new BlocksProvider_GriefPrevention());
//        if(Bukkit.getPluginManager().isPluginEnabled("SuperiorSkyblock2"))
//            blocksProviders.add(new BlocksProvider_SuperiorSkyblock());
//        if(Bukkit.getPluginManager().isPluginEnabled("Towny"))
//            blocksProviders.add(new BlocksProvider_Towny());
//        if(Bukkit.getPluginManager().isPluginEnabled("Villages"))
//            blocksProviders.add(new BlocksProvider_Villages());
//        if(Bukkit.getPluginManager().isPluginEnabled("WorldGuard")){
//            try{
//                Class.forName("com.sk89q.worldguard.internal.platform.WorldGuardPlatform");
//                blocksProviders.add(new BlocksProvider_WorldGuard());
//            }catch (Throwable ex){
//                blocksProviders.add((BlocksProvider) getInstance("com.bgsoftware.wildtools.hooks.BlocksProvider_WorldGuardOld"));
//            }
//        }
//        if(Bukkit.getPluginManager().isPluginEnabled("Lands"))
//            blocksProviders.add(new BlocksProvider_Lands());
//        if(Bukkit.getPluginManager().isPluginEnabled("PlotSquared")) {
//            try {
//                Class.forName("com.intellectualcrafters.plot.api.PlotAPI");
//                blocksProviders.add(new BlocksProvider_PlotSquaredLegacy());
//            }catch(ClassNotFoundException ex){
//                try {
//                    Class.forName("com.github.intellectualsites.plotsquared.api.PlotAPI");
//                    blocksProviders.add(new BlocksProvider_PlotSquared4());
//                }catch (Exception ex2){
//                    blocksProviders.add(new BlocksProvider_PlotSquared5());
//                }
//            }
//        }
//        if(Bukkit.getPluginManager().isPluginEnabled("Residence"))
//            blocksProviders.add(new BlocksProvider_Residence());
//        if(Bukkit.getPluginManager().isPluginEnabled("Shop") && Bukkit.getPluginManager().getPlugin("Shop")
//                .getDescription().getAuthors().stream().anyMatch(line -> line.contains("SnowGears")))
//            blocksProviders.add(new BlocksProvider_SnowGearsShops());
//        if(Bukkit.getPluginManager().isPluginEnabled("QuickShop")){
//            try{
//                QuickShop.getInstance();
//                blocksProviders.add(new BlocksProvider_QuickShop());
//            }catch (Throwable ex){
//                blocksProviders.add((BlocksProvider) getInstance("com.bgsoftware.wildtools.hooks.BlocksProvider_QuickShopOld"));
//            }
//        }
        if(Bukkit.getPluginManager().isPluginEnabled("ChunkHoppers")) {
            dropsProviders.add(new DropsProvider_ChunkHoppers());
            //blocksProviders.add(new BlocksProvider_ChunkHoppers());
        }
        if(Bukkit.getPluginManager().isPluginEnabled("mcMMO")){
            try{
                PrimarySkillType.valueOf("HERBALISM");

                dropsProviders.add(new DropsProvider_mcMMO());
            }catch(Throwable ex){
                dropsProviders.add((DropsProvider) getInstance("com.bgsoftware.wildtools.hooks.DropsProvider_mcMMOOld"));
            }
        }
//        if(Bukkit.getPluginManager().isPluginEnabled("LockettePro"))
//            blocksProviders.add(new BlocksProvider_LockettePro());
//        if(Bukkit.getPluginManager().isPluginEnabled("IslandWorld"))
//            blocksProviders.add(new BlocksProvider_IslandWorld());
        if(Bukkit.getPluginManager().isPluginEnabled("VoidChest"))
            dropsProviders.add(new DropsProvider_VoidChest());
        if(Bukkit.getPluginManager().isPluginEnabled("SuperLuckyBlock"))
            dropsProviders.add(new DropsProvider_SuperLuckyBlock());
        //Drops for spawners
        if(Bukkit.getPluginManager().isPluginEnabled("WildStacker"))
            dropsProviders.add(new DropsProvider_WildStacker());
        else if(Bukkit.getPluginManager().isPluginEnabled("SilkSpawners")){
            try{
                de.dustplanet.util.SilkUtil.class.getMethod("getCreatureName", String.class);
                dropsProviders.add(new DropsProvider_SilkSpawners());
            }catch(Throwable ex){
                dropsProviders.add((DropsProvider) getInstance("com.bgsoftware.wildtools.hooks.DropsProvider_SilkSpawnersOld"));
            }
        }
        else if(Bukkit.getPluginManager().isPluginEnabled("MergedSpawner"))
            dropsProviders.add(new DropsProvider_MergedSpawner());
        else
            dropsProviders.add(new DropsProviders_WildToolsSpawners());
        //Containers
        if(Bukkit.getPluginManager().isPluginEnabled("ChunkCollectors")){
            addContainerProvider(new ContainerProvider_ChunkCollectors());
        }
        if(Bukkit.getPluginManager().isPluginEnabled("WildChests")){
            addContainerProvider(new ContainerProvider_WildChests(plugin));
        }
        addContainerProvider(new ContainerProvider_Default(plugin));

        if(Bukkit.getPluginManager().isPluginEnabled("AdvancedEnchantment"))
            AdvancedEnchantmentsHook.register(plugin);
    }

    @Override
    public void addContainerProvider(ContainerProvider containerProvider) {
        containerProviders.add(containerProvider);
    }

    public boolean hasAdvancedEnchantmentsEnabled(){
        return advancedEnchantmentsHook;
    }

    public static void reload(){
        plugin.getProviders().loadData();
    }

    private static Object getInstance(String clazz){
        try{
            return Class.forName(clazz).newInstance();
        }catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

}
