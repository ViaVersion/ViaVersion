package us.myles.ViaVersion.protocols.protocolsnapshotto1_10;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.remapper.ValueCreator;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;

public class ProtocolSnapshotTo1_10 extends Protocol {
    @Override
    protected void registerPackets() {
        // Spawn mob packet
        registerOutgoing(State.PLAY, 0x03, 0x03, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.UUID); // 1 - Entity UUID
                map(Type.UNSIGNED_BYTE, Type.VAR_INT); // 2 - Entity Type

            }
        });

        // Collect item packet
        registerOutgoing(State.PLAY, 0x48, 0x48, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Collected entity id
                map(Type.VAR_INT); // 1 - Collector entity id

                create(new ValueCreator() {
                    @Override
                    public void write(PacketWrapper wrapper) throws Exception {
                        // TODO FIND OUT WHAT THE NEW VAR_INT IS
                    }
                });
            }
        });
    }

    @Override
    public void init(UserConnection userConnection) {

    }
}
