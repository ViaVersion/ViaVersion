package us.myles.ViaVersion.protocols.protocol1_9_1to1_9;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.ServerboundPackets1_9;

public class Protocol1_9_1To1_9 extends Protocol<ClientboundPackets1_9, ClientboundPackets1_9, ServerboundPackets1_9, ServerboundPackets1_9> {

    public Protocol1_9_1To1_9() {
        super(ClientboundPackets1_9.class, ClientboundPackets1_9.class, ServerboundPackets1_9.class, ServerboundPackets1_9.class);
    }

    @Override
    protected void registerPackets() {
        // Currently supports 1.9.1 and 1.9.2

        registerOutgoing(ClientboundPackets1_9.JOIN_GAME, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // 0 - Player ID
                map(Type.UNSIGNED_BYTE); // 1 - Player Gamemode
                // 1.9.1 PRE 2 Changed this
                map(Type.BYTE, Type.INT); // 2 - Player Dimension
                map(Type.UNSIGNED_BYTE); // 3 - World Difficulty
                map(Type.UNSIGNED_BYTE); // 4 - Max Players (Tab)
                map(Type.STRING); // 5 - Level Type
                map(Type.BOOLEAN); // 6 - Reduced Debug info
            }
        });

        registerOutgoing(ClientboundPackets1_9.SOUND, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Sound ID

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int sound = wrapper.get(Type.VAR_INT, 0);

                        if (sound >= 415) // Add 1 to every sound id since there is no Elytra sound on a 1.9 server
                            wrapper.set(Type.VAR_INT, 0, sound + 1);
                    }
                });
            }
        });
    }
}
