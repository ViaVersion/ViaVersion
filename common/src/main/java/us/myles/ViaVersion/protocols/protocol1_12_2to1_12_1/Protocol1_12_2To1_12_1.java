package us.myles.ViaVersion.protocols.protocol1_12_2to1_12_1;

import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;

public class Protocol1_12_2To1_12_1 extends Protocol {

    @Override
    protected void registerPackets() {
        // Outgoing
        // 0x1f - Keep alive
        registerOutgoing(State.PLAY, 0x1f, 0x1f, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT, Type.LONG);
            }
        }); // Keep alive
        // Incoming
        // 0xb - Keep alive
        registerIncoming(State.PLAY, 0xb, 0xb, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.LONG, Type.VAR_INT);
            }
        });
    }
}
