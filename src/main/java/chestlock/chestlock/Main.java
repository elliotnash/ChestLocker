package chestlock.chestlock;

import chestlock.chestlock.commands.CL;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Logger;

public final class Main extends JavaPlugin {

    //TODO allow shulkers to keep uuids when in item form
    //TODO add and remove commands (should be simple)

    private static Configuration config;
    private boolean disabled = false;
    private static Main plugin;
    private static Logger logger;

    @Override
    public void onEnable() {
        plugin = this;
        logger = getLogger();

        loadConfig();
        logger.info("Config initialized");

        getServer().getPluginManager().registerEvents(new CLListener(), this);

        //Command initialization
        this.getCommand("cl").setExecutor(new CL());

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static void log(String toLog){ logger.info(toLog); }

    public static Main getPlugin(){
        return plugin;
    }

    public void loadConfig() {
        // loadConfig config
        if (!getDataFolder().exists())
            getDataFolder().mkdir();

        File file = new File(getDataFolder(), "config.yml");

        if (!file.exists()) {
            try (InputStream in = getResource("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isLockable(Material mat){
        return mat == Material.CHEST
                || mat == Material.TRAPPED_CHEST
                || mat == Material.BARREL
                || mat == Material.SHULKER_BOX
                || mat == Material.FURNACE
                || mat == Material.DISPENSER
                || mat == Material.SMOKER
                || mat == Material.BLAST_FURNACE
                || mat == Material.DROPPER
                || mat == Material.BREWING_STAND
                || mat == Material.HOPPER
                || mat == Material.BEACON
                || mat == Material.ENDER_CHEST;
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
