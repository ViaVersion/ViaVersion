package us.myles.ViaVersion.bungee.storage;

import us.myles.ViaVersion.api.Pair;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.HashMap;
import java.util.Map;

public class BungeeBlockConnectionData extends StoredObject {
    private Map<Pair<Integer, Integer>, Map<Position, Integer>> blockStorage = new HashMap<>();

    public BungeeBlockConnectionData(UserConnection user) {
        super(user);
    }

    public void store(Position position, int blockState) {
        Pair pair = getPair(position);
        Map<Position, Integer> map = getChunkMap(pair);
        map.put(position, blockState);
    }

    public int get(Position position) {
        Pair pair = getPair(position);
        Map<Position, Integer> map = getChunkMap(pair);
        return map.containsKey(position) ? map.get(position) : 0;
    }

    public void remove(Position position) {
        Pair pair = getPair(position);
        Map<Position, Integer> map = getChunkMap(pair);
        map.remove(position);
        if(map.isEmpty()){
            blockStorage.remove(pair);
        }
    }

    public void clear(){
        blockStorage.clear();
    }

    public void unloadChunk(int x, int z){
        blockStorage.remove(new Pair<Integer, Integer>(x, z));
    }

    private Map<Position, Integer> getChunkMap(Pair pair){
        Map<Position, Integer> map = blockStorage.get(pair);
        if(map == null){
            map = new HashMap<>();
        }
        return map;
    }

    private Pair<Integer, Integer> getPair(Position position){
        int chunkX = (int) (position.getX() >> 4);
        int chunkZ = (int) (position.getZ() >> 4);
        return new Pair<Integer, Integer>(chunkX, chunkZ);
    }
}
