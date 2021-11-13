package com.bgsoftware.wildtools.handlers;

import com.bgsoftware.wildtools.SellWandLogger;
import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.tools.CrowbarTool;
import com.bgsoftware.wildtools.api.objects.tools.MagnetTool;
import com.bgsoftware.wildtools.config.CommentedConfiguration;
import com.bgsoftware.wildtools.api.objects.tools.BuilderTool;
import com.bgsoftware.wildtools.api.objects.tools.CannonTool;
import com.bgsoftware.wildtools.api.objects.tools.CraftingTool;
import com.bgsoftware.wildtools.api.objects.tools.CuboidTool;
import com.bgsoftware.wildtools.api.objects.tools.DrainTool;
import com.bgsoftware.wildtools.api.objects.tools.HarvesterTool;
import com.bgsoftware.wildtools.api.objects.tools.IceTool;
import com.bgsoftware.wildtools.api.objects.tools.LightningTool;
import com.bgsoftware.wildtools.api.objects.tools.PillarTool;
import com.bgsoftware.wildtools.api.objects.tools.SellTool;
import com.bgsoftware.wildtools.api.objects.tools.SortTool;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import com.bgsoftware.wildtools.hooks.PricesProvider_Default;

import com.bgsoftware.wildtools.utils.Executor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DataHandler {

    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    public static void loadData(){
        WildToolsPlugin.log("Loading configuration started...");
        long startTime = System.currentTimeMillis();
        int toolsAmount = 0;
        File file = new File(plugin.getDataFolder(), "config.yml");

        if(!file.exists()) {
            try{
                Material.valueOf("GOLD_HOE");
                plugin.saveResource("config.yml", false);
            }catch(Exception ex){
                plugin.saveResource("config1_13.yml", false);
                File config113 = new File(plugin.getDataFolder(), "config1_13.yml");
                //noinspection ResultOfMethodCallIgnored
                config113.renameTo(file);
            }
        }

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        if(cfg.hasFailed())
            return;

        cfg.syncWithConfig(file, plugin.getResource("config.yml"), "tools");

        ProvidersHandler.pricesPlugin = cfg.getString("prices-plugin", "ShopGUIPlus");

        Executor.sync(() -> {
            plugin.getEvents().loadClaimingPlugins(cfg.getStringList("events-manipulations.claiming-plugins"));
            plugin.getEvents().loadNotifiedPlugins(cfg.getStringList("events-manipulations.other-plugins"));
        }, 20L);

        SellWandLogger.setLogsFile(cfg.getString("logs-file", "logs.txt"));

        Map<String, Double> prices = new HashMap<>();

        if(cfg.contains("prices-list")){
            for(String line : cfg.getStringList("prices-list")){
                String[] split = line.split(":");
                try {
                    if (split.length == 2) {
                        prices.put(split[0], Double.valueOf(split[1]));
                    } else if (split.length == 3) {
                        prices.put(split[0] + ":" + split[1], Double.valueOf(split[2]));
                    }
                } catch(IllegalArgumentException ignored){}
            }
        }

        PricesProvider_Default.prices = prices;

        for(String name : cfg.getConfigurationSection("tools").getKeys(false)){
            Material type;

            try{
                type = Material.valueOf(cfg.getString("tools." + name + ".type"));
            } catch (IllegalArgumentException e){
                WildToolsPlugin.log("Couldn't find a valid type for tool " + name + ", skipping");
                continue;
            }

            Tool tool;

            switch(cfg.getString("tools." + name + ".tool-mode", "")){
                case "BUILDER":
                    if(!cfg.contains("tools." + name + ".length")){
                        WildToolsPlugin.log("Couldn't find a length for tool " + name + ", skipping");
                        continue;
                    }

                    tool = plugin.getToolsManager().registerTool(type, name, BuilderTool.class, cfg.getInt("tools." + name + ".length"));
                    break;
                case "CANNON":
                    if(!cfg.contains("tools." + name + ".tnt-amount")){
                        WildToolsPlugin.log("Couldn't find a tnt amount for tool " + name + ", skipping");
                        continue;
                    }

                    tool = plugin.getToolsManager().registerTool(type, name, CannonTool.class, cfg.getInt("tools." + name + ".tnt-amount"));
                    break;
                case "CRAFTING":
                    if(!cfg.contains("tools." + name + ".craftings")){
                        WildToolsPlugin.log("Couldn't find a craftings list for tool " + name + ", skipping");
                        continue;
                    }

                    tool = plugin.getToolsManager().registerTool(type, name, CraftingTool.class, cfg.getStringList("tools." + name + ".craftings"));
                    break;
                case "CROWBAR":
                    tool = plugin.getToolsManager().registerTool(type, name, CrowbarTool.class, cfg.getStringList("tools." + name + ".commands-on-use"));
                    break;
                case "CUBOID":
                    if(!cfg.contains("tools." + name + ".break-level")){
                        WildToolsPlugin.log("Couldn't find a break-level for tool " + name + ", skipping");
                        continue;
                    }

                    tool = plugin.getToolsManager().registerTool(type, name, CuboidTool.class, cfg.getInt("tools." + name + ".break-level"));
                    break;
                case "DRAIN":
                    if(!cfg.contains("tools." + name + ".radius")){
                        WildToolsPlugin.log("Couldn't find a radius for tool " + name + ", skipping");
                        continue;
                    }

                    tool = plugin.getToolsManager().registerTool(type, name, DrainTool.class, cfg.getInt("tools." + name + ".radius"));
                    break;
                case "HARVESTER":
                    if(!cfg.contains("tools." + name + ".radius")){
                        WildToolsPlugin.log("Couldn't find a radius for tool " + name + ", skipping");
                        continue;
                    }

                    tool = plugin.getToolsManager().registerTool(type, name, HarvesterTool.class, cfg.getInt("tools." + name + ".radius"));

                    ((HarvesterTool) tool).setActivationAction(cfg.getString("tools." + name + ".active-action", "RIGHT_CLICK"));
                    ((HarvesterTool) tool).setFarmlandRadius(cfg.getInt("tools." + name + ".farmland-radius", 0));
                    ((HarvesterTool) tool).setOneLayerOnly(cfg.getBoolean("tools." + name + ".one-layer-only", false));
                    break;
                case "ICE":
                    if(!cfg.contains("tools." + name + ".radius")){
                        WildToolsPlugin.log("Couldn't find a radius for tool " + name + ", skipping");
                        continue;
                    }

                    tool = plugin.getToolsManager().registerTool(type, name, IceTool.class, cfg.getInt("tools." + name + ".radius"));
                    break;
                case "LIGHTNING":
                    tool = plugin.getToolsManager().registerTool(type, name, LightningTool.class, null);
                    break;
                case "MAGNET":
                    tool = plugin.getToolsManager().registerTool(type, name, MagnetTool.class, cfg.getInt("tools." + name + ".radius"));
                    break;
                case "PILLAR":
                    tool = plugin.getToolsManager().registerTool(type, name, PillarTool.class, null);
                    break;
                case "SELL":
                    tool = plugin.getToolsManager().registerTool(type, name, SellTool.class, null);
                    break;
                case "SORT":
                    tool = plugin.getToolsManager().registerTool(type, name, SortTool.class, null);
                    break;
                default:
                    continue;
            }

            if(cfg.contains("tools." + name + ".cooldown"))
                tool.setCooldown(cfg.getLong("tools." + name + ".cooldown"));

            if(cfg.contains("tools." + name + ".auto-collect"))
                tool.setAutoCollect(cfg.getBoolean("tools." + name + ".auto-collect"));

            if(cfg.contains("tools." + name + ".instant-break"))
                tool.setInstantBreak(cfg.getBoolean("tools." + name + ".instant-break"));

            if(cfg.contains("tools." + name + ".silk-touch"))
                tool.setSilkTouch(cfg.getBoolean("tools." + name + ".silk-touch"));

            if(cfg.contains("tools." + name + ".only-same-type"))
                tool.setOnlySameType(cfg.getBoolean("tools." + name + ".only-same-type"));

            if(cfg.contains("tools." + name + ".only-inside-claim"))
                tool.setOnlyInsideClaim(cfg.getBoolean("tools." + name + ".only-inside-claim"));

            if(cfg.contains("tools." + name + ".unbreakable"))
                tool.setUnbreakable(cfg.getBoolean("tools." + name + ".unbreakable"));

            if(cfg.contains("tools." + name + ".vanilla-damage"))
                tool.setVanillaDamage(cfg.getBoolean("tools." + name + ".vanilla-damage"));

            if(cfg.contains("tools." + name + ".uses"))
                tool.setUsesLeft(cfg.getInt("tools." + name + ".uses"));

            if(cfg.contains("tools." + name + ".keep-inventory"))
                tool.setKeepInventory(cfg.getBoolean("tools." + name + ".keep-inventory"));

            if(cfg.contains("tools." + name + ".name"))
                tool.setDisplayName(cfg.getString("tools." + name + ".name"));

            if(cfg.contains("tools." + name + ".lore"))
                tool.setLore(cfg.getStringList("tools." + name + ".lore"));

            if(cfg.getBoolean("tools." + name + ".glow", false))
                tool.addEnchantment(plugin.getGlowEnchant(), 1);

            if(cfg.getBoolean("tools." + name + ".spigot-unbreakable", false))
                tool.setSpigotUnbreakable(cfg.getBoolean("tools." + name + ".spigot-unbreakable"));

            if(cfg.contains("tools." + name + ".enchants")){
                List<String> enchants = cfg.getStringList("tools." + name + ".enchants");
                for(String line : enchants)
                    try {
                        tool.addEnchantment(Enchantment.getByName(line.split(":")[0]),
                                Integer.parseInt(line.split(":")[1]));
                    } catch (IllegalArgumentException ignored){}
            }

            if(cfg.contains("tools." + name + ".blacklisted-blocks")){
                List<String> materials = cfg.getStringList("tools." + name + ".blacklisted-blocks");
                for(String mat : materials)
                    tool.addBlacklistedMaterial(mat);
            }

            if(cfg.contains("tools." + name + ".whitelisted-blocks")){
                List<String> materials = cfg.getStringList("tools." + name + ".whitelisted-blocks");
                for(String mat : materials)
                    tool.addWhitelistedMaterial(mat);
            }

            if(cfg.contains("tools." + name + ".blacklisted-drops")){
                List<String> drops = cfg.getStringList("tools." + name + ".blacklisted-drops");
                for(String drop : drops)
                    tool.addBlacklistedDrop(drop);
            }

            if(cfg.contains("tools." + name + ".whitelisted-drops")){
                List<String> drops = cfg.getStringList("tools." + name + ".whitelisted-drops");
                for(String drop : drops)
                    tool.addWhitelistedDrop(drop);
            }

            if(cfg.contains("tools." + name + ".multiplier"))
                tool.setMultiplier(cfg.getDouble("tools." + name + ".multiplier"));

            if(cfg.contains("tools." + name + ".omni-tool") && type.name().contains("_"))
                tool.setOmni(cfg.getBoolean("tools." + name + ".omni-tool"));

            if(cfg.contains("tools." + name + ".private"))
                tool.setPrivate(cfg.getBoolean("tools." + name + ".private"));

            if(cfg.contains("tools." + name + ".uses-progress"))
                tool.setUsesProgress(cfg.getBoolean("tools." + name + ".uses-progress"));

            if(cfg.contains("tools." + name + ".anvil-combine-exp"))
                tool.setAnvilCombineExp(cfg.getInt("tools." + name + ".anvil-combine-exp"));

            if(cfg.contains("tools." + name + ".anvil-combine-limit"))
                tool.setAnvilCombineLimit(cfg.getInt("tools." + name + ".anvil-combine-limit"));

            if(cfg.contains("tools." + name + ".blacklisted-worlds")){
                tool.setBlacklistedWorlds(cfg.getStringList("tools." + name + ".blacklisted-worlds"));
            }

            if(cfg.contains("tools." + name + ".whitelisted-worlds")){
                tool.setWhitelistedWorlds(cfg.getStringList("tools." + name + ".whitelisted-worlds"));
            }

            if(cfg.contains("tools." + name + ".notified-plugins"))
                tool.setNotifiedPlugins(cfg.getStringList("tools." + name + ".notified-plugins"));

            toolsAmount++;
        }

        WildToolsPlugin.log(" - Found " + toolsAmount + " tools in config.yml.");
        WildToolsPlugin.log("Loading configuration done (Took " + (System.currentTimeMillis() - startTime) + "ms)");
    }

    public static void reload(){
        loadData();
    }

}
