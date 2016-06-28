package us.myles.ViaVersion.protocols.protocol1_9_1_2to1_9_3_4;

import lombok.Getter;
import org.spacehq.opennbt.tag.builtin.CompoundTag;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.type.Type;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BlockEntity {
    @Getter
    private static final Map<String, Integer> types = new ConcurrentHashMap<>();

    static {
        types.put("MobSpawner", 1);
        types.put("Control", 2);
        types.put("Beacon", 3);
        types.put("Skull", 4);
        types.put("FlowerPot", 5);
        types.put("Banner", 6);
        types.put("UNKNOWN", 7);
        types.put("EndGateway", 8);
        types.put("Sign", 9);
    }

    public static void handle(List<CompoundTag> tags, UserConnection connection) {
        for (CompoundTag tag : tags) {
            try {
                if (!tag.contains("id"))
                    throw new Exception("NBT tag not handled because the id key is missing");

                String id = (String) tag.get("id").getValue();
                if (!types.containsKey(id))
                    throw new Exception("Not handled id: " + id);

                int newId = types.get(id);
                if (newId == -1)
                    continue;

                int x = (int) tag.get("x").getValue();
                int y = (int) tag.get("y").getValue();
                int z = (int) tag.get("z").getValue();

                Position pos = new Position((long) x, (long) y, (long) z);

                updateBlockEntity(pos, (short) newId, tag, connection);
            } catch (Exception e) {
                if (ViaVersion.getInstance().isDebug()) {
                    System.out.println("Block Entity: " + e.getMessage() + ": " + tag);
                }
            }
        }
    }

    private static void updateBlockEntity(Position pos, short id, CompoundTag tag, UserConnection connection) throws Exception {
        PacketWrapper wrapper = new PacketWrapper(0x09, null, connection);
        wrapper.write(Type.POSITION, pos);
        wrapper.write(Type.UNSIGNED_BYTE, id);
        wrapper.write(Type.NBT, tag);
        wrapper.send(Protocol1_9_1_2TO1_9_3_4.class, false);
    }
}
