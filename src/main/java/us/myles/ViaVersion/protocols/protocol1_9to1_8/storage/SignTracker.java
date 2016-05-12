package us.myles.ViaVersion.protocols.protocol1_9to1_8.storage;

import com.google.common.collect.Sets;
import lombok.Getter;
import org.spacehq.opennbt.tag.builtin.CompoundTag;
import org.spacehq.opennbt.tag.builtin.IntTag;
import org.spacehq.opennbt.tag.builtin.StringTag;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.chunk.Sign;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.types.ChunkType;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class SignTracker extends StoredObject {
    private Map<Long, Set<Sign>> signs = new ConcurrentHashMap<>();

    public SignTracker(UserConnection user) {
        super(user);
    }

    public long positionToChunk(Position pos) {
        int x = (int) Math.floor(pos.getX() / 16);
        int z = (int) Math.floor(pos.getZ() / 16);

        return ChunkType.toLong(x, z);
    }

    public void add(Long pos, Sign s) {
        if (!signs.containsKey(pos))
            signs.put(pos, Sets.<Sign>newConcurrentHashSet());

        if (contains(pos, s)) {
            get(pos, s).setLines(s.getLines());
            return;
        }

        signs.get(pos).add(s);
    }

    public boolean contains(Long pos, Sign s) {
        if (!signs.containsKey(pos))
            return false;
        return signs.get(pos).contains(s);
    }

    public Sign get(Long pos, Sign s) {
        if (!signs.containsKey(pos))
            return null;
        for (Sign sign : signs.get(pos))
            if (sign.equals(s))
                return sign;
        return null;
    }

    public void clear() {
        signs.clear();
    }

    public void loadChunk(long chunk) {
        if (!signs.containsKey(chunk))
            return;

        Set<Sign> signSet = signs.remove(chunk);
        System.out.println("Loaded chunk " + chunk + " " + signs.size() + " left");

        for (Sign s : signSet) {
            try {
                if (is1_9_3())
                    sendPacket_1_9_3(s);
                else
                    sendPacket1_9(s);
            } catch (Exception e) {
                System.out.println("Something went wrong while sending the sign packet");
                e.printStackTrace();
            }
        }

    }

    public void removeChunk(long chunk) {
        signs.remove(chunk);
        System.out.println("Removed chunk " + chunk + " " + signs.size() + " left");
    }

    public boolean is1_9_3() {
        return getUser().get(ProtocolInfo.class).getProtocolVersion() >= ProtocolVersion.v1_9_3.getId();
    }

    private void sendPacket1_9(Sign sign) throws Exception { // TODO: Possibility to let the PacketWrapper.send() go through the protocol translators
        PacketWrapper wrapper = new PacketWrapper(0x46, null, getUser()); //Update sign

        wrapper.write(Type.POSITION, sign.getBlockPosition()); //Block position
        for (int i = 0; i < sign.getLines().length; i++)
            wrapper.write(Type.STRING, sign.getLines()[i]); // Text line

        wrapper.send();
    }

    private void sendPacket_1_9_3(Sign s) throws Exception {
        Position position = s.getBlockPosition();

        PacketWrapper wrapper = new PacketWrapper(0x09, null, getUser()); //Update block entity
        wrapper.setId(0x09); //Update block entity
        wrapper.write(Type.POSITION, position); //Block location
        wrapper.write(Type.UNSIGNED_BYTE, (short) 9); //Action type (9 update sign)

        //Create nbt
        CompoundTag tag = new CompoundTag("");
        tag.put(new StringTag("id", "Sign"));
        tag.put(new IntTag("x", position.getX().intValue()));
        tag.put(new IntTag("y", position.getY().intValue()));
        tag.put(new IntTag("z", position.getZ().intValue()));
        for (int i = 0; i < s.getLines().length; i++)
            tag.put(new StringTag("Text" + (i + 1), s.getLines()[i]));

        wrapper.write(Type.NBT, tag);

        wrapper.send();
    }
}
