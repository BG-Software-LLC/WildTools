package xyz.wildseries.wildtools.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.wildseries.wildtools.WildToolsPlugin;
import xyz.wildseries.wildtools.objects.WMaterial;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public final class ItemBuilder {

    private static WildToolsPlugin plugin = WildToolsPlugin.getPlugin();

    private ItemStack itemStack;
    private ItemMeta itemMeta;

    public ItemBuilder(ItemStack itemStack){
        this(itemStack.getType(), itemStack.getDurability());
        this.itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder(WMaterial type){
        this(type.parseMaterial(), 0);
    }

    public ItemBuilder(Material type){
        this(type, 0);
    }

    public ItemBuilder(Material type, int damage){
        itemStack = new ItemStack(type, 1, (short) damage);
        itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder(ConfigurationSection section){
        Material type;
        int damage = section.getInt("data", 0);

        try{
            type = Material.valueOf(section.getString("type"));
        }catch(Exception ex){
            throw new IllegalArgumentException("Couldn't find valid type for " + section.getCurrentPath() + "...");
        }

        itemStack = new ItemStack(type, 1, (short) damage);
        itemMeta = itemStack.getItemMeta();

        if(section.contains("name"))
            withName(section.getString("name"));

        if(section.contains("lore"))
            withLore(section.getStringList("lore"));

        if(section.getBoolean("glow", false))
            itemMeta.addEnchant(plugin.getGlowEnchant(), 1, true);

        if(section.contains("enchants")){
            List<String> enchants = section.getStringList("enchants");
            for(String line : enchants) {
                try {
                    itemMeta.addEnchant(Enchantment.getByName(line.split(":")[0]),
                            Integer.valueOf(line.split(":")[1]), true);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    public ItemBuilder withName(String name){
        if(!name.isEmpty())
            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        return this;
    }

    public ItemBuilder withLore(String firstLine, List<String> listLine){
        List<String> loreList = new ArrayList<>();

        firstLine = ChatColor.translateAlternateColorCodes('&', firstLine);
        loreList.add(firstLine);

        for(String line : listLine){
            loreList.add(ChatColor.getLastColors(firstLine) + ChatColor.translateAlternateColorCodes('&', line));
        }

        if(loreList.size() > 10){
            for(int i = 10; i < loreList.size(); i++){
                loreList.remove(loreList.get(i));
            }
            loreList.add(ChatColor.getLastColors(firstLine) + "...");
        }

        itemMeta.setLore(loreList);
        return this;
    }

    public ItemBuilder withLore(List<String> listLine){
        List<String> loreList = new ArrayList<>();

        for(String line : listLine){
            loreList.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        itemMeta.setLore(loreList);
        return this;
    }

    public ItemBuilder withLore(String... lore){
        List<String> loreList = new ArrayList<>();

        for(String line : lore){
            loreList.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        itemMeta.setLore(loreList);
        return this;
    }

    public ItemStack build(){
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

}
