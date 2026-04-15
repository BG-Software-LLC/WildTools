package com.bgsoftware.wildtools.handlers.loaders;

import com.bgsoftware.wildtools.api.objects.ToolSectionView;
import com.bgsoftware.wildtools.api.objects.tools.Tool;
import com.bgsoftware.wildtools.tools.WTool;
import org.bukkit.enchantments.Enchantment;

import java.util.List;


public final class CommonToolLoader {

    private CommonToolLoader() {
    }

    public static void applyCommonProperties(Tool tool, ToolSectionView cfg) {
        if (cfg.contains("cooldown"))
            tool.setCooldown(cfg.getLong("cooldown", 0L));
        if (cfg.contains("auto-collect"))
            tool.setAutoCollect(cfg.getBoolean("auto-collect", false));
        if (cfg.contains("instant-break"))
            tool.setInstantBreak(cfg.getBoolean("instant-break", false));
        if (cfg.contains("silk-touch"))
            tool.setSilkTouch(cfg.getBoolean("silk-touch", false));
        if (cfg.contains("only-same-type"))
            tool.setOnlySameType(cfg.getBoolean("only-same-type", false));
        if (cfg.contains("only-inside-claim"))
            tool.setOnlyInsideClaim(cfg.getBoolean("only-inside-claim", false));
        if (cfg.contains("unbreakable"))
            tool.setUnbreakable(cfg.getBoolean("unbreakable", false));
        if (cfg.contains("vanilla-damage"))
            tool.setVanillaDamage(cfg.getBoolean("vanilla-damage", false));
        if (cfg.contains("uses"))
            tool.setUsesLeft(cfg.getInt("uses", 0));
        if (cfg.contains("keep-inventory"))
            tool.setKeepInventory(cfg.getBoolean("keep-inventory", false));
        if (cfg.contains("name"))
            tool.setDisplayName(cfg.getString("name", null));
        if (cfg.contains("lore"))
            tool.setLore(cfg.getStringList("lore"));
        if (cfg.getBoolean("glow", false) && tool instanceof WTool)
            ((WTool) tool).makeToolGlow();
        if (cfg.contains("spigot-unbreakable"))
            tool.setSpigotUnbreakable(cfg.getBoolean("spigot-unbreakable", false));
        if (cfg.contains("custom-model"))
            tool.setCustomModel(cfg.getInt("custom-model", 0));

        List<String> enchants = cfg.getStringList("enchants");
        if (enchants != null) {
            for (String line : enchants) {
                try {
                    String[] s = line.split(":");
                    Enchantment ench = Enchantment.getByName(s[0]);
                    int lvl = Integer.parseInt(s[1]);
                    tool.addEnchantment(ench, lvl);
                } catch (Exception ignored) {
                }
            }
        }

        List<String> mats = cfg.getStringList("blacklisted-blocks");
        if (mats != null)
            for (String m : mats) tool.addBlacklistedMaterial(m);

        mats = cfg.getStringList("whitelisted-blocks");
        if (mats != null)
            for (String m : mats) tool.addWhitelistedMaterial(m);

        List<String> drops = cfg.getStringList("blacklisted-drops");
        if (drops != null)
            for (String d : drops) tool.addBlacklistedDrop(d);

        drops = cfg.getStringList("whitelisted-drops");
        if (drops != null)
            for (String d : drops) tool.addWhitelistedDrop(d);

        if (cfg.contains("multiplier"))
            tool.setMultiplier(cfg.getDouble("multiplier", 1.0));
        if (cfg.contains("omni-tool"))
            tool.setOmni(cfg.getBoolean("omni-tool", false));
        if (cfg.contains("private"))
            tool.setPrivate(cfg.getBoolean("private", false));
        if (cfg.contains("uses-progress"))
            tool.setUsesProgress(cfg.getBoolean("uses-progress", false));
        if (cfg.contains("anvil-combine-exp"))
            tool.setAnvilCombineExp(cfg.getInt("anvil-combine-exp", 0));
        if (cfg.contains("anvil-combine-limit"))
            tool.setAnvilCombineLimit(cfg.getInt("anvil-combine-limit", 0));

        List<String> worlds = cfg.getStringList("blacklisted-worlds");
        if (worlds != null)
            tool.setBlacklistedWorlds(worlds);

        worlds = cfg.getStringList("whitelisted-worlds");
        if (worlds != null)
            tool.setWhitelistedWorlds(worlds);

        List<String> plugins = cfg.getStringList("notified-plugins");
        if (plugins != null)
            tool.setNotifiedPlugins(plugins);
    }
}
