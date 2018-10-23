package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

@Data
public class WrappedBlockData {
    private String minecraftKey;
    private LinkedHashMap<String, String> blockData = new LinkedHashMap<>();

    public static WrappedBlockData fromString(String s){
        String[] array = s.split("\\[");
        String key = array[0];
        WrappedBlockData wrappedBlockdata = new WrappedBlockData(key);
        String blockData = array[1];
        blockData = blockData.replace("]", "");
        String[] data = blockData.split(",");
        for (String d : data) {
            String[] a = d.split("=");
            wrappedBlockdata.blockData.put(a[0], a[1]);
        }
        return wrappedBlockdata;
    }

    public static WrappedBlockData fromStateId(int id){
        return fromString(ConnectionData.getKey(id));
    }

    public WrappedBlockData(String s){
        minecraftKey = s;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder(minecraftKey + "[");
        for (Entry<String, String> entry : blockData.entrySet()) {
            sb.append(entry.getKey() + "=" + entry.getValue() + ",");
        }
        return sb.substring(0, sb.length()-1) + "]";
    }

    public int getBlockStateId(){
        return ConnectionData.getId(toString());
    }

    public WrappedBlockData set(String data, Object value){
        if(!hasData(data)) throw new UnsupportedOperationException("No blockdata found for " + data + " at " + minecraftKey);
        blockData.put(data, value.toString());
        return this;
    }

    public String getValue(String data){
        return blockData.get(data);
    }

    public boolean hasData(String key){
        return blockData.containsKey(key);
    }

}
