package chestlock.chestlock.persist;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.UUID;

public class PersistConvert {
    public static void setPDE(BlockState blockState, NamespacedKey key, LinkedList<UUID> uuids){
        blockState.update();
        PersistentDataContainer container = ((TileState) blockState).getPersistentDataContainer();

        container.set(key, uuidType, uuids);

        blockState.update();
    }
    public static LinkedList<UUID> getPDE(BlockState blockState, NamespacedKey key){
        blockState.update();
        PersistentDataContainer container = ((TileState) blockState).getPersistentDataContainer();


        LinkedList<UUID> list = container.get(key, uuidType);
        if (list==null){
            list = new LinkedList<>();
        }
        return list;
    }

    private static final Class<LinkedList<UUID>> uuidListType = (Class<LinkedList<UUID>>) new LinkedList<UUID>().getClass();
    public static final PersistentDataType<long[], LinkedList<UUID>> uuidType = new PersistentDataType<long[], LinkedList<UUID>>() {
        @Override
        @NotNull
        public Class<long[]> getPrimitiveType() {
            return long[].class;
        }

        @Override
        public @NotNull Class<LinkedList<UUID>> getComplexType() {
            return uuidListType;
        }


        @Override
        public long @NotNull [] toPrimitive(@NotNull LinkedList<UUID> uuids, @NotNull PersistentDataAdapterContext context) {
            long[] longs = new long[uuids.size()*2];
            for (int i = 0;i < uuids.size();i++) {
                longs[i * 2] = uuids.get(i).getMostSignificantBits();
                longs[(i * 2) + 1] = uuids.get(i).getLeastSignificantBits();
            }

            return longs;
        }

        @Override
        public @NotNull LinkedList<UUID> fromPrimitive(long @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
            LinkedList<UUID> uuid = new LinkedList<>();

            for (int i = 0;i < primitive.length;i+=2) {
                uuid.add(new UUID(primitive[i], primitive[i+1]));
            }
            return uuid;
        }
    };
}
