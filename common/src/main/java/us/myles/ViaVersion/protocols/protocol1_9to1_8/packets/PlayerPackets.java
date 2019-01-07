package us.myles.ViaVersion.protocols.protocol1_9to1_8.packets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.entities.Entity1_10Types;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.remapper.ValueCreator;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.ItemRewriter;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.PlayerMovementMapper;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9TO1_8;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.chat.ChatRewriter;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.chat.GameMode;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.CommandBlockProvider;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.ClientChunks;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.EntityTracker;

public class PlayerPackets {
    public static void register(Protocol protocol) {
        // Chat Message Packet
        protocol.registerOutgoing(State.PLAY, 0x02, 0x0F, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING, Protocol1_9TO1_8.FIX_JSON); // 0 - Chat Message (json)
                map(Type.BYTE); // 1 - Chat Positon

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        try {
                            JsonObject obj = (JsonObject) new JsonParser().parse(wrapper.get(Type.STRING, 0));
                            ChatRewriter.toClient(obj, wrapper.user());
                            wrapper.set(Type.STRING, 0, obj.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
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
                map(Type.STRING); // 0 - Team Name
                map(Type.BYTE); // 1 - Mode
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        byte mode = wrapper.get(Type.BYTE, 0); // Mode
                        if (mode == 0 || mode == 2) {
                            wrapper.passthrough(Type.STRING); // Display Name
                            wrapper.passthrough(Type.STRING); // Prefix
                            wrapper.passthrough(Type.STRING); // Suffix

                            wrapper.passthrough(Type.BYTE); // Friendly Fire

                            wrapper.passthrough(Type.STRING); // Name tag visibility

                            wrapper.write(Type.STRING, Via.getConfig().isPreventCollision() ? "never" : "");

                            wrapper.passthrough(Type.BYTE); // Colour
                        }

                        if (mode == 0 || mode == 3 || mode == 4) {
                            String[] players = wrapper.read(Type.STRING_ARRAY); // Players
                            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
                            String myName = wrapper.user().get(ProtocolInfo.class).getUsername();
                            String teamName = wrapper.get(Type.STRING, 0);
                            for (String player : players) {
                                if (entityTracker.isAutoTeam() && player.equalsIgnoreCase(myName)) {
                                    if (mode == 4) {
                                        // since removing add to auto team
                                        entityTracker.sendTeamPacket(true, false);
                                        entityTracker.setCurrentTeam("viaversion");
                                    } else {
                                        // since adding remove from auto team
                                        entityTracker.sendTeamPacket(false, true);
                                        entityTracker.setCurrentTeam(teamName);
                                    }
                                }
                            }
                            wrapper.write(Type.STRING_ARRAY, players);
                        }

                        if (mode == 1) { // Remove team
                            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
                            String teamName = wrapper.get(Type.STRING, 0);
                            if (entityTracker.isAutoTeam()
                                    && teamName.equals(entityTracker.getCurrentTeam())) {
                                // team was removed
                                entityTracker.sendTeamPacket(true, false);
                                entityTracker.setCurrentTeam("viaversion");
                            }
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
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int entityID = wrapper.get(Type.INT, 0);
                        EntityTracker tracker = wrapper.user().get(EntityTracker.class);
                        tracker.getClientEntityTypes().put(entityID, Entity1_10Types.EntityType.PLAYER);
                        tracker.setEntityID(entityID);
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
                        EntityTracker tracker = wrapper.user().get(EntityTracker.class);
                        tracker.setGameMode(GameMode.getById(wrapper.get(Type.UNSIGNED_BYTE, 0))); //Set player gamemode
                    }
                });

                // Gotta fake their op
                handler(new PacketHandler() {
                            @Override
                            public void handle(PacketWrapper wrapper) throws Exception {
                                CommandBlockProvider provider = Via.getManager().getProviders().get(CommandBlockProvider.class);
                                provider.sendPermission(wrapper.user());
                            }
                        }
                );

                // Scoreboard will be cleared when join game is received
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        if (Via.getConfig().isAutoTeam()) {
                            EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
                            entityTracker.setAutoTeam(true);
                            entityTracker.sendTeamPacket(true, false);
                            entityTracker.setCurrentTeam("viaversion");
                        }
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

        // Packet Plugin Message Outgoing
        protocol.registerOutgoing(State.PLAY, 0x3F, 0x18, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING); // 0 - Channel Name
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        String name = wrapper.get(Type.STRING, 0);
                        if (name.equalsIgnoreCase("MC|BOpen")) {
                            wrapper.passthrough(Type.REMAINING_BYTES); // This is so ugly, :(
                            wrapper.write(Type.VAR_INT, 0);
                        }
                        if (name.equalsIgnoreCase("MC|TrList")) {
                            wrapper.passthrough(Type.INT); // ID

                            Short size = wrapper.passthrough(Type.UNSIGNED_BYTE);

                            for (int i = 0; i < size; ++i) {
                                Item item1 = wrapper.passthrough(Type.ITEM);
                                ItemRewriter.toClient(item1);

                                Item item2 = wrapper.passthrough(Type.ITEM);
                                ItemRewriter.toClient(item2);

                                boolean present = wrapper.passthrough(Type.BOOLEAN);

                                if (present) {
                                    Item item3 = wrapper.passthrough(Type.ITEM);
                                    ItemRewriter.toClient(item3);
                                }

                                wrapper.passthrough(Type.BOOLEAN);

                                wrapper.passthrough(Type.INT);
                                wrapper.passthrough(Type.INT);
                            }
                        }
                    }
                });
            }
        });

        // Update Health Packet
        protocol.registerOutgoing(State.PLAY, 0x06, 0x3E, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.FLOAT); // 0 - Health
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        float health = wrapper.get(Type.FLOAT, 0);
                        if (health <= 0) {
                            // Client unloads chunks on respawn, take note
                            ClientChunks cc = wrapper.user().get(ClientChunks.class);
                            cc.getBulkChunks().clear();
                            cc.getLoadedChunks().clear();
                        }
                    }
                });
            }
        });

        // Respawn Packet
        protocol.registerOutgoing(State.PLAY, 0x07, 0x33, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // 0 - Dimension
                map(Type.UNSIGNED_BYTE); // 1 - Difficulty
                map(Type.UNSIGNED_BYTE); // 2 - GameMode
                map(Type.STRING); // 3 - Level Type

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        // Client unloads chunks on respawn, take note
                        ClientChunks cc = wrapper.user().get(ClientChunks.class);
                        cc.getBulkChunks().clear();
                        cc.getLoadedChunks().clear();

                        int gamemode = wrapper.get(Type.UNSIGNED_BYTE, 0);
                        wrapper.user().get(EntityTracker.class).setGameMode(GameMode.getById(gamemode));
                    }
                });

                // Fake permissions to get Commandblocks working
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        CommandBlockProvider provider = Via.getManager().getProviders().get(CommandBlockProvider.class);
                        provider.sendPermission(wrapper.user());
                        provider.unloadChunks(wrapper.user());
                    }
                });
            }
        });

        // Change Game State Packet
        protocol.registerOutgoing(State.PLAY, 0x2B, 0x1E, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); //0 - Reason
                map(Type.FLOAT); //1 - Value

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        if (wrapper.get(Type.UNSIGNED_BYTE, 0) == 3) { //Change gamemode
                            int gamemode = wrapper.get(Type.FLOAT, 0).intValue();
                            wrapper.user().get(EntityTracker.class).setGameMode(GameMode.getById(gamemode));
                        }
                    }
                });
            }
        });

        /* Removed packets */

        // Map Bulk
        protocol.registerOutgoing(State.PLAY, 0x26, 0x26, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        wrapper.cancel();
                    }
                });
            }
        });

        // Set Compression
        protocol.registerOutgoing(State.PLAY, 0x46, 0x46, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        wrapper.cancel();
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
        protocol.registerOutgoing(State.PLAY, 0x43, 0x36); // Camera Packet

        protocol.registerOutgoing(State.PLAY, 0x3D, 0x38); // Display Scoreboard Packet
        protocol.registerOutgoing(State.PLAY, 0x3B, 0x3F); // Scoreboard Objective Packet
        protocol.registerOutgoing(State.PLAY, 0x3C, 0x42); // Update Score Packet

        protocol.registerOutgoing(State.PLAY, 0x05, 0x43); // Spawn Position Packet
        protocol.registerOutgoing(State.PLAY, 0x1F, 0x3D); // Set XP Packet
        protocol.registerOutgoing(State.PLAY, 0x0D, 0x49); // Collect Item Packet

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

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int hand = wrapper.read(Type.VAR_INT);

                        EntityTracker tracker = wrapper.user().get(EntityTracker.class);
                        tracker.setMainHand(hand);
                    }
                });
            }
        });

        // Animation Request Packet
        protocol.registerIncoming(State.PLAY, 0x0A, 0x1A, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT, Type.NOTHING); // 0 - Hand
            }
        });

        // TP Confirm
        protocol.registerIncoming(State.PLAY, -1, 0x00, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        wrapper.cancel();
                    }
                });
            }
        });

        // Vehicle Move
        protocol.registerIncoming(State.PLAY, -1, 0x10, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        wrapper.cancel();
                    }
                });
            }
        });

        // Steer Boat
        protocol.registerIncoming(State.PLAY, -1, 0x11, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        wrapper.cancel();
                    }
                });
            }
        });

        // Packet Plugin Message Incoming
        protocol.registerIncoming(State.PLAY, 0x17, 0x09, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING); // 0 - Channel Name
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        String name = wrapper.get(Type.STRING, 0);
                        if (name.equalsIgnoreCase("MC|BSign")) {
                            Item item = wrapper.passthrough(Type.ITEM);
                            if (item != null) {
                                item.setId((short) 387); // Written Book
                                ItemRewriter.rewriteBookToServer(item);
                            }
                        }
                        if (name.equalsIgnoreCase("MC|AutoCmd")) {
                            wrapper.set(Type.STRING, 0, "MC|AdvCdm");
                            wrapper.write(Type.BYTE, (byte) 0);
                            wrapper.passthrough(Type.INT); // X
                            wrapper.passthrough(Type.INT); // Y
                            wrapper.passthrough(Type.INT); // Z
                            wrapper.passthrough(Type.STRING); // Command
                            wrapper.passthrough(Type.BOOLEAN); // Flag
                            wrapper.clearInputBuffer();
                        }
                        if (name.equalsIgnoreCase("MC|AdvCmd")) {
                            wrapper.set(Type.STRING, 0, "MC|AdvCdm");
                        }
                    }
                });
            }
        });

        // Client Status Packet
        protocol.registerIncoming(State.PLAY, 0x16, 0x03, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Action ID
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int action = wrapper.get(Type.VAR_INT, 0);
                        if (action == 2) {
                            // cancel any blocking >.>
                            EntityTracker tracker = wrapper.user().get(EntityTracker.class);
                            if (tracker.isBlocking()) {
                                tracker.setSecondHand(null);
                                tracker.setBlocking(false);
                            }
                        }
                    }
                });
            }
        });

        // Player Position Packet
        protocol.registerIncoming(State.PLAY, 0x04, 0x0C, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.DOUBLE); // 0 - X
                map(Type.DOUBLE); // 1 - Y
                map(Type.DOUBLE); // 2 - Z
                map(Type.BOOLEAN); // 3 - Ground
                handler(new PlayerMovementMapper());
            }
        });

        // Player Move & Look Packet
        protocol.registerIncoming(State.PLAY, 0x06, 0x0D, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.DOUBLE); // 0 - X
                map(Type.DOUBLE); // 1 - Y
                map(Type.DOUBLE); // 2 - Z
                map(Type.FLOAT); // 3 - Yaw
                map(Type.FLOAT); // 4 - Pitch
                map(Type.BOOLEAN); // 5 - Ground
                handler(new PlayerMovementMapper());
            }
        });

        // Player Look Packet
        protocol.registerIncoming(State.PLAY, 0x05, 0x0E, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.FLOAT); // 0 - Yaw
                map(Type.FLOAT); // 1 - Pitch
                map(Type.BOOLEAN); // 2 - Ground
                handler(new PlayerMovementMapper());
            }
        });

        // Player Packet
        protocol.registerIncoming(State.PLAY, 0x03, 0x0F, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.BOOLEAN); // 0 - Ground
                handler(new PlayerMovementMapper());
            }
        });

        /* Packets which do not have any field remapping or handlers */

        protocol.registerIncoming(State.PLAY, 0x01, 0x02); // Chat Message Packet
        protocol.registerIncoming(State.PLAY, 0x13, 0x12); // Player Abilities Request Packet
        protocol.registerIncoming(State.PLAY, 0x19, 0x16); // Resource Pack Status Packet

        protocol.registerIncoming(State.PLAY, 0x00, 0x0B); // Keep Alive Request Packet


    }
}
