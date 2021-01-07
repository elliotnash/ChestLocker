package chestlock.chestlock;

import chestlock.chestlock.data.chestManager;
import chestlock.chestlock.commands.CL;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class Main extends JavaPlugin {

    //TODO make tab complete show when you arent looking at a block

    private static FileConfiguration config;
    private static Main plugin;
    private static Logger logger;
    public static final chestManager chestManager = new chestManager();

    @Override
    public void onEnable() {
        //load json for chestMap
        chestManager.loadJson();

        plugin = this;
        logger = getLogger();

        this.saveDefaultConfig();
        config = this.getConfig();


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

    //info messages
    public static final String LOCK_SUCCESS = ChatColor.AQUA + "Chest locked";
    public static final String UNLOCK_SUCCESS = ChatColor.AQUA + "Chest unlocked";

    public static final String ACCESS_GRANTED = ChatColor.AQUA + "%s has been given %s access to this chest";
    public static final String GIVEN_ACCESS = ChatColor.AQUA + "You have been given %s access to %s's chest";

    public static final String MEMBER_REMOVED = ChatColor.AQUA + "%s can no longer access this chest";
    public static final String ADMIN_REMOVED = ChatColor.AQUA + "%s no longer has owner privileges";

    public static final String HOPPER_NOT_LOCKED = ChatColor.AQUA + "Remember this hopper is not locked.";


    //error messages
    public static final String ALREADY_LOCKED = ChatColor.RED + "This chest is already locked";
    public static final String CHEST_IS_LOCKED = ChatColor.RED + "Chest is locked!";
    public static final String HOPPER_UNDER_CHEST = ChatColor.RED + "You can't place hoppers under a locked chest you don't have access to";
    public static final String NOT_LOCKED = ChatColor.RED + "This chest isn't locked";
    public static final String LOCKABLE_BLOCK = ChatColor.RED + "Please make sure you are looking at a lockable block";
    public static final String FROM_CONSOLE = ChatColor.RED + "Please run this command as a player";
    public static final String NOT_OWNER = ChatColor.RED+"Only a chest owner can add or remove players";
    public static final String INVALID_PLAYER = ChatColor.RED + "This player is invalid or has not played before";
    public static final String HAS_ACCESS = ChatColor.RED + "%s already has access %s to this chest";
    public static final String MEMBER_NOT_ALLOWED = ChatColor.RED + "%s was not allowed in this chest";
    public static final String OWNER_NOT_ALLOWED = ChatColor.RED + "%s was not an owner";


    //list messages
    public static final String ALLOWED_OWNERS = ChatColor.GOLD + "The following players are owners of this chest:";
    public static final String ALLOWED_MEMBERS = ChatColor.GOLD + "The following players are allowed to open this chest:";


}
