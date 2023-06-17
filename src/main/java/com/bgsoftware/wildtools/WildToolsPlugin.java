package com.bgsoftware.wildtools;

import com.bgsoftware.common.reflection.ReflectMethod;
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
import com.bgsoftware.wildtools.nms.NMSWorld;
import com.bgsoftware.wildtools.utils.Pair;
import com.bgsoftware.wildtools.utils.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.UnsafeValues;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class WildToolsPlugin extends JavaPlugin implements WildTools {

    private static WildToolsPlugin plugin;

    private ToolsHandler toolsManager;
    private ProvidersHandler providersHandler;
    private EditorHandler editorHandler;
    private RecipesHandler recipesHandler;
    private EventsHandler eventsHandler;

    private Enchantment glowEnchant;

    private NMSAdapter nmsAdapter;
    private NMSWorld nmsWorld;

    private boolean shouldEnable = true;

    @Override
    public void onLoad() {
        plugin = this;
        shouldEnable = loadNMSAdapter();
    }

    @Override
    public void onEnable() {
        if (!shouldEnable) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

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
        if (!shouldEnable)
            return;

        for (Player player : nmsAdapter.getOnlinePlayers()) {
            while (player.getOpenInventory().getType() == InventoryType.CHEST)
                player.closeInventory();
        }
        SellWandLogger.close();
    }

    private boolean loadNMSAdapter() {
        String version = null;

        if (ServerVersion.isLessThan(ServerVersion.v1_17)) {
            version = getServer().getClass().getPackage().getName().split("\\.")[3];
        } else {
            ReflectMethod<Integer> getDataVersion = new ReflectMethod<>(UnsafeValues.class, "getDataVersion");
            int dataVersion = getDataVersion.invoke(Bukkit.getUnsafe());

            List<Pair<Integer, String>> versions = Arrays.asList(
                    new Pair<>(2729, null),
                    new Pair<>(2730, "v117"),
                    new Pair<>(2865, "v1181"),
                    new Pair<>(2975, "v1182"),
                    new Pair<>(3105, "v119"),
                    new Pair<>(3117, "v1191"),
                    new Pair<>(3120, "v1192"),
                    new Pair<>(3218, "v1193"),
                    new Pair<>(3337, "v1194"),
                    new Pair<>(3465, "v1201")
            );

            for (Pair<Integer, String> versionData : versions) {
                if (dataVersion <= versionData.getX()) {
                    version = versionData.getY();
                    break;
                }
            }

            if (version == null) {
                log("Data version: " + dataVersion);
            }
        }

        if (version != null) {
            try {
                nmsAdapter = (NMSAdapter) Class.forName(String.format("com.bgsoftware.wildtools.nms.%s.NMSAdapter", version)).newInstance();
                nmsWorld = (NMSWorld) Class.forName(String.format("com.bgsoftware.wildtools.nms.%s.NMSWorld", version)).newInstance();
                return true;
            } catch (Exception error) {
                error.printStackTrace();
            }
        }

        log("&cThe plugin doesn't support your minecraft version.");
        log("&cPlease try a different version.");

        return false;
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

    public NMSWorld getNMSWorld() {
        return nmsWorld;
    }

    public static void log(String message) {
        plugin.getLogger().info(message);
    }

    public static WildToolsPlugin getPlugin() {
        return plugin;
    }

}
