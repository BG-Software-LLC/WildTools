package com.bgsoftware.wildtools;

import com.bgsoftware.wildtools.api.WildTools;
import com.bgsoftware.wildtools.api.WildToolsAPI;
import com.bgsoftware.wildtools.command.CommandsHandler;
import com.bgsoftware.wildtools.handlers.DataHandler;
import com.bgsoftware.wildtools.handlers.EditorHandler;
import com.bgsoftware.wildtools.handlers.EventsHandler;
import com.bgsoftware.wildtools.handlers.ProvidersHandler;
import com.bgsoftware.wildtools.handlers.RecipesHandler;
import com.bgsoftware.wildtools.handlers.ToolsHandler;
import com.bgsoftware.wildtools.listeners.AnvilListener;
import com.bgsoftware.wildtools.listeners.BlocksListener;
import com.bgsoftware.wildtools.listeners.EditorListener;
import com.bgsoftware.wildtools.listeners.PlayerListener;
import com.bgsoftware.wildtools.metrics.Metrics;
import com.bgsoftware.wildtools.nms.NMSAdapter;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;

public final class WildToolsPlugin extends JavaPlugin implements WildTools {

    private static WildToolsPlugin plugin;

    private ToolsHandler toolsManager;
    private ProvidersHandler providersHandler;
    private EditorHandler editorHandler;
    private RecipesHandler recipesHandler;
    private EventsHandler eventsHandler;

    private Enchantment glowEnchant;

    private NMSAdapter nmsAdapter;

    @Override
    public void onEnable() {
        plugin = this;
        new Metrics(this);

        log("******** ENABLE START ********");

        try {
            Class.forName("org.bukkit.event.inventory.PrepareAnvilEvent");
            getServer().getPluginManager().registerEvents(new AnvilListener(this), this);
        } catch (Exception ignored) {
        }
        getServer().getPluginManager().registerEvents(new BlocksListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new EditorListener(this), this);

        CommandsHandler commandsHandler = new CommandsHandler(this);
        getCommand("tools").setExecutor(commandsHandler);
        getCommand("tools").setTabCompleter(commandsHandler);

        loadNMSAdapter();
        registerGlowEnchantment();

        providersHandler = new ProvidersHandler(this);
        toolsManager = new ToolsHandler(this);
        eventsHandler = new EventsHandler();

        DataHandler.loadData();
        Locale.reload();
        loadAPI();

        editorHandler = new EditorHandler(this);
        recipesHandler = new RecipesHandler(this);

        if (Updater.isOutdated()) {
            log("");
            log("A new version is available (v" + Updater.getLatestVersion() + ")!");
            log("Version's description: \"" + Updater.getVersionDescription() + "\"");
            log("");
        }

        log("******** ENABLE DONE ********");
    }

    @Override
    public void onDisable() {
        for (Player player : nmsAdapter.getOnlinePlayers()) {
            while (player.getOpenInventory().getType() == InventoryType.CHEST)
                player.closeInventory();
        }
        SellWandLogger.close();
    }

    private void loadNMSAdapter() {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            nmsAdapter = (NMSAdapter) Class.forName("com.bgsoftware.wildtools.nms.NMSAdapter_" + version).newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            getLogger().info("Error while loading adapter - unknown adapter " + version + "... Please contact @Ome_R");
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
            ex.printStackTrace();
        }
    }

    private void registerGlowEnchantment() {
        glowEnchant = nmsAdapter.getGlowEnchant();

        try {
            Field field = Enchantment.class.getDeclaredField("acceptingNew");
            field.setAccessible(true);
            field.set(null, true);
            field.setAccessible(false);
        } catch (Exception ignored) {
        }

        try {
            Enchantment.registerEnchantment(glowEnchant);
        } catch (Exception ignored) {
        }
    }

    public Enchantment getGlowEnchant() {
        return glowEnchant;
    }

    @Override
    public ToolsHandler getToolsManager() {
        return toolsManager;
    }

    @Override
    public ProvidersHandler getProviders() {
        return providersHandler;
    }

    public EventsHandler getEvents() {
        return eventsHandler;
    }

    public EditorHandler getEditor() {
        return editorHandler;
    }

    public RecipesHandler getRecipes() {
        return recipesHandler;
    }

    public NMSAdapter getNMSAdapter() {
        return nmsAdapter;
    }

    public static void log(String message) {
        plugin.getLogger().info(message);
    }

    public static WildToolsPlugin getPlugin() {
        return plugin;
    }

}
