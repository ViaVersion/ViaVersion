/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.protocols.v1_12_2to1_13;

import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_13;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.remapper.ValueTransformer;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import com.viaversion.viaversion.api.type.types.version.Types1_13;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.base.ClientboundStatusPackets;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.blockconnections.ConnectionData;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.blockconnections.providers.BlockConnectionProvider;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.blockconnections.providers.PacketBlockConnectionProvider;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.data.BlockIdData;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.data.MappingData1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.data.RecipeData;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.data.StatisticData;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.data.StatisticMappings1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ServerboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.provider.BlockEntityProvider;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.provider.PaintingProvider;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.provider.PlayerLookTargetProvider;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.rewriter.ComponentRewriter1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.rewriter.EntityPacketRewriter1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.rewriter.ItemPacketRewriter1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.rewriter.WorldPacketRewriter1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.storage.BlockConnectionStorage;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.storage.BlockStorage;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.storage.TabCompleteTracker;
import com.viaversion.viaversion.protocols.v1_12to1_12_1.packet.ClientboundPackets1_12_1;
import com.viaversion.viaversion.protocols.v1_12to1_12_1.packet.ServerboundPackets1_12_1;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.util.ChatColorUtil;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.GsonUtil;
import com.viaversion.viaversion.util.IdAndData;
import com.viaversion.viaversion.util.ProtocolLogger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class Protocol1_12_2To1_13 extends AbstractProtocol<ClientboundPackets1_12_1, ClientboundPackets1_13, ServerboundPackets1_12_1, ServerboundPackets1_13> {

    public static final MappingData1_13 MAPPINGS = new MappingData1_13();
    public static final ProtocolLogger LOGGER = new ProtocolLogger(Protocol1_12_2To1_13.class);
    // These are arbitrary rewrite values, it just needs an invalid color code character.
    private static final Map<Character, Character> SCOREBOARD_TEAM_NAME_REWRITE = new HashMap<>();
    private static final Set<Character> FORMATTING_CODES = Sets.newHashSet('k', 'l', 'm', 'n', 'o', 'r');
    private final EntityPacketRewriter1_13 entityRewriter = new EntityPacketRewriter1_13(this);
    private final ItemPacketRewriter1_13 itemRewriter = new ItemPacketRewriter1_13(this);
    private final ComponentRewriter1_13<ClientboundPackets1_12_1> componentRewriter = new ComponentRewriter1_13<>(this);

    static {
        SCOREBOARD_TEAM_NAME_REWRITE.put('0', 'g');
        SCOREBOARD_TEAM_NAME_REWRITE.put('1', 'h');
        SCOREBOARD_TEAM_NAME_REWRITE.put('2', 'i');
        SCOREBOARD_TEAM_NAME_REWRITE.put('3', 'j');
        SCOREBOARD_TEAM_NAME_REWRITE.put('4', 'p');
        SCOREBOARD_TEAM_NAME_REWRITE.put('5', 'q');
        SCOREBOARD_TEAM_NAME_REWRITE.put('6', 's');
        SCOREBOARD_TEAM_NAME_REWRITE.put('7', 't');
        SCOREBOARD_TEAM_NAME_REWRITE.put('8', 'u');
        SCOREBOARD_TEAM_NAME_REWRITE.put('9', 'v');
        SCOREBOARD_TEAM_NAME_REWRITE.put('a', 'w');
        SCOREBOARD_TEAM_NAME_REWRITE.put('b', 'x');
        SCOREBOARD_TEAM_NAME_REWRITE.put('c', 'y');
        SCOREBOARD_TEAM_NAME_REWRITE.put('d', 'z');
        SCOREBOARD_TEAM_NAME_REWRITE.put('e', '!');
        SCOREBOARD_TEAM_NAME_REWRITE.put('f', '?');
        SCOREBOARD_TEAM_NAME_REWRITE.put('k', '#');
        SCOREBOARD_TEAM_NAME_REWRITE.put('l', '(');
        SCOREBOARD_TEAM_NAME_REWRITE.put('m', ')');
        SCOREBOARD_TEAM_NAME_REWRITE.put('n', ':');
        SCOREBOARD_TEAM_NAME_REWRITE.put('o', ';');
        SCOREBOARD_TEAM_NAME_REWRITE.put('r', '/');
    }

    public Protocol1_12_2To1_13() {
        super(ClientboundPackets1_12_1.class, ClientboundPackets1_13.class, ServerboundPackets1_12_1.class, ServerboundPackets1_13.class);
    }

    public static final PacketHandler POS_TO_3_INT = wrapper -> {
        BlockPosition position = wrapper.read(Types.BLOCK_POSITION1_8);
        wrapper.write(Types.INT, position.x());
        wrapper.write(Types.INT, position.y());
        wrapper.write(Types.INT, position.z());
    };

    public static final PacketHandler SEND_DECLARE_COMMANDS_AND_TAGS =
        w -> {
            // Send fake declare commands
            w.create(ClientboundPackets1_13.COMMANDS, wrapper -> {
                wrapper.write(Types.VAR_INT, 2); // Size
                // Write root node
                wrapper.write(Types.BYTE, (byte) 0); // Mark as command
                wrapper.write(Types.VAR_INT_ARRAY_PRIMITIVE, new int[]{1}); // 1 child at index 1

                // Write arg node
                wrapper.write(Types.BYTE, (byte) (0x02 | 0x04 | 0x10)); // Mark as command
                wrapper.write(Types.VAR_INT_ARRAY_PRIMITIVE, new int[0]); // No children
                // Extra data
                wrapper.write(Types.STRING, "args"); // Arg name
                wrapper.write(Types.STRING, "brigadier:string");
                wrapper.write(Types.VAR_INT, 2); // Greedy
                wrapper.write(Types.STRING, "minecraft:ask_server"); // Ask server

                wrapper.write(Types.VAR_INT, 0); // Root node index
            }).scheduleSend(Protocol1_12_2To1_13.class);

            // Send tags packet
            final PacketWrapper tagsPacket = w.create(ClientboundPackets1_13.UPDATE_TAGS, wrapper -> {
                wrapper.write(Types.VAR_INT, MAPPINGS.getBlockTags().size()); // block tags
                for (Map.Entry<String, int[]> tag : MAPPINGS.getBlockTags().entrySet()) {
                    wrapper.write(Types.STRING, tag.getKey());
                    // Needs copy as other protocols may modify it
                    wrapper.write(Types.VAR_INT_ARRAY_PRIMITIVE, tag.getValue().clone());
                }
                wrapper.write(Types.VAR_INT, MAPPINGS.getItemTags().size()); // item tags
                for (Map.Entry<String, int[]> tag : MAPPINGS.getItemTags().entrySet()) {
                    wrapper.write(Types.STRING, tag.getKey());
                    // Needs copy as other protocols may modify it
                    wrapper.write(Types.VAR_INT_ARRAY_PRIMITIVE, tag.getValue().clone());
                }
                wrapper.write(Types.VAR_INT, MAPPINGS.getFluidTags().size()); // fluid tags
                for (Map.Entry<String, int[]> tag : MAPPINGS.getFluidTags().entrySet()) {
                    wrapper.write(Types.STRING, tag.getKey());
                    // Needs copy as other protocols may modify it
                    wrapper.write(Types.VAR_INT_ARRAY_PRIMITIVE, tag.getValue().clone());
                }
            });
            if (w.user().getProtocolInfo().protocolVersion().newerThanOrEqualTo(ProtocolVersion.v1_20_5)) {
                // Make sure it's included in the configuration packets as it may already be required for registry data
                tagsPacket.send(Protocol1_12_2To1_13.class);
            } else {
                tagsPacket.scheduleSend(Protocol1_12_2To1_13.class);
            }
        };

    @Override
    protected void registerPackets() {
        super.registerPackets();

        WorldPacketRewriter1_13.register(this);

        registerClientbound(State.LOGIN, ClientboundLoginPackets.LOGIN_DISCONNECT, wrapper -> {
            componentRewriter.processText(wrapper.user(), wrapper.passthrough(Types.COMPONENT));
        });

        registerClientbound(State.STATUS, ClientboundStatusPackets.STATUS_RESPONSE, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING);
                handler(wrapper -> {
                    String response = wrapper.get(Types.STRING, 0);
                    try {
                        JsonObject json = GsonUtil.getGson().fromJson(response, JsonObject.class);
                        if (json.has("favicon")) {
                            json.addProperty("favicon", json.get("favicon").getAsString().replace("\n", ""));
                        }
                        wrapper.set(Types.STRING, 0, GsonUtil.getGson().toJson(json));
                    } catch (JsonParseException e) {
                        LOGGER.log(Level.SEVERE, "Error transforming status response", e);
                    }
                });
            }
        });

        // New packet 0x04 - Login Plugin Message

        // Statistics
        registerClientbound(ClientboundPackets1_12_1.AWARD_STATS, wrapper -> {
            int size = wrapper.read(Types.VAR_INT);
            List<StatisticData> remappedStats = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                String name = wrapper.read(Types.STRING);
                String[] split = name.split("\\.");
                int categoryId = 0;
                int newId = -1;
                int value = wrapper.read(Types.VAR_INT);
                if (split.length == 2) {
                    // Custom types
                    categoryId = 8;
                    Integer newIdRaw = StatisticMappings1_13.CUSTOM_STATS.get(name);
                    if (newIdRaw != null) {
                        newId = newIdRaw;
                    } else {
                        LOGGER.warning("Could not find statistic mapping for " + name);
                    }
                } else if (split.length > 2) {
                    String category = split[1];
                    //TODO convert string ids (blocks, items, entities)
                    categoryId = switch (category) {
                        case "mineBlock" -> 0;
                        case "craftItem" -> 1;
                        case "useItem" -> 2;
                        case "breakItem" -> 3;
                        case "pickup" -> 4;
                        case "drop" -> 5;
                        case "killEntity" -> 6;
                        case "entityKilledBy" -> 7;
                        default -> categoryId;
                    };
                }
                if (newId != -1)
                    remappedStats.add(new StatisticData(categoryId, newId, value));
            }

            wrapper.write(Types.VAR_INT, remappedStats.size()); // size
            for (StatisticData stat : remappedStats) {
                wrapper.write(Types.VAR_INT, stat.categoryId()); // category id
                wrapper.write(Types.VAR_INT, stat.newId()); // statistics id
                wrapper.write(Types.VAR_INT, stat.value()); // value
            }
        });


        componentRewriter.registerBossEvent(ClientboundPackets1_12_1.BOSS_EVENT);
        componentRewriter.registerComponentPacket(ClientboundPackets1_12_1.CHAT);

        registerClientbound(ClientboundPackets1_12_1.COMMAND_SUGGESTIONS, wrapper -> {
            wrapper.write(Types.VAR_INT, wrapper.user().get(TabCompleteTracker.class).getTransactionId());

            String input = wrapper.user().get(TabCompleteTracker.class).getInput();
            // Start & End
            int index;
            int length;
            // If no input or new word (then it's the start)
            if (input.endsWith(" ") || input.isEmpty()) {
                index = input.length();
                length = 0;
            } else {
                // Otherwise find the last space (+1 as we include it)
                int lastSpace = input.lastIndexOf(' ') + 1;
                index = lastSpace;
                length = input.length() - lastSpace;
            }
            // Write index + length
            wrapper.write(Types.VAR_INT, index);
            wrapper.write(Types.VAR_INT, length);

            int count = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < count; i++) {
                String suggestion = wrapper.read(Types.STRING);
                // If we're at the start then handle removing slash
                if (suggestion.startsWith("/") && index == 0) {
                    suggestion = suggestion.substring(1);
                }
                wrapper.write(Types.STRING, suggestion);
                wrapper.write(Types.OPTIONAL_COMPONENT, null); // Tooltip
            }
        });

        registerClientbound(ClientboundPackets1_12_1.OPEN_SCREEN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.UNSIGNED_BYTE); // Id
                map(Types.STRING); // Window type
                handler(wrapper -> componentRewriter.processText(wrapper.user(), wrapper.passthrough(Types.COMPONENT))); // Title
            }
        });

        registerClientbound(ClientboundPackets1_12_1.COOLDOWN, wrapper -> {
            int item = wrapper.read(Types.VAR_INT);
            int ticks = wrapper.read(Types.VAR_INT);
            wrapper.cancel();
            if (item == 383) { // Spawn egg
                for (int i = 0; i < 44; i++) {
                    int newItem = MAPPINGS.getItemMappings().getNewId(item << 16 | i);
                    if (newItem != -1) {
                        PacketWrapper packet = wrapper.create(ClientboundPackets1_13.COOLDOWN);
                        packet.write(Types.VAR_INT, newItem);
                        packet.write(Types.VAR_INT, ticks);
                        packet.send(Protocol1_12_2To1_13.class);
                    } else {
                        break;
                    }
                }
            } else {
                for (int i = 0; i < 16; i++) {
                    int newItem = MAPPINGS.getItemMappings().getNewId(IdAndData.toRawData(item, i));
                    if (newItem != -1) {
                        PacketWrapper packet = wrapper.create(ClientboundPackets1_13.COOLDOWN);
                        packet.write(Types.VAR_INT, newItem);
                        packet.write(Types.VAR_INT, ticks);
                        packet.send(Protocol1_12_2To1_13.class);
                    } else {
                        break;
                    }
                }
            }
        });

        componentRewriter.registerComponentPacket(ClientboundPackets1_12_1.DISCONNECT);

        registerClientbound(ClientboundPackets1_12_1.LEVEL_EVENT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); // Effect Id
                map(Types.BLOCK_POSITION1_8); // Location
                map(Types.INT); // Data
                handler(wrapper -> {
                    int id = wrapper.get(Types.INT, 0);
                    int data = wrapper.get(Types.INT, 1);
                    if (id == 1010) { // Play record
                        wrapper.set(Types.INT, 1, getMappingData().getItemMappings().getNewId(IdAndData.toRawData(data)));
                    } else if (id == 2001) { // Block break + block break sound
                        int blockId = data & 0xFFF;
                        int blockData = data >> 12;
                        wrapper.set(Types.INT, 1, WorldPacketRewriter1_13.toNewId(IdAndData.toRawData(blockId, blockData)));
                    }
                });
            }
        });

        registerClientbound(ClientboundPackets1_12_1.PLACE_GHOST_RECIPE, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.BYTE);
                handler(wrapper -> wrapper.write(Types.STRING, "viaversion:legacy/" + wrapper.read(Types.VAR_INT)));
            }
        });

        componentRewriter.registerPlayerCombat(ClientboundPackets1_12_1.PLAYER_COMBAT);

        registerClientbound(ClientboundPackets1_12_1.MAP_ITEM_DATA, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Map id
                map(Types.BYTE); // 1 - Scale
                map(Types.BOOLEAN); // 2 - Tracking Position
                handler(wrapper -> {
                    int iconCount = wrapper.passthrough(Types.VAR_INT);
                    for (int i = 0; i < iconCount; i++) {
                        byte directionAndType = wrapper.read(Types.BYTE);
                        int type = (directionAndType & 0xF0) >> 4;
                        wrapper.write(Types.VAR_INT, type);
                        wrapper.passthrough(Types.BYTE); // Icon X
                        wrapper.passthrough(Types.BYTE); // Icon Z
                        byte direction = (byte) (directionAndType & 0x0F);
                        wrapper.write(Types.BYTE, direction);
                        wrapper.write(Types.OPTIONAL_COMPONENT, null); // Display Name
                    }
                });
            }
        });
        registerClientbound(ClientboundPackets1_12_1.RECIPE, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // action
                map(Types.BOOLEAN); // crafting book open
                map(Types.BOOLEAN); // crafting filter active
                handler(wrapper -> {
                    wrapper.write(Types.BOOLEAN, false); // smelting book open
                    wrapper.write(Types.BOOLEAN, false); // smelting filter active
                });
                handler(wrapper -> {
                    int action = wrapper.get(Types.VAR_INT, 0);
                    for (int i = 0; i < (action == 0 ? 2 : 1); i++) {
                        int[] ids = wrapper.read(Types.VAR_INT_ARRAY_PRIMITIVE);
                        String[] stringIds = new String[ids.length];
                        for (int j = 0; j < ids.length; j++) {
                            stringIds[j] = "viaversion:legacy/" + ids[j];
                        }
                        wrapper.write(Types.STRING_ARRAY, stringIds);
                    }
                    if (action == 0) {
                        wrapper.create(ClientboundPackets1_13.UPDATE_RECIPES, w -> writeDeclareRecipes(w)).send(Protocol1_12_2To1_13.class);
                    }
                });
            }
        });

        registerClientbound(ClientboundPackets1_12_1.SET_OBJECTIVE, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); // 0 - Objective name
                map(Types.BYTE); // 1 - Mode
                handler(wrapper -> {
                    byte mode = wrapper.get(Types.BYTE, 0);
                    // On create or update
                    if (mode == 0 || mode == 2) {
                        String value = wrapper.read(Types.STRING); // Value
                        wrapper.write(Types.COMPONENT, ComponentUtil.legacyToJson(value));

                        String type = wrapper.read(Types.STRING);
                        // integer or hearts
                        wrapper.write(Types.VAR_INT, type.equals("integer") ? 0 : 1);
                    }
                });
            }
        });

        registerClientbound(ClientboundPackets1_12_1.SET_PLAYER_TEAM, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); // 0 - Team Name
                map(Types.BYTE); // 1 - Mode

                handler(wrapper -> {
                    byte action = wrapper.get(Types.BYTE, 0);

                    if (action == 0 || action == 2) {
                        String displayName = wrapper.read(Types.STRING); // Display Name
                        wrapper.write(Types.COMPONENT, ComponentUtil.legacyToJson(displayName));

                        String prefix = wrapper.read(Types.STRING); // Prefix moved
                        String suffix = wrapper.read(Types.STRING); // Suffix moved

                        wrapper.passthrough(Types.BYTE); // Flags

                        wrapper.passthrough(Types.STRING); // Name Tag Visibility
                        wrapper.passthrough(Types.STRING); // Collision rule

                        // Handle new colors
                        int colour = wrapper.read(Types.BYTE).intValue();
                        if (colour == -1) {
                            colour = 21; // -1 changed to 21
                        }

                        if (Via.getConfig().is1_13TeamColourFix()) {
                            char lastColorChar = getLastColorChar(prefix);
                            colour = ChatColorUtil.getColorOrdinal(lastColorChar);
                            suffix = ChatColorUtil.COLOR_CHAR + Character.toString(lastColorChar) + suffix;
                        }

                        wrapper.write(Types.VAR_INT, colour);

                        wrapper.write(Types.COMPONENT, ComponentUtil.legacyToJson(prefix)); // Prefix
                        wrapper.write(Types.COMPONENT, ComponentUtil.legacyToJson(suffix)); // Suffix
                    }

                    if (action == 0 || action == 3 || action == 4) {
                        String[] names = wrapper.read(Types.STRING_ARRAY); // Entities
                        for (int i = 0; i < names.length; i++) {
                            names[i] = rewriteTeamMemberName(names[i]);
                        }
                        wrapper.write(Types.STRING_ARRAY, names);
                    }
                });

            }
        });
        registerClientbound(ClientboundPackets1_12_1.SET_SCORE, wrapper -> {
            String displayName = wrapper.read(Types.STRING); // Display Name
            displayName = rewriteTeamMemberName(displayName);
            wrapper.write(Types.STRING, displayName);
        });

        componentRewriter.registerTitle(ClientboundPackets1_12_1.SET_TITLES);

        // New 0x4C - Stop Sound

        new SoundRewriter<>(this).registerSound(ClientboundPackets1_12_1.SOUND);

        registerClientbound(ClientboundPackets1_12_1.TAB_LIST, wrapper -> {
            componentRewriter.processText(wrapper.user(), wrapper.passthrough(Types.COMPONENT));
            componentRewriter.processText(wrapper.user(), wrapper.passthrough(Types.COMPONENT));
        });

        registerClientbound(ClientboundPackets1_12_1.UPDATE_ADVANCEMENTS, wrapper -> {
            wrapper.passthrough(Types.BOOLEAN); // Reset/clear
            int size = wrapper.passthrough(Types.VAR_INT); // Mapping size

            for (int i = 0; i < size; i++) {
                wrapper.passthrough(Types.STRING); // Identifier
                wrapper.passthrough(Types.OPTIONAL_STRING); // Parent

                // Display data
                if (wrapper.passthrough(Types.BOOLEAN)) {
                    componentRewriter.processText(wrapper.user(), wrapper.passthrough(Types.COMPONENT)); // Title
                    componentRewriter.processText(wrapper.user(), wrapper.passthrough(Types.COMPONENT)); // Description
                    Item icon = wrapper.read(Types.ITEM1_8);
                    itemRewriter.handleItemToClient(wrapper.user(), icon);
                    wrapper.write(Types.ITEM1_13, icon); // Translate item to flat item
                    wrapper.passthrough(Types.VAR_INT); // Frame type
                    int flags = wrapper.passthrough(Types.INT); // Flags
                    if ((flags & 1) != 0) {
                        wrapper.passthrough(Types.STRING); // Background texture
                    }
                    wrapper.passthrough(Types.FLOAT); // X
                    wrapper.passthrough(Types.FLOAT); // Y
                }

                wrapper.passthrough(Types.STRING_ARRAY); // Criteria

                int arrayLength = wrapper.passthrough(Types.VAR_INT);
                for (int array = 0; array < arrayLength; array++) {
                    wrapper.passthrough(Types.STRING_ARRAY); // String array
                }
            }
        });


        // Incoming packets
        // New packet 0x02 - Login Plugin Message
        cancelServerbound(State.LOGIN, ServerboundLoginPackets.CUSTOM_QUERY_ANSWER.getId());

        // New 0x01 - Query Block NBT
        cancelServerbound(ServerboundPackets1_13.BLOCK_ENTITY_TAG_QUERY);

        // Tab-Complete
        registerServerbound(ServerboundPackets1_13.COMMAND_SUGGESTION, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    // Disable auto-complete if configured
                    if (Via.getConfig().isDisable1_13AutoComplete()) {
                        wrapper.cancel();
                    }
                    int tid = wrapper.read(Types.VAR_INT);
                    // Save transaction id
                    wrapper.user().get(TabCompleteTracker.class).setTransactionId(tid);
                });
                // Prepend /
                map(Types.STRING, new ValueTransformer<>(Types.STRING) {
                    @Override
                    public String transform(PacketWrapper wrapper, String inputValue) {
                        wrapper.user().get(TabCompleteTracker.class).setInput(inputValue);
                        return "/" + inputValue;
                    }
                });
                // Fake the end of the packet
                handler(wrapper -> {
                    wrapper.write(Types.BOOLEAN, false);
                    final BlockPosition playerLookTarget = Via.getManager().getProviders().get(PlayerLookTargetProvider.class).getPlayerLookTarget(wrapper.user());
                    wrapper.write(Types.OPTIONAL_POSITION1_8, playerLookTarget);
                    if (!wrapper.isCancelled() && Via.getConfig().get1_13TabCompleteDelay() > 0) {
                        TabCompleteTracker tracker = wrapper.user().get(TabCompleteTracker.class);
                        wrapper.cancel();
                        tracker.setTimeToSend(System.currentTimeMillis() + Via.getConfig().get1_13TabCompleteDelay() * 50L);
                        tracker.setLastTabComplete(wrapper.get(Types.STRING, 0));
                    }
                });
            }
        });

        // New 0x0A - Edit book -> Plugin Message
        registerServerbound(ServerboundPackets1_13.EDIT_BOOK, ServerboundPackets1_12_1.CUSTOM_PAYLOAD, wrapper -> {
            Item item = wrapper.read(Types.ITEM1_13);
            boolean isSigning = wrapper.read(Types.BOOLEAN);

            itemRewriter.handleItemToServer(wrapper.user(), item);

            wrapper.write(Types.STRING, isSigning ? "MC|BSign" : "MC|BEdit"); // Channel
            wrapper.write(Types.ITEM1_8, item);
        });

        // New 0x0C - Query Entity NBT
        cancelServerbound(ServerboundPackets1_13.ENTITY_TAG_QUERY);

        // New 0x15 - Pick Item -> Plugin Message
        registerServerbound(ServerboundPackets1_13.PICK_ITEM, ServerboundPackets1_12_1.CUSTOM_PAYLOAD, wrapper -> {
            wrapper.write(Types.STRING, "MC|PickItem"); // Channel
        });

        registerServerbound(ServerboundPackets1_13.PLACE_RECIPE, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.BYTE); // Window id

                handler(wrapper -> {
                    String s = wrapper.read(Types.STRING);
                    Integer id;
                    if (s.length() < 19 || (id = Ints.tryParse(s.substring(18))) == null) {
                        wrapper.cancel();
                        return;
                    }

                    wrapper.write(Types.VAR_INT, id);
                });
            }
        });

        registerServerbound(ServerboundPackets1_13.RECIPE_BOOK_UPDATE, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Type

                handler(wrapper -> {
                    int type = wrapper.get(Types.VAR_INT, 0);

                    if (type == 0) {
                        String s = wrapper.read(Types.STRING);
                        Integer id;
                        // Custom recipes
                        if (s.length() < 19 || (id = Ints.tryParse(s.substring(18))) == null) {
                            wrapper.cancel();
                            return;
                        }

                        wrapper.write(Types.INT, id);
                    }
                    if (type == 1) {
                        wrapper.passthrough(Types.BOOLEAN); // Crafting Recipe Book Open
                        wrapper.passthrough(Types.BOOLEAN); // Crafting Recipe Filter Active
                        wrapper.read(Types.BOOLEAN); // Smelting Recipe Book Open | IGNORE NEW 1.13 FIELD
                        wrapper.read(Types.BOOLEAN); // Smelting Recipe Filter Active | IGNORE NEW 1.13 FIELD
                    }
                });
            }
        });

        // New 0x1C - Name Item -> Plugin Message
        registerServerbound(ServerboundPackets1_13.RENAME_ITEM, ServerboundPackets1_12_1.CUSTOM_PAYLOAD, wrapper -> {
            wrapper.write(Types.STRING, "MC|ItemName"); // Channel
        });

        // New 0x1F - Select Trade -> Plugin Message
        registerServerbound(ServerboundPackets1_13.SELECT_TRADE, ServerboundPackets1_12_1.CUSTOM_PAYLOAD, new PacketHandlers() {
            @Override
            public void register() {
                create(Types.STRING, "MC|TrSel"); // Channel
                map(Types.VAR_INT, Types.INT); // Slot
            }
        });

        // New 0x20 - Set Beacon Effect -> Plugin Message
        registerServerbound(ServerboundPackets1_13.SET_BEACON, ServerboundPackets1_12_1.CUSTOM_PAYLOAD, new PacketHandlers() {
            @Override
            public void register() {
                create(Types.STRING, "MC|Beacon"); // Channel
                map(Types.VAR_INT, Types.INT); // Primary Effect
                map(Types.VAR_INT, Types.INT); // Secondary Effect
            }
        });

        // New 0x22 - Update Command Block -> Plugin Message
        registerServerbound(ServerboundPackets1_13.SET_COMMAND_BLOCK, ServerboundPackets1_12_1.CUSTOM_PAYLOAD, new PacketHandlers() {
            @Override
            public void register() {
                create(Types.STRING, "MC|AutoCmd"); // Channel
                handler(POS_TO_3_INT);
                map(Types.STRING); // Command
                handler(wrapper -> {
                    int mode = wrapper.read(Types.VAR_INT);
                    byte flags = wrapper.read(Types.BYTE);

                    String stringMode = mode == 0 ? "SEQUENCE"
                        : mode == 1 ? "AUTO"
                        : "REDSTONE";

                    wrapper.write(Types.BOOLEAN, (flags & 0x1) != 0); // Track output
                    wrapper.write(Types.STRING, stringMode);
                    wrapper.write(Types.BOOLEAN, (flags & 0x2) != 0); // Is conditional
                    wrapper.write(Types.BOOLEAN, (flags & 0x4) != 0); // Automatic
                });
            }
        });

        // New 0x23 - Update Command Block Minecart -> Plugin Message
        registerServerbound(ServerboundPackets1_13.SET_COMMAND_MINECART, ServerboundPackets1_12_1.CUSTOM_PAYLOAD, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    wrapper.write(Types.STRING, "MC|AdvCmd");
                    wrapper.write(Types.BYTE, (byte) 1); // Type 1 for Entity
                });
                map(Types.VAR_INT, Types.INT); // Entity id
            }
        });

        // 0x1B -> 0x24 in InventoryPackets

        // New 0x25 - Update Structure Block -> Message Channel
        registerServerbound(ServerboundPackets1_13.SET_STRUCTURE_BLOCK, ServerboundPackets1_12_1.CUSTOM_PAYLOAD, new PacketHandlers() {
            @Override
            public void register() {
                create(Types.STRING, "MC|Struct"); // Channel
                handler(POS_TO_3_INT);
                map(Types.VAR_INT, new ValueTransformer<>(Types.BYTE) { // Action
                    @Override
                    public Byte transform(PacketWrapper wrapper, Integer action) {
                        return (byte) (action + 1);
                    }
                }); // Action
                map(Types.VAR_INT, new ValueTransformer<>(Types.STRING) {
                    @Override
                    public String transform(PacketWrapper wrapper, Integer mode) {
                        return mode == 0 ? "SAVE"
                            : mode == 1 ? "LOAD"
                            : mode == 2 ? "CORNER"
                            : "DATA";
                    }
                });
                map(Types.STRING); // Name
                map(Types.BYTE, Types.INT); // Offset X
                map(Types.BYTE, Types.INT); // Offset Y
                map(Types.BYTE, Types.INT); // Offset Z
                map(Types.BYTE, Types.INT); // Size X
                map(Types.BYTE, Types.INT); // Size Y
                map(Types.BYTE, Types.INT); // Size Z
                map(Types.VAR_INT, new ValueTransformer<>(Types.STRING) { // Mirror
                    @Override
                    public String transform(PacketWrapper wrapper, Integer mirror) {
                        return mirror == 0 ? "NONE"
                            : mirror == 1 ? "LEFT_RIGHT"
                            : "FRONT_BACK";
                    }
                });
                map(Types.VAR_INT, new ValueTransformer<>(Types.STRING) { // Rotation
                    @Override
                    public String transform(PacketWrapper wrapper, Integer rotation) {
                        return rotation == 0 ? "NONE"
                            : rotation == 1 ? "CLOCKWISE_90"
                            : rotation == 2 ? "CLOCKWISE_180"
                            : "COUNTERCLOCKWISE_90";
                    }
                });
                map(Types.STRING);
                handler(wrapper -> {
                    float integrity = wrapper.read(Types.FLOAT);
                    long seed = wrapper.read(Types.VAR_LONG);
                    byte flags = wrapper.read(Types.BYTE);

                    wrapper.write(Types.BOOLEAN, (flags & 0x1) != 0); // Ignore Entities
                    wrapper.write(Types.BOOLEAN, (flags & 0x2) != 0); // Show air
                    wrapper.write(Types.BOOLEAN, (flags & 0x4) != 0); // Show bounding box
                    wrapper.write(Types.FLOAT, integrity);
                    wrapper.write(Types.VAR_LONG, seed);
                });
            }
        });
    }

    private void writeDeclareRecipes(PacketWrapper recipesPacket) {
        recipesPacket.write(Types.VAR_INT, RecipeData.recipes.size());
        for (Map.Entry<String, RecipeData.Recipe> entry : RecipeData.recipes.entrySet()) {
            RecipeData.Recipe recipe = entry.getValue();
            recipesPacket.write(Types.STRING, entry.getKey()); // Id
            recipesPacket.write(Types.STRING, recipe.type());

            // Clone item arrays because array and item are mutable, also make sure the array type is the interface
            switch (recipe.type()) {
                case "crafting_shapeless" -> {
                    recipesPacket.write(Types.STRING, recipe.group());
                    recipesPacket.write(Types.VAR_INT, recipe.ingredients().length);
                    for (Item[] ingredient : recipe.ingredients()) {
                        Item[] clone = new Item[ingredient.length];
                        for (int i = 0; i < ingredient.length; i++) {
                            if (ingredient[i] == null) {
                                continue;
                            }

                            clone[i] = ingredient[i].copy();
                        }
                        recipesPacket.write(Types.ITEM1_13_ARRAY, clone);
                    }
                    recipesPacket.write(Types.ITEM1_13, recipe.result().copy());
                }
                case "crafting_shaped" -> {
                    recipesPacket.write(Types.VAR_INT, recipe.width());
                    recipesPacket.write(Types.VAR_INT, recipe.height());
                    recipesPacket.write(Types.STRING, recipe.group());
                    for (Item[] ingredient : recipe.ingredients()) {
                        Item[] clone = new Item[ingredient.length];
                        for (int i = 0; i < ingredient.length; i++) {
                            if (ingredient[i] == null) {
                                continue;
                            }

                            clone[i] = ingredient[i].copy();
                        }
                        recipesPacket.write(Types.ITEM1_13_ARRAY, clone);
                    }
                    recipesPacket.write(Types.ITEM1_13, recipe.result().copy());
                }
                case "smelting" -> {
                    recipesPacket.write(Types.STRING, recipe.group());
                    Item[] ingredient = new Item[recipe.ingredient().length];
                    for (int i = 0; i < ingredient.length; i++) {
                        if (recipe.ingredient()[i] == null) {
                            continue;
                        }

                        ingredient[i] = recipe.ingredient()[i].copy();
                    }
                    recipesPacket.write(Types.ITEM1_13_ARRAY, ingredient);
                    recipesPacket.write(Types.ITEM1_13, recipe.result().copy());
                    recipesPacket.write(Types.FLOAT, recipe.experience());
                    recipesPacket.write(Types.VAR_INT, recipe.cookingTime());
                }
            }
        }
    }

    @Override
    protected void onMappingDataLoaded() {
        ConnectionData.init();
        RecipeData.init();
        BlockIdData.init();

        Types1_13.PARTICLE.rawFiller()
            .reader(3, ParticleType.Readers.BLOCK)
            .reader(20, ParticleType.Readers.DUST)
            .reader(11, ParticleType.Readers.DUST)
            .reader(27, ParticleType.Readers.ITEM1_13);

        if (Via.getConfig().isServersideBlockConnections() && Via.getManager().getProviders().get(BlockConnectionProvider.class) instanceof PacketBlockConnectionProvider) {
            BlockConnectionStorage.init();
        }

        super.onMappingDataLoaded();
    }

    @Override
    public void init(UserConnection userConnection) {
        userConnection.addEntityTracker(this.getClass(), new EntityTrackerBase(userConnection, EntityTypes1_13.EntityType.PLAYER));
        userConnection.addClientWorld(this.getClass(), new ClientWorld());

        userConnection.put(new TabCompleteTracker());
        userConnection.put(new BlockStorage());
        if (Via.getConfig().isServersideBlockConnections()) {
            if (Via.getManager().getProviders().get(BlockConnectionProvider.class) instanceof PacketBlockConnectionProvider) {
                userConnection.put(new BlockConnectionStorage());
            }
        }
    }

    @Override
    public void register(ViaProviders providers) {
        providers.register(BlockEntityProvider.class, new BlockEntityProvider());
        providers.register(PaintingProvider.class, new PaintingProvider());
        providers.register(PlayerLookTargetProvider.class, new PlayerLookTargetProvider());
    }

    // Based on method from https://github.com/Bukkit/Bukkit/blob/master/src/main/java/org/bukkit/ChatColor.java
    public char getLastColorChar(String input) {
        int length = input.length();
        for (int index = length - 1; index > -1; index--) {
            char section = input.charAt(index);
            if (section == ChatColorUtil.COLOR_CHAR && index < length - 1) {
                char c = input.charAt(index + 1);
                if (ChatColorUtil.isColorCode(c) && !FORMATTING_CODES.contains(c)) {
                    return c;
                }
            }
        }
        return 'r';
    }

    protected String rewriteTeamMemberName(String name) {
        // The Display Name is just colours which overwrites the suffix
        // It also overwrites for ANY colour in name but most plugins
        // will just send colour as 'invisible' character
        if (ChatColorUtil.stripColor(name).isEmpty()) {
            StringBuilder newName = new StringBuilder();
            for (int i = 1; i < name.length(); i += 2) {
                char colorChar = name.charAt(i);
                Character rewrite = SCOREBOARD_TEAM_NAME_REWRITE.get(colorChar);
                if (rewrite == null) {
                    rewrite = colorChar;
                }
                newName.append(ChatColorUtil.COLOR_CHAR).append(rewrite);
            }
            name = newName.toString();
        }
        return name;
    }

    @Override
    public MappingData1_13 getMappingData() {
        return MAPPINGS;
    }

    @Override
    public ProtocolLogger getLogger() {
        return LOGGER;
    }

    @Override
    public EntityPacketRewriter1_13 getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    public ItemPacketRewriter1_13 getItemRewriter() {
        return itemRewriter;
    }

    public ComponentRewriter1_13 getComponentRewriter() {
        return componentRewriter;
    }
}
