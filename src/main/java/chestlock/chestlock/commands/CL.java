package chestlock.chestlock.commands;

import java.util.*;

import chestlock.chestlock.Main;
import chestlock.chestlock.data.Perms;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import static org.bukkit.Bukkit.*;

public class CL implements TabExecutor {
    public static String adminPerm = "chestlock.admin";
    public static String lockPerm = "chestlock.lock";
    private static final NamespacedKey BypassKey = new NamespacedKey(Main.getPlugin(), "BYPASS");
    private static final List<String> COMMANDS = Arrays.asList("add", "remove", "list");
    private static final List<String> OPCOMMANDS = Arrays.asList("add", "remove", "list", "bypass");
    private static final List<String> BLANK = Collections.emptyList();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof Player) {
            if (args.length < 2) {
                if (hasAdminPerms((Player) sender)) {
                    return StringUtil.copyPartialMatches(args[0], OPCOMMANDS, new ArrayList<>());
                } else {
                    return StringUtil.copyPartialMatches(args[0], COMMANDS, new ArrayList<>());
                }
            }
            if (args.length == 2) {
                if (args[0].equalsIgnoreCase("bypass")) {
                    return BLANK;
                }
                Block block = ((Player)sender).getTargetBlock(10);
                if (args[0].equalsIgnoreCase("add")&&block!=null&&Main.chestManager.isLockable(block.getType())
                        &&(Main.chestManager.containsUUID(block.getLocation(), ((Player) sender).getUniqueId().toString(), Perms.ADMIN)
                        || !Main.chestManager.isLocked(block.getLocation()))){

                    List<String> playerNames = new LinkedList<>();
                    playerNames.add("owner");
                    for (OfflinePlayer player : getOfflinePlayers()){
                        playerNames.add(player.getName());
                    }
                    return StringUtil.copyPartialMatches(args[1], playerNames, new ArrayList<>());
                }
                if (args[0].equalsIgnoreCase("remove")&&block!=null&&Main.chestManager.isLockable(block.getType())
                        && Main.chestManager.containsUUID(block.getLocation(), ((Player) sender).getUniqueId().toString(), Perms.ADMIN)){

                    List<String> memberUUIDs = Main.chestManager.getUUIDs(block.getLocation(), Perms.MEMBER);
                    List<String> playerNames = new LinkedList<>();
                    for (String uuid : memberUUIDs){
                        playerNames.add(Bukkit.getOfflinePlayer(uuid).getName());
                    }
                    playerNames.add("owner");
                    return StringUtil.copyPartialMatches(args[1], playerNames, new ArrayList<>());
                }
            }
            if (args.length == 3){
                Block block = ((Player)sender).getTargetBlock(10);
                if (args[1].equalsIgnoreCase("owner")&&args[0].equalsIgnoreCase("add")&&block!=null
                        && Main.chestManager.isLockable(block.getType())&&(Main.chestManager.containsUUID(block.getLocation(), ((Player) sender).getUniqueId().toString(), Perms.ADMIN)
                        || !Main.chestManager.isLocked(block.getLocation()))){

                    List<String> playerNames = new LinkedList<>();
                    for (OfflinePlayer player : getOfflinePlayers()){
                        playerNames.add(player.getName());
                    }
                    return StringUtil.copyPartialMatches(args[2], playerNames, new ArrayList<>());
                }
                if (args[1].equalsIgnoreCase("owner")&&block!=null&&Main.chestManager.isLockable(block.getType())&&args[0].equalsIgnoreCase("remove")){
                    List<String> ownerUUIDS = Main.chestManager.getUUIDs(block.getLocation(), Perms.ADMIN);
                    if (ownerUUIDS.contains(((Player) sender).getUniqueId().toString())) {
                        List<String> playerNames = new LinkedList<>();
                        for (String uuid : ownerUUIDS) {
                            playerNames.add(Bukkit.getOfflinePlayer(uuid).getName());
                        }
                        return StringUtil.copyPartialMatches(args[2], playerNames, new ArrayList<>());
                    } else return BLANK;
                }
            }
        }
        return BLANK;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        switch (args.length){
            case 1: if (args[0].equalsIgnoreCase("list")) {
                listPlayers(sender);
                return true;
            }
            if (args[0].equalsIgnoreCase("bypass")) {
                return toggleBypass(sender);
            }
            if (args[0].equalsIgnoreCase("add")){
                lockChest(sender);
                return true;
            }
            if (args[0].equalsIgnoreCase("remove")){
                unlockChest(sender);
                return true;
            }
            return false;
            case 2: if (args[0].equalsIgnoreCase("add")&&!args[1].equalsIgnoreCase("owner")){
                addPlayer(sender, args[1], Perms.MEMBER);
                return true;
            }
            if (args[0].equalsIgnoreCase("remove")&&!args[1].equalsIgnoreCase("owner")){
                removePlayer(sender, args[1], Perms.MEMBER);
                return true;
            } return false;
            case 3:

                if (args[0].equalsIgnoreCase("add")&&args[1].equalsIgnoreCase("owner")){
                addPlayer(sender, args[2], Perms.ADMIN);
                return true;
            }
            if (args[0].equalsIgnoreCase("remove")&&args[1].equalsIgnoreCase("owner")){
                removePlayer(sender, args[2], Perms.ADMIN);
                return true;
            }
        }
        return false;
    }

    public static void lockChest(CommandSender sender){
        if (sender instanceof Player) {
            Player playerSender = (Player) sender;

            Block block = playerSender.getTargetBlock(10);
            if (block != null && Main.chestManager.isLockable(block.getType())) {
                if (!Main.chestManager.isLocked(block.getLocation())) {
                    Main.chestManager.addUUID(block.getLocation(), playerSender.getUniqueId().toString(), Perms.ADMIN);
                    playerSender.sendMessage(Main.LOCK_SUCCESS);
                } else {
                    playerSender.sendMessage(Main.ALREADY_LOCKED);
                }
            } else {
                playerSender.sendMessage(Main.LOCKABLE_BLOCK);
            }
        } else {
            sender.sendMessage(Main.FROM_CONSOLE);
        }
    }

    public static void unlockChest(CommandSender sender){
        if (sender instanceof Player) {
            Player playerSender = (Player) sender;

            Block block = playerSender.getTargetBlock(10);
            if (block != null && Main.chestManager.isLockable(block.getType())) {
                //List<UUID> ownerUUIDs = DataManager.getOwnerUUIDS(block);
                if (Main.chestManager.isLocked(block.getLocation())){
                    if (Main.chestManager.containsUUID(block.getLocation(), playerSender.getUniqueId().toString(), Perms.ADMIN)){
                        Main.chestManager.removeChest(block.getLocation());
                        playerSender.sendMessage(Main.UNLOCK_SUCCESS);
                    } else {
                        playerSender.sendMessage(Main.NOT_OWNER);
                    }
                } else {
                    playerSender.sendMessage(Main.NOT_LOCKED);
                }

            } else {
                playerSender.sendMessage(Main.LOCKABLE_BLOCK);
            }
        } else {
            sender.sendMessage(Main.FROM_CONSOLE);
        }
    }

    public static void listPlayers(CommandSender sender){
        System.out.println(Main.chestManager.getMap());
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Block block = player.getTargetBlock(10);
            if (block != null && Main.chestManager.isLockable(block.getType())
                    && (Main.chestManager.containsUUID(block.getLocation(), player.getUniqueId().toString(), Perms.MEMBER)
                    || hasAdminPerms(player) )) {

                //Do send chest info stuff
                if (Main.chestManager.isLocked(block.getLocation())) {
                    //gets admins
                    player.sendMessage(Main.ALLOWED_OWNERS);
                    List<String> adminUUIDs = Main.chestManager.getUUIDs(block.getLocation(), Perms.ADMIN);
                    for (String uuid : adminUUIDs) {
                        String name = getOfflinePlayer(UUID.fromString(uuid)).getName();
                        if (name!=null)
                            player.sendMessage(ChatColor.LIGHT_PURPLE+name);
                    }


                    //gets users
                    player.sendMessage(Main.ALLOWED_MEMBERS);
                    List<String> memberUUIDs = Main.chestManager.getUUIDs(block.getLocation(), Perms.MEMBER);
                    for (String uuid : memberUUIDs) {
                        String name = getOfflinePlayer(UUID.fromString(uuid)).getName();
                        if (name!=null)
                            player.sendMessage(ChatColor.LIGHT_PURPLE+name);
                    }
                } else {
                    player.sendMessage(Main.NOT_LOCKED);
                }
            } else {
                player.sendMessage(ChatColor.RED+"Please make sure you are looking at a lockable block and that you have permissions");
            }
        } else {
            sender.sendMessage("Please run this command as a player");
        }

    }

    @SuppressWarnings("all")
    public static OfflinePlayer getBedrockOfflinePlayer(String playerName){
        OfflinePlayer[] offlinePLayers = getOfflinePlayers();
        for (OfflinePlayer player : offlinePLayers){
            if (player.getName().equals(playerName)){
                return player;
            }
        }
        return null;
    }

    public static void addPlayer(CommandSender sender, String targetName, String permissionLevel){
        if (sender instanceof Player) {
            Player playerSender = (Player) sender;


            OfflinePlayer playerTarget = Arrays.stream(Bukkit.getOfflinePlayers())
                    .filter(player -> player.getName().equals(targetName))
                    .findFirst().orElse(null);
            UUID playerTargetUUID = playerTarget == null ? null : playerTarget.getUniqueId();


            if (playerTargetUUID==null){
                playerSender.sendMessage(Main.INVALID_PLAYER);
                return;
            }

            Block block = playerSender.getTargetBlock(10);
            if (block != null && Main.chestManager.isLockable(block.getType())) {
                if ((Main.chestManager.containsUUID(block.getLocation(), playerSender.getUniqueId().toString(), Perms.ADMIN)
                        || shouldBypass(playerSender))) {
                    //do chest locking stuff
                    if (Main.chestManager.addUUID(block.getLocation(), playerTargetUUID.toString(), permissionLevel)) {
                        playerSender.sendMessage(String.format(Main.ACCESS_GRANTED, targetName, permissionLevel.toLowerCase()));
                        if (playerTarget.isOnline()) {
                            playerTarget.getPlayer().sendMessage(String.format(Main.GIVEN_ACCESS, permissionLevel.toLowerCase(), playerSender.getName()));
                        }
                    } else {
                        playerSender.sendMessage(String.format(Main.HAS_ACCESS, targetName, permissionLevel.toLowerCase()));
                    }
                } else {
                    playerSender.sendMessage(Main.NOT_OWNER);
                }

            } else {
                playerSender.sendMessage(Main.LOCKABLE_BLOCK);
            }
        } else {
            sender.sendMessage(Main.FROM_CONSOLE);
        }
    }

    public static void removePlayer(CommandSender sender, String targetName, String permissionLevel){
        if (sender instanceof Player) {
            Player playerSender = (Player) sender;

            OfflinePlayer playerTarget = Arrays.stream(Bukkit.getOfflinePlayers())
                    .filter(player -> player.getName().equals(targetName))
                    .findFirst().orElse(null);
            UUID playerTargetUUID = playerTarget == null ? null : playerTarget.getUniqueId();


            if (playerTargetUUID==null){
                playerSender.sendMessage(Main.INVALID_PLAYER);
                return;
            }

            Block block = playerSender.getTargetBlock(10);
            if (block != null && Main.chestManager.isLockable(block.getType())){
                if (Main.chestManager.containsUUID(block.getLocation(), playerSender.getUniqueId().toString(), permissionLevel)
                        || shouldBypass(playerSender)){


                    if (Main.chestManager.removeUUID(block.getLocation(), playerTargetUUID.toString(), permissionLevel)) {
                        switch (permissionLevel) {
                            case Perms.ADMIN:
                                playerSender.sendMessage(String.format(Main.ADMIN_REMOVED, targetName));
                            case Perms.MEMBER:
                                playerSender.sendMessage(String.format(Main.MEMBER_REMOVED, targetName));
                        }
                    } else {
                        switch (permissionLevel) {
                            case Perms.ADMIN:
                                playerSender.sendMessage(String.format(Main.MEMBER_NOT_ALLOWED, targetName));
                            case Perms.MEMBER:
                                playerSender.sendMessage(String.format(Main.OWNER_NOT_ALLOWED, targetName));
                        }
                    }
                } else {
                    playerSender.sendMessage(Main.NOT_OWNER);
                }

            } else {
                playerSender.sendMessage(Main.LOCKABLE_BLOCK);
            }
        } else {
            sender.sendMessage(Main.FROM_CONSOLE);
        }
    }

    public static boolean toggleBypass(CommandSender sender){
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (hasAdminPerms(player)) {
                Block block = player.getTargetBlock(10);
                PersistentDataContainer container = player.getPersistentDataContainer();
                Integer bypass = container.get(BypassKey, PersistentDataType.INTEGER);
                if (bypass!=null) {
                    if (bypass == 0) {
                        container.set(BypassKey, PersistentDataType.INTEGER, 1);
                        player.sendMessage(ChatColor.DARK_AQUA + "You are now bypassing chest protection");
                    } else {
                        container.set(BypassKey, PersistentDataType.INTEGER, 0);
                        player.sendMessage(ChatColor.DARK_AQUA + "You are no longer bypassing chest protection");
                    }
                } else {
                    container.set(BypassKey, PersistentDataType.INTEGER, 1);
                    player.sendMessage(ChatColor.DARK_AQUA + "You are now bypassing chest protection");
                }
                return true;
            }
        } else {
            sender.sendMessage("Please run this command as a player");
        }
        return false;
    }

    public static boolean shouldBypass(Player player){
        PersistentDataContainer container = player.getPersistentDataContainer();
        if (hasAdminPerms(player)) {
            Integer bypass = container.get(BypassKey, PersistentDataType.INTEGER);
            if (bypass!=null && bypass == 1) {
                player.sendMessage(ChatColor.DARK_PURPLE+"You are bypassing protection");
                return true;
            } else {
                player.sendMessage(ChatColor.DARK_PURPLE+"Please use /cl bypass to toggle bypassing");
                return false;
            }
        }
        return false;
    }

    public static boolean hasAdminPerms(Player player){
        return player.hasPermission(adminPerm);
    }

    public static boolean hasLockPerms(Player player) {
        return player.hasPermission(lockPerm);
    }
}
