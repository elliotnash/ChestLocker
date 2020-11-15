package chestlock.chestlock;

import chestlock.chestlock.data.Perms;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDate;
import java.util.*;

import static chestlock.chestlock.commands.CL.hasAdminPerms;
import static chestlock.chestlock.commands.CL.shouldBypass;
import static org.bukkit.Bukkit.getOfflinePlayer;

public class CLListener implements Listener {

    private final HashMap<Player, Long> lastClickTimeMap = new HashMap<>();



    @EventHandler
    public void OnBlockUseEvent(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock!=null && Main.chestManager.isLockable(clickedBlock.getType())) {
            Location clickedLocation = clickedBlock.getLocation();

            Player player = event.getPlayer();

            Long lastClickTime = lastClickTimeMap.get(player);
            boolean notRepeat = (lastClickTime==null||((System.currentTimeMillis()-lastClickTime)>50));

            lastClickTimeMap.put(player, System.currentTimeMillis());
            if (event.getItem() == null && event.getPlayer().isSneaking()) {
                //do chest locking stuff
                if (notRepeat) {
                    if (!Main.chestManager.isLocked(clickedLocation)) {
                        Main.chestManager.addUUID(clickedLocation, player.getUniqueId().toString(), Perms.ADMIN);
                        player.sendMessage(Main.LOCK_SUCCESS);
                    } else if (Main.chestManager.containsUUID(clickedLocation, player.getUniqueId().toString(), Perms.ADMIN) || shouldBypass(player)) {
                        Main.chestManager.removeChest(clickedLocation);
                        player.sendMessage(Main.UNLOCK_SUCCESS);
                    } else
                        player.sendMessage(Main.ALREADY_LOCKED);
                }
                event.setCancelled(true);
                return;
            }
            if (event.getItem() != null && event.getItem().getType() == (Material.BONE) && event.getPlayer().isSneaking() && (Main.chestManager.containsUUID(clickedLocation, player.getUniqueId().toString(), Perms.MEMBER) || hasAdminPerms(event.getPlayer()))) {
                //Do send chest info stuff
                if (Main.chestManager.isLocked(clickedLocation)) {
                    //gets owners
                    if (notRepeat) {
                        player.sendMessage(Main.ALLOWED_OWNERS);
                        List<String> adminUUIDs = Main.chestManager.getUUIDs(clickedLocation, Perms.ADMIN);
                        for (String uuid : adminUUIDs) {
                            String name = getOfflinePlayer(UUID.fromString(uuid)).getName();
                            if (name != null)
                                player.sendMessage(ChatColor.LIGHT_PURPLE + name);
                        }


                        //gets users
                        player.sendMessage(Main.ALLOWED_MEMBERS);
                        List<String> memberUUIDs = Main.chestManager.getUUIDs(clickedLocation, Perms.MEMBER);
                        for (String uuid : memberUUIDs) {
                            String name = getOfflinePlayer(UUID.fromString(uuid)).getName();
                            if (name != null)
                                player.sendMessage(ChatColor.LIGHT_PURPLE + name);
                        }
                    }
                    event.setCancelled(true);
                } else {
                    if (event.getItem() != null && event.getItem().getType() == Material.BONE)
                        if (notRepeat)
                            player.sendMessage(Main.NOT_LOCKED);
                }

            }


            if (Main.chestManager.isLocked(clickedLocation) && (!player.isSneaking())) {
                if (!(Main.chestManager.containsUUID(clickedLocation, player.getUniqueId().toString(), Perms.MEMBER) || shouldBypass(event.getPlayer()))) {
                    if (notRepeat)
                        player.sendMessage(Main.CHEST_IS_LOCKED);


                    event.setCancelled(true);
                }
            }

        }
    }
    @EventHandler
    public void OnPlaceEvent(BlockPlaceEvent event){
        Block blockPlaced = event.getBlock();
        if (blockPlaced.getType()==(Material.HOPPER)){
            Block aboveBlock = blockPlaced.getLocation().add(0,1,0).getBlock();
            if (Main.chestManager.isLockable(aboveBlock.getType())){
                if (Main.chestManager.isLocked(aboveBlock.getLocation())) {
                    if (!Main.chestManager.containsUUID(aboveBlock.getLocation(), event.getPlayer().getUniqueId().toString(), Perms.ADMIN)) {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage(Main.HOPPER_UNDER_CHEST);
                    } else {
                        event.getPlayer().sendMessage(Main.HOPPER_NOT_LOCKED);
                    }
                }
            }
        }
    }
    @EventHandler
    public void OnBreakEvent(BlockBreakEvent event){
        if (Main.chestManager.isLockable(event.getBlock().getType())){
            if (Main.chestManager.isLocked(event.getBlock().getLocation())) {
                if (!(Main.chestManager.containsUUID(event.getBlock().getLocation(), event.getPlayer().getUniqueId().toString(), Perms.ADMIN)
                        || shouldBypass(event.getPlayer()))) {

                    event.setCancelled(true);

                } else {
                    //This is neccessary because of how fucking stupidly double chests are coded in minecraft.
                    //When a double chest is broken it will get the other side and copy the data to that side.
                    if (Main.chestManager.canBeDouble(event.getBlock().getType())) {
                        Chest chest = ((Chest) event.getBlock().getState());
                        if (chest.getInventory() instanceof DoubleChestInventory) {
                            DoubleChestInventory dbChest = (DoubleChestInventory) chest.getInventory();
                            Location otherSide;

                            //get the location of the side that wasn't broken
                            if (event.getBlock().getLocation() == dbChest.getRightSide().getLocation()) {
                                otherSide = dbChest.getLeftSide().getLocation();
                            } else {
                                otherSide = dbChest.getRightSide().getLocation();
                            }

                            HashMap<String, LinkedList<String>> tempMap = Main.chestManager.getChest(event.getBlock().getLocation(), false);
                            Main.chestManager.removeChest(event.getBlock().getLocation(), false);
                            Main.chestManager.setChest(otherSide, tempMap, false);

                        } else {
                            Main.chestManager.removeChest(event.getBlock().getLocation());
                        }
                    }
                }
            }

        }
    }
    @EventHandler
    public void OnBlockExplosionEvent(BlockExplodeEvent event){
        for (int i = 0; i < event.blockList().size(); i++){
            event.blockList().removeIf(this::shouldExplode);
        }

    }
    @EventHandler
    public void OnEntityExplosionEvent(EntityExplodeEvent event){
        for (int i = 0; i < event.blockList().size(); i++){
            event.blockList().removeIf(this::shouldExplode);
        }
    }

    public boolean shouldExplode(Block block){
        if (Main.chestManager.isLockable(block.getType()))
            return Main.chestManager.isLocked(block.getLocation());
        return false;
    }

    @EventHandler
    public void onItemDropEvent(BlockDropItemEvent event){

    }

    @EventHandler
    public void InventoryMoveItem(InventoryMoveItemEvent event) {
        if (event.getDestination().getHolder() instanceof HopperMinecart){
            Location loc = event.getSource().getLocation();
            if (loc == null)
                return;
            Block lockedBlock = loc.getBlock();
            if (Main.chestManager.isLockable(lockedBlock.getType())){
                if (Main.chestManager.isLocked(loc)) {
                    event.setCancelled(true);
                }
            }
        }
    }


    @EventHandler
    public void onBlockPlace (BlockPlaceEvent event){
        if (Main.chestManager.isLocked(event.getBlock().getLocation())) {
            Main.chestManager.removeChest(event.getBlock().getLocation());
        }
        if (Main.chestManager.canBeDouble(event.getBlock().getType()))
            new BukkitRunnable() {
                @Override
                public void run() {
                    System.out.println("RUNNABLE RUNNING");
                    Chest chest = ((Chest) event.getBlock().getState());
                    if (chest.getInventory() instanceof DoubleChestInventory){
                        System.out.println("IT IS INSTANCE OF IT");
                        DoubleChestInventory dbChest = (DoubleChestInventory) chest.getInventory();
                        Location otherSide;
                        Location invSide = chest.getInventory().getLocation().toBlockLocation();

                        //get the location of the side that wasn't placed
                        if (event.getBlock().getLocation() == dbChest.getRightSide().getLocation()){
                            otherSide = dbChest.getLeftSide().getLocation();
                        } else {
                            otherSide = dbChest.getRightSide().getLocation();
                        }

                        System.out.println(otherSide);
                        System.out.println(invSide);

                        if (Main.chestManager.isLocked(otherSide, false)){
                            System.out.println("OTHER SIDE IS LOCKED");
                            HashMap<String, LinkedList<String>> tempMap = Main.chestManager.getChest(otherSide, false);
                            System.out.println(tempMap);
                            Main.chestManager.removeChest(otherSide, false).removeChest(event.getBlock().getLocation(), false);
                            Main.chestManager.setChest(invSide, tempMap, false);
                        }


                    }
                }
            }.runTaskLater(Main.getPlugin(), 1);
    }


}
