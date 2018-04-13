package us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_13Types;
import us.myles.ViaVersion.api.platform.providers.ViaProviders;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.remapper.ValueCreator;
import us.myles.ViaVersion.api.remapper.ValueTransformer;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.data.MappingData;
import us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.packets.EntityPackets;
import us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.packets.InventoryPackets;
import us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.packets.WorldPackets;
import us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.providers.BlockEntityProvider;
import us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.providers.PaintingProvider;
import us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.storage.BlockStorage;
import us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.storage.EntityTracker;
import us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.storage.TabCompleteTracker;
import us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.type.Particle1_13Type;

// Development of 1.13 support!
public class ProtocolSnapshotTo1_12_2 extends Protocol {
    public static final Particle1_13Type PARTICLE_TYPE = new Particle1_13Type();

    static {
        MappingData.init();
    }

    @Override
    protected void registerPackets() {
        // Register grouped packet changes
        EntityPackets.register(this);
        WorldPackets.register(this);
        InventoryPackets.register(this);

        // Outgoing packets

        // Statistics
        registerOutgoing(State.PLAY, 0x07, 0x07, new PacketRemapper() {
            @Override
            public void registerMap() {
                // TODO: This packet has changed

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        wrapper.cancel();
                    }
                });
            }
        });

        registerOutgoing(State.PLAY, 0xF, 0xE);

        // Tab-Complete
        registerOutgoing(State.PLAY, 0xE, 0x10, new PacketRemapper() {
            @Override
            public void registerMap() {
                create(new ValueCreator() {
                    @Override
                    public void write(PacketWrapper wrapper) throws Exception {
                        wrapper.write(Type.VAR_INT, wrapper.user().get(TabCompleteTracker.class).getTransactionId());

                        String input = wrapper.user().get(TabCompleteTracker.class).getInput();
                        // Start & End
                        int index;
                        int length;
                        // If no input or new word (then it's the start)
                        if (input.endsWith(" ") || input.length() == 0) {
                            index = input.length();
                            length = 0;
                        } else {
                            // Otherwise find the last space (+1 as we include it)
                            int lastSpace = input.lastIndexOf(" ") + 1;
                            index = lastSpace;
                            length = input.length() - lastSpace;
                        }
                        // Write index + length
                        wrapper.write(Type.VAR_INT, index);
                        wrapper.write(Type.VAR_INT, length);

                        int count = wrapper.passthrough(Type.VAR_INT);
                        for (int i = 0; i < count; i++) {
                            String suggestion = wrapper.read(Type.STRING);
                            // If we're at the start then handle removing slash
                            if (suggestion.startsWith("/") && index == 0) {
                                suggestion = suggestion.substring(1);
                            }
                            wrapper.write(Type.STRING, suggestion);
                            wrapper.write(Type.BOOLEAN, false);
                        }
                    }
                });
            }
        });

        // New packet 0x11, declare commands
        registerOutgoing(State.PLAY, 0x11, 0x12);
        registerOutgoing(State.PLAY, 0x12, 0x13);
        registerOutgoing(State.PLAY, 0x13, 0x14);

        registerOutgoing(State.PLAY, 0x15, 0x16);

        registerOutgoing(State.PLAY, 0x17, 0x18);

        registerOutgoing(State.PLAY, 0x1A, 0x1B);
        registerOutgoing(State.PLAY, 0x1B, 0x1C);
        registerOutgoing(State.PLAY, 0x1C, 0x1D);
        registerOutgoing(State.PLAY, 0x1D, 0x1E);
        registerOutgoing(State.PLAY, 0x1E, 0x1F);
        registerOutgoing(State.PLAY, 0x1F, 0x20);

        registerOutgoing(State.PLAY, 0x21, 0x22);

        // Join (save dimension id)
        registerOutgoing(State.PLAY, 0x23, 0x24, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // 0 - Entity ID
                map(Type.UNSIGNED_BYTE); // 1 - Gamemode
                map(Type.INT); // 2 - Dimension

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        // Store the player
                        int entityId = wrapper.get(Type.INT, 0);
                        wrapper.user().get(EntityTracker.class).addEntity(entityId, Entity1_13Types.EntityType.PLAYER);

                        ClientWorld clientChunks = wrapper.user().get(ClientWorld.class);
                        int dimensionId = wrapper.get(Type.INT, 1);
                        clientChunks.setEnvironment(dimensionId);

                        // Send fake declare commands
                        wrapper.create(0x11, new ValueCreator() {
                            @Override
                            public void write(PacketWrapper wrapper) {
                                wrapper.write(Type.VAR_INT, 2); // Size
                                // Write root node
                                wrapper.write(Type.VAR_INT, 0); // Mark as command
                                wrapper.write(Type.VAR_INT, 1); // 1 child
                                wrapper.write(Type.VAR_INT, 1); // Child is at 1

                                // Write arg node
                                wrapper.write(Type.VAR_INT, 0x02 | 0x04 | 0x10); // Mark as command
                                wrapper.write(Type.VAR_INT, 0); // No children
                                // Extra data
                                wrapper.write(Type.STRING, "args"); // Arg name
                                wrapper.write(Type.STRING, "brigadier:string");
                                wrapper.write(Type.VAR_INT, 2); // Greedy
                                wrapper.write(Type.STRING, "minecraft:ask_server"); // Ask server

                                wrapper.write(Type.VAR_INT, 0); // Root node index
                            }
                        }).send(ProtocolSnapshotTo1_12_2.class);
                        PacketWrapper tagsPacket = wrapper.create(0x54, new ValueCreator() {
                            @Override
                            public void write(PacketWrapper wrapper) throws Exception {
                                wrapper.write(Type.VAR_INT, 0);
                                wrapper.write(Type.VAR_INT, 0);
                            }
                        });
                        tagsPacket.send(ProtocolSnapshotTo1_12_2.class);
                        tagsPacket.send(ProtocolSnapshotTo1_12_2.class);
                    }
                });
            }
        });

        registerOutgoing(State.PLAY, 0x24, 0x25, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT);
                map(Type.BYTE);
                map(Type.BOOLEAN);
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int iconCount = wrapper.passthrough(Type.VAR_INT);
                        for (int i = 0; i < iconCount; i++) {
                            wrapper.passthrough(Type.BYTE);
                            wrapper.passthrough(Type.BYTE);
                            wrapper.passthrough(Type.BYTE);
                            wrapper.write(Type.BOOLEAN, false);
                        }
                        wrapper.passthroughAll();
                    }
                });
            }
        });
        registerOutgoing(State.PLAY, 0x25, 0x26);
        registerOutgoing(State.PLAY, 0x26, 0x27);
        registerOutgoing(State.PLAY, 0x27, 0x28);
        registerOutgoing(State.PLAY, 0x28, 0x29);
        registerOutgoing(State.PLAY, 0x29, 0x2A);
        registerOutgoing(State.PLAY, 0x2A, 0x2B);
        // Craft recipe response
        registerOutgoing(State.PLAY, 0x2B, 0x2C, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        // TODO This packet changed
                        wrapper.cancel();
                    }
                });
            }
        });
        registerOutgoing(State.PLAY, 0x2C, 0x2D);
        registerOutgoing(State.PLAY, 0x2D, 0x2E);
        registerOutgoing(State.PLAY, 0x2E, 0x2F);
        registerOutgoing(State.PLAY, 0x2F, 0x31);
        registerOutgoing(State.PLAY, 0x30, 0x32);
        // Recipe
        registerOutgoing(State.PLAY, 0x31, 0x33, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        // TODO: This has changed >.>
                        wrapper.cancel();
                    }
                });
            }
        });

        registerOutgoing(State.PLAY, 0x33, 0x35);
        registerOutgoing(State.PLAY, 0x34, 0x36);

        // Respawn (save dimension id)
        registerOutgoing(State.PLAY, 0x35, 0x37, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // 0 - Dimension ID
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
                        int dimensionId = wrapper.get(Type.INT, 0);
                        clientWorld.setEnvironment(dimensionId);
                    }
                });
            }
        });

        registerOutgoing(State.PLAY, 0x36, 0x38);
        registerOutgoing(State.PLAY, 0x37, 0x39);
        registerOutgoing(State.PLAY, 0x38, 0x3A);
        registerOutgoing(State.PLAY, 0x39, 0x3B);
        registerOutgoing(State.PLAY, 0x3A, 0x3C);
        registerOutgoing(State.PLAY, 0x3B, 0x3D);

        registerOutgoing(State.PLAY, 0x3D, 0x3F);
        registerOutgoing(State.PLAY, 0x3E, 0x40);

        registerOutgoing(State.PLAY, 0x40, 0x42);
        registerOutgoing(State.PLAY, 0x41, 0x43);
        // Scoreboard Objective
        registerOutgoing(State.PLAY, 0x42, 0x44, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING);
                map(Type.BYTE);
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        // On create or update
                        if (wrapper.get(Type.BYTE, 0) == 0 || wrapper.get(Type.BYTE, 0) == 2) {
                            wrapper.passthrough(Type.STRING);
                            String type = wrapper.read(Type.STRING);
                            // integer or hearts
                            wrapper.write(Type.VAR_INT, type.equals("integer") ? 0 : 1);
                        }
                    }
                });
            }
        });

        registerOutgoing(State.PLAY, 0x43, 0x45);
        // Team packet
        registerOutgoing(State.PLAY, 0x44, 0x46, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING); // 0 - Team Name
                map(Type.BYTE); // 1 - Mode

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        byte action = wrapper.get(Type.BYTE, 0);

                        if (action == 0 || action == 2) {
                            wrapper.passthrough(Type.STRING); // Display Name

                            wrapper.read(Type.STRING); // Prefix !REMOVED! TODO alternative or drop?
                            wrapper.read(Type.STRING); // Suffix !REMOVED!

                            wrapper.passthrough(Type.BYTE); // Flags

                            wrapper.passthrough(Type.STRING); // Name Tag Visibility
                            wrapper.passthrough(Type.STRING); // Collision rule

                            // Handle new colors
                            byte color = wrapper.read(Type.BYTE);

                            if (color == -1) // -1 changed to 21
                                wrapper.write(Type.VAR_INT, 21); // RESET
                            else
                                wrapper.write(Type.VAR_INT, (int) color);

                        }
                    }
                });

            }
        });
        registerOutgoing(State.PLAY, 0x45, 0x47);
        registerOutgoing(State.PLAY, 0x46, 0x48);
        registerOutgoing(State.PLAY, 0x47, 0x49);
        registerOutgoing(State.PLAY, 0x48, 0x4A);

        // Sound Effect packet
        registerOutgoing(State.PLAY, 0x49, 0x4C, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Sound ID

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int soundId = wrapper.get(Type.VAR_INT, 0);
                        wrapper.set(Type.VAR_INT, 0, getNewSoundID(soundId));
                    }
                });
            }
        });
        registerOutgoing(State.PLAY, 0x4A, 0x4D);
        registerOutgoing(State.PLAY, 0x4B, 0x4E);
        registerOutgoing(State.PLAY, 0x4C, 0x4F);
        // Advancements
        registerOutgoing(State.PLAY, 0x4D, 0x50, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        // TODO Temporary cancel advancements because of 'Non [a-z0-9/._-] character in path of location: minecraft:? https://fs.matsv.nl/media?id=auwje4z4lxw.png
                        wrapper.cancel();
                    }
                });
            }
        });
        registerOutgoing(State.PLAY, 0x4E, 0x51);
        registerOutgoing(State.PLAY, 0x4F, 0x52);
        // New packet 0x52 - Declare Recipes
        // New packet 0x53 - Tags

        // Incoming packets
        registerIncoming(State.PLAY, 0x2, 0x1);
        registerIncoming(State.PLAY, 0x3, 0x2);
        registerIncoming(State.PLAY, 0x4, 0x3);

        // Tab-Complete
        registerIncoming(State.PLAY, 0x1, 0x4, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int tid = wrapper.read(Type.VAR_INT);
                        // Save transaction id
                        wrapper.user().get(TabCompleteTracker.class).setTransactionId(tid);
                    }
                });
                // Prepend /
                map(Type.STRING, new ValueTransformer<String, String>(Type.STRING) {
                    @Override
                    public String transform(PacketWrapper wrapper, String inputValue) {
                        wrapper.user().get(TabCompleteTracker.class).setInput(inputValue);
                        return "/" + inputValue;
                    }
                });
                // Fake the end of the packet
                create(new ValueCreator() {
                    @Override
                    public void write(PacketWrapper wrapper) {
                        wrapper.write(Type.BOOLEAN, false);
                        wrapper.write(Type.OPTIONAL_POSITION, null);
                    }
                });
            }
        });

        // Craft recipe request
        registerIncoming(State.PLAY, 0x12, 0x12, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        // TODO: This has changed >.>
                        wrapper.cancel();
                    }
                });
            }
        });

        // Recipe Book Data
        registerIncoming(State.PLAY, 0x17, 0x17, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Type

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int type = wrapper.get(Type.VAR_INT, 0);

                        if (type == 1) {
                            wrapper.passthrough(Type.BOOLEAN); // Crafting Recipe Book Open
                            wrapper.passthrough(Type.BOOLEAN); // Crafting Recipe Filter Active
                            wrapper.read(Type.BOOLEAN); // Smelting Recipe Book Open | IGNORE NEW 1.13 FIELD
                            wrapper.read(Type.BOOLEAN); // Smelting Recipe Filter Active | IGNORE NEW 1.13 FIELD
                        }
                    }
                });
            }
        });
    }

    @Override
    public void init(UserConnection userConnection) {
        userConnection.put(new EntityTracker(userConnection));
        userConnection.put(new TabCompleteTracker(userConnection));
        if (!userConnection.has(ClientWorld.class))
            userConnection.put(new ClientWorld(userConnection));
        userConnection.put(new BlockStorage(userConnection));
    }

    @Override
    protected void register(ViaProviders providers) {
        providers.register(BlockEntityProvider.class, new BlockEntityProvider());
        providers.register(PaintingProvider.class, new PaintingProvider());
    }

    // Generated with PAaaS
    private int getNewSoundID(final int oldID) {
        int newId = oldID;
        if (oldID >= 1)
            newId += 6;
        if (oldID >= 10)
            newId += 5;
        if (oldID >= 86)
            newId += 1;
        if (oldID >= 166)
            newId += 4;
        if (oldID >= 174)
            newId += 10;
        if (oldID >= 179)
            newId += 9;
        if (oldID >= 226)
            newId += 1;
        if (oldID >= 352)
            newId += 5;
        if (oldID >= 373)
            newId += 1;
        if (oldID >= 380)
            newId += 7;
        if (oldID >= 385)
            newId += 4;
        if (oldID >= 438)
            newId += 1;
        if (oldID >= 443)
            newId += 16;
        if (oldID >= 484)
            newId += 1;
        if (oldID >= 485)
            newId += 1;
        if (oldID >= 508)
            newId += 2;
        if (oldID >= 513)
            newId += 1;
        if (oldID >= 515)
            newId += 1;
        if (oldID >= 524)
            newId += 8;
        return newId;
    }
}
