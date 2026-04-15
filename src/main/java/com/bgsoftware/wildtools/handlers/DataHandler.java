package com.bgsoftware.wildtools.handlers;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.wildtools.SellWandLogger;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.handlers.config.YamlToolSectionView;
import com.bgsoftware.wildtools.scheduler.Scheduler;
import com.bgsoftware.wildtools.hooks.PricesProvider_Default;

import org.bukkit.Material;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DataHandler {

    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    public static void loadData() {
        WildToolsPlugin.log("Loading configuration started...");
        long startTime = System.currentTimeMillis();
        int toolsAmount = 0;
        File file = new File(plugin.getDataFolder(), "config.yml");

        if (!file.exists()) {
            try {
                Material.valueOf("GOLD_HOE");
                plugin.saveResource("config.yml", false);
            } catch (Exception ex) {
                plugin.saveResource("config1_13.yml", false);
                File config113 = new File(plugin.getDataFolder(), "config1_13.yml");
                //noinspection ResultOfMethodCallIgnored
                config113.renameTo(file);
            }
        }

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);
        if (cfg.hasFailed()) return;

        try {
            cfg.syncWithConfig(file, plugin.getResource("config.yml"), "tools");
        } catch (IOException error) {
            error.printStackTrace();
            return;
        }

        ProvidersHandler.pricesPlugin = cfg.getString("prices-plugin", "ShopGUIPlus");

        Scheduler.runTask(() -> {
            plugin.getEvents().loadClaimingPlugins(cfg.getStringList("events-manipulations.claiming-plugins"));
            plugin.getEvents().loadNotifiedPlugins(cfg.getStringList("events-manipulations.other-plugins"));
        }, 20L);

        SellWandLogger.setLogsFile(cfg.getString("logs-file", "logs.txt"));

        Map<String, Double> prices = new HashMap<>();

        if (cfg.contains("prices-list")) {
            for (String line : cfg.getStringList("prices-list")) {
                String[] split = line.split(":");
                try {
                    if (split.length == 2) {
                        prices.put(split[0], Double.valueOf(split[1]));
                    } else if (split.length == 3) {
                        prices.put(split[0] + ":" + split[1], Double.valueOf(split[2]));
                    }
                } catch (IllegalArgumentException ignored) {}
            }
        }

        PricesProvider_Default.prices = prices;

        for (String name : cfg.getConfigurationSection("tools").getKeys(false)) {
            String base = "tools." + name;
            String kindId = cfg.getString(base + ".tool-mode", "").toUpperCase();
            Material type;

            try {
                type = Material.valueOf(cfg.getString(base + ".type"));
            } catch (IllegalArgumentException e) {
                WildToolsPlugin.log("Couldn't find a valid type for tool " + name + ", skipping");
                continue;
            }

            if (plugin.getToolsManager().getKind(kindId) == null) {
                WildToolsPlugin.log("Unknown tool-mode " + kindId + " for " + name + ", skipping");
                continue;
            }

            plugin.getToolsManager().registerTool(kindId, type, name, new YamlToolSectionView(cfg, base));

            toolsAmount++;
        }

        WildToolsPlugin.log(" - Found " + toolsAmount + " tools in config.yml.");
        WildToolsPlugin.log("Loading configuration done (Took " + (System.currentTimeMillis() - startTime) + "ms)");
    }

    public static void reload() {
        loadData();
    }

}
