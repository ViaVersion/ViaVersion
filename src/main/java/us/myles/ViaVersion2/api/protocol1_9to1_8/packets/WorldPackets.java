package us.myles.ViaVersion2.api.protocol1_9to1_8.packets;

import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion2.api.protocol.Protocol;
import us.myles.ViaVersion2.api.protocol1_9to1_8.Protocol1_9TO1_8;
import us.myles.ViaVersion2.api.remapper.PacketRemapper;
import us.myles.ViaVersion2.api.type.Type;

public class WorldPackets {
    public static void register(Protocol protocol) {
        // Sign Update Packet
        protocol.registerOutgoing(State.PLAY, 0x33, 0x46, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.LONG); // 0 - Sign Position
                map(Type.STRING, Protocol1_9TO1_8.FIX_JSON); // 1 - Sign Line (json)
                map(Type.STRING, Protocol1_9TO1_8.FIX_JSON); // 2 - Sign Line (json)
                map(Type.STRING, Protocol1_9TO1_8.FIX_JSON); // 3 - Sign Line (json)
                map(Type.STRING, Protocol1_9TO1_8.FIX_JSON); // 4 - Sign Line (json)
            }
        });

        // Play Effect Packet
        protocol.registerOutgoing(State.PLAY, 0x28, 0x21, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // 0 - Effect ID
                // Everything else get's written through

                // TODO: Effect canceller patch
            }
        });

        // Play Named Sound Effect Packet
        protocol.registerOutgoing(State.PLAY, 0x29, 0x19, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING); // 0 - Sound Name
                // 1 - Sound Category ID
                // Everything else get's written through

                // TODO: Sound Effect translator patch
            }
        });

        /* Packets which do not have any field remapping or handlers */

        protocol.registerOutgoing(State.PLAY, 0x25, 0x08); // Block Break Animation Packet
        protocol.registerOutgoing(State.PLAY, 0x35, 0x09); // Update Block Entity Packet
        // TODO: Update_Block_Entity actually implement
        protocol.registerOutgoing(State.PLAY, 0x24, 0x0A); // Block Action Packet
        protocol.registerOutgoing(State.PLAY, 0x23, 0x0B); // Block Change Packet
        protocol.registerOutgoing(State.PLAY, 0x22, 0x10); // Multi Block Change Packet
        protocol.registerOutgoing(State.PLAY, 0x27, 0x1C); // Explosion Packet
        protocol.registerOutgoing(State.PLAY, 0x2A, 0x22); // Particle Packet

        protocol.registerOutgoing(State.PLAY, 0x41, 0x0D); // Server Difficulty Packet
        protocol.registerOutgoing(State.PLAY, 0x03, 0x44); // Update Time Packet
        protocol.registerOutgoing(State.PLAY, 0x44, 0x35); // World Border Packet

        // TODO: Chunk Data, Bulk Chunk :)
    }
}
