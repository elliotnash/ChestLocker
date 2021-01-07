package chestlock.chestlock.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.inventory.DoubleChestInventory;
import sun.awt.image.ImageWatched;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedList;

@SuppressWarnings("unchecked")
public class chestManager {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final HashMap<String, HashMap<String, HashMap<String, LinkedList<String>>>> chestMap = new HashMap<>();

    public HashMap<String, HashMap<String, HashMap<String, LinkedList<String>>>> getMap(){
        return chestMap;
    }


    //Runs on initialization, loads the json file into the hash map
    public void loadJson(){
        Type hashType = new TypeToken<HashMap<String, HashMap<String, LinkedList<String>>>>(){}.getType();
        for (World world : Bukkit.getWorlds()) {
            try (FileReader reader = new FileReader(world.getName()+"/chest.locks")) {
                HashMap<String, HashMap<String, LinkedList<String>>> tempMap = gson.fromJson(reader, hashType);
                if (tempMap == null)
                    tempMap = new HashMap<>();
                chestMap.put(world.getName(), tempMap);

            } catch (FileNotFoundException ignored) {

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    //Writes the current map to the json file
    private void writeHash(String worldName){
        if (!chestMap.containsKey(worldName)){
            return;
        }
        HashMap<String, HashMap<String, LinkedList<String>>> worldMap = chestMap.get(worldName);

        try (FileWriter file = new FileWriter(worldName+"/chest.locks")) {

            file.write(gson.toJson(worldMap));

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    //default getChest to search double chests
    public HashMap<String, LinkedList<String>> getChest(Location location){
        return getChest(location, true);
    }
    //returns a HashMap with a each perm level with a list of UUIDs
    public HashMap<String, LinkedList<String>> getChest(Location location, Boolean searchDC){
        String worldName = getWorldName(location);
        String chestLocation = getXYZ(location, searchDC);

        if (!chestMap.containsKey(worldName) || !chestMap.get(worldName).containsKey(chestLocation)){
            return null;
        }
        return chestMap.get(worldName).get(chestLocation);
    }


    //returns a list from the given perm
    public LinkedList<String> getUUIDs(Location location, String permissionLevel){
        String worldName = getWorldName(location);
        String chestLocation = getXYZ(location);

        if (!chestMap.containsKey(worldName) || !chestMap.get(worldName).containsKey(chestLocation)){
            return new LinkedList<>();
        }
        return chestMap.get(worldName).get(chestLocation).get(permissionLevel);
    }


    //sets a chest hashmap to the given hashmap
    public chestManager setChest(Location location, HashMap<String, LinkedList<String>> chestMap, Boolean searchDC){
        String worldName = getWorldName(location);
        String chestLocation = getXYZ(location, searchDC);

        //Get the HashMap for the specified world if it exists, or make a new blank one if not
        HashMap<String, HashMap<String, LinkedList<String>>> worldMap = new HashMap<>();
        if (this.chestMap.containsKey(worldName))
            worldMap = this.chestMap.get(worldName);

        worldMap.put(chestLocation, chestMap);
        this.chestMap.put(worldName, worldMap);

        writeHash(worldName);
        return this;
    }


    //sets the uuid of a given perm in a chest
    public chestManager setUUIDs(Location location, LinkedList<String> UUIDS, String permissionLevel){
        String worldName = getWorldName(location);
        String chestLocation = getXYZ(location);

        //Get the HashMap for the specified world if it exists, or make a new blank one if not
        HashMap<String, HashMap<String, LinkedList<String>>> worldMap = new HashMap<>();
        if (chestMap.containsKey(worldName))
            worldMap = chestMap.get(worldName);
        //Get hashmap for the chest
        HashMap<String, LinkedList<String>> newChestMap = new HashMap<>();
        if (worldMap.containsKey(chestLocation))
            newChestMap = worldMap.get(chestLocation);

        //put uuid string in the permission level
        newChestMap.put(permissionLevel, UUIDS);
        //put that chest in the world
        worldMap.put(chestLocation, newChestMap);
        //put the world in the master list
        chestMap.put(worldName, worldMap);

        writeHash(worldName);
        return this;
    }


    //default searchDC to true
    public chestManager removeChest(Location location){
        return removeChest(location, true);
    }
    //Removes a chest from the ChestMap
    public chestManager removeChest(Location location, Boolean searchDC){
        String worldName = getWorldName(location);
        String chestLocation = getXYZ(location, searchDC);

        if (!chestMap.containsKey(worldName))
            return this;
        HashMap<String, HashMap<String, LinkedList<String>>> worldMap = chestMap.get(worldName);
        worldMap.remove(chestLocation);

        writeHash(worldName);
        return this;
    }





    //Adds the specified uuid to the chest
    public Boolean addUUID(Location location, String UUID, String permissionLevel){
        if (permissionLevel.equals(Perms.ADMIN)){
            addUUID(location, UUID, Perms.MEMBER);
        }


        String worldName = getWorldName(location);
        String chestLocation = getXYZ(location);

        //Get the HashMap for the specified world if it exists, or make a new blank one if not
        HashMap<String, HashMap<String, LinkedList<String>>> worldMap = new HashMap<>();
        if (chestMap.containsKey(worldName))
            worldMap = chestMap.get(worldName);
        //Get hashmap for the chest
        HashMap<String, LinkedList<String>> newChestMap = new HashMap<>();
        if (worldMap.containsKey(chestLocation))
            newChestMap = worldMap.get(chestLocation);
        //gets uuid list of the specified permission level
        LinkedList<String> UUIDs = new LinkedList<>();
        if (newChestMap.containsKey(permissionLevel))
            UUIDs = newChestMap.get(permissionLevel);

        //Add given uuid to list if not exists
        if (!UUIDs.contains(UUID))
            UUIDs.add(UUID);
        else
            return false;


        //put uuid string in the permission level
        newChestMap.put(permissionLevel, UUIDs);
        //put that chest in the world
        worldMap.put(chestLocation, newChestMap);
        //put the world in the master list
        chestMap.put(worldName, worldMap);

        writeHash(worldName);
        return true;
    }


    //Removes a uuid from a chest
    public Boolean removeUUID(Location location, String UUID, String permissionLevel){
        if (permissionLevel.equals(Perms.MEMBER)){
            addUUID(location, UUID, Perms.ADMIN);
        }

        String worldName = getWorldName(location);
        String chestLocation = getXYZ(location);

        if ( !( chestMap.containsKey(worldName) && chestMap.get(worldName).containsKey(chestLocation) ) )
            return false;

        //Get current hashmaps
        HashMap<String, HashMap<String, LinkedList<String>>> worldMap = chestMap.get(worldName);
        HashMap<String, LinkedList<String>> newChestMap = worldMap.get(chestLocation);

        if ( !( newChestMap.containsKey(permissionLevel) && newChestMap.get(permissionLevel).contains(UUID) ) )
            return false;
        //get uuid list
        LinkedList<String> UUIDs = newChestMap.get(permissionLevel);
        //remove given uuid
        UUIDs.remove(UUID);


        //put uuid string in the permission level
        newChestMap.put(permissionLevel, UUIDs);
        //put that chest in the world
        worldMap.put(chestLocation, newChestMap);
        //put the world in the master list
        chestMap.put(worldName, worldMap);

        writeHash(worldName);
        return true;
    }


    //Checks if the given chest contains a uuid
    public Boolean containsUUID(Location location, String UUID, String permissionLevel){
        String worldName = getWorldName(location);
        String chestLocation = getXYZ(location);

        return chestMap.containsKey(worldName)
                && chestMap.get(worldName).containsKey(chestLocation)
                && chestMap.get(worldName).get(chestLocation).containsKey(permissionLevel)
                && chestMap.get(worldName).get(chestLocation).get(permissionLevel).contains(UUID);
    }

    //default searchDC to true
    public Boolean isLocked(Location location){
        return isLocked(location, true);
    }
    //checks if given chest is locked
    public Boolean isLocked(Location location, Boolean searchDC){
        String worldName = getWorldName(location);
        String chestLocation = getXYZ(location, searchDC);

        return chestMap.containsKey(worldName)
                && chestMap.get(worldName).containsKey(chestLocation);
    }




    //converts a location to a String of format X, Y, Z
    private String getXYZ(Location location){
        location = getDoubleChestLocation(location);

        return location.getBlockX()+", "+location.getBlockY()+", "+location.getBlockZ();
    }
    //converts location to world name
    private String getWorldName(Location location){
        return location.getWorld().getName();
    }

    //this version will not get of double chest
    private String getXYZ(Location location, Boolean searchDC){
        if (searchDC) {
            return getXYZ(location);
        }
        return location.getBlockX()+", "+location.getBlockY()+", "+location.getBlockZ();
    }

    //gets true block of double chest
    private Location getDoubleChestLocation(Location location){
        System.out.println("YOU ARE GETTING DOUBLE CHEST LOCATION RIGHT NOW");
        if (canBeDouble(location.getBlock().getType())) {
            Chest chest = ((Chest) location.getBlock().getState());
            if (chest.getInventory() instanceof DoubleChestInventory){
                return chest.getInventory().getLocation();
            }
        }
        return location;
    }


    public boolean isLockable(Material mat){
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

    public boolean isShulker(Material mat){
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

    public boolean canBeDouble(Material mat){
        return mat == Material.CHEST
                || mat == Material.TRAPPED_CHEST;
    }

    public boolean isDrainable(Material mat){
        return mat == Material.CHEST
                || mat == Material.TRAPPED_CHEST
                || mat == Material.BARREL
                || mat == Material.DISPENSER
                || mat == Material.DROPPER;
    }

}
