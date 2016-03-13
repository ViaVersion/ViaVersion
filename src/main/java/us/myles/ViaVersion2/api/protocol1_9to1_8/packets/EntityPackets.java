package us.myles.ViaVersion2.api.protocol1_9to1_8.packets;

import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion2.api.PacketWrapper;
import us.myles.ViaVersion2.api.protocol.Protocol;
import us.myles.ViaVersion2.api.protocol1_9to1_8.Protocol1_9TO1_8;
import us.myles.ViaVersion2.api.remapper.PacketRemapper;
import us.myles.ViaVersion2.api.remapper.ValueTransformer;
import us.myles.ViaVersion2.api.type.Type;

public class EntityPackets {
    public static ValueTransformer<Byte, Short> toNewShort = new ValueTransformer<Byte, Short>(Type.SHORT) {
        @Override
        public Short transform(PacketWrapper wrapper, Byte inputValue) {
            return (short) (inputValue * 128);
        }
    };

    public static void register(Protocol protocol) {
        // Attach Entity Packet
        protocol.registerOutgoing(State.PLAY, 0x1B, 0x3A, new PacketRemapper() {

            @Override
            public void registerMap() {
                map(Type.INT); // 0 - Entity ID
                map(Type.INT); // 1 - Vehicle

                // Leash boolean is removed in new versions
                map(Type.BOOLEAN, new ValueTransformer<Boolean, Void>(Type.NOTHING) {
                    @Override
                    public Void transform(PacketWrapper wrapper, Boolean inputValue) {
                        if(!inputValue){
                            // TODO: Write Set Passengers packet
                        }
                        return null;
                    }
                });
            }
        });
        // Entity Teleport Packet
        protocol.registerOutgoing(State.PLAY, 0x18, 0x4A, new PacketRemapper() {

            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.INT, SpawnPackets.toNewDouble); // 1 - X - Needs to be divide by 32
                map(Type.INT, SpawnPackets.toNewDouble); // 2 - Y - Needs to be divide by 32
                map(Type.INT, SpawnPackets.toNewDouble); // 3 - Z - Needs to be divide by 32

                map(Type.BYTE); // 4 - Pitch
                map(Type.BYTE); // 5 - Yaw

                map(Type.BOOLEAN); // 6 - On Ground

                // TODO: Move holograms up on Y by offset (*32)
            }
        });
        // Entity Look Move Packet
        protocol.registerOutgoing(State.PLAY, 0x17, 0x26, new PacketRemapper() {

            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.BYTE, toNewShort); // 1 - X
                map(Type.BYTE, toNewShort); // 2 - Y
                map(Type.BYTE, toNewShort); // 3 - Z

                map(Type.BYTE); // 4 - Yaw
                map(Type.BYTE); // 5 - Pitch

                map(Type.BOOLEAN); // 6 - On Ground

                // TODO: Hologram patch moves down by 1 in Y
            }
        });
        // Entity Relative Move Packet
        protocol.registerOutgoing(State.PLAY, 0x15, 0x25, new PacketRemapper() {

            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.BYTE, toNewShort); // 1 - X
                map(Type.BYTE, toNewShort); // 2 - Y
                map(Type.BYTE, toNewShort); // 3 - Z

                map(Type.BOOLEAN); // 4 - On Ground

                // TODO: Hologram patch moves down by 1 in Y
            }
        });
        // Entity Equipment Packet
        protocol.registerOutgoing(State.PLAY, 0x04, 0x3C, new PacketRemapper() {

            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                // 1 - Slot ID
                map(Type.SHORT, new ValueTransformer<Short, Integer>(Type.VAR_INT) {
                    @Override
                    public Integer transform(PacketWrapper wrapper, Short slot) {
                        return slot > 0 ? slot.intValue() + 1 : slot.intValue();
                    }
                });
                map(Type.ITEM); // 2 - Item

                // TODO - Blocking patch

                // TODO - ItemStack rewriter
            }
        });
        // Entity Metadata Packet
        protocol.registerOutgoing(State.PLAY, 0x1C, 0x39, new PacketRemapper() {

            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Protocol1_9TO1_8.METADATA_LIST); // 1 - Metadata List
                // TODO Transform metadata
            }
        });

        // Entity Effect Packet
        protocol.registerOutgoing(State.PLAY, 0x1D, 0x4C, new PacketRemapper() {

            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.BYTE); // 1 - Effect ID
                map(Type.BYTE); // 2 - Amplifier
                map(Type.VAR_INT); // 3 - Duration
                map(Type.BOOLEAN, Type.BYTE);  // 4 - Hide particles
                // TODO: Test particles as conversion might not work
            }
        });

        /* Packets which do not have any field remapping or handlers */

        protocol.registerOutgoing(State.PLAY, 0x20, 0x4B); // Entity Properties Packet
        protocol.registerOutgoing(State.PLAY, 0x1A, 0x1B); // Entity Status Packet
        protocol.registerOutgoing(State.PLAY, 0x16, 0x27); // Entity Look Packet
        protocol.registerOutgoing(State.PLAY, 0x14, 0x28); // Entity Packet

        protocol.registerOutgoing(State.PLAY, 0x42, 0x2C); // Combat Event Packet
        protocol.registerOutgoing(State.PLAY, 0x0A, 0x2F); // Use Bed Packet

        protocol.registerOutgoing(State.PLAY, 0x1E, 0x31); // Remove Entity Effect Packet
        protocol.registerOutgoing(State.PLAY, 0x19, 0x34); // Entity Head Look Packet
        protocol.registerOutgoing(State.PLAY, 0x12, 0x3B); // Entity Velocity Packet
    }
}
