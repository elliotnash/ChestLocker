package chestlock.chestlock.persist;

import chestlock.chestlock.Main;
import chestlock.chestlock.persist.PersistConvert;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.TileState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;


import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class PersistInput {
    private static final NamespacedKey UUIDKey = new NamespacedKey(Main.getPlugin(), "UUIDS");
    Plugin plugin;
    private static final NamespacedKey OWNERKey = new NamespacedKey(Main.getPlugin(), "OWNERUUID");

    public static boolean addOwnerUUID(Block blockToSet, UUID playerUUID){
        //gets true block of double chests
        if (Main.canBeDouble(blockToSet.getType()))
            blockToSet = ((Chest) blockToSet.getState()).getInventory().getLocation().getBlock();

        //check if block is already locked
        LinkedList<UUID> uuidList = PersistConvert.getPDE(blockToSet.getState(), OWNERKey);
        if (!uuidList.isEmpty()){
            if (!uuidList.contains(playerUUID)){
                uuidList.add(playerUUID);
                PersistConvert.setPDE(blockToSet.getState(), OWNERKey, uuidList);;
                addPlayerUUID(blockToSet, playerUUID);
                return true;
            } else {
                return false;
            }
        } else {
            uuidList = new LinkedList<>();
            uuidList.add(playerUUID);


            PersistConvert.setPDE(blockToSet.getState(), OWNERKey, uuidList);
            addPlayerUUID(blockToSet, playerUUID);
            return true;
        }
    }

    public static boolean addPlayerUUID(Block blockToSet, UUID playerUUID){
        //gets true block of double chests
        if (Main.canBeDouble(blockToSet.getType()))
            blockToSet = ((Chest) blockToSet.getState()).getInventory().getLocation().getBlock();

        //check if block is already locked
        LinkedList<UUID> uuidList = PersistConvert.getPDE(blockToSet.getState(), UUIDKey);
        if (!uuidList.isEmpty()){
            if (!uuidList.contains(playerUUID)){
                uuidList.add(playerUUID);
                PersistConvert.setPDE(blockToSet.getState(), UUIDKey, uuidList);
                return true;
            } else {
                return false;
            }
        } else {
            uuidList = new LinkedList<>();
            uuidList.add(playerUUID);
            PersistConvert.setPDE(blockToSet.getState(), UUIDKey, uuidList);
            return true;
        }
    }

    public static boolean removePlayerUUID(Block blockToRemove, UUID playerUUID){
        //gets true block of double chests
        if (Main.canBeDouble(blockToRemove.getType()))
            blockToRemove = ((Chest) blockToRemove.getState()).getInventory().getLocation().getBlock();

        LinkedList<UUID> uuidList = PersistConvert.getPDE(blockToRemove.getState(), UUIDKey);
        if (!uuidList.isEmpty()) {
            boolean removed = uuidList.remove(playerUUID);
            //Makes UUID[] and transfers all elements minus the one to remove
            if (!removed)
                return false;
            //If you've gotten this far, the player's UUID was on the array and is now removed
            //Put new array back in
            PersistConvert.setPDE(blockToRemove.getState(), UUIDKey, uuidList);
            return true;
        }
        return false;

    }
    public static boolean removeOwnerUUID(Block blockToRemove, UUID playerUUID){
        //gets true block of double chests
        if (Main.canBeDouble(blockToRemove.getType()))
            blockToRemove = ((Chest) blockToRemove.getState()).getInventory().getLocation().getBlock();

        LinkedList<UUID> uuidList = PersistConvert.getPDE(blockToRemove.getState(), OWNERKey);
        if (!uuidList.isEmpty()) {
            boolean removed = uuidList.remove(playerUUID);
            //Makes UUID[] and transfers all elements minus the one to remove
            if (!removed)
                return false;
            //If you've gotten this far, the player's UUID was on the array and is now removed
            //Put new array back in
            PersistConvert.setPDE(blockToRemove.getState(), OWNERKey, uuidList);
            return true;
        }
        return false;

    }

    public static LinkedList<UUID> getPlayerUUIDS(Block blockToSearch){
        //gets true block of double chests
        if (Main.canBeDouble(blockToSearch.getType()))
            blockToSearch = ((Chest) blockToSearch.getState()).getInventory().getLocation().getBlock();

        return PersistConvert.getPDE(blockToSearch.getState(), UUIDKey);
    }

    public static LinkedList<UUID> getOwnerUUIDS(Block blockToSearch){
        //gets true block of double chests
        if (Main.canBeDouble(blockToSearch.getType()))
            blockToSearch = ((Chest) blockToSearch.getState()).getInventory().getLocation().getBlock();

        return PersistConvert.getPDE(blockToSearch.getState(), OWNERKey);
    }

    public static void setPlayerUUIDS(Block blockToSet, LinkedList<UUID> uuids){
        //gets true block of double chests
        if (Main.canBeDouble(blockToSet.getType()))
            blockToSet = ((Chest) blockToSet.getState()).getInventory().getLocation().getBlock();

        PersistConvert.setPDE(blockToSet.getState(), OWNERKey, uuids);
    }

    public static void setOwnerUUIDS(Block blockToSet, LinkedList<UUID> uuids){
        //gets true block of double chests
        if (Main.canBeDouble(blockToSet.getType()))
            blockToSet = ((Chest) blockToSet.getState()).getInventory().getLocation().getBlock();

        PersistConvert.setPDE(blockToSet.getState(), OWNERKey, uuids);
    }

    public static boolean isLocked(Block blockToCheck) {
        //gets true block of double chests
        if (Main.canBeDouble(blockToCheck.getType()))
            blockToCheck = ((Chest) blockToCheck.getState()).getInventory().getLocation().getBlock();
        //updates block state
        blockToCheck.getState().update();
        //gets data container
        PersistentDataContainer container = ((TileState) blockToCheck.getState(false)).getPersistentDataContainer();
        return container.has(UUIDKey, PersistentDataType.LONG_ARRAY);
    }

    public static void unlockChest(Block blockToRemove){
        //gets true block of double chests
        if (Main.canBeDouble(blockToRemove.getType()))
            blockToRemove = ((Chest) blockToRemove.getState()).getInventory().getLocation().getBlock();
        //updates block state because it's needed? idk seems to fix my pages upon pages of errors
        blockToRemove.getState().update();
        //gets data holder of block to add to
        TileState blockState = (TileState) blockToRemove.getState();
        //gets data container
        PersistentDataContainer container = blockState.getPersistentDataContainer();
        if (container.has(UUIDKey, PersistentDataType.LONG_ARRAY)) {
            container.remove(UUIDKey);
        }
        if (container.has(OWNERKey, PersistentDataType.LONG_ARRAY)){
            container.remove(OWNERKey);
        }
        blockState.update();
    }

    @SuppressWarnings("all")
    public static boolean containsUUID(Block blockToSearch, UUID uuidToSearch){
        //gets true block of double chests
        if (Main.canBeDouble(blockToSearch.getType()))
            blockToSearch = ((Chest) blockToSearch.getState()).getInventory().getLocation().getBlock();

        LinkedList<UUID> uuidList = PersistConvert.getPDE(blockToSearch.getState(), UUIDKey);
        if (uuidList!=null){
            return uuidList.contains(uuidToSearch);
        }

        return false;
    }
    @SuppressWarnings("all")
    public static boolean containsOwnerUUID(Block blockToSearch, UUID uuidToSearch){
        //gets true block of double chests
        if (Main.canBeDouble(blockToSearch.getType()))
            blockToSearch = ((Chest) blockToSearch.getState()).getInventory().getLocation().getBlock();

        LinkedList<UUID> uuidList = PersistConvert.getPDE(blockToSearch.getState(), OWNERKey);
        if (uuidList!=null){
            return uuidList.contains(uuidToSearch);
        }

        return false;
    }

    public static void setShulkerOwnerPDC(ItemStack itemStack, LinkedList<UUID> uuids){
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
        container.set(OWNERKey, PersistConvert.uuidType, uuids);

    }

    public static void setShulkerPlayerPDC(ItemStack itemStack, LinkedList<UUID> uuids){
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
        container.set(UUIDKey, PersistConvert.uuidType, uuids);
    }

    public static LinkedList<UUID> getShulkerOwnerPDC(ItemStack itemStack){
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
        return container.get(OWNERKey, PersistConvert.uuidType);
    }

    public static LinkedList<UUID> getShulkerPlayerPDC(ItemStack itemStack){
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
        return container.get(UUIDKey, PersistConvert.uuidType);
    }


}

