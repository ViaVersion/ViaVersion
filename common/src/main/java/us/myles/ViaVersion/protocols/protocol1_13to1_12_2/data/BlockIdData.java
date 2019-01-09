package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data;

import com.google.common.collect.ObjectArrays;
import com.google.gson.reflect.TypeToken;
import us.myles.ViaVersion.util.GsonUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class BlockIdData {
    public static Map<String, String[]> blockIdMapping;
    public static Map<String, String[]> fallbackReverseMapping;

    public static void init() {
        InputStream stream = MappingData.class.getClassLoader()
                .getResourceAsStream("assets/viaversion/data/blockIds1.12to1.13.json");
        InputStreamReader reader = new InputStreamReader(stream);
        try {
            blockIdMapping = new HashMap<>((Map<String, String[]>) GsonUtil.getGson().fromJson(
                    reader,
                    new TypeToken<Map<String, String[]>>() {
                    }.getType()
            ));
            fallbackReverseMapping = new HashMap<>();
            for (Map.Entry<String, String[]> entry : blockIdMapping.entrySet()) {
                for (String val : entry.getValue()) {
                    String[] previous = fallbackReverseMapping.get(val);
                    if (previous == null) previous = new String[0];
                    fallbackReverseMapping.put(val, ObjectArrays.concat(previous, entry.getKey()));
                }
            }
        } finally {
            try {
                reader.close();
            } catch (IOException ignored) {
                // Ignored
            }
        }
    }
}
