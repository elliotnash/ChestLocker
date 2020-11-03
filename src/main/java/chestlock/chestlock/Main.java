package chestlock.chestlock;

import chestlock.chestlock.commands.CL;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Logger;

public final class Main extends JavaPlugin {

    //TODO make tab complete show when you arent looking at a block

    private static FileConfiguration config;
    private static Main plugin;
    private static Logger logger;
    public static String geyserPrefix;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static HashMap<String, HashMap<String, LinkedList<String>>> chestMap = new HashMap<>();

    @Override
    public void onEnable() {
        plugin = this;
        logger = getLogger();

        this.saveDefaultConfig();
        config = this.getConfig();


        getServer().getPluginManager().registerEvents(new CLListener(), this);

        //Command initialization
        this.getCommand("cl").setExecutor(new CL());

        if (config.getBoolean("geyserSupport"))
            geyserPrefix = config.getString("geyserPrefix");

        //Convert json bellow

        try (FileReader reader = new FileReader(getDataFolder().getPath()+"/chests.json"))
        {
            Main.chestMap = gson.fromJson(reader, HashMap.class);

        } catch (FileNotFoundException e){
            logger.warning("Chest data file not found (chests.json), will create new one");
        } catch (IOException e){
            e.printStackTrace();
        }

        //no convert anymore


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static void log(String toLog){ logger.info(toLog); }

    public static Main getPlugin(){
        return plugin;
    }

    public static boolean isLockable(Material mat){
        return mat == Material.CHEST
                || mat == Material.TRAPPED_CHEST
                || mat == Material.BARREL
                || mat == Material.FURNACE
                || mat == Material.DISPENSER
                || mat == Material.SMOKER
                || mat == Material.BLAST_FURNACE
                || mat == Material.DROPPER
                || mat == Material.BREWING_STAND
                || mat == Material.HOPPER
                || mat == Material.BEACON
                || mat == Material.ENDER_CHEST
                || isShulker(mat);
    }

    public static boolean isShulker(Material mat){
        return mat == Material.SHULKER_BOX
                || mat == Material.BLACK_SHULKER_BOX
                || mat == Material.BLUE_SHULKER_BOX
                || mat == Material.BROWN_SHULKER_BOX
                || mat == Material.CYAN_SHULKER_BOX
                || mat == Material.GRAY_SHULKER_BOX
                || mat == Material.GREEN_SHULKER_BOX
                || mat == Material.LIGHT_BLUE_SHULKER_BOX
                || mat == Material.LIGHT_GRAY_SHULKER_BOX
                || mat == Material.LIME_SHULKER_BOX
                || mat == Material.MAGENTA_SHULKER_BOX
                || mat == Material.ORANGE_SHULKER_BOX
                || mat == Material.PINK_SHULKER_BOX
                || mat == Material.PURPLE_SHULKER_BOX
                || mat == Material.RED_SHULKER_BOX
                || mat == Material.WHITE_SHULKER_BOX
                || mat == Material.YELLOW_SHULKER_BOX;
    }

    public static boolean canBeDouble(Material mat){
        return mat == Material.CHEST
                || mat == Material.TRAPPED_CHEST;
    }

    public static boolean isDrainable(Material mat){
        return mat == Material.CHEST
                || mat == Material.TRAPPED_CHEST
                || mat == Material.BARREL
                || mat == Material.DISPENSER
                || mat == Material.DROPPER;
    }

}
