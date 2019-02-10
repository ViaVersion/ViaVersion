package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import lombok.Getter;
import us.myles.ViaVersion.api.Via;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class WrappedBlockData {
    @Getter
    private String minecraftKey;
    @Getter
    private int savedBlockStateId;
    private LinkedHashMap<String, String> blockData = new LinkedHashMap<>();

    public static WrappedBlockData fromString(String s) {
        String[] array = s.split("\\[");
        String key = array[0];
        WrappedBlockData wrappedBlockdata = new WrappedBlockData(key, ConnectionData.getId(s));
        if (array.length > 1) {
            String blockData = array[1];
            blockData = blockData.replace("]", "");
            String[] data = blockData.split(",");
            for (String d : data) {
                String[] a = d.split("=");
                wrappedBlockdata.blockData.put(a[0], a[1]);
            }
        }
        return wrappedBlockdata;
    }

    public static WrappedBlockData fromStateId(int id) {
        String blockData = ConnectionData.getKey(id);
        if (blockData != null) {
            return fromString(blockData);
        }
        Via.getPlatform().getLogger().info("Unable to get blockdata from " + id);
        return fromString("minecraft:air");
    }

    private WrappedBlockData(String minecraftKey, int savedBlockStateId) {
        this.minecraftKey = minecraftKey;
        this.savedBlockStateId = savedBlockStateId;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(minecraftKey + "[");
        for (Entry<String, String> entry : blockData.entrySet()) {
            sb.append(entry.getKey()).append('=').append(entry.getValue()).append(',');
        }
        return sb.substring(0, sb.length()-1) + "]";
    }

    public int getBlockStateId() {
        return ConnectionData.getId(toString());
    }

    public WrappedBlockData set(String data, Object value) {
        if (!hasData(data)) throw new UnsupportedOperationException("No blockdata found for " + data + " at " + minecraftKey);
        blockData.put(data, value.toString());
        return this;
    }

    public String getValue(String data) {
        return blockData.get(data);
    }

    public boolean hasData(String key) {
        return blockData.containsKey(key);
    }
}
