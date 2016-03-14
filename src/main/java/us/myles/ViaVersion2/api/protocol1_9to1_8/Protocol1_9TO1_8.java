package us.myles.ViaVersion2.api.protocol1_9to1_8;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import us.myles.ViaVersion2.api.PacketWrapper;
import us.myles.ViaVersion2.api.data.UserConnection;
import us.myles.ViaVersion2.api.metadata.Metadata;
import us.myles.ViaVersion2.api.protocol.Protocol;
import us.myles.ViaVersion2.api.protocol1_9to1_8.packets.*;
import us.myles.ViaVersion2.api.protocol1_9to1_8.storage.ClientChunks;
import us.myles.ViaVersion2.api.protocol1_9to1_8.storage.EntityTracker;
import us.myles.ViaVersion2.api.protocol1_9to1_8.types.MetadataListType;
import us.myles.ViaVersion2.api.protocol1_9to1_8.types.MetadataType;
import us.myles.ViaVersion2.api.remapper.ValueTransformer;
import us.myles.ViaVersion2.api.type.Type;

import java.util.List;

public class Protocol1_9TO1_8 extends Protocol {
    public static Type<List<Metadata>> METADATA_LIST = new MetadataListType();
    public static Type<Metadata> METADATA = new MetadataType();

    public static ValueTransformer<String, String> FIX_JSON = new ValueTransformer<String, String>(Type.STRING) {
        @Override
        public String transform(PacketWrapper wrapper, String line) {
            if (line == null || line.equalsIgnoreCase("null")) {
                line = "{\"text\":\"\"}";
            } else {
                if ((!line.startsWith("\"") || !line.endsWith("\"")) && (!line.startsWith("{") || !line.endsWith("}"))) {
                    JSONObject obj = new JSONObject();
                    obj.put("text", line);
                    return obj.toJSONString();
                }
                if (line.startsWith("\"") && line.endsWith("\"")) {
                    line = "{\"text\":" + line + "}";
                }
            }
            try {
                new JSONParser().parse(line);
            } catch (Exception e) {
                System.out.println("Invalid JSON String: \"" + line + "\" Please report this issue to the ViaVersion Github: " + e.getMessage());
                return "{\"text\":\"\"}";
            }
            return line;
        }
    };

    @Override
    protected void registerPackets() {
        SpawnPackets.register(this);
        InventoryPackets.register(this);
        EntityPackets.register(this);
        PlayerPackets.register(this);
        WorldPackets.register(this);
    }

    @Override
    public void init(UserConnection userConnection) {
        // Entity tracker
        userConnection.put(new EntityTracker());
        // Chunk tracker
        userConnection.put(new ClientChunks());
    }
}
