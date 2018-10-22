package us.myles.ViaVersion.protocols.protocol1_13to1_12_2;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.md_5.bungee.api.ChatColor;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_13Types;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.platform.providers.ViaProviders;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.remapper.ValueCreator;
import us.myles.ViaVersion.api.remapper.ValueTransformer;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.MappingData;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.packets.EntityPackets;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.packets.InventoryPackets;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.packets.WorldPackets;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.BlockEntityProvider;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.PaintingProvider;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage.BlockStorage;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage.EntityTracker;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage.TabCompleteTracker;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.types.Particle1_13Type;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import us.myles.ViaVersion.util.GsonUtil;

import java.util.EnumMap;
import java.util.Map;

// Development of 1.13 support!
public class Protocol1_13To1_12_2 extends Protocol {
    public static final Particle1_13Type PARTICLE_TYPE = new Particle1_13Type();

    public static final PacketHandler POS_TO_3_INT = new PacketHandler() {
        @Override
        public void handle(PacketWrapper wrapper) throws Exception {
            Position position = wrapper.read(Type.POSITION);
            wrapper.write(Type.INT, position.getX().intValue());
            wrapper.write(Type.INT, position.getY().intValue());
            wrapper.write(Type.INT, position.getZ().intValue());
        }
    };

    public static final PacketHandler SEND_DECLARE_COMMANDS_AND_TAGS = new PacketHandler() { // *insert here a good name*
        @Override
        public void handle(PacketWrapper w) throws Exception {
            // Send fake declare commands
            w.create(0x11, new ValueCreator() {
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
            }).send(Protocol1_13To1_12_2.class);

            // Send tags packet
            w.create(0x55, new ValueCreator() {
                @Override
                public void write(PacketWrapper wrapper) throws Exception {
                    wrapper.write(Type.VAR_INT, MappingData.blockTags.size()); // block tags
                    for (Map.Entry<String, Integer[]> tag : MappingData.blockTags.entrySet()) {
                        wrapper.write(Type.STRING, tag.getKey());
                        wrapper.write(Type.VAR_INT_ARRAY, tag.getValue().clone());
                    }
                    wrapper.write(Type.VAR_INT, MappingData.itemTags.size()); // item tags
                    for (Map.Entry<String, Integer[]> tag : MappingData.itemTags.entrySet()) {
                        wrapper.write(Type.STRING, tag.getKey());
                        wrapper.write(Type.VAR_INT_ARRAY, tag.getValue().clone());
                    }
                    wrapper.write(Type.VAR_INT, MappingData.fluidTags.size()); // fluid tags
                    for (Map.Entry<String, Integer[]> tag : MappingData.fluidTags.entrySet()) {
                        wrapper.write(Type.STRING, tag.getKey());
                        wrapper.write(Type.VAR_INT_ARRAY, tag.getValue().clone());
                    }
                }
            }).send(Protocol1_13To1_12_2.class);
        }
    };

    // @formatter:off
    // These are arbitrary rewrite values, it just needs an invalid color code character.
    protected static EnumMap<ChatColor, String> SCOREBOARD_TEAM_NAME_REWRITE = new EnumMap<ChatColor, String>(ChatColor.class) {{
        put(ChatColor.BLACK,        ChatColor.COLOR_CHAR + "g");
        put(ChatColor.DARK_BLUE,    ChatColor.COLOR_CHAR + "h");
        put(ChatColor.DARK_GREEN,   ChatColor.COLOR_CHAR + "i");
        put(ChatColor.DARK_AQUA,    ChatColor.COLOR_CHAR + "j");
        put(ChatColor.DARK_RED,     ChatColor.COLOR_CHAR + "p");
        put(ChatColor.DARK_PURPLE,  ChatColor.COLOR_CHAR + "q");
        put(ChatColor.GOLD,         ChatColor.COLOR_CHAR + "s");
        put(ChatColor.GRAY,         ChatColor.COLOR_CHAR + "t");
        put(ChatColor.DARK_GRAY,    ChatColor.COLOR_CHAR + "u");
        put(ChatColor.BLUE,         ChatColor.COLOR_CHAR + "v");
        put(ChatColor.GREEN,        ChatColor.COLOR_CHAR + "w");
        put(ChatColor.AQUA,         ChatColor.COLOR_CHAR + "x");
        put(ChatColor.RED,          ChatColor.COLOR_CHAR + "y");
        put(ChatColor.LIGHT_PURPLE, ChatColor.COLOR_CHAR + "z");
        put(ChatColor.YELLOW,       ChatColor.COLOR_CHAR + "!");
        put(ChatColor.WHITE,        ChatColor.COLOR_CHAR + "?");
    }};
    // @formatter:on

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

