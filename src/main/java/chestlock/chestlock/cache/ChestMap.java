package chestlock.chestlock.cache;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

@SuppressWarnings("unchecked")
public class ChestMap {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private HashMap<String, HashMap<String, HashMap<String, LinkedList<String>>>> chestMap = new HashMap<>();

    {
        for (World world : Bukkit.getWorlds()) {
            try (FileReader reader = new FileReader(world.getName()+"/chest.locks")) {
                chestMap.put(world.getName(), gson.fromJson(reader, HashMap.class));

            } catch (FileNotFoundException ignored) {

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
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
    public HashMap<String, LinkedList<String>> getChest(String worldName, String chestLocation){
        if (!chestMap.containsKey(worldName) || !chestMap.get(worldName).containsKey(chestLocation)){
            return null;
        }
        return chestMap.get(worldName).get(chestLocation);
    }
    public void setChest(String worldName, String chestLocation, HashMap<String, LinkedList<String>> chestMap){
        //Get the HashMap for the specified world if it exists, or make a new blank one if not
        HashMap<String, HashMap<String, LinkedList<String>>> worldMap = new HashMap<>();
        if (this.chestMap.containsKey(worldName))
            worldMap = this.chestMap.get(worldName);

        worldMap.put(chestLocation, chestMap);
        this.chestMap.put(worldName, worldMap);

        writeHash(worldName);
    }
    public void removeChest(String worldName, String chestLocation){
        if (!chestMap.containsKey(worldName))
            return;
        HashMap<String, HashMap<String, LinkedList<String>>> worldMap = chestMap.get(worldName);
        worldMap.remove(chestLocation);

        writeHash(worldName);
    }
    public String getXYZ(Location location){
        return location.getBlockX()+", "+location.getBlockY()+", "+location.getBlockZ();
    }
}
