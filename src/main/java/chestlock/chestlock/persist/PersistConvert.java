package chestlock.chestlock.persist;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.LinkedList;
import java.util.UUID;

public class PersistConvert {
    public static void setPDE(BlockState blockState, NamespacedKey key, LinkedList<UUID> uuids){
        blockState.update();
        PersistentDataContainer container = ((TileState) blockState).getPersistentDataContainer();
        long[] longs = new long[uuids.size()*2];
        for (int i = 0;i < uuids.size();i++) {
            longs[i * 2] = uuids.get(i).getMostSignificantBits();
            longs[(i * 2) + 1] = uuids.get(i).getLeastSignificantBits();
        }

        container.set(key, PersistentDataType.LONG_ARRAY, longs);

        blockState.update();
    }
    public static LinkedList<UUID> getPDE(BlockState blockState, NamespacedKey key){
        blockState.update();
        PersistentDataContainer container = ((TileState) blockState).getPersistentDataContainer();


        long[] primitive = container.get(key, PersistentDataType.LONG_ARRAY);

        LinkedList<UUID> uuid = new LinkedList<>();

        if (primitive==null)
            return uuid;

        for (int i = 0;i < primitive.length;i+=2) {
            uuid.add(new UUID(primitive[i], primitive[i+1]));
        }
        return uuid;
    }
}
