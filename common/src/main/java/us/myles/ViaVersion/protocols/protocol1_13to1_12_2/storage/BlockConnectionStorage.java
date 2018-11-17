package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage;

import lombok.Data;
import lombok.EqualsAndHashCode;
import us.myles.ViaVersion.api.Pair;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.HashMap;
import java.util.Map;

public class BlockConnectionStorage extends StoredObject {
    private Map<Pair<Integer, Integer>, Map<BlockPositon, Integer>> blockStorage = new HashMap<>();

    public BlockConnectionStorage(UserConnection user) {
        super(user);
    }

    public void store(Position position, int blockState) {
        Pair pair = getPair(position);
        Map<BlockPositon, Integer> map = getChunkMap(pair);
        map.put(new BlockPositon(position), blockState);
    }

    public int get(Position position) {
        Pair pair = getPair(position);
        Map<BlockPositon, Integer> map = getChunkMap(pair);
        BlockPositon blockPositon = new BlockPositon(position);
        return map.containsKey(blockPositon) ? map.get(blockPositon) : 0;
    }

    public void remove(Position position) {
        Pair pair = getPair(position);
        Map<BlockPositon, Integer> map = getChunkMap(pair);
        map.remove(new BlockPositon(position));
        if(map.isEmpty()){
            blockStorage.remove(pair);
        }
    }

    public void clear(){
        blockStorage.clear();
    }

    public void unloadChunk(int x, int z){
        blockStorage.remove(new Pair<>(x, z));
    }

    private Map<BlockPositon, Integer> getChunkMap(Pair pair){
        Map<BlockPositon, Integer> map = blockStorage.get(pair);
        if(map == null){
            map = new HashMap<>();
            blockStorage.put(pair, map);
        }
        return map;
    }

    private Pair<Integer, Integer> getPair(Position position){
        int chunkX = (int) (position.getX() >> 4);
        int chunkZ = (int) (position.getZ() >> 4);
        return new Pair<>(chunkX, chunkZ);
    }

    @EqualsAndHashCode
    @Data
    private class BlockPositon {
        int x,y,z;
        public BlockPositon(Position position){
            x = position.getX().intValue();
            y = position.getY().intValue();
            z = position.getZ().intValue();
        }
    }
}
