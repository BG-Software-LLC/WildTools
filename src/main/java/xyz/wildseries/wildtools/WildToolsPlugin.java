package xyz.wildseries.wildtools;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import xyz.wildseries.wildtools.api.WildTools;
import xyz.wildseries.wildtools.api.WildToolsAPI;
import xyz.wildseries.wildtools.command.CommandsHandler;
import xyz.wildseries.wildtools.handlers.EditorHandler;
import xyz.wildseries.wildtools.handlers.ProvidersHandler;
import xyz.wildseries.wildtools.handlers.DataHandler;
import xyz.wildseries.wildtools.handlers.RecipesHandler;
import xyz.wildseries.wildtools.handlers.ToolsHandler;
import xyz.wildseries.wildtools.listeners.BlocksListener;
import xyz.wildseries.wildtools.listeners.EditorListener;
import xyz.wildseries.wildtools.listeners.McMMOListener;
import xyz.wildseries.wildtools.listeners.PlayerListener;
import xyz.wildseries.wildtools.listeners.RecipesListener;
import xyz.wildseries.wildtools.nms.NMSAdapter;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;

public final class WildToolsPlugin extends JavaPlugin implements WildTools {

    private static WildToolsPlugin plugin;

    private ToolsHandler toolsManager;
    private ProvidersHandler providersHandler;
    private EditorHandler editorHandler;
    private RecipesHandler recipesHandler;
    private Enchantment glowEnchant;

    private NMSAdapter nmsAdapter;

    @Override
    public void onEnable() {
        plugin = this;
        log("******** ENABLE START ********");

        getServer().getPluginManager().registerEvents(new BlocksListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new EditorListener(this), this);
        getServer().getPluginManager().registerEvents(new RecipesListener(this), this);
        if(getServer().getPluginManager().isPluginEnabled("mcMMO"))
            getServer().getPluginManager().registerEvents(new McMMOListener(this), this);

        CommandsHandler commandsHandler = new CommandsHandler(this);
        getCommand("tools").setExecutor(commandsHandler);
        getCommand("tools").setTabCompleter(commandsHandler);

        loadNMSAdapter();
        registerGlowEnchantment();

        toolsManager = new ToolsHandler(this);

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

        Bukkit.getScheduler().runTask(plugin, this::loadProviders);
    }

    @Override
    public void onDisable() {
        for(Player player : nmsAdapter.getOnlinePlayers()) {
            while(player.getOpenInventory().getType() == InventoryType.CHEST)
                player.closeInventory();
        }
    }

    private void loadNMSAdapter(){
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            nmsAdapter = (NMSAdapter) Class.forName("xyz.wildseries.wildtools.nms.NMSAdapter_" + version).newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e){
            getLogger().info("Error while loading adapter - unknown adapter " + version + "... Please contact @Ome_R");
        }
    }

    private void loadProviders(){
        log("Loading providers started...");
        long startTime = System.currentTimeMillis();
        log(" - Using " + nmsAdapter.getVersion() + " adapter.");
        providersHandler = new ProvidersHandler();
        log("Loading providers done (Took " + (System.currentTimeMillis() - startTime) + "ms)");

        if(!isVaultEnabled()) {
            log("");
            log("If you want sell-wands to be enabled, please install Vault with an economy plugin.");
            log("");
        }
    }

    private boolean isVaultEnabled() {
        if (getServer().getPluginManager().getPlugin("Vault") == null)
            return false;

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null || rsp.getProvider() == null)
            return false;

        providersHandler.enableVault();

        return true;
    }

    private void loadAPI(){
        try{
            Field instance = WildToolsAPI.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, this);
        }catch(Exception ex){
            log("Failed to set-up API - disabling plugin...");
            setEnabled(false);
            ex.printStackTrace();
        }
    }

    private void registerGlowEnchantment(){
        glowEnchant = nmsAdapter.getGlowEnchant();

        try{
            Field field = Enchantment.class.getDeclaredField("acceptingNew");
            field.setAccessible(true);
            field.set(null, true);
            field.setAccessible(false);
        }catch(Exception ignored){}

        try{
            Enchantment.registerEnchantment(glowEnchant);
        }catch(Exception ignored){}
    }

    public Enchantment getGlowEnchant() {
        return glowEnchant;
    }

    @Override
    public ToolsHandler getToolsManager() {
        return toolsManager;
    }

    public ProvidersHandler getProviders() {
        return providersHandler;
    }

    public EditorHandler getEditor() {
        return editorHandler;
    }

    public RecipesHandler getRecipes() {
        return recipesHandler;
    }

    public NMSAdapter getNMSAdapter(){
        return nmsAdapter;
    }

    public static void log(String message){
        plugin.getLogger().info(message);
    }

    public static WildToolsPlugin getPlugin(){
        return plugin;
    }

}
