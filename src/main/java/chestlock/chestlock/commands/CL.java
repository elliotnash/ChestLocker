package chestlock.chestlock.commands;

import java.util.*;

import chestlock.chestlock.Main;
import chestlock.chestlock.Vars;
import chestlock.chestlock.persist.PersistConvert;
import chestlock.chestlock.persist.PersistInput;
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
                if (args[0].equalsIgnoreCase("add")||args[0].equalsIgnoreCase("remove")){
                    List<Player> players = (List<Player>) getOnlinePlayers();
                    List<String> playerNames = new LinkedList<>();
                    playerNames.add("owner");
                    for (Player player : players){
                        playerNames.add(player.getName());
                    }
                    return StringUtil.copyPartialMatches(args[1], playerNames, new ArrayList<>());
                }
            }
            if (args.length == 3){
                if (args[1].equalsIgnoreCase("owner")){
                    List<Player> players = (List<Player>) getOnlinePlayers();
                    List<String> playerNames = new LinkedList<>();
                    for (Player player : players){
                        playerNames.add(player.getName());
                    }
                    return StringUtil.copyPartialMatches(args[2], playerNames, new ArrayList<>());
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
            return false;
            case 2: if (args[0].equalsIgnoreCase("add")){
                addPlayer(sender, args[1]);
                return true;
            }
            if (args[0].equalsIgnoreCase("remove")){
                removePlayers(sender, args[1]);
                return true;
            } return false;
        }
        return false;
    }

    public static void listPlayers(CommandSender sender){
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Block block = player.getTargetBlock(10);
            if (block != null && Main.isLockable(block.getType()) && (PersistInput.containsUUID(block, player.getUniqueId()) || hasAdminPerms(player) )) {
                //Do send chest info stuff
                List<UUID> uuids = PersistInput.getPlayerUUIDS(block);
                if (!uuids.isEmpty()) {
                    player.sendMessage(ChatColor.DARK_PURPLE + "The following players are allowed to open this chest:");
                    for (UUID uuid : uuids) {
                        String name = getOfflinePlayer(uuid).getName();
                        if (name != null)
                            player.sendMessage(ChatColor.LIGHT_PURPLE + name);
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

    public static void addPlayer(CommandSender sender, String targetName){
        if (sender instanceof Player) {
            Player playerSender = (Player) sender;
            UUID playerTargetUUID = getPlayerUniqueId(targetName);
            if (playerTargetUUID==null){
                playerSender.sendMessage(ChatColor.RED+"Invalid player");
                return;
            }
            OfflinePlayer playerTarget = Bukkit.getOfflinePlayer(playerTargetUUID);
            Block block = playerSender.getTargetBlock(10);
            if (block != null && Main.isLockable(block.getType()) && (PersistInput.containsOwnerUUID(block, playerSender.getUniqueId()) || shouldBypass(playerSender))) {
                //do chest locking stuff
                if (PersistInput.addPlayerUUID(block, playerTargetUUID)){
                    playerSender.sendMessage(ChatColor.AQUA+playerTarget.getName()+" has been given access to this chest");
                    if (playerTarget.isOnline()){
                        playerTarget.getPlayer().sendMessage(ChatColor.AQUA+"You have been given access to "+playerSender+"'s chest");
                    }
                } else {
                    playerSender.sendMessage(ChatColor.AQUA+playerTarget.getName()+" already has access to this chest");
                }

            } else {
                playerSender.sendMessage(ChatColor.RED+"Please make sure you are looking at a lockable block and that you have permissions");
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
            if (block != null && Main.isLockable(block.getType()) && (PersistInput.containsUUID(block, playerSender.getUniqueId()) || shouldBypass(playerSender))) {
                //do chest locking stuff
                List<UUID> storedUUID = PersistInput.getPlayerUUIDS(block);
                if (PersistInput.containsOwnerUUID(block, playerSender.getUniqueId())) {

                    if (PersistInput.removePlayerUUID(block, playerTargetUUID))
                        playerSender.sendMessage(ChatColor.AQUA + targetName + " can no longer access this chest");
                    else playerSender.sendMessage(ChatColor.AQUA + targetName + " was not allowed in this chest");
                } else
                if (PersistInput.isLocked(block))
                    playerSender.sendMessage(ChatColor.RED + "Only the owner can add players");
                else playerSender.sendMessage(ChatColor.AQUA + "This chest isn't locked");
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
                if (container.has(BypassKey, PersistentDataType.INTEGER)) {
                    if (container.get(BypassKey, PersistentDataType.INTEGER) == 0) {
                        container.set(BypassKey, PersistentDataType.INTEGER, 1);
                        player.sendMessage(ChatColor.DARK_AQUA + "You are now bypassing chest protection");
                        return true;
                    } else {
                        container.set(BypassKey, PersistentDataType.INTEGER, 0);
                        player.sendMessage(ChatColor.DARK_AQUA + "You are no longer bypassing chest protection");
                        return true;
                    }
                } else {
                    container.set(BypassKey, PersistentDataType.INTEGER, 0);
                    player.sendMessage(ChatColor.DARK_AQUA + "You are no longer bypassing chest protection");
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
        if (hasAdminPerms(player) && container.has(BypassKey, PersistentDataType.INTEGER)) {
            if (container.get(BypassKey, PersistentDataType.INTEGER) == 1) {
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
}
