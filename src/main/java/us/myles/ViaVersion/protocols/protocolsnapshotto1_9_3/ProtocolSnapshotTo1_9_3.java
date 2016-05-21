package us.myles.ViaVersion.protocols.protocolsnapshotto1_9_3;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.remapper.ValueTransformer;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;

import java.util.List;

public class ProtocolSnapshotTo1_9_3 extends Protocol {
    public static final Type<List<Metadata>> METADATA_LIST = new MetaListSnapshotType();
    public static final Type<Metadata> METADATA = new MetaSnapshotType();
    public static ValueTransformer<Short, Float> toNewPitch = new ValueTransformer<Short, Float>(Type.FLOAT) {
        @Override
        public Float transform(PacketWrapper wrapper, Short inputValue) throws Exception {
            return inputValue / 63.5F;

        }
    };

    @Override
    protected void registerPackets() {
        // Named sound effect
        registerOutgoing(State.PLAY, 0x19, 0x19, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING); // 0 - Sound name
                map(Type.VAR_INT); // 1 - Sound Category
                map(Type.INT); // 2 - x
                map(Type.INT); // 3 - y
                map(Type.INT); // 4 - z
                map(Type.FLOAT); // 5 - Volume
                map(Type.UNSIGNED_BYTE, toNewPitch); // 6 - Pitch
            }
        });

        // Sound effect
        registerOutgoing(State.PLAY, 0x46, 0x46, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Sound name
                map(Type.VAR_INT); // 1 - Sound Category
                map(Type.INT); // 2 - x
                map(Type.INT); // 3 - y
                map(Type.INT); // 4 - z
                map(Type.FLOAT); // 5 - Volume
                map(Type.UNSIGNED_BYTE, toNewPitch); // 6 - Pitch

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int id = wrapper.get(Type.VAR_INT, 0);
                        wrapper.set(Type.VAR_INT, 0, getNewSoundId(id));
                    }
                });
            }
        });

        // Metadata packet
        registerOutgoing(State.PLAY, 0x39, 0x39, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(METADATA_LIST); // 1 - Metadata list
            }
        });

        // Spawn Mob
        registerOutgoing(State.PLAY, 0x03, 0x03, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity id
                map(Type.UUID); // 1 - UUID
                map(Type.UNSIGNED_BYTE); // 2 - Entity Type
                map(Type.DOUBLE); // 3 - X
                map(Type.DOUBLE); // 4 - Y
                map(Type.DOUBLE); // 5 - Z
                map(Type.BYTE); // 6 - Yaw
                map(Type.BYTE); // 7 - Pitch
                map(Type.BYTE); // 8 - Head Pitch
                map(Type.SHORT); // 9 - Velocity X
                map(Type.SHORT); // 10 - Velocity Y
                map(Type.SHORT); // 11 - Velocity Z
                map(METADATA_LIST); // 12 - Metadata
            }
        });

        // Spawn Player
        registerOutgoing(State.PLAY, 0x05, 0x05, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.UUID); // 1 - Player UUID
                map(Type.DOUBLE); // 2 - X
                map(Type.DOUBLE); // 3 - Y
                map(Type.DOUBLE); // 4 - Z
                map(Type.BYTE); // 5 - Yaw
                map(Type.BYTE); // 6 - Pitch
                map(METADATA_LIST); // 7 - Metadata list
            }
        });
    }

    public int getNewSoundId(int id) { //TODO organize things later
        if (id >= 24 && id <= 295) //One EnchantTable sound makes everything move between 24 and 295, blame the enchant table.
            return ++id;
        else if (id >= 296 && id < 443) //Blame the polar bear
            return id + 7;
        return id;
    }

    @Override
    public void init(UserConnection userConnection) {

    }
}