        registerOutgoing(State.STATUS, 0x00, 0x00, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING);
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        String response = wrapper.get(Type.STRING, 0);
                        try {
                            JsonObject json = GsonUtil.getGson().fromJson(response, JsonObject.class);
                            if (json.has("favicon")) {
                                json.addProperty("favicon", json.get("favicon").getAsString().replace("\n", ""));
                            }
                            wrapper.set(Type.STRING, 0, GsonUtil.getGson().toJson(json));
                        } catch (JsonParseException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        // New packet 0x04 - Login Plugin Message

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
        // WorldPackets 0x10 -> 0x0F

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
        // InventoryPackets 0x14 -> 0x15
        // InventoryPackets 0x15 -> 0x16
        // InventoryPackets 0x16 -> 0x17
        registerOutgoing(State.PLAY, 0x17, 0x18);
        // WorldPackets 0x18 -> 0x19
        registerOutgoing(State.PLAY, 0x1A, 0x1B);
        registerOutgoing(State.PLAY, 0x1B, 0x1C);
        // New packet 0x1D - NBT Query
        registerOutgoing(State.PLAY, 0x1C, 0x1E);
        registerOutgoing(State.PLAY, 0x1D, 0x1F);
        registerOutgoing(State.PLAY, 0x1E, 0x20);
        registerOutgoing(State.PLAY, 0x1F, 0x21);
        // WorldPackets 0x20 -> 0x22

        // Effect packet
        registerOutgoing(State.PLAY, 0x21, 0x23, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // Effect Id
                map(Type.POSITION); // Location
                map(Type.INT); // Data
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int id = wrapper.get(Type.INT, 0);
                        int data = wrapper.get(Type.INT, 1);
                        if (id == 1010) { // Play record
                            wrapper.set(Type.INT, 1, data = MappingData.oldToNewItems.get(data << 4));
                        } else if (id == 2001) { // Block break + block break sound
                            int blockId = data & 0xFFF;
                            int blockData = data >> 12;
                            wrapper.set(Type.INT, 1, data = WorldPackets.toNewId(blockId << 4 | blockData));
                        }
                    }
                });
            }
        });

        // WorldPackets 0x22 -> 0x24
        // Join (save dimension id)
        registerOutgoing(State.PLAY, 0x23, 0x25, new PacketRemapper() {
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
                    }
                });
                handler(SEND_DECLARE_COMMANDS_AND_TAGS);
            }
        });

        // Map packet
        registerOutgoing(State.PLAY, 0x24, 0x26, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Map id
                map(Type.BYTE); // 1 - Scale
                map(Type.BOOLEAN); // 2 - Tracking Position
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int iconCount = wrapper.passthrough(Type.VAR_INT);
                        for (int i = 0; i < iconCount; i++) {
                            byte directionAndType = wrapper.read(Type.BYTE);
                            int type = (directionAndType & 0xF0) >> 4;
                            wrapper.write(Type.VAR_INT, type);
                            wrapper.passthrough(Type.BYTE); // Icon X
                            wrapper.passthrough(Type.BYTE); // Icon Z
                            byte direction = (byte) (directionAndType & 0x0F);
                            wrapper.write(Type.BYTE, direction);
                            wrapper.write(Type.OPTIONAL_CHAT, null); // Display Name
                        }
                    }
                });
            }
        });
        registerOutgoing(State.PLAY, 0x25, 0x27);
        registerOutgoing(State.PLAY, 0x26, 0x28);
        registerOutgoing(State.PLAY, 0x27, 0x29);
        registerOutgoing(State.PLAY, 0x28, 0x2A);
        registerOutgoing(State.PLAY, 0x29, 0x2B);
        registerOutgoing(State.PLAY, 0x2A, 0x2C);
        // Craft recipe response
        registerOutgoing(State.PLAY, 0x2B, 0x2D, new PacketRemapper() {
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
        registerOutgoing(State.PLAY, 0x2C, 0x2E);
        registerOutgoing(State.PLAY, 0x2D, 0x2F);
        registerOutgoing(State.PLAY, 0x2E, 0x30);
        // New 0x31 - Face Player
        registerOutgoing(State.PLAY, 0x2F, 0x32);
        registerOutgoing(State.PLAY, 0x30, 0x33);
        // Recipe
        registerOutgoing(State.PLAY, 0x31, 0x34, new PacketRemapper() {
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

        // EntityPackets 0x32 -> 0x35
        registerOutgoing(State.PLAY, 0x33, 0x36);
        registerOutgoing(State.PLAY, 0x34, 0x37);

        // Respawn (save dimension id)
        registerOutgoing(State.PLAY, 0x35, 0x38, new PacketRemapper() {
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
                handler(SEND_DECLARE_COMMANDS_AND_TAGS);
            }
        });

        registerOutgoing(State.PLAY, 0x36, 0x39);
        registerOutgoing(State.PLAY, 0x37, 0x3A);
        registerOutgoing(State.PLAY, 0x38, 0x3B);
        registerOutgoing(State.PLAY, 0x39, 0x3C);
        registerOutgoing(State.PLAY, 0x3A, 0x3D);
        registerOutgoing(State.PLAY, 0x3B, 0x3E);
        // EntityPackets 0x3C -> 0x3F
        registerOutgoing(State.PLAY, 0x3D, 0x40);
        registerOutgoing(State.PLAY, 0x3E, 0x41);
        // InventoryPackets 0x3F -> 0x42
        registerOutgoing(State.PLAY, 0x40, 0x43);
        registerOutgoing(State.PLAY, 0x41, 0x44);
        // Scoreboard Objective
        registerOutgoing(State.PLAY, 0x42, 0x45, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING); // 0 - Objective name
                map(Type.BYTE); // 1 - Mode
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        byte mode = wrapper.get(Type.BYTE, 0);
                        // On create or update
                        if (mode == 0 || mode == 2) {
                            String value = wrapper.read(Type.STRING); // Value
                            value = ChatRewriter.legacyTextToJson(value);
                            wrapper.write(Type.STRING, value);

                            String type = wrapper.read(Type.STRING);
                            // integer or hearts
                            wrapper.write(Type.VAR_INT, type.equals("integer") ? 0 : 1);
                        }
                    }
                });
            }
        });

        registerOutgoing(State.PLAY, 0x43, 0x46);
        // Team packet
        registerOutgoing(State.PLAY, 0x44, 0x47, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING); // 0 - Team Name
                map(Type.BYTE); // 1 - Mode

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        byte action = wrapper.get(Type.BYTE, 0);

                        if (action == 0 || action == 2) {
                            String displayName = wrapper.read(Type.STRING); // Display Name
                            displayName = ChatRewriter.legacyTextToJson(displayName);
                            wrapper.write(Type.STRING, displayName);

                            String prefix = wrapper.read(Type.STRING); // Prefix moved
                            String suffix = wrapper.read(Type.STRING); // Suffix moved

                            wrapper.passthrough(Type.BYTE); // Flags

                            wrapper.passthrough(Type.STRING); // Name Tag Visibility
                            wrapper.passthrough(Type.STRING); // Collision rule

                            // Handle new colors
                            int colour = wrapper.read(Type.BYTE).intValue();
                            if (colour == -1) {
                                colour = 21; // -1 changed to 21
                            }

                            if (Via.getConfig().is1_13TeamColourFix()) {
                                colour = getLastColor(prefix).ordinal();
                                suffix = getLastColor(prefix).toString() + suffix;
                            }

                            wrapper.write(Type.VAR_INT, colour);

                            wrapper.write(Type.STRING, ChatRewriter.legacyTextToJson(prefix)); // Prefix
                            wrapper.write(Type.STRING, ChatRewriter.legacyTextToJson(suffix)); // Suffix
                        }

                        if (action == 0 || action == 3 || action == 4) {
                            String[] names = wrapper.read(Type.STRING_ARRAY); // Entities
                            for (int i = 0; i < names.length; i++) {
                                names[i] = rewriteTeamMemberName(names[i]);
                            }
                            wrapper.write(Type.STRING_ARRAY, names);
                        }
                    }
                });

            }
        });
        registerOutgoing(State.PLAY, 0x45, 0x48, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        String displayName = wrapper.read(Type.STRING); // Display Name
                        displayName = rewriteTeamMemberName(displayName);
                        wrapper.write(Type.STRING, displayName);

                        byte action = wrapper.read(Type.BYTE);
                        wrapper.write(Type.BYTE, action);
                        wrapper.passthrough(Type.STRING); // Objective Name
                        if (action != 1) {
                            wrapper.passthrough(Type.VAR_INT); // Value
                        }
                    }
                });
            }
        });
        registerOutgoing(State.PLAY, 0x46, 0x49);
        registerOutgoing(State.PLAY, 0x47, 0x4A);
        registerOutgoing(State.PLAY, 0x48, 0x4B);
        // New 0x4C - Stop Sound

        // Sound Effect packet
        registerOutgoing(State.PLAY, 0x49, 0x4D, new PacketRemapper() {
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
        registerOutgoing(State.PLAY, 0x4A, 0x4E);
        registerOutgoing(State.PLAY, 0x4B, 0x4F);
        registerOutgoing(State.PLAY, 0x4C, 0x50);
        // Advancements
        registerOutgoing(State.PLAY, 0x4D, 0x51, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        wrapper.passthrough(Type.BOOLEAN); // Reset/clear
                        int size = wrapper.passthrough(Type.VAR_INT); // Mapping size

                        for (int i = 0; i < size; i++) {
                            wrapper.passthrough(Type.STRING); // Identifier

                            // Parent
                            if (wrapper.passthrough(Type.BOOLEAN))
                                wrapper.passthrough(Type.STRING);

                            // Display data
                            if (wrapper.passthrough(Type.BOOLEAN)) {
                                wrapper.passthrough(Type.STRING); // Title
                                wrapper.passthrough(Type.STRING); // Description
                                Item icon = wrapper.read(Type.ITEM);
                                InventoryPackets.toClient(icon);
                                wrapper.write(Type.FLAT_ITEM, icon); // Translate item to flat item
                                wrapper.passthrough(Type.VAR_INT); // Frame type
                                int flags = wrapper.passthrough(Type.INT); // Flags
                                if ((flags & 1) != 0)
                                    wrapper.passthrough(Type.STRING); // Background texture
                                wrapper.passthrough(Type.FLOAT); // X
                                wrapper.passthrough(Type.FLOAT); // Y
                            }

                            wrapper.passthrough(Type.STRING_ARRAY); // Criteria

                            int arrayLength = wrapper.passthrough(Type.VAR_INT);
                            for (int array = 0; array < arrayLength; array++) {
                                wrapper.passthrough(Type.STRING_ARRAY); // String array
                            }
                        }
                    }
                });
            }
        });
        registerOutgoing(State.PLAY, 0x4E, 0x52);
        registerOutgoing(State.PLAY, 0x4F, 0x53);
        // New packet 0x54 - Declare Recipes
        // New packet 0x55 - Tags

        // Incoming packets

        // New packet 0x02 - Login Plugin Message
        registerIncoming(State.LOGIN, -1, 0x02, new PacketRemapper() {
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

        // New 0x01 - Query Block NBT
        registerIncoming(State.PLAY, -1, 0x01, new PacketRemapper() {
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

        // Tab-Complete
        registerIncoming(State.PLAY, 0x1, 0x5, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        // Disable auto-complete if configured
                        if (Via.getConfig().isDisable1_13AutoComplete()) {
                            wrapper.cancel();
                        }
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

        registerIncoming(State.PLAY, 0x05, 0x06);
        registerIncoming(State.PLAY, 0x06, 0x07);
        // InventoryPackets 0x07, 0x08
        registerIncoming(State.PLAY, 0x08, 0x09);
        // InventoryPackets 0x09 -> 0x0A

        // New 0x0A - Edit book -> Plugin Message
        registerIncoming(State.PLAY, 0x09, 0x0B, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        Item item = wrapper.read(Type.FLAT_ITEM);
                        boolean isSigning = wrapper.read(Type.BOOLEAN);

                        InventoryPackets.toServer(item);

                        wrapper.write(Type.STRING, isSigning ? "MC|BSign" : "MC|BEdit"); // Channel
                        wrapper.write(Type.ITEM, item);
                    }
                });
            }
        });
        // New 0x0C - Query Entity NBT
        registerIncoming(State.PLAY, -1, 0x0C, new PacketRemapper() {
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
        registerIncoming(State.PLAY, 0x0A, 0x0D);
        registerIncoming(State.PLAY, 0x0B, 0x0E);
        registerIncoming(State.PLAY, 0x0C, 0x0F);
        registerIncoming(State.PLAY, 0x0D, 0x10);
        registerIncoming(State.PLAY, 0x0E, 0x11);
        registerIncoming(State.PLAY, 0x0F, 0x12);
        registerIncoming(State.PLAY, 0x10, 0x13);
        registerIncoming(State.PLAY, 0x11, 0x14);
        // New 0x15 - Pick Item -> Plugin Message
        registerIncoming(State.PLAY, 0x09, 0x15, new PacketRemapper() {
            @Override
            public void registerMap() {
                create(new ValueCreator() {
                    @Override
                    public void write(PacketWrapper wrapper) throws Exception {
                        wrapper.write(Type.STRING, "MC|PickItem"); // Channel
                    }
                });
            }
        });

        // Craft recipe request
        registerIncoming(State.PLAY, 0x12, 0x16, new PacketRemapper() {
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

        registerIncoming(State.PLAY, 0x13, 0x17);
        registerIncoming(State.PLAY, 0x14, 0x18);
        registerIncoming(State.PLAY, 0x15, 0x19);
        registerIncoming(State.PLAY, 0x16, 0x1A);
        // Recipe Book Data
        registerIncoming(State.PLAY, 0x17, 0x1B, new PacketRemapper() {
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

        // New 0x1C - Name Item -> Plugin Message
        registerIncoming(State.PLAY, 0x09, 0x1C, new PacketRemapper() {
            @Override
            public void registerMap() {
                create(new ValueCreator() {
                    @Override
                    public void write(PacketWrapper wrapper) throws Exception {
                        wrapper.write(Type.STRING, "MC|ItemName"); // Channel
                    }
                });
            }
        });

        registerIncoming(State.PLAY, 0x18, 0x1D);
        registerIncoming(State.PLAY, 0x19, 0x1E);

        // New 0x1F - Select Trade -> Plugin Message
        registerIncoming(State.PLAY, 0x09, 0x1F, new PacketRemapper() {
            @Override
            public void registerMap() {
                create(new ValueCreator() {
                    @Override
                    public void write(PacketWrapper wrapper) throws Exception {
                        wrapper.write(Type.STRING, "MC|TrSel"); // Channel
                    }
                });
                map(Type.VAR_INT, Type.INT); // Slot
            }
        });
        // New 0x20 - Set Beacon Effect -> Plugin Message
        registerIncoming(State.PLAY, 0x09, 0x20, new PacketRemapper() {
            @Override
            public void registerMap() {
                create(new ValueCreator() {
                    @Override
                    public void write(PacketWrapper wrapper) throws Exception {
                        wrapper.write(Type.STRING, "MC|Beacon"); // Channel
                    }
                });
                map(Type.VAR_INT, Type.INT); // Primary Effect
                map(Type.VAR_INT, Type.INT); // Secondary Effect
            }
        });

        registerIncoming(State.PLAY, 0x1A, 0x21);

        // New 0x22 - Update Command Block -> Plugin Message
        registerIncoming(State.PLAY, 0x09, 0x22, new PacketRemapper() {
            @Override
            public void registerMap() {
                create(new ValueCreator() {
                    @Override
                    public void write(PacketWrapper wrapper) throws Exception {
                        wrapper.write(Type.STRING, "MC|AutoCmd");
                    }
                });
                handler(POS_TO_3_INT);
                map(Type.STRING); // Command
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int mode = wrapper.read(Type.VAR_INT);
                        byte flags = wrapper.read(Type.BYTE);

                        String stringMode = mode == 0 ? "SEQUENCE"
                                : mode == 1 ? "AUTO"
                                : "REDSTONE";

                        wrapper.write(Type.BOOLEAN, (flags & 0x1) != 0); // Track output
                        wrapper.write(Type.STRING, stringMode);
                        wrapper.write(Type.BOOLEAN, (flags & 0x2) != 0); // Is conditional
                        wrapper.write(Type.BOOLEAN, (flags & 0x4) != 0); // Automatic
                    }
                });
            }
        });
        // New 0x23 - Update Command Block Minecart -> Plugin Message
        registerIncoming(State.PLAY, 0x09, 0x23, new PacketRemapper() {
            @Override
            public void registerMap() {
                create(new ValueCreator() {
                    @Override
                    public void write(PacketWrapper wrapper) throws Exception {
                        wrapper.write(Type.STRING, "MC|AdvCmd");
                        wrapper.write(Type.BYTE, (byte) 1); // Type 1 for Entity
                    }
                });
                map(Type.VAR_INT, Type.INT); // Entity Id
            }
        });

        // 0x1B -> 0x24 in InventoryPackets

        // New 0x25 - Update Structure Block -> Message Channel
        registerIncoming(State.PLAY, 0x09, 0x25, new PacketRemapper() {
            @Override
            public void registerMap() {
                create(new ValueCreator() {
                    @Override
                    public void write(PacketWrapper wrapper) throws Exception {
                        wrapper.write(Type.STRING, "MC|Struct"); // Channel
                    }
                });
                handler(POS_TO_3_INT);
                map(Type.VAR_INT, new ValueTransformer<Integer, Byte>(Type.BYTE) { // Action
                    @Override
                    public Byte transform(PacketWrapper wrapper, Integer action) throws Exception {
                        return (byte) (action + 1);
                    }
                }); // Action
                map(Type.VAR_INT, new ValueTransformer<Integer, String>(Type.STRING) {
                    @Override
                    public String transform(PacketWrapper wrapper, Integer mode) throws Exception {
                        return mode == 0 ? "SAVE"
                                : mode == 1 ? "LOAD"
                                : mode == 2 ? "CORNER"
                                : "DATA";
                    }
                });
                map(Type.STRING); // Name
                map(Type.BYTE, Type.INT); // Offset X
                map(Type.BYTE, Type.INT); // Offset Y
                map(Type.BYTE, Type.INT); // Offset Z
                map(Type.BYTE, Type.INT); // Size X
                map(Type.BYTE, Type.INT); // Size Y
                map(Type.BYTE, Type.INT); // Size Z
                map(Type.VAR_INT, new ValueTransformer<Integer, String>(Type.STRING) { // Mirror
                    @Override
                    public String transform(PacketWrapper wrapper, Integer mirror) throws Exception {
                        return mirror == 0 ? "NONE"
                                : mirror == 1 ? "LEFT_RIGHT"
                                : "FRONT_BACK";
                    }
                });
                map(Type.VAR_INT, new ValueTransformer<Integer, String>(Type.STRING) { // Rotation
                    @Override
                    public String transform(PacketWrapper wrapper, Integer rotation) throws Exception {
                        return rotation == 0 ? "NONE"
                                : rotation == 1 ? "CLOCKWISE_90"
                                : rotation == 2 ? "CLOCKWISE_180"
                                : "COUNTERCLOCKWISE_90";
                    }
                });
                map(Type.STRING);
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        float integrity = wrapper.read(Type.FLOAT);
                        long seed = wrapper.read(Type.VAR_LONG);
                        byte flags = wrapper.read(Type.BYTE);

                        wrapper.write(Type.BOOLEAN, (flags & 0x1) != 0); // Ignore Entities
                        wrapper.write(Type.BOOLEAN, (flags & 0x2) != 0); // Show air
                        wrapper.write(Type.BOOLEAN, (flags & 0x4) != 0); // Show bounding box
                        wrapper.write(Type.FLOAT, integrity);
                        wrapper.write(Type.VAR_LONG, seed);
                    }
                });
            }
        });

        registerIncoming(State.PLAY, 0x1C, 0x26);
        registerIncoming(State.PLAY, 0x1D, 0x27);
        registerIncoming(State.PLAY, 0x1E, 0x28);
        registerIncoming(State.PLAY, 0x1F, 0x29);
        registerIncoming(State.PLAY, 0x20, 0x2A);
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

    private int getNewSoundID(final int oldID) {
        return MappingData.soundMappings.getNewSound(oldID);
    }

    // Based on method from https://github.com/Bukkit/Bukkit/blob/master/src/main/java/org/bukkit/ChatColor.java
    public ChatColor getLastColor(String input) {
        int length = input.length();

        for (int index = length - 1; index > -1; index--) {
            char section = input.charAt(index);
            if (section == ChatColor.COLOR_CHAR && index < length - 1) {
                char c = input.charAt(index + 1);
                ChatColor color = ChatColor.getByChar(c);

                if (color != null) {
                    switch (color) {
                        case MAGIC:
                        case BOLD:
                        case STRIKETHROUGH:
                        case UNDERLINE:
                        case ITALIC:
                        case RESET:
                            break;
                        default:
                            return color;
                    }
                }
            }
        }

        return ChatColor.RESET;
    }

    protected String rewriteTeamMemberName(String name) {
        // The Display Name is just colours which overwrites the suffix
        // It also overwrites for ANY colour in name but most plugins
        // will just send colour as 'invisible' character
        if (ChatColor.stripColor(name).length() == 0) {
            StringBuilder newName = new StringBuilder();
            for (int i = 0; i < name.length() / 2; i++) {
                ChatColor color = ChatColor.getByChar(name.charAt(i * 2 + 1));
                String rewrite = SCOREBOARD_TEAM_NAME_REWRITE.get(color);
                if (rewrite != null) { // just in case, should never happen
                    newName.append(rewrite);
                } else {
                    newName.append(name);
                }
            }
            name = newName.toString();
        }
        return name;
    }
}
