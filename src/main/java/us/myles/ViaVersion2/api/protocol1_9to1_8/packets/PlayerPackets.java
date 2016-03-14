package us.myles.ViaVersion2.api.protocol1_9to1_8.packets;

import org.bukkit.entity.EntityType;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion2.api.PacketWrapper;
import us.myles.ViaVersion2.api.protocol.Protocol;
import us.myles.ViaVersion2.api.protocol1_9to1_8.Protocol1_9TO1_8;
import us.myles.ViaVersion2.api.protocol1_9to1_8.storage.EntityTracker;
import us.myles.ViaVersion2.api.remapper.PacketHandler;
import us.myles.ViaVersion2.api.remapper.PacketRemapper;
import us.myles.ViaVersion2.api.remapper.ValueCreator;
import us.myles.ViaVersion2.api.type.Type;

public class PlayerPackets {
    public static void register(Protocol protocol) {
        // Chat Message Packet
        protocol.registerOutgoing(State.PLAY, 0x02, 0x0F, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING, Protocol1_9TO1_8.FIX_JSON); // 0 - Chat Message (json)
                map(Type.BYTE); // 1 - Chat Positon
            }
        });

        // Header and Footer Packet
        protocol.registerOutgoing(State.PLAY, 0x47, 0x48, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING, Protocol1_9TO1_8.FIX_JSON); // 0 - Header
                map(Type.STRING, Protocol1_9TO1_8.FIX_JSON); // 1 - Footer
            }
        });

        // Disconnect Packet
        protocol.registerOutgoing(State.PLAY, 0x40, 0x1A, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING, Protocol1_9TO1_8.FIX_JSON); // 0 - Reason
            }
        });

        // Title Packet
        protocol.registerOutgoing(State.PLAY, 0x45, 0x45, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Action
                // We only handle if the title or subtitle is set then just write through.
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int action = wrapper.get(Type.VAR_INT, 0);
                        if (action == 0 || action == 1) {
                            Protocol1_9TO1_8.FIX_JSON.write(wrapper, wrapper.read(Type.STRING));
                        }
                    }
                });
                // Everything else is handled.
            }
        });

        // Player Position Packet
        protocol.registerOutgoing(State.PLAY, 0x08, 0x2E, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.DOUBLE); // 0 - Player X
                map(Type.DOUBLE); // 1 - Player Y
                map(Type.DOUBLE); // 2 - Player Z

                map(Type.FLOAT); // 3 - Player Yaw
                map(Type.FLOAT); // 4 - Player Pitch

                map(Type.BYTE); // 5 - Player Flags

                create(new ValueCreator() {
                    @Override
                    public void write(PacketWrapper wrapper) {
                        wrapper.write(Type.VAR_INT, 0); // 6 - Teleport ID was added
                    }
                });
            }
        });

        // Team Packet
        protocol.registerOutgoing(State.PLAY, 0x3E, 0x41, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING);
                map(Type.BYTE);
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        byte mode = wrapper.get(Type.BYTE, 1);
                        if (mode == 0 || mode == 2) {
                            wrapper.passthrough(Type.STRING);
                            wrapper.passthrough(Type.STRING);
                            wrapper.passthrough(Type.STRING);

                            wrapper.passthrough(Type.BYTE);

                            wrapper.passthrough(Type.STRING);

                            wrapper.write(Type.STRING, ((ViaVersionPlugin) ViaVersion.getInstance()).isPreventCollision() ? "never" : "");

                            wrapper.passthrough(Type.BYTE);
                        }

                        if (mode == 0 || mode == 2) {
                            String[] players = wrapper.read(Type.STRING_ARRAY);
                            // TODO Handler for sending autoteam
                            wrapper.write(Type.STRING_ARRAY, players);
                        }
                    }
                });
            }
        });

        // Join Game Packet
        protocol.registerOutgoing(State.PLAY, 0x01, 0x23, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // 0 - Player ID
                // Parse this info
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) {
                        int entityID = wrapper.get(Type.INT, 0);
                        EntityTracker tracker = wrapper.user().get(EntityTracker.class);
                        tracker.getClientEntityTypes().put(entityID, EntityType.PLAYER);
                    }
                });
                map(Type.UNSIGNED_BYTE); // 1 - Player Gamemode
                map(Type.BYTE); // 2 - Player Dimension
                map(Type.UNSIGNED_BYTE); // 3 - World Difficulty
                map(Type.UNSIGNED_BYTE); // 4 - Max Players (Tab)
                map(Type.STRING); // 5 - Level Type
                map(Type.BOOLEAN); // 6 - Reduced Debug info

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int myID = wrapper.get(Type.INT, 0);
                        EntityTracker tracker = wrapper.user().get(EntityTracker.class);
                        tracker.setEntityID(myID);
                    }
                });
            }
        });

        // Player List Item Packet
        protocol.registerOutgoing(State.PLAY, 0x38, 0x2D, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Action
                map(Type.VAR_INT); // 1 - Player Count

                // Due to this being a complex data structure we just use a handler.
                handler(new PacketHandler() {

                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int action = wrapper.get(Type.VAR_INT, 0);
                        int count = wrapper.get(Type.VAR_INT, 1);

                        for (int i = 0; i < count; i++) {
                            wrapper.passthrough(Type.UUID); // Player UUID
                            if (action == 0) { // add player
                                wrapper.passthrough(Type.STRING); // Player Name

                                int properties = wrapper.passthrough(Type.VAR_INT);

                                // loop through properties
                                for (int j = 0; j < properties; j++) {
                                    wrapper.passthrough(Type.STRING); // name
                                    wrapper.passthrough(Type.STRING); // value
                                    boolean isSigned = wrapper.passthrough(Type.BOOLEAN);
                                    if (isSigned) {
                                        wrapper.passthrough(Type.STRING); // signature
                                    }
                                }

                                wrapper.passthrough(Type.VAR_INT); // gamemode
                                wrapper.passthrough(Type.VAR_INT); // ping
                                boolean hasDisplayName = wrapper.passthrough(Type.BOOLEAN);
                                if (hasDisplayName) {
                                    Protocol1_9TO1_8.FIX_JSON.write(wrapper, wrapper.read(Type.STRING)); // display name
                                }
                            } else if ((action == 1) || (action == 2)) { // update gamemode || update latency
                                wrapper.passthrough(Type.VAR_INT);
                            } else if (action == 3) { // update display name
                                boolean hasDisplayName = wrapper.passthrough(Type.BOOLEAN);
                                if (hasDisplayName) {
                                    Protocol1_9TO1_8.FIX_JSON.write(wrapper, wrapper.read(Type.STRING)); // display name
                                }
                            } else if (action == 4) { // remove player
                                // no fields
                            }
                        }
                    }
                });
            }
        });

        /* Packets which do not have any field remapping or handlers */

        protocol.registerOutgoing(State.PLAY, 0x3A, 0x0E); // Tab Complete Response Packet
        protocol.registerOutgoing(State.PLAY, 0x0B, 0x06); // Animation Packet
        protocol.registerOutgoing(State.PLAY, 0x37, 0x07); // Stats Packet
        protocol.registerOutgoing(State.PLAY, 0x36, 0x2A); // Open Sign Editor Packet
        protocol.registerOutgoing(State.PLAY, 0x39, 0x2B); // Player Abilities Packet
        protocol.registerOutgoing(State.PLAY, 0x00, 0x1F); // Keep Alive Packet
        protocol.registerOutgoing(State.PLAY, 0x48, 0x32); // Resource Pack Send Packet
        protocol.registerOutgoing(State.PLAY, 0x07, 0x33); // Respawn Packet
        protocol.registerOutgoing(State.PLAY, 0x43, 0x36); // Camera Packet

        protocol.registerOutgoing(State.PLAY, 0x09, 0x37); // Held Item Change Packet

        protocol.registerOutgoing(State.PLAY, 0x3D, 0x38); // Display Scoreboard Packet
        protocol.registerOutgoing(State.PLAY, 0x3B, 0x3F); // Scoreboard Objective Packet
        protocol.registerOutgoing(State.PLAY, 0x3C, 0x42); // Update Score Packet

        protocol.registerOutgoing(State.PLAY, 0x05, 0x43); // Spawn Position Packet
        protocol.registerOutgoing(State.PLAY, 0x1F, 0x3D); // Set XP Packet
        protocol.registerOutgoing(State.PLAY, 0x06, 0x3E); // Update Health Packet
        protocol.registerOutgoing(State.PLAY, 0x0D, 0x49); // Collect Item Packet

        protocol.registerOutgoing(State.PLAY, 0x3F, 0x18); // Plugin Message

        // TODO:
        // Server Difficulty - Activate Auto-Team

        /* Incoming Packets */

        // Tab Complete Request Packet
        protocol.registerIncoming(State.PLAY, 0x14, 0x01, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING); // 0 - Requested Command
                map(Type.BOOLEAN, Type.NOTHING); // 1 - Is Command Block
            }
        });

        // Client Settings Packet
        protocol.registerIncoming(State.PLAY, 0x15, 0x04, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING); // 0 - locale
                map(Type.BYTE); // 1 - View Distance
                map(Type.VAR_INT, Type.BYTE); // 2 - Chat Mode
                map(Type.BOOLEAN); // 3 - If Chat Colours on
                map(Type.UNSIGNED_BYTE); // 4 - Skin Parts
                map(Type.VAR_INT, Type.NOTHING); // 5 - Main Hand
            }
        });

        // Animation Request Packet
        protocol.registerIncoming(State.PLAY, 0x0A, 0x1A, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT, Type.NOTHING); // 0 - Hand
            }
        });

        /* Packets which do not have any field remapping or handlers */

        protocol.registerIncoming(State.PLAY, 0x01, 0x02); // Chat Message Packet
        protocol.registerIncoming(State.PLAY, 0x16, 0x03); // Client Status Packet
        protocol.registerIncoming(State.PLAY, 0x13, 0x12); // Player Abilities Request Packet
        protocol.registerIncoming(State.PLAY, 0x19, 0x16); // Resource Pack Status Packet

        protocol.registerIncoming(State.PLAY, 0x00, 0x0B); // Keep Alive Request Packet

        protocol.registerIncoming(State.PLAY, 0x04, 0x0C); // Player Position Packet
        protocol.registerIncoming(State.PLAY, 0x06, 0x0D); // Player Move & Look Packet
        protocol.registerIncoming(State.PLAY, 0x05, 0x0E); // Player Look Packet
        protocol.registerIncoming(State.PLAY, 0x03, 0x0F); // Player Packet

        // TODO Plugin Channels :(
    }
}
