package com.bgsoftware.wildtools.handlers;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.handlers.ProvidersManager;
import com.bgsoftware.wildtools.api.hooks.ClaimsProvider;
import com.bgsoftware.wildtools.api.hooks.ContainerProvider;
import com.bgsoftware.wildtools.api.hooks.DropsProvider;
import com.bgsoftware.wildtools.api.hooks.PricesProvider;
import com.bgsoftware.wildtools.api.hooks.SellInfo;
import com.bgsoftware.wildtools.hooks.ClaimsProvider_Residence;
import com.bgsoftware.wildtools.hooks.ClaimsProvider_Towny;
import com.bgsoftware.wildtools.hooks.ClaimsProvider_Villages;
import com.bgsoftware.wildtools.hooks.ContainerProvider_Default;
import com.bgsoftware.wildtools.hooks.ContainerProvider_WildChests;
import com.bgsoftware.wildtools.hooks.DropsProvider_MergedSpawner;
import com.bgsoftware.wildtools.hooks.DropsProvider_RoseStacker;
import com.bgsoftware.wildtools.hooks.DropsProvider_SilkSpawners;
import com.bgsoftware.wildtools.hooks.DropsProvider_WildStacker;
import com.bgsoftware.wildtools.hooks.DropsProvider_mcMMO;
import com.bgsoftware.wildtools.hooks.DropsProviders_WildToolsSpawners;
import com.bgsoftware.wildtools.hooks.FactionsProvider;
import com.bgsoftware.wildtools.hooks.FactionsProvider_Default;
import com.bgsoftware.wildtools.hooks.PricesProvider_Default;
import com.bgsoftware.wildtools.hooks.SuperMobCoinsHook;
import com.bgsoftware.wildtools.utils.Executor;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.google.common.collect.Lists;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public final class ProvidersHandler implements ProvidersManager {

    private static final SellInfo EMPTY_INFO = new SellInfo(new HashMap<>(), 0.0);

    static String pricesPlugin;

    private final WildToolsPlugin plugin;

    private boolean isVaultEnabled = false;
    private Economy economy;

    private final List<DropsProvider> dropsProviders = Lists.newArrayList();
    private final List<ContainerProvider> containerProviders = Lists.newArrayList();
    private final ContainerProvider defaultContainer;
    private final List<ClaimsProvider> claimsProviders = Lists.newArrayList();
    private PricesProvider pricesProvider;
    private FactionsProvider factionsProvider;

    public ProvidersHandler(WildToolsPlugin plugin) {
        this.plugin = plugin;
        this.defaultContainer = new ContainerProvider_Default(plugin);
        Executor.sync(this::loadProviders, 1L);
    }

    /*
     * Hooks' methods
     */

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

    public List<ItemStack> getBlockDrops(Player player, Block block, boolean onlySpawner) {
        List<ItemStack> drops = new ArrayList<>();
        dropsProviders.stream().filter(dropsProvider -> onlySpawner == dropsProvider.isSpawnersOnly())
                .forEach(dropsProvider -> drops.addAll(dropsProvider.getBlockDrops(player, block)));
        return drops;
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

    public boolean isInsideClaim(Player player, Location location) {
        return claimsProviders.stream().anyMatch(claimsProvider -> claimsProvider.isPlayerClaim(player, location));
    }

    /*
     * Handler' methods
     */

    public void enableVault() {
        isVaultEnabled = true;
        economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
    }

    public void depositPlayer(Player player, double price) {
        economy.depositPlayer(player, price);
    }

    public boolean canSellItem(Player player, ItemStack itemStack) {
        return isVaultEnabled && getPrice(player, itemStack) > 0;
    }

    public boolean isVaultEnabled() {
        return isVaultEnabled;
    }

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

    private void loadProviders() {
        WildToolsPlugin.log("Loading providers started...");
        long startTime = System.currentTimeMillis();

        WildToolsPlugin.log(" - Using " + plugin.getNMSAdapter().getVersion() + " adapter.");

        loadPricesProvider();
        loadFactionsProvider();
        loadDropsProviders();
        loadContainerProviders();
        loadClaimsProviders();

        WildToolsPlugin.log("Loading providers done (Took " + (System.currentTimeMillis() - startTime) + "ms)");

        if (Bukkit.getPluginManager().isPluginEnabled("SuperMobCoins"))
            SuperMobCoinsHook.register();

        if (!isVaultEnabled()) {
            WildToolsPlugin.log("");
            WildToolsPlugin.log("If you want sell-wands to be enabled, please install Vault with an economy plugin.");
            WildToolsPlugin.log("");
        }
    }

    private void loadPricesProvider() {
        if (!(pricesProvider instanceof PricesProvider_Default))
            return;

        Optional<PricesProvider> pricesProvider = Optional.empty();

        if (pricesPlugin.equalsIgnoreCase("ShopGUIPlus") && Bukkit.getPluginManager().isPluginEnabled("ShopGUIPlus")) {
            pricesProvider = createInstance("PricesProvider_ShopGUIPlus");
        } else if (pricesPlugin.equalsIgnoreCase("GUIShop") && Bukkit.getPluginManager().isPluginEnabled("GUIShop")) {
            pricesProvider = createInstance("PricesProvider_GUIShop");
        } else if (pricesPlugin.equalsIgnoreCase("Essentials") && Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
            Plugin essentials = Bukkit.getPluginManager().getPlugin("Essentials");
            if (essentials.getDescription().getVersion().startsWith("2.15")) {
                pricesProvider = createInstance("PricesProvider_Essentials215");
            } else {
                pricesProvider = createInstance("PricesProvider_Essentials216");
            }
        } else if (pricesPlugin.equals("CMI") && Bukkit.getPluginManager().isPluginEnabled("CMI")) {
            pricesProvider = createInstance("PricesProvider_CMI");
        } else if (pricesPlugin.equalsIgnoreCase("newtShop") && Bukkit.getPluginManager().isPluginEnabled("newtShop")) {
            //pricesProvider = new PricesProvider_NewtShop();
        } else if (pricesPlugin.equalsIgnoreCase("QuantumShop") && Bukkit.getPluginManager().isPluginEnabled("QuantumShop")) {
            //pricesProvider = new PricesProvider_QuantumShop();
        } else if (pricesPlugin.equalsIgnoreCase("EconomyShopGUI") && (Bukkit.getPluginManager().isPluginEnabled("EconomyShopGUI") ||
                Bukkit.getPluginManager().isPluginEnabled("EconomyShopGUI-Premium"))) {
            //pricesProvider = new PricesProvider_EconomyShopGUI();
        } else {
            pricesProvider = Optional.of(new PricesProvider_Default());
        }

        pricesProvider.ifPresent(this::setPricesProvider);
    }

    private void loadFactionsProvider() {
        Optional<FactionsProvider> factionsProvider = Optional.empty();

        if (Bukkit.getPluginManager().isPluginEnabled("Factions") &&
                Bukkit.getPluginManager().getPlugin("Factions").getDescription().getAuthors().contains("ProSavage")) {
            //factionsProvider = (FactionsProvider) getInstance("com.bgsoftware.wildtools.hooks.FactionsProvider_SavageFactions");
        } else if (Bukkit.getPluginManager().isPluginEnabled("FactionsX") &&
                containsClass("net.prosavage.factionsx.persist.TNTAddonData")) {
            factionsProvider = createInstance("FactionsProvider_FactionsX");
        } else {
            factionsProvider = Optional.of(new FactionsProvider_Default());
        }

        factionsProvider.ifPresent(this::setFactionsProvider);
    }

    private void setFactionsProvider(FactionsProvider factionsProvider) {
        this.factionsProvider = factionsProvider;
    }

    private void loadDropsProviders() {
        if (Bukkit.getPluginManager().isPluginEnabled("ChunkHoppers")) {
            Optional<DropsProvider> dropsProvider = createInstance("DropsProvider_ChunkHoppers");
            dropsProvider.ifPresent(this::addDropsProvider);
        }
        if (Bukkit.getPluginManager().isPluginEnabled("mcMMO")) {
            try {
                PrimarySkillType.valueOf("HERBALISM");
                addDropsProvider(new DropsProvider_mcMMO());
            } catch (Throwable ex) {
                addDropsProvider((DropsProvider) getInstance("com.bgsoftware.wildtools.hooks.DropsProvider_mcMMOOld"));
            }
        }

        // Spawners related drops
        if (Bukkit.getPluginManager().isPluginEnabled("WildStacker")) {
            addDropsProvider(new DropsProvider_WildStacker());
        } else if (Bukkit.getPluginManager().isPluginEnabled("SilkSpawners")) {
            try {
                de.dustplanet.util.SilkUtil.class.getMethod("getCreatureName", String.class);
                addDropsProvider(new DropsProvider_SilkSpawners());
            } catch (Throwable ex) {
                addDropsProvider((DropsProvider) getInstance("com.bgsoftware.wildtools.hooks.DropsProvider_SilkSpawnersOld"));
            }
        } else if (Bukkit.getPluginManager().isPluginEnabled("MergedSpawner")) {
            addDropsProvider(new DropsProvider_MergedSpawner());
        } else if (Bukkit.getPluginManager().isPluginEnabled("RoseStacker")) {
            addDropsProvider(new DropsProvider_RoseStacker());
        } else {
            addDropsProvider(new DropsProviders_WildToolsSpawners());
        }

    }

    private void loadContainerProviders() {
        if (Bukkit.getPluginManager().isPluginEnabled("ChunkCollectors")) {
            Optional<ContainerProvider> containerProvider = createInstance("ContainerProvider_ChunkCollectors");
            containerProvider.ifPresent(this::addContainerProvider);
        }
        if (Bukkit.getPluginManager().isPluginEnabled("WildChests")) {
            addContainerProvider(new ContainerProvider_WildChests(plugin));
        }
    }

    private void loadClaimsProviders() {
        if (Bukkit.getPluginManager().isPluginEnabled("Factions")) {
            Plugin factions = Bukkit.getPluginManager().getPlugin("Factions");
            if (factions.getDescription().getAuthors().contains("drtshock")) {
                if (factions.getDescription().getVersion().startsWith("1.6.9.5-U0.5")) {
                    Optional<ClaimsProvider> claimsProvider = createInstance("ClaimsProvider_FactionsUUID05");
                    claimsProvider.ifPresent(this::addClaimsProvider);
                } else {
                    Optional<ClaimsProvider> claimsProvider = createInstance("ClaimsProvider_FactionsUUID02");
                    claimsProvider.ifPresent(this::addClaimsProvider);
                }
            } else {
                Optional<ClaimsProvider> claimsProvider = createInstance("ClaimsProvider_MassiveFactions");
                claimsProvider.ifPresent(this::addClaimsProvider);
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
            addClaimsProvider(new ClaimsProvider_Residence());
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Towny")) {
            addClaimsProvider(new ClaimsProvider_Towny());
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Villages")) {
            addClaimsProvider(new ClaimsProvider_Villages());
        }
    }

    public static void reload() {
        WildToolsPlugin.getPlugin().getProviders().loadProviders();
    }

    private void registerHook(String className) {
        try {
            Class<?> clazz = Class.forName("com.bgsoftware.superiorskyblock.hooks.support." + className);
            Method registerMethod = clazz.getMethod("register", WildToolsPlugin.class);
            registerMethod.invoke(null, plugin);
        } catch (Exception ignored) {
        }
    }

    private <T> Optional<T> createInstance(String className) {
        try {
            Class<?> clazz = Class.forName("com.bgsoftware.superiorskyblock.hooks.provider." + className);
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
        } catch (Exception error) {
            error.printStackTrace();
            return Optional.empty();
        }
    }

    private static Object getInstance(String clazz) {
        try {
            return Class.forName(clazz).newInstance();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
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
