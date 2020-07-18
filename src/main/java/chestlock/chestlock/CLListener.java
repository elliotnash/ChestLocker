package chestlock.chestlock;

import chestlock.chestlock.persist.PersistConvert;
import chestlock.chestlock.persist.PersistInput;
import io.papermc.lib.PaperLib;
import io.papermc.lib.features.blockstatesnapshot.BlockStateSnapshotResult;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.*;

import static chestlock.chestlock.commands.CL.hasAdminPerms;
import static chestlock.chestlock.commands.CL.shouldBypass;
import static org.bukkit.Bukkit.broadcastMessage;
import static org.bukkit.Bukkit.getOfflinePlayer;

public class CLListener implements Listener {

    private static Map<Player, Long> lastClickTimeMap = new HashMap<Player, Long>();

    @EventHandler
    public void OnBlockUseEvent(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
            if (clickedBlock!=null && Main.isLockable(clickedBlock.getType())) {
                Player player = event.getPlayer();
                List<UUID> uuids = PersistInput.getPlayerUUIDS(clickedBlock);

                Long lastClickTime = lastClickTimeMap.get(player);
                boolean notRepeat = (lastClickTime==null||((System.currentTimeMillis()-lastClickTime)>50));

                lastClickTimeMap.put(player, System.currentTimeMillis());
                if (event.getItem() == null && event.getPlayer().isSneaking()) {
                    //do chest locking stuff
                    if (notRepeat) {
                        if (uuids.isEmpty()) {
                            PersistInput.addOwnerUUID(clickedBlock, player.getUniqueId());
                            player.sendMessage(ChatColor.AQUA + "Chest locked");
                        } else if (PersistInput.containsOwnerUUID(clickedBlock, player.getUniqueId()) || shouldBypass(player)) {
                            PersistInput.unlockChest(clickedBlock);
                            player.sendMessage(ChatColor.AQUA + "Chest unlocked");
                        } else
                            player.sendMessage(ChatColor.RED + "This chest is already locked");
                    }
                    event.setCancelled(true);
                    return;
                }
                if (event.getItem() != null && event.getItem().getType() == (Material.BONE) && event.getPlayer().isSneaking() && (PersistInput.containsUUID(clickedBlock, player.getUniqueId()) || hasAdminPerms(event.getPlayer()))) {
                    //Do send chest info stuff
                    if (!uuids.isEmpty()) {
                        //gets owners
                        if (notRepeat) {
                            player.sendMessage(ChatColor.GOLD + "The following players are owners of this chest:");
                            List<UUID> ownerUUID = PersistInput.getOwnerUUIDS(clickedBlock);
                            for (UUID uuid : ownerUUID) {
                                String name = getOfflinePlayer(uuid).getName();
                                if (name != null)
                                    player.sendMessage(ChatColor.LIGHT_PURPLE + name);
                            }


                            //gets users
                            player.sendMessage(ChatColor.GOLD + "The following players are allowed to open this chest:");
                            for (UUID uuid : uuids) {
                                String name = getOfflinePlayer(uuid).getName();
                                if (name != null)
                                    player.sendMessage(ChatColor.LIGHT_PURPLE + name);
                            }
                        }
                        event.setCancelled(true);
                    } else {
                        if (event.getItem() != null && event.getItem().getType() == Material.BONE)
                            if (notRepeat)
                                player.sendMessage(ChatColor.DARK_PURPLE + "This chest isn't locked");
                    }
                }


                if (!uuids.isEmpty()&&(!player.isSneaking())) {
                    if (!(PersistInput.containsUUID(clickedBlock, player.getUniqueId()) || shouldBypass(event.getPlayer()))) {
                        if (notRepeat)
                            player.sendMessage(ChatColor.RED + "Chest is locked!");


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
            if (Main.isLockable(aboveBlock.getType())){
                if (PersistInput.isLocked(aboveBlock)) {
                    if (!PersistInput.containsOwnerUUID(aboveBlock, event.getPlayer().getUniqueId())) {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage(ChatColor.RED + "You can't place hoppers under a locked chest you don't have access to");
                    } else {
                        event.getPlayer().sendMessage(ChatColor.AQUA + "Remember this hopper is not locked.");
                    }
                }
            }
        }
    }
    @EventHandler
    public void OnBreakEvent(BlockBreakEvent event){
        if (Main.isLockable(event.getBlock().getType())){
            if (PersistInput.isLocked(event.getBlock())) {
                if (!(PersistInput.containsOwnerUUID(event.getBlock(), event.getPlayer().getUniqueId()) || shouldBypass(event.getPlayer()))) {
                    event.setCancelled(true);
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
        if (Main.isLockable(block.getType()))
            return PersistInput.isLocked(block);
        return false;
    }
    
    //add meta data to shulker item on drop
    @EventHandler
    public void onItemDropEvent(BlockDropItemEvent event){
        BlockState brokenBlock = event.getBlockState();
        if (brokenBlock.getType()==Material.SHULKER_BOX){
            if (PersistInput.isLockedState(brokenBlock)) {
                //here is where we transfer meta data
                //gets uuids from shulker before broken
                if (event.getItems().size()==1) {
                    List<String> lore = new ArrayList<>(1);
                    lore.add("locked");
                    ItemStack toReturn =
                            PersistInput.setShulkerPlayerPDC(
                                    PersistInput.setShulkerOwnerPDC(event.getItems().get(0).getItemStack(),
                                            PersistInput.getOwnerUUIDSState(brokenBlock)),
                                    PersistInput.getPlayerUUIDSState(brokenBlock));
                    toReturn.setLore(lore);
                    event.getItems().get(0).setItemStack(toReturn);
                }
            }

        }
    }

    @EventHandler
    public void onBlockPlace (BlockPlaceEvent event){
        if (event.getBlock().getType()==Material.SHULKER_BOX){
            LinkedList<UUID> ownerList = PersistInput.getShulkerOwnerPDC(event.getItemInHand());
            LinkedList<UUID> playerList = PersistInput.getShulkerPlayerPDC(event.getItemInHand());
            if (!ownerList.isEmpty()){
                BlockStateSnapshotResult blockStateSnapshotResult = PaperLib.getBlockState(event.getBlockPlaced(), true);
                //then do pdc transfer


                PersistInput.setUUIDS(blockStateSnapshotResult.getState(), ownerList, playerList);
            }
        }
    }


}
