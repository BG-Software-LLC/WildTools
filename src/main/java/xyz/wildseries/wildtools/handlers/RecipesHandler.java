package xyz.wildseries.wildtools.handlers;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import xyz.wildseries.wildtools.WildToolsPlugin;
import xyz.wildseries.wildtools.utils.ItemBuilder;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RecipesHandler {

    private Map<String, ItemStack[]> recipes = new HashMap<>();

    public RecipesHandler(WildToolsPlugin plugin){
        File file = new File(plugin.getDataFolder(), "recipes.yml");

        if(!file.exists()){
            plugin.saveResource("recipes.yml", false);
        }

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        Map<String, ItemStack> recipeItems = new HashMap<>();

        if(cfg.contains("items")){
            for(String name : cfg.getConfigurationSection("items").getKeys(false)){
                recipeItems.put(name.toLowerCase(), new ItemBuilder(cfg.getConfigurationSection("items." + name)).build());
            }
        }

        if(cfg.contains("recipes")){
            outerLoop: for(String name : cfg.getConfigurationSection("recipes").getKeys(false)){
                List<String> configRecipe = cfg.getStringList("recipes." + name);
                ItemStack[] recipe = new ItemStack[9];

                for(int i = 0; i < 3; i++){
                    String[] types = configRecipe.get(i).split(", ");
                    for(int j = 0; j < 3; j++){
                        ItemStack itemStack;

                        try {
                            if (recipeItems.containsKey(types[j].toLowerCase())) {
                                itemStack = recipeItems.get(types[j].toLowerCase());
                            } else {
                                if (types[j].contains(":")) {
                                    itemStack = new ItemStack(Material.valueOf(types[j].split(":")[0]), 1, Short.valueOf(types[j].split(":")[1]));
                                } else {
                                    itemStack = new ItemStack(Material.valueOf(types[j]));
                                }
                            }
                        }catch(Exception ex){
                            WildToolsPlugin.log("Couldn't find valid recipe for " + name + ", skipping...");
                            continue outerLoop;
                        }

                        recipe[3*i + j] = itemStack;
                    }
                }

                for(int i = 0; i < 9; i++){
                    if(recipe[i] == null)
                        recipe[i] = new ItemStack(Material.AIR);
                }

                recipes.put(name, recipe);
            }
        }

    }

    public Map<String, ItemStack[]> getRecipes() {
        return new HashMap<>(recipes);
    }
}
