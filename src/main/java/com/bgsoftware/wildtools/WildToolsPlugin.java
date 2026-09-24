package com.bgsoftware.wildtools;

import com.bgsoftware.common.dependencies.DependenciesManager;
import com.bgsoftware.common.nmsloader.INMSLoader;
import com.bgsoftware.common.nmsloader.NMSHandlersFactory;
import com.bgsoftware.common.nmsloader.NMSLoadException;
import com.bgsoftware.common.nmsloader.config.NMSConfiguration;
import com.bgsoftware.common.updater.Updater;
import com.bgsoftware.wildtools.api.WildTools;
import com.bgsoftware.wildtools.api.WildToolsAPI;
import com.bgsoftware.wildtools.command.CommandsHandler;
import com.bgsoftware.wildtools.events.EventsSimulation;
import com.bgsoftware.wildtools.handlers.DataHandler;
import com.bgsoftware.wildtools.handlers.EditorHandler;
import com.bgsoftware.wildtools.handlers.EventsHandler;
import com.bgsoftware.wildtools.handlers.ProvidersHandler;
import com.bgsoftware.wildtools.handlers.RecipesHandler;
import com.bgsoftware.wildtools.handlers.ToolsHandler;
import com.bgsoftware.wildtools.listeners.AnvilListener;
import com.bgsoftware.wildtools.listeners.BlocksListener;
import com.bgsoftware.wildtools.listeners.CraftingListener;
import com.bgsoftware.wildtools.listeners.EditorListener;
import com.bgsoftware.wildtools.listeners.PlayerListener;
import com.bgsoftware.wildtools.nms.NMSAdapter;
import com.bgsoftware.wildtools.nms.NMSWorld;
import org.bstats.bukkit.Metrics;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;

public class WildToolsPlugin extends JavaPlugin implements WildTools {

    private final Updater updater = new Updater(this, "wildtools");

    private static WildToolsPlugin plugin;

    private ToolsHandler toolsManager;
    private ProvidersHandler providersHandler;
    private EditorHandler editorHandler;
    private RecipesHandler recipesHandler;
    private EventsHandler eventsHandler;

    private NMSAdapter nmsAdapter;
    private NMSWorld nmsWorld;

    private boolean shouldEnable = true;

    @Override
    public void onLoad() {
        plugin = this;

        DependenciesManager.inject(this);

        this.shouldEnable = loadNMSAdapter();

        EventsSimulation.init();
    }

    @Override
    public void onEnable() {
        if (!this.shouldEnable) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.nmsAdapter.loadLegacy();

        new Metrics(this, 4107);

        log("******** ENABLE START ********");

        try {
            Class.forName("org.bukkit.event.inventory.PrepareAnvilEvent");
            getServer().getPluginManager().registerEvents(new AnvilListener(this), this);
        } catch (Exception ignored) {
        }
        getServer().getPluginManager().registerEvents(new BlocksListener(this), this);
        getServer().getPluginManager().registerEvents(new CraftingListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new EditorListener(this), this);

        CommandsHandler commandsHandler = new CommandsHandler(this);
        getCommand("tools").setExecutor(commandsHandler);
        getCommand("tools").setTabCompleter(commandsHandler);

        this.providersHandler = new ProvidersHandler(this);
        this.toolsManager = new ToolsHandler(this);
        this.eventsHandler = new EventsHandler();

        DataHandler.loadData();
        Locale.reload();
        loadAPI();

        this.editorHandler = new EditorHandler(this);
        this.recipesHandler = new RecipesHandler(this);

        if (this.updater.isOutdated()) {
            log("");
            log("A new version is available (v" + this.updater.getLatestVersion() + ")!");
            log("Version's description: \"" + this.updater.getVersionDescription() + "\"");
            log("");
        }

        log("******** ENABLE DONE ********");
    }

    @Override
    public void onDisable() {
        if (!this.shouldEnable) {
            return;
        }

        for (Player player : this.nmsAdapter.getOnlinePlayers()) {
            while (player.getOpenInventory().getType() == InventoryType.CHEST) {
                player.closeInventory();
            }
        }

        SellWandLogger.close();
    }

    private boolean loadNMSAdapter() {
        try {
            INMSLoader nmsLoader = NMSHandlersFactory.createNMSLoader(this, NMSConfiguration.forPlugin(this));
            this.nmsAdapter = nmsLoader.loadNMSHandler(NMSAdapter.class);
            this.nmsWorld = nmsLoader.loadNMSHandler(NMSWorld.class);

            return true;
        } catch (NMSLoadException error) {
            log("The plugin doesn't support your minecraft version.");
            log("Please try a different version.");
            //noinspection all
            error.printStackTrace();

            return false;
        }
    }

    private void loadAPI() {
        try {
            Field instance = WildToolsAPI.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, this);
        } catch (Exception ex) {
            log("Failed to set-up API - disabling plugin...");
            setEnabled(false);
            //noinspection all
            ex.printStackTrace();
        }
    }

    @Override
    public ToolsHandler getToolsManager() {
        return this.toolsManager;
    }

    @Override
    public ProvidersHandler getProviders() {
        return this.providersHandler;
    }

    public EventsHandler getEvents() {
        return this.eventsHandler;
    }

    public EditorHandler getEditor() {
        return this.editorHandler;
    }

    public RecipesHandler getRecipes() {
        return this.recipesHandler;
    }

    public NMSAdapter getNMSAdapter() {
        return this.nmsAdapter;
    }

    public NMSWorld getNMSWorld() {
        return this.nmsWorld;
    }

    public Updater getUpdater() {
        return this.updater;
    }

    public static void log(String message) {
        plugin.getLogger().info(message);
    }

    public static WildToolsPlugin getPlugin() {
        return plugin;
    }

}
