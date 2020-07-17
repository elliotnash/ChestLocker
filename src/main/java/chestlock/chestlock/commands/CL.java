package chestlock.chestlock.commands;

import java.util.*;

import chestlock.chestlock.Main;
import chestlock.chestlock.Vars;
import chestlock.chestlock.persist.PersistConvert;
import chestlock.chestlock.persist.PersistInput;
import javafx.util.Pair;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.HumanEntity;
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
                if (args[0].equalsIgnoreCase("add")&&block!=null&&Main.isLockable(block.getType())&&(PersistInput.containsOwnerUUID(block, ((Player) sender).getUniqueId()) || !PersistInput.isLocked(block))){
                    List<Player> players = (List<Player>) getOnlinePlayers();
                    List<String> playerNames = new LinkedList<>();
                    playerNames.add("owner");
                    for (Player player : players){
                        playerNames.add(player.getName());
                    }
                    return StringUtil.copyPartialMatches(args[1], playerNames, new ArrayList<>());
                }
                if (args[0].equalsIgnoreCase("remove")&&block!=null&&Main.isLockable(block.getType())&&PersistInput.containsOwnerUUID(block, ((Player) sender).getUniqueId())){
                    List<UUID> ownerUUIDS = PersistInput.getPlayerUUIDS(block);
                    List<String> playerNames = new LinkedList<>();
                    for (UUID uuid : ownerUUIDS){
                        playerNames.add(Bukkit.getOfflinePlayer(uuid).getName());
                    }
                    playerNames.add("owner");
                    return StringUtil.copyPartialMatches(args[1], playerNames, new ArrayList<>());
                }
            }
            if (args.length == 3){
                if (args[1].equalsIgnoreCase("owner")&&args[0].equalsIgnoreCase("add")){
                    List<Player> players = (List<Player>) getOnlinePlayers();
                    List<String> playerNames = new LinkedList<>();
                    for (Player player : players){
                        playerNames.add(player.getName());
                    }
                    return StringUtil.copyPartialMatches(args[2], playerNames, new ArrayList<>());
                }
                Block block = ((Player)sender).getTargetBlock(10);
                if (args[1].equalsIgnoreCase("owner")&&block!=null&&Main.isLockable(block.getType())&&args[0].equalsIgnoreCase("remove")){
                    List<UUID> ownerUUIDS = PersistInput.getOwnerUUIDS(block);
                    if (ownerUUIDS.contains(((Player) sender).getUniqueId())) {
                        List<String> playerNames = new LinkedList<>();
                        for (UUID uuid : ownerUUIDS) {
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
                addPlayer(sender, args[1]);
                return true;
            }
            if (args[0].equalsIgnoreCase("remove")&&!args[1].equalsIgnoreCase("owner")){
                removePlayers(sender, args[1]);
                return true;
            } return false;
            case 3:

                if (args[0].equalsIgnoreCase("add")&&args[1].equalsIgnoreCase("owner")){
                addOwner(sender, args[2]);
                return true;
            }
            if (args[0].equalsIgnoreCase("remove")&&args[1].equalsIgnoreCase("owner")){
                removeOwner(sender, args[2]);
                return true;
            }
        }
        return false;
    }

    public static void lockChest(CommandSender sender){
        if (sender instanceof Player) {
            Player playerSender = (Player) sender;

            Block block = playerSender.getTargetBlock(10);
            if (block != null && Main.isLockable(block.getType())) {
                if (!PersistInput.isLocked(block)) {
                    if (PersistInput.addOwnerUUID(block, playerSender.getUniqueId())) {
                        playerSender.sendMessage(ChatColor.AQUA + "Chest locked");
                    } else {
                        playerSender.sendMessage(ChatColor.RED + "This chest is already locked!");
                    }
                } else {
                    playerSender.sendMessage(ChatColor.RED + "This chest is already locked!");
                }
            } else {
                playerSender.sendMessage(ChatColor.RED+"Please make sure you are looking at a lockable block");
            }
        } else {
            sender.sendMessage("Please run this command as a player");
        }
    }

    public static void unlockChest(CommandSender sender){
        if (sender instanceof Player) {
            Player playerSender = (Player) sender;

            Block block = playerSender.getTargetBlock(10);
            if (block != null && Main.isLockable(block.getType())) {
                List<UUID> ownerUUIDs = PersistInput.getOwnerUUIDS(block);
                if (!ownerUUIDs.isEmpty()){
                    if (ownerUUIDs.contains(playerSender.getUniqueId())){
                        PersistInput.unlockChest(block);
                        playerSender.sendMessage(ChatColor.AQUA+"Chest unlocked");
                    } else {
                        playerSender.sendMessage(ChatColor.RED+"Only a chest owner can add players");
                    }
                } else {
                    playerSender.sendMessage(ChatColor.RED+"This chest isn't locked");
                }

            } else {
                playerSender.sendMessage(ChatColor.RED+"Please make sure you are looking at a lockable block");
            }
        } else {
            sender.sendMessage("Please run this command as a player");
        }
    }

    public static void listPlayers(CommandSender sender){
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Block block = player.getTargetBlock(10);
            if (block != null && Main.isLockable(block.getType()) && (PersistInput.containsUUID(block, player.getUniqueId()) || hasAdminPerms(player) )) {
                //Do send chest info stuff
                List<UUID> uuids = PersistInput.getPlayerUUIDS(block);
                if (!uuids.isEmpty()) {
                    //gets owners
                    player.sendMessage(ChatColor.GOLD+"The following players are owners of this chest:");
                    List<UUID> ownerUUID = PersistInput.getOwnerUUIDS(block);
                    for (UUID uuid : ownerUUID) {
                        String name = getOfflinePlayer(uuid).getName();
                        if (name!=null)
                            player.sendMessage(ChatColor.LIGHT_PURPLE+name);
                    }


                    //gets users
                    player.sendMessage(ChatColor.GOLD+"The following players are allowed to open this chest:");
                    for (UUID uuid : uuids) {
                        String name = getOfflinePlayer(uuid).getName();
                        if (name!=null)
                            player.sendMessage(ChatColor.LIGHT_PURPLE+name);
                    }
                } else {
                    player.sendMessage(ChatColor.DARK_PURPLE + "This chest isn't locked");
                }
            } else {
                player.sendMessage(ChatColor.RED+"Please make sure you are looking at a lockable block and that you have permissions");
            }
        } else {
            sender.sendMessage("Please run this command as a player");
        }

    }

    @SuppressWarnings("all")
    public static Pair<Boolean, OfflinePlayer> getBedrockOfflinePlayer(String playerName){
        OfflinePlayer[] offlinePLayers = getOfflinePlayers();
        for (OfflinePlayer player : offlinePLayers){
            if (player.getName().equals(playerName)){
                return new Pair<>(true, player);
            }
        }
        return new Pair<>(false, null);
    }

    public static void addPlayer(CommandSender sender, String targetName){
        if (sender instanceof Player) {
            Player playerSender = (Player) sender;


            UUID playerTargetUUID;
            OfflinePlayer playerTarget;


            if (Main.geyserPrefix==null) {

                //runs if target is java player
                playerTargetUUID = getPlayerUniqueId(targetName);
                if (playerTargetUUID == null) {
                    playerSender.sendMessage(ChatColor.RED + "This player is invalid or has not played before");
                    return;
                }
                playerTarget = Bukkit.getOfflinePlayer(playerTargetUUID);
                if (!playerTarget.hasPlayedBefore()) {
                    playerSender.sendMessage(ChatColor.RED + "This player is invalid or has not played before");
                    return;
                }
            } else if (!targetName.startsWith(Main.geyserPrefix)) {
                //runs if target is java player
                playerTargetUUID = getPlayerUniqueId(targetName);
                if (playerTargetUUID == null) {
                    playerSender.sendMessage(ChatColor.RED + "This player is invalid or has not played before");
                    return;
                }
                playerTarget = Bukkit.getOfflinePlayer(playerTargetUUID);
                if (!playerTarget.hasPlayedBefore()) {
                    playerSender.sendMessage(ChatColor.RED + "This player is invalid or has not played before");
                    return;
                }
            }else {
                // runs if target player is bedrock to detect if the player is an offline player
                Pair<Boolean, OfflinePlayer> bedrockPair = getBedrockOfflinePlayer(targetName);
                if (bedrockPair.getKey()) {
                    playerTarget = bedrockPair.getValue();
                    playerTargetUUID = playerTarget.getUniqueId();
                } else {
                    playerSender.sendMessage(ChatColor.RED + "This player is invalid or has not played before");
                    return;
                }
            }
            Block block = playerSender.getTargetBlock(10);
            if (block != null && Main.isLockable(block.getType())) {
                if ((PersistInput.containsOwnerUUID(block, playerSender.getUniqueId()) || shouldBypass(playerSender))) {
                    //do chest locking stuff
                    if (PersistInput.addPlayerUUID(block, playerTargetUUID)) {
                        playerSender.sendMessage(ChatColor.AQUA + playerTarget.getName() + " has been given access to this chest");
                        if (playerTarget.isOnline()) {
                            playerTarget.getPlayer().sendMessage(ChatColor.AQUA + "You have been given access to " + playerSender.getName() + "'s chest");
                        }
                    } else {
                        playerSender.sendMessage(ChatColor.RED + playerTarget.getName() + " already has access to this chest");
                    }
                } else {
                    playerSender.sendMessage(ChatColor.RED+"Only a chest owner can add players");
                }

            } else {
                playerSender.sendMessage(ChatColor.RED+"Please make sure you are looking at a lockable block");
            }
        } else {
            sender.sendMessage("Please run this command as a player");
        }
    }

    public static void addOwner(CommandSender sender, String targetName){
        if (sender instanceof Player) {
            Player playerSender = (Player) sender;

            UUID playerTargetUUID;
            OfflinePlayer playerTarget;


            if (Main.geyserPrefix==null) {

                //runs if target is java player
                playerTargetUUID = getPlayerUniqueId(targetName);
                if (playerTargetUUID == null) {
                    playerSender.sendMessage(ChatColor.RED + "This player is invalid or has not played before");
                    return;
                }
                playerTarget = Bukkit.getOfflinePlayer(playerTargetUUID);
                if (!playerTarget.hasPlayedBefore()) {
                    playerSender.sendMessage(ChatColor.RED + "This player is invalid or has not played before");
                    return;
                }
            } else if (!targetName.startsWith(Main.geyserPrefix)) {
                //runs if target is java player
                playerTargetUUID = getPlayerUniqueId(targetName);
                if (playerTargetUUID == null) {
                    playerSender.sendMessage(ChatColor.RED + "This player is invalid or has not played before");
                    return;
                }
                playerTarget = Bukkit.getOfflinePlayer(playerTargetUUID);
                if (!playerTarget.hasPlayedBefore()) {
                    playerSender.sendMessage(ChatColor.RED + "This player is invalid or has not played before");
                    return;
                }
            }else {
                // runs if target player is bedrock to detect if the player is an offline player
                Pair<Boolean, OfflinePlayer> bedrockPair = getBedrockOfflinePlayer(targetName);
                if (bedrockPair.getKey()) {
                    playerTarget = bedrockPair.getValue();
                    playerTargetUUID = playerTarget.getUniqueId();
                } else {
                    playerSender.sendMessage(ChatColor.RED + "This player is invalid or has not played before");
                    return;
                }
            }

            Block block = playerSender.getTargetBlock(10);
            if (block != null && Main.isLockable(block.getType()) && (PersistInput.containsOwnerUUID(block, playerSender.getUniqueId()) || shouldBypass(playerSender))) {
                //do chest locking stuff
                if (PersistInput.addOwnerUUID(block, playerTargetUUID)){
                    playerSender.sendMessage(ChatColor.AQUA+playerTarget.getName()+" has been given owner access to this chest");
                    if (playerTarget.isOnline()){
                        playerTarget.getPlayer().sendMessage(ChatColor.AQUA+"You have been given owner access to "+playerSender.getName()+"'s chest");
                    }
                } else {
                    playerSender.sendMessage(ChatColor.RED+playerTarget.getName()+" already has owner access to this chest");
                }

            } else {
                playerSender.sendMessage(ChatColor.RED+"Please make sure you are looking at a locked block and that you have permissions");
            }
        } else {
            sender.sendMessage("Please run this command as a player");
        }
    }

    public static void removePlayers(CommandSender sender, String targetName){
        if (sender instanceof Player) {
            Player playerSender = (Player) sender;

            OfflinePlayer playerTarget = Arrays.stream(Bukkit.getOfflinePlayers())
                    .filter(player -> player.getName().equals(targetName))
                    .findFirst().orElse(null);
            UUID playerTargetUUID = playerTarget == null ? null : playerTarget.getUniqueId();


            if (playerTargetUUID==null){
                playerSender.sendMessage(ChatColor.RED+"Invalid player");
                return;
            }

            Block block = playerSender.getTargetBlock(10);
            if (block != null && Main.isLockable(block.getType()) && (PersistInput.containsOwnerUUID(block, playerSender.getUniqueId()) || shouldBypass(playerSender))) {
                if (PersistInput.containsOwnerUUID(block, playerSender.getUniqueId())) {
                    PersistInput.removeOwnerUUID(block, playerTargetUUID);
                    if (PersistInput.removePlayerUUID(block, playerTargetUUID))
                        playerSender.sendMessage(ChatColor.AQUA + targetName + " can no longer access this chest");
                    else playerSender.sendMessage(ChatColor.RED + targetName + " was not allowed in this chest");
                }
            } else {
                playerSender.sendMessage(ChatColor.RED+"Please make sure you are looking at a lockable block and that you have permissions");
            }
        } else {
            sender.sendMessage("Please run this command as a player");
        }
    }

    public static void removeOwner(CommandSender sender, String targetName){
        if (sender instanceof Player) {
            Player playerSender = (Player) sender;

            OfflinePlayer playerTarget = Arrays.stream(Bukkit.getOfflinePlayers())
                    .filter(player -> player.getName().equals(targetName))
                    .findFirst().orElse(null);
            UUID playerTargetUUID = playerTarget == null ? null : playerTarget.getUniqueId();


            if (playerTargetUUID==null){
                playerSender.sendMessage(ChatColor.RED+"Invalid player");
                return;
            }

            Block block = playerSender.getTargetBlock(10);
            if (block != null && Main.isLockable(block.getType()) && (PersistInput.containsOwnerUUID(block, playerSender.getUniqueId()) || shouldBypass(playerSender))) {
                    if (PersistInput.removeOwnerUUID(block, playerTargetUUID))
                        playerSender.sendMessage(ChatColor.AQUA + targetName + " no longer has owner privileges");
                    else playerSender.sendMessage(ChatColor.RED + targetName + " was not an owner");

            } else {
                playerSender.sendMessage(ChatColor.RED+"Please make sure you are looking at a lockable block and that you have permissions");
            }
        } else {
            sender.sendMessage("Please run this command as a player");
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
                        return true;
                    } else {
                        container.set(BypassKey, PersistentDataType.INTEGER, 0);
                        player.sendMessage(ChatColor.DARK_AQUA + "You are no longer bypassing chest protection");
                        return true;
                    }
                } else {
                    container.set(BypassKey, PersistentDataType.INTEGER, 1);
                    player.sendMessage(ChatColor.DARK_AQUA + "You are now bypassing chest protection");
                    return true;
                }
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
