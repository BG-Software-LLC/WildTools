package com.bgsoftware.wildtools.handlers;

import com.bgsoftware.common.shopsbridge.ShopsProvider;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.handlers.ProvidersManager;
import com.bgsoftware.wildtools.api.hooks.ClaimsProvider;
import com.bgsoftware.wildtools.api.hooks.ContainerProvider;
import com.bgsoftware.wildtools.api.hooks.DropsProvider;
import com.bgsoftware.wildtools.api.hooks.PricesProvider;
import com.bgsoftware.wildtools.api.hooks.SellInfo;
import com.bgsoftware.wildtools.hooks.ContainerProvider_Default;
import com.bgsoftware.wildtools.hooks.DropsProviders_Default;
import com.bgsoftware.wildtools.hooks.EconomyProvider;
import com.bgsoftware.wildtools.hooks.EconomyProvider_Default;
import com.bgsoftware.wildtools.hooks.ExtendedContainerProvider;
import com.bgsoftware.wildtools.hooks.FactionsProvider;
import com.bgsoftware.wildtools.hooks.FactionsProvider_Default;
import com.bgsoftware.wildtools.hooks.PricesProvider_Default;
import com.bgsoftware.wildtools.hooks.PricesProvider_ShopsBridgeWrapper;
import com.bgsoftware.wildtools.hooks.StackedItemProvider;
import com.bgsoftware.wildtools.hooks.StackedItemProvider_Default;
import com.bgsoftware.wildtools.hooks.listener.IToolBlockListener;
import com.bgsoftware.wildtools.scheduler.Scheduler;
import com.bgsoftware.wildtools.utils.math.Vector3;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class ProvidersHandler implements ProvidersManager {

    private static final SellInfo EMPTY_INFO = new SellInfo(new HashMap<>(), 0.0);

    static String pricesPlugin;

    private final WildToolsPlugin plugin;

    private final List<DropsProvider> dropsProviders = Lists.newArrayList();
    private final List<ContainerProvider> containerProviders = Lists.newArrayList();
    private final ExtendedContainerProvider defaultContainer;
    private final List<ClaimsProvider> claimsProviders = Lists.newArrayList();
    private PricesProvider pricesProvider;
    private FactionsProvider factionsProvider;
    private StackedItemProvider stackedItemProvider;
    private EconomyProvider economyProvider;

    private boolean checkPickupEventStatus = true;

    private final List<IToolBlockListener> toolBlockListeners = Lists.newArrayList();

    public ProvidersHandler(WildToolsPlugin plugin) {
        this.plugin = plugin;
        this.defaultContainer = new ContainerProvider_Default(plugin);
        Scheduler.runTask(this::loadProviders, 1L);
    }

    /*
     * Hooks' methods
     */

    public void startBulkSell() {
        if (this.pricesProvider instanceof PricesProvider_ShopsBridgeWrapper) {
            ((PricesProvider_ShopsBridgeWrapper) this.pricesProvider).startBulkTransaction();
        }
    }

    public void stopBulkSell() {
        if (this.pricesProvider instanceof PricesProvider_ShopsBridgeWrapper) {
            ((PricesProvider_ShopsBridgeWrapper) this.pricesProvider).stopBulkTransaction();
        }
    }

    public double getPrice(Player player, ItemStack itemStack) {
        if (plugin.getToolsManager().getTool(itemStack) != null)
            return -1;

        try {
            return pricesProvider.getPrice(player, itemStack);
        } catch (Exception ex) {
            return -1;
        }
    }

    public int getTNTAmountFromBank(Player player) {
        return factionsProvider.getTNTAmountFromBank(player);
    }

    public void takeTNTFromBank(Player player, int amount) {
        factionsProvider.takeTNTFromBank(player, amount);
    }

    public boolean getBlockDrops(List<ItemStack> drops, Player player, Block block, boolean onlySpawner) {
        boolean foundDropsProvider = false;

        for (DropsProvider dropsProvider : this.dropsProviders) {
            if (dropsProvider.isSpawnersOnly() == onlySpawner) {
                List<ItemStack> hookDrops = dropsProvider.getBlockDrops(player, block);
                if (hookDrops != null) {
                    drops.addAll(hookDrops);
                    foundDropsProvider = true;
                }
            }
        }

        return foundDropsProvider;
    }

    public boolean isContainer(BlockState blockState) {
        for (ContainerProvider containerProvider : containerProviders) {
            if (containerProvider.isContainer(blockState))
                return true;
        }

        return defaultContainer.isContainer(blockState);
    }

    public SellInfo sellContainer(BlockState blockState, Inventory inventory, Player player) {
        for (ContainerProvider containerProvider : containerProviders) {
            if (containerProvider.isContainer(blockState))
                return containerProvider.sellContainer(blockState, inventory, player);
        }

        if (defaultContainer.isContainer(blockState))
            return defaultContainer.sellContainer(blockState, inventory, player);

        return EMPTY_INFO;
    }

    public void removeContainer(BlockState blockState, Inventory inventory, SellInfo sellInfo) {
        for (ContainerProvider containerProvider : containerProviders) {
            if (containerProvider.isContainer(blockState)) {
                containerProvider.removeContainer(blockState, inventory, sellInfo);
                return;
            }
        }

        if (defaultContainer.isContainer(blockState))
            defaultContainer.removeContainer(blockState, inventory, sellInfo);
    }

    public List<Inventory> getAllInventories(BlockState blockState, Inventory chestInventory) {
        for (ContainerProvider containerProvider : containerProviders) {
            if (containerProvider instanceof ExtendedContainerProvider && containerProvider.isContainer(blockState)) {
                return ((ExtendedContainerProvider) containerProvider).getAllInventories(blockState, chestInventory);
            }
        }

        if (defaultContainer.isContainer(blockState))
            return defaultContainer.getAllInventories(blockState, chestInventory);

        return Collections.emptyList();
    }

    public void addItems(BlockState blockState, Inventory chestInventory, List<ItemStack> itemStackList) {
        for (ContainerProvider containerProvider : containerProviders) {
            if (containerProvider instanceof ExtendedContainerProvider && containerProvider.isContainer(blockState)) {
                ((ExtendedContainerProvider) containerProvider).addItems(blockState, chestInventory, itemStackList);
                return;
            }
        }

        if (defaultContainer.isContainer(blockState))
            defaultContainer.addItems(blockState, chestInventory, itemStackList);
    }

    public boolean isInsideClaim(Player player, Location location) {
        return claimsProviders.stream().anyMatch(claimsProvider -> claimsProvider.isPlayerClaim(player, location));
    }

    /*
     * Handler' methods
     */

    @Override
    public void addContainerProvider(ContainerProvider containerProvider) {
        containerProviders.add(containerProvider);
    }

    @Override
    public void addDropsProvider(DropsProvider dropsProvider) {
        dropsProviders.add(dropsProvider);
    }

    @Override
    public void setPricesProvider(PricesProvider pricesProvider) {
        this.pricesProvider = pricesProvider;
    }

    @Override
    public void addClaimsProvider(ClaimsProvider claimsProvider) {
        claimsProviders.add(claimsProvider);
    }

    public void registerToolBlockListener(IToolBlockListener toolBlockListener) {
        this.toolBlockListeners.add(toolBlockListener);
    }

    public void notifyToolBlockListeners(World world, Vector3 location, IToolBlockListener.Action action) {
        this.toolBlockListeners.forEach(toolBlockListener -> toolBlockListener.recordBlockChange(world, location, action));
    }

    public boolean hasEconomyProvider() {
        return !(economyProvider instanceof EconomyProvider_Default);
    }

    public StackedItemProvider getStackedItemProvider() {
        return stackedItemProvider;
    }

    public EconomyProvider getEconomyProvider() {
        return economyProvider;
    }

    private void loadProviders() {
        WildToolsPlugin.log("Loading providers started...");
        long startTime = System.currentTimeMillis();

        loadGeneralHooks();
        loadPricesProvider();
        loadFactionsProvider();
        loadDropsProviders();
        loadContainerProviders();
        loadClaimsProviders();
        loadStackedItemProviders();
        loadEconomyProvider();

        WildToolsPlugin.log("Loading providers done (Took " + (System.currentTimeMillis() - startTime) + "ms)");

        if (!hasEconomyProvider()) {
            WildToolsPlugin.log("");
            WildToolsPlugin.log("If you want sell-wands to be enabled, please install Vault with an economy plugin.");
            WildToolsPlugin.log("");
        }
    }

    private void loadGeneralHooks() {
        if (Bukkit.getPluginManager().isPluginEnabled("mcMMO")) {
            registerHook("McMMOHook");
        }
        if (Bukkit.getPluginManager().isPluginEnabled("SuperMobCoins")) {
            registerHook("SuperMobCoinsHook");
        }
    }

    private void loadPricesProvider() {
        if (pricesProvider != null && !(pricesProvider instanceof PricesProvider_Default))
            return;

        setPricesProvider((pricesPlugin.equalsIgnoreCase("AUTO") ?
                ShopsProvider.findAvailableProvider() : ShopsProvider.getShopsProvider(pricesPlugin))
                .flatMap(shopsProvider -> shopsProvider.createInstance(plugin).map(shopsBridge ->
                        (PricesProvider) new PricesProvider_ShopsBridgeWrapper(shopsProvider, shopsBridge)))
                .orElseGet(PricesProvider_Default::new));
    }

    private void loadFactionsProvider() {
        Optional<FactionsProvider> factionsProvider = Optional.empty();

        if (Bukkit.getPluginManager().isPluginEnabled("Factions") &&
                Bukkit.getPluginManager().getPlugin("Factions").getDescription().getAuthors().contains("ProSavage")) {
            factionsProvider = createInstance("FactionsProvider_SavageFactions");
        } else if (Bukkit.getPluginManager().isPluginEnabled("FactionsX") &&
                containsClass("net.prosavage.factionsx.persist.TNTAddonData")) {
            factionsProvider = createInstance("FactionsProvider_FactionsX");
        }

        this.factionsProvider = factionsProvider.orElse(new FactionsProvider_Default());
    }

    private void loadDropsProviders() {
        if (Bukkit.getPluginManager().isPluginEnabled("ChunkHoppers") && containsClass("dev.warse.chunkhoppers.ChunkHoppers")) {
            Optional<DropsProvider> dropsProvider = createInstance("DropsProvider_ChunkHoppers");
            dropsProvider.ifPresent(this::addDropsProvider);
        }
        if (Bukkit.getPluginManager().isPluginEnabled("mcMMO")) {
            Plugin mcMMO = Bukkit.getPluginManager().getPlugin("mcMMO");
            if (mcMMO.getDescription().getVersion().startsWith("1")) {
                Optional<DropsProvider> dropsProvider = createInstance("DropsProvider_mcMMO1");
                dropsProvider.ifPresent(this::addDropsProvider);
            } else {
                Optional<DropsProvider> dropsProvider = createInstance("DropsProvider_mcMMO2");
                dropsProvider.ifPresent(this::addDropsProvider);
            }
        }

        // Spawners related drops
        if (Bukkit.getPluginManager().isPluginEnabled("WildStacker")) {
            Optional<DropsProvider> dropsProvider = createInstance("DropsProvider_WildStacker");
            dropsProvider.ifPresent(this::addDropsProvider);
        } else if (Bukkit.getPluginManager().isPluginEnabled("SilkSpawners")) {
            Plugin silkSpawners = Bukkit.getPluginManager().getPlugin("SilkSpawners");
            if (silkSpawners.getDescription().getVersion().startsWith("5")) {
                Optional<DropsProvider> dropsProvider = createInstance("DropsProvider_SilkSpawners_Timbru5");
                dropsProvider.ifPresent(this::addDropsProvider);
            } else if (silkSpawners.getDescription().getVersion().startsWith("6") ||
                    silkSpawners.getDescription().getVersion().startsWith("7") ||
                    silkSpawners.getDescription().getVersion().startsWith("8")) {
                Optional<DropsProvider> dropsProvider = createInstance("DropsProvider_SilkSpawners_Timbru6");
                dropsProvider.ifPresent(this::addDropsProvider);
            }
        } else if (Bukkit.getPluginManager().isPluginEnabled("SilkSpawners_v2")) {
            Optional<DropsProvider> dropsProvider = createInstance("DropsProvider_SilkSpawners_CandC2");
            dropsProvider.ifPresent(this::addDropsProvider);
        } else if (Bukkit.getPluginManager().isPluginEnabled("MergedSpawner")) {
            Optional<DropsProvider> dropsProvider = createInstance("DropsProvider_MergedSpawner");
            dropsProvider.ifPresent(this::addDropsProvider);
        } else if (Bukkit.getPluginManager().isPluginEnabled("RoseStacker")) {
            Optional<DropsProvider> dropsProvider = createInstance("DropsProvider_RoseStacker");
            dropsProvider.ifPresent(this::addDropsProvider);
        } else {
            addDropsProvider(new DropsProviders_Default());
        }

    }

    private void loadContainerProviders() {
        if (Bukkit.getPluginManager().isPluginEnabled("ChunkCollectors")) {
            Optional<ContainerProvider> containerProvider = createInstance("ContainerProvider_ChunkCollectors");
            containerProvider.ifPresent(this::addContainerProvider);
        }
        if (Bukkit.getPluginManager().isPluginEnabled("WildChests")) {
            Optional<ContainerProvider> containerProvider = createInstance("ContainerProvider_WildChests");
            containerProvider.ifPresent(this::addContainerProvider);
        }
    }

    private void loadClaimsProviders() {
        if (Bukkit.getPluginManager().isPluginEnabled("Factions")) {
            Plugin factions = Bukkit.getPluginManager().getPlugin("Factions");
            if (factions.getDescription().getAuthors().contains("drtshock")) {
                Optional<ClaimsProvider> claimsProvider = createInstance("ClaimsProvider_FactionsUUID");
                claimsProvider.ifPresent(this::addClaimsProvider);
            } else {
                String website = factions.getDescription().getWebsite();
                if (website != null && website.contains("massivecraft")) {
                    Optional<ClaimsProvider> claimsProvider = createInstance("ClaimsProvider_MassiveFactions");
                    claimsProvider.ifPresent(this::addClaimsProvider);
                }
            }
        }
        if (Bukkit.getPluginManager().isPluginEnabled("FactionsX")) {
            Optional<ClaimsProvider> claimsProvider = createInstance("ClaimsProvider_FactionsX");
            claimsProvider.ifPresent(this::addClaimsProvider);
        }
        if (Bukkit.getPluginManager().isPluginEnabled("GriefPrevention")) {
            Optional<ClaimsProvider> claimsProvider = createInstance("ClaimsProvider_GriefPrevention");
            claimsProvider.ifPresent(this::addClaimsProvider);
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Lands")) {
            Optional<ClaimsProvider> claimsProvider = createInstance("ClaimsProvider_Lands");
            claimsProvider.ifPresent(this::addClaimsProvider);
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Residence")) {
            Optional<ClaimsProvider> claimsProvider = createInstance("ClaimsProvider_Residence");
            claimsProvider.ifPresent(this::addClaimsProvider);
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Towny")) {
            Optional<ClaimsProvider> claimsProvider = createInstance("ClaimsProvider_Towny");
            claimsProvider.ifPresent(this::addClaimsProvider);
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Villages")) {
            Optional<ClaimsProvider> claimsProvider = createInstance("ClaimsProvider_Villages");
            claimsProvider.ifPresent(this::addClaimsProvider);
        }
    }

    private void loadStackedItemProviders() {
        Optional<StackedItemProvider> stackedItemProvider = Optional.empty();

        if (Bukkit.getPluginManager().isPluginEnabled("WildStacker")) {
            stackedItemProvider = createInstance("StackedItemProvider_WildStacker");
        }

        this.stackedItemProvider = stackedItemProvider.orElse(new StackedItemProvider_Default());
    }

    private void loadEconomyProvider() {
        Optional<EconomyProvider> economyProvider = Optional.empty();

        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            economyProvider = createInstance("EconomyProvider_Vault");
        }

        this.economyProvider = economyProvider.orElse(new EconomyProvider_Default());
    }

    public static void reload() {
        WildToolsPlugin.getPlugin().getProviders().loadProviders();
    }

    private void registerHook(String className) {
        try {
            Class<?> clazz = Class.forName("com.bgsoftware.wildtools.hooks." + className);
            Method registerMethod = clazz.getMethod("register", WildToolsPlugin.class);
            registerMethod.invoke(null, plugin);
        } catch (Exception ignored) {
        }
    }

    private <T> Optional<T> createInstance(String className) {
        try {
            Class<?> clazz = Class.forName("com.bgsoftware.wildtools.hooks." + className);
            try {
                Method compatibleMethod = clazz.getDeclaredMethod("isCompatible");
                if (!(boolean) compatibleMethod.invoke(null))
                    return Optional.empty();
            } catch (Exception ignored) {
            }

            try {
                Constructor<?> constructor = clazz.getConstructor(WildToolsPlugin.class);
                // noinspection unchecked
                return Optional.of((T) constructor.newInstance(plugin));
            } catch (Exception error) {
                // noinspection unchecked
                return Optional.of((T) clazz.newInstance());
            }
        } catch (ClassNotFoundException ignored) {
            return Optional.empty();
        } catch (Throwable error) {
            error.printStackTrace();
            return Optional.empty();
        }
    }

    private static boolean containsClass(String path) {
        try {
            Class.forName(path);
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

}
