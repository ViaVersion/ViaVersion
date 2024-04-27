/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2;

import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_13;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.remapper.ValueTransformer;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import com.viaversion.viaversion.api.type.types.version.Types1_13;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import com.viaversion.viaversion.protocols.protocol1_12_1to1_12.ClientboundPackets1_12_1;
import com.viaversion.viaversion.protocols.protocol1_12_1to1_12.ServerboundPackets1_12_1;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.blockconnections.ConnectionData;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.blockconnections.providers.BlockConnectionProvider;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.blockconnections.providers.PacketBlockConnectionProvider;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data.BlockIdData;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data.ComponentRewriter1_13;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data.MappingData;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data.RecipeData;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data.StatisticData;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data.StatisticMappings;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.metadata.MetadataRewriter1_13To1_12_2;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.packets.EntityPackets;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.packets.InventoryPackets;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.packets.WorldPackets;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.providers.BlockEntityProvider;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.providers.PaintingProvider;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.providers.PlayerLookTargetProvider;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.storage.BlockConnectionStorage;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.storage.BlockStorage;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.storage.TabCompleteTracker;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.util.ChatColorUtil;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.GsonUtil;
import com.viaversion.viaversion.util.IdAndData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class Protocol1_13To1_12_2 extends AbstractProtocol<ClientboundPackets1_12_1, ClientboundPackets1_13, ServerboundPackets1_12_1, ServerboundPackets1_13> {

    public static final MappingData MAPPINGS = new MappingData();
    // These are arbitrary rewrite values, it just needs an invalid color code character.
    private static final Map<Character, Character> SCOREBOARD_TEAM_NAME_REWRITE = new HashMap<>();
    private static final Set<Character> FORMATTING_CODES = Sets.newHashSet('k', 'l', 'm', 'n', 'o', 'r');
    private final MetadataRewriter1_13To1_12_2 entityRewriter = new MetadataRewriter1_13To1_12_2(this);
    private final InventoryPackets itemRewriter = new InventoryPackets(this);
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

    public Protocol1_13To1_12_2() {
        super(ClientboundPackets1_12_1.class, ClientboundPackets1_13.class, ServerboundPackets1_12_1.class, ServerboundPackets1_13.class);
    }

    public static final PacketHandler POS_TO_3_INT = wrapper -> {
        Position position = wrapper.read(Type.POSITION1_8);
        wrapper.write(Type.INT, position.x());
        wrapper.write(Type.INT, position.y());
        wrapper.write(Type.INT, position.z());
    };

    public static final PacketHandler SEND_DECLARE_COMMANDS_AND_TAGS =
            w -> {
                // Send fake declare commands
                w.create(ClientboundPackets1_13.DECLARE_COMMANDS, wrapper -> {
                    wrapper.write(Type.VAR_INT, 2); // Size
                    // Write root node
                    wrapper.write(Type.BYTE, (byte) 0); // Mark as command
                    wrapper.write(Type.VAR_INT_ARRAY_PRIMITIVE, new int[]{1}); // 1 child at index 1

                    // Write arg node
                    wrapper.write(Type.BYTE, (byte) (0x02 | 0x04 | 0x10)); // Mark as command
                    wrapper.write(Type.VAR_INT_ARRAY_PRIMITIVE, new int[0]); // No children
                    // Extra data
                    wrapper.write(Type.STRING, "args"); // Arg name
                    wrapper.write(Type.STRING, "brigadier:string");
                    wrapper.write(Type.VAR_INT, 2); // Greedy
                    wrapper.write(Type.STRING, "minecraft:ask_server"); // Ask server

                    wrapper.write(Type.VAR_INT, 0); // Root node index
                }).scheduleSend(Protocol1_13To1_12_2.class);

                // Send tags packet
                w.create(ClientboundPackets1_13.TAGS, wrapper -> {
                    wrapper.write(Type.VAR_INT, MAPPINGS.getBlockTags().size()); // block tags
                    for (Map.Entry<String, int[]> tag : MAPPINGS.getBlockTags().entrySet()) {
                        wrapper.write(Type.STRING, tag.getKey());
                        // Needs copy as other protocols may modify it
                        wrapper.write(Type.VAR_INT_ARRAY_PRIMITIVE, tag.getValue());
                    }
                    wrapper.write(Type.VAR_INT, MAPPINGS.getItemTags().size()); // item tags
                    for (Map.Entry<String, int[]> tag : MAPPINGS.getItemTags().entrySet()) {
                        wrapper.write(Type.STRING, tag.getKey());
                        // Needs copy as other protocols may modify it
                        wrapper.write(Type.VAR_INT_ARRAY_PRIMITIVE, tag.getValue());
                    }
                    wrapper.write(Type.VAR_INT, MAPPINGS.getFluidTags().size()); // fluid tags
                    for (Map.Entry<String, int[]> tag : MAPPINGS.getFluidTags().entrySet()) {
                        wrapper.write(Type.STRING, tag.getKey());
                        // Needs copy as other protocols may modify it
                        wrapper.write(Type.VAR_INT_ARRAY_PRIMITIVE, tag.getValue());
                    }
                }).scheduleSend(Protocol1_13To1_12_2.class);
            };

    @Override
    protected void registerPackets() {
        super.registerPackets();

        EntityPackets.register(this);
        WorldPackets.register(this);

        registerClientbound(State.LOGIN, 0x00, 0x00, wrapper -> componentRewriter.processText(wrapper.user(), wrapper.passthrough(Type.COMPONENT)));

        registerClientbound(State.STATUS, 0x00, 0x00, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING);
                handler(wrapper -> {
                    String response = wrapper.get(Type.STRING, 0);
                    try {
                        JsonObject json = GsonUtil.getGson().fromJson(response, JsonObject.class);
                        if (json.has("favicon")) {
                            json.addProperty("favicon", json.get("favicon").getAsString().replace("\n", ""));
                        }
                        wrapper.set(Type.STRING, 0, GsonUtil.getGson().toJson(json));
                    } catch (JsonParseException e) {
                        Via.getPlatform().getLogger().log(Level.SEVERE, "Error transforming status response", e);
                    }
                });
            }
        });

        // New packet 0x04 - Login Plugin Message

        // Statistics
        registerClientbound(ClientboundPackets1_12_1.STATISTICS, wrapper -> {
            int size = wrapper.read(Type.VAR_INT);
            List<StatisticData> remappedStats = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                String name = wrapper.read(Type.STRING);
                String[] split = name.split("\\.");
                int categoryId = 0;
                int newId = -1;
                int value = wrapper.read(Type.VAR_INT);
                if (split.length == 2) {
                    // Custom types
                    categoryId = 8;
                    Integer newIdRaw = StatisticMappings.CUSTOM_STATS.get(name);
                    if (newIdRaw != null) {
                        newId = newIdRaw;
                    } else {
                        Via.getPlatform().getLogger().warning("Could not find 1.13 -> 1.12.2 statistic mapping for " + name);
                    }
                } else if (split.length > 2) {
                    String category = split[1];
                    //TODO convert string ids (blocks, items, entities)
                    switch (category) {
                        case "mineBlock":
                            categoryId = 0;
                            break;
                        case "craftItem":
                            categoryId = 1;
                            break;
                        case "useItem":
                            categoryId = 2;
                            break;
                        case "breakItem":
                            categoryId = 3;
                            break;
                        case "pickup":
                            categoryId = 4;
                            break;
                        case "drop":
                            categoryId = 5;
                            break;
                        case "killEntity":
                            categoryId = 6;
                            break;
                        case "entityKilledBy":
                            categoryId = 7;
                            break;
                    }
                }
                if (newId != -1)
                    remappedStats.add(new StatisticData(categoryId, newId, value));
            }

            wrapper.write(Type.VAR_INT, remappedStats.size()); // size
            for (StatisticData stat : remappedStats) {
                wrapper.write(Type.VAR_INT, stat.getCategoryId()); // category id
                wrapper.write(Type.VAR_INT, stat.getNewId()); // statistics id
                wrapper.write(Type.VAR_INT, stat.getValue()); // value
            }
        });


        componentRewriter.registerBossBar(ClientboundPackets1_12_1.BOSSBAR);
        componentRewriter.registerComponentPacket(ClientboundPackets1_12_1.CHAT_MESSAGE);

        registerClientbound(ClientboundPackets1_12_1.TAB_COMPLETE, wrapper -> {
            wrapper.write(Type.VAR_INT, wrapper.user().get(TabCompleteTracker.class).getTransactionId());

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
                wrapper.write(Type.OPTIONAL_COMPONENT, null); // Tooltip
            }
        });

        registerClientbound(ClientboundPackets1_12_1.OPEN_WINDOW, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.UNSIGNED_BYTE); // Id
                map(Type.STRING); // Window type
                handler(wrapper -> componentRewriter.processText(wrapper.user(), wrapper.passthrough(Type.COMPONENT))); // Title
            }
        });

        registerClientbound(ClientboundPackets1_12_1.COOLDOWN, wrapper -> {
            int item = wrapper.read(Type.VAR_INT);
            int ticks = wrapper.read(Type.VAR_INT);
            wrapper.cancel();
            if (item == 383) { // Spawn egg
                for (int i = 0; i < 44; i++) {
                    int newItem = MAPPINGS.getItemMappings().getNewId(item << 16 | i);
                    if (newItem != -1) {
                        PacketWrapper packet = wrapper.create(ClientboundPackets1_13.COOLDOWN);
                        packet.write(Type.VAR_INT, newItem);
                        packet.write(Type.VAR_INT, ticks);
                        packet.send(Protocol1_13To1_12_2.class);
                    } else {
                        break;
                    }
                }
            } else {
                for (int i = 0; i < 16; i++) {
                    int newItem = MAPPINGS.getItemMappings().getNewId(IdAndData.toRawData(item, i));
                    if (newItem != -1) {
                        PacketWrapper packet = wrapper.create(ClientboundPackets1_13.COOLDOWN);
                        packet.write(Type.VAR_INT, newItem);
                        packet.write(Type.VAR_INT, ticks);
                        packet.send(Protocol1_13To1_12_2.class);
                    } else {
                        break;
                    }
                }
            }
        });

        componentRewriter.registerComponentPacket(ClientboundPackets1_12_1.DISCONNECT);

        registerClientbound(ClientboundPackets1_12_1.EFFECT, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); // Effect Id
                map(Type.POSITION1_8); // Location
                map(Type.INT); // Data
                handler(wrapper -> {
                    int id = wrapper.get(Type.INT, 0);
                    int data = wrapper.get(Type.INT, 1);
                    if (id == 1010) { // Play record
                        wrapper.set(Type.INT, 1, getMappingData().getItemMappings().getNewId(IdAndData.toRawData(data)));
                    } else if (id == 2001) { // Block break + block break sound
                        int blockId = data & 0xFFF;
                        int blockData = data >> 12;
                        wrapper.set(Type.INT, 1, WorldPackets.toNewId(IdAndData.toRawData(blockId, blockData)));
                    }
                });
            }
        });

        registerClientbound(ClientboundPackets1_12_1.CRAFT_RECIPE_RESPONSE, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.BYTE);
                handler(wrapper -> wrapper.write(Type.STRING, "viaversion:legacy/" + wrapper.read(Type.VAR_INT)));
            }
        });

        componentRewriter.registerCombatEvent(ClientboundPackets1_12_1.COMBAT_EVENT);

        registerClientbound(ClientboundPackets1_12_1.MAP_DATA, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Map id
                map(Type.BYTE); // 1 - Scale
                map(Type.BOOLEAN); // 2 - Tracking Position
                handler(wrapper -> {
                    int iconCount = wrapper.passthrough(Type.VAR_INT);
                    for (int i = 0; i < iconCount; i++) {
                        byte directionAndType = wrapper.read(Type.BYTE);
                        int type = (directionAndType & 0xF0) >> 4;
                        wrapper.write(Type.VAR_INT, type);
                        wrapper.passthrough(Type.BYTE); // Icon X
                        wrapper.passthrough(Type.BYTE); // Icon Z
                        byte direction = (byte) (directionAndType & 0x0F);
                        wrapper.write(Type.BYTE, direction);
                        wrapper.write(Type.OPTIONAL_COMPONENT, null); // Display Name
                    }
                });
            }
        });
        registerClientbound(ClientboundPackets1_12_1.UNLOCK_RECIPES, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // action
                map(Type.BOOLEAN); // crafting book open
                map(Type.BOOLEAN); // crafting filter active
                handler(wrapper -> {
                    wrapper.write(Type.BOOLEAN, false); // smelting book open
                    wrapper.write(Type.BOOLEAN, false); // smelting filter active
                });
                handler(wrapper -> {
                    int action = wrapper.get(Type.VAR_INT, 0);
                    for (int i = 0; i < (action == 0 ? 2 : 1); i++) {
                        int[] ids = wrapper.read(Type.VAR_INT_ARRAY_PRIMITIVE);
                        String[] stringIds = new String[ids.length];
                        for (int j = 0; j < ids.length; j++) {
                            stringIds[j] = "viaversion:legacy/" + ids[j];
                        }
                        wrapper.write(Type.STRING_ARRAY, stringIds);
                    }
                    if (action == 0) {
                        wrapper.create(ClientboundPackets1_13.DECLARE_RECIPES, w -> writeDeclareRecipes(w)).send(Protocol1_13To1_12_2.class);
                    }
                });
            }
        });

        registerClientbound(ClientboundPackets1_12_1.RESPAWN, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); // 0 - Dimension ID
                handler(wrapper -> {
                    ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
                    int dimensionId = wrapper.get(Type.INT, 0);
                    clientWorld.setEnvironment(dimensionId);

                    if (Via.getConfig().isServersideBlockConnections()) {
                        ConnectionData.clearBlockStorage(wrapper.user());
                    }
                });
                handler(SEND_DECLARE_COMMANDS_AND_TAGS);
            }
        });

        registerClientbound(ClientboundPackets1_12_1.SCOREBOARD_OBJECTIVE, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // 0 - Objective name
                map(Type.BYTE); // 1 - Mode
                handler(wrapper -> {
                    byte mode = wrapper.get(Type.BYTE, 0);
                    // On create or update
                    if (mode == 0 || mode == 2) {
                        String value = wrapper.read(Type.STRING); // Value
                        wrapper.write(Type.COMPONENT, ComponentUtil.legacyToJson(value));

                        String type = wrapper.read(Type.STRING);
                        // integer or hearts
                        wrapper.write(Type.VAR_INT, type.equals("integer") ? 0 : 1);
                    }
                });
            }
        });

        registerClientbound(ClientboundPackets1_12_1.TEAMS, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // 0 - Team Name
                map(Type.BYTE); // 1 - Mode

                handler(wrapper -> {
                    byte action = wrapper.get(Type.BYTE, 0);

                    if (action == 0 || action == 2) {
                        String displayName = wrapper.read(Type.STRING); // Display Name
                        wrapper.write(Type.COMPONENT, ComponentUtil.legacyToJson(displayName));

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
                            char lastColorChar = getLastColorChar(prefix);
                            colour = ChatColorUtil.getColorOrdinal(lastColorChar);
                            suffix = ChatColorUtil.COLOR_CHAR + Character.toString(lastColorChar) + suffix;
                        }

                        wrapper.write(Type.VAR_INT, colour);

                        wrapper.write(Type.COMPONENT, ComponentUtil.legacyToJson(prefix)); // Prefix
                        wrapper.write(Type.COMPONENT, ComponentUtil.legacyToJson(suffix)); // Suffix
                    }

                    if (action == 0 || action == 3 || action == 4) {
                        String[] names = wrapper.read(Type.STRING_ARRAY); // Entities
                        for (int i = 0; i < names.length; i++) {
                            names[i] = rewriteTeamMemberName(names[i]);
                        }
                        wrapper.write(Type.STRING_ARRAY, names);
                    }
                });

            }
        });
        registerClientbound(ClientboundPackets1_12_1.UPDATE_SCORE, wrapper -> {
            String displayName = wrapper.read(Type.STRING); // Display Name
            displayName = rewriteTeamMemberName(displayName);
            wrapper.write(Type.STRING, displayName);
        });

        componentRewriter.registerTitle(ClientboundPackets1_12_1.TITLE);

        // New 0x4C - Stop Sound

        new SoundRewriter<>(this).registerSound(ClientboundPackets1_12_1.SOUND);

        registerClientbound(ClientboundPackets1_12_1.TAB_LIST, wrapper -> {
            componentRewriter.processText(wrapper.user(), wrapper.passthrough(Type.COMPONENT));
            componentRewriter.processText(wrapper.user(), wrapper.passthrough(Type.COMPONENT));
        });

        registerClientbound(ClientboundPackets1_12_1.ADVANCEMENTS, wrapper -> {
            wrapper.passthrough(Type.BOOLEAN); // Reset/clear
            int size = wrapper.passthrough(Type.VAR_INT); // Mapping size

            for (int i = 0; i < size; i++) {
                wrapper.passthrough(Type.STRING); // Identifier
                wrapper.passthrough(Type.OPTIONAL_STRING); // Parent

                // Display data
                if (wrapper.passthrough(Type.BOOLEAN)) {
                    componentRewriter.processText(wrapper.user(), wrapper.passthrough(Type.COMPONENT)); // Title
                    componentRewriter.processText(wrapper.user(), wrapper.passthrough(Type.COMPONENT)); // Description
                    Item icon = wrapper.read(Type.ITEM1_8);
                    itemRewriter.handleItemToClient(wrapper.user(), icon);
                    wrapper.write(Type.ITEM1_13, icon); // Translate item to flat item
                    wrapper.passthrough(Type.VAR_INT); // Frame type
                    int flags = wrapper.passthrough(Type.INT); // Flags
                    if ((flags & 1) != 0) {
                        wrapper.passthrough(Type.STRING); // Background texture
                    }
                    wrapper.passthrough(Type.FLOAT); // X
                    wrapper.passthrough(Type.FLOAT); // Y
                }

                wrapper.passthrough(Type.STRING_ARRAY); // Criteria

                int arrayLength = wrapper.passthrough(Type.VAR_INT);
                for (int array = 0; array < arrayLength; array++) {
                    wrapper.passthrough(Type.STRING_ARRAY); // String array
                }
            }
        });


        // Incoming packets
        // New packet 0x02 - Login Plugin Message
        cancelServerbound(State.LOGIN, ServerboundLoginPackets.CUSTOM_QUERY_ANSWER.getId());

        // New 0x01 - Query Block NBT
        cancelServerbound(ServerboundPackets1_13.QUERY_BLOCK_NBT);

        // Tab-Complete
        registerServerbound(ServerboundPackets1_13.TAB_COMPLETE, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    // Disable auto-complete if configured
                    if (Via.getConfig().isDisable1_13AutoComplete()) {
                        wrapper.cancel();
                    }
                    int tid = wrapper.read(Type.VAR_INT);
                    // Save transaction id
                    wrapper.user().get(TabCompleteTracker.class).setTransactionId(tid);
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
                handler(wrapper -> {
                    wrapper.write(Type.BOOLEAN, false);
                    final Position playerLookTarget = Via.getManager().getProviders().get(PlayerLookTargetProvider.class).getPlayerLookTarget(wrapper.user());
                    wrapper.write(Type.OPTIONAL_POSITION1_8, playerLookTarget);
                    if (!wrapper.isCancelled() && Via.getConfig().get1_13TabCompleteDelay() > 0) {
                        TabCompleteTracker tracker = wrapper.user().get(TabCompleteTracker.class);
                        wrapper.cancel();
                        tracker.setTimeToSend(System.currentTimeMillis() + Via.getConfig().get1_13TabCompleteDelay() * 50L);
                        tracker.setLastTabComplete(wrapper.get(Type.STRING, 0));
                    }
                });
            }
        });

        // New 0x0A - Edit book -> Plugin Message
        registerServerbound(ServerboundPackets1_13.EDIT_BOOK, ServerboundPackets1_12_1.PLUGIN_MESSAGE, wrapper -> {
            Item item = wrapper.read(Type.ITEM1_13);
            boolean isSigning = wrapper.read(Type.BOOLEAN);

            itemRewriter.handleItemToServer(wrapper.user(), item);

            wrapper.write(Type.STRING, isSigning ? "MC|BSign" : "MC|BEdit"); // Channel
            wrapper.write(Type.ITEM1_8, item);
        });

        // New 0x0C - Query Entity NBT
        cancelServerbound(ServerboundPackets1_13.ENTITY_NBT_REQUEST);

        // New 0x15 - Pick Item -> Plugin Message
        registerServerbound(ServerboundPackets1_13.PICK_ITEM, ServerboundPackets1_12_1.PLUGIN_MESSAGE, wrapper -> {
            wrapper.write(Type.STRING, "MC|PickItem"); // Channel
        });

        registerServerbound(ServerboundPackets1_13.CRAFT_RECIPE_REQUEST, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.BYTE); // Window id

                handler(wrapper -> {
                    String s = wrapper.read(Type.STRING);
                    Integer id;
                    if (s.length() < 19 || (id = Ints.tryParse(s.substring(18))) == null) {
                        wrapper.cancel();
                        return;
                    }

                    wrapper.write(Type.VAR_INT, id);
                });
            }
        });

        registerServerbound(ServerboundPackets1_13.RECIPE_BOOK_DATA, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Type

                handler(wrapper -> {
                    int type = wrapper.get(Type.VAR_INT, 0);

                    if (type == 0) {
                        String s = wrapper.read(Type.STRING);
                        Integer id;
                        // Custom recipes
                        if (s.length() < 19 || (id = Ints.tryParse(s.substring(18))) == null) {
                            wrapper.cancel();
                            return;
                        }

                        wrapper.write(Type.INT, id);
                    }
                    if (type == 1) {
                        wrapper.passthrough(Type.BOOLEAN); // Crafting Recipe Book Open
                        wrapper.passthrough(Type.BOOLEAN); // Crafting Recipe Filter Active
                        wrapper.read(Type.BOOLEAN); // Smelting Recipe Book Open | IGNORE NEW 1.13 FIELD
                        wrapper.read(Type.BOOLEAN); // Smelting Recipe Filter Active | IGNORE NEW 1.13 FIELD
                    }
                });
            }
        });

        // New 0x1C - Name Item -> Plugin Message
        registerServerbound(ServerboundPackets1_13.RENAME_ITEM, ServerboundPackets1_12_1.PLUGIN_MESSAGE, wrapper -> {
            wrapper.write(Type.STRING, "MC|ItemName"); // Channel
        });

        // New 0x1F - Select Trade -> Plugin Message
        registerServerbound(ServerboundPackets1_13.SELECT_TRADE, ServerboundPackets1_12_1.PLUGIN_MESSAGE, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    wrapper.write(Type.STRING, "MC|TrSel"); // Channel
                });
                map(Type.VAR_INT, Type.INT); // Slot
            }
        });

        // New 0x20 - Set Beacon Effect -> Plugin Message
        registerServerbound(ServerboundPackets1_13.SET_BEACON_EFFECT, ServerboundPackets1_12_1.PLUGIN_MESSAGE, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    wrapper.write(Type.STRING, "MC|Beacon"); // Channel
                });
                map(Type.VAR_INT, Type.INT); // Primary Effect
                map(Type.VAR_INT, Type.INT); // Secondary Effect
            }
        });

        // New 0x22 - Update Command Block -> Plugin Message
        registerServerbound(ServerboundPackets1_13.UPDATE_COMMAND_BLOCK, ServerboundPackets1_12_1.PLUGIN_MESSAGE, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> wrapper.write(Type.STRING, "MC|AutoCmd"));
                handler(POS_TO_3_INT);
                map(Type.STRING); // Command
                handler(wrapper -> {
                    int mode = wrapper.read(Type.VAR_INT);
                    byte flags = wrapper.read(Type.BYTE);

                    String stringMode = mode == 0 ? "SEQUENCE"
                            : mode == 1 ? "AUTO"
                            : "REDSTONE";

                    wrapper.write(Type.BOOLEAN, (flags & 0x1) != 0); // Track output
                    wrapper.write(Type.STRING, stringMode);
                    wrapper.write(Type.BOOLEAN, (flags & 0x2) != 0); // Is conditional
                    wrapper.write(Type.BOOLEAN, (flags & 0x4) != 0); // Automatic
                });
            }
        });

        // New 0x23 - Update Command Block Minecart -> Plugin Message
        registerServerbound(ServerboundPackets1_13.UPDATE_COMMAND_BLOCK_MINECART, ServerboundPackets1_12_1.PLUGIN_MESSAGE, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    wrapper.write(Type.STRING, "MC|AdvCmd");
                    wrapper.write(Type.BYTE, (byte) 1); // Type 1 for Entity
                });
                map(Type.VAR_INT, Type.INT); // Entity Id
            }
        });

        // 0x1B -> 0x24 in InventoryPackets

        // New 0x25 - Update Structure Block -> Message Channel
        registerServerbound(ServerboundPackets1_13.UPDATE_STRUCTURE_BLOCK, ServerboundPackets1_12_1.PLUGIN_MESSAGE, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    wrapper.write(Type.STRING, "MC|Struct"); // Channel
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
                handler(wrapper -> {
                    float integrity = wrapper.read(Type.FLOAT);
                    long seed = wrapper.read(Type.VAR_LONG);
                    byte flags = wrapper.read(Type.BYTE);

                    wrapper.write(Type.BOOLEAN, (flags & 0x1) != 0); // Ignore Entities
                    wrapper.write(Type.BOOLEAN, (flags & 0x2) != 0); // Show air
                    wrapper.write(Type.BOOLEAN, (flags & 0x4) != 0); // Show bounding box
                    wrapper.write(Type.FLOAT, integrity);
                    wrapper.write(Type.VAR_LONG, seed);
                });
            }
        });
    }

    private void writeDeclareRecipes(PacketWrapper recipesPacket) {
        recipesPacket.write(Type.VAR_INT, RecipeData.recipes.size());
        for (Map.Entry<String, RecipeData.Recipe> entry : RecipeData.recipes.entrySet()) {
            recipesPacket.write(Type.STRING, entry.getKey()); // Id
            recipesPacket.write(Type.STRING, entry.getValue().getType());
            switch (entry.getValue().getType()) {
                case "crafting_shapeless": {
                    recipesPacket.write(Type.STRING, entry.getValue().getGroup());
                    recipesPacket.write(Type.VAR_INT, entry.getValue().getIngredients().length);
                    for (Item[] ingredient : entry.getValue().getIngredients()) {
                        Item[] clone = ingredient.clone(); // Clone because array and item is mutable
                        for (int i = 0; i < clone.length; i++) {
                            if (clone[i] == null) continue;
                            clone[i] = new DataItem(clone[i]);
                        }
                        recipesPacket.write(Type.ITEM1_13_ARRAY, clone);
                    }
                    recipesPacket.write(Type.ITEM1_13, new DataItem(entry.getValue().getResult()));
                    break;
                }
                case "crafting_shaped": {
                    recipesPacket.write(Type.VAR_INT, entry.getValue().getWidth());
                    recipesPacket.write(Type.VAR_INT, entry.getValue().getHeight());
                    recipesPacket.write(Type.STRING, entry.getValue().getGroup());
                    for (Item[] ingredient : entry.getValue().getIngredients()) {
                        Item[] clone = ingredient.clone(); // Clone because array and item is mutable
                        for (int i = 0; i < clone.length; i++) {
                            if (clone[i] == null) continue;
                            clone[i] = new DataItem(clone[i]);
                        }
                        recipesPacket.write(Type.ITEM1_13_ARRAY, clone);
                    }
                    recipesPacket.write(Type.ITEM1_13, new DataItem(entry.getValue().getResult()));
                    break;
                }
                case "smelting": {
                    recipesPacket.write(Type.STRING, entry.getValue().getGroup());
                    Item[] clone = entry.getValue().getIngredient().clone(); // Clone because array and item is mutable
                    for (int i = 0; i < clone.length; i++) {
                        if (clone[i] == null) continue;
                        clone[i] = new DataItem(clone[i]);
                    }
                    recipesPacket.write(Type.ITEM1_13_ARRAY, clone);
                    recipesPacket.write(Type.ITEM1_13, new DataItem(entry.getValue().getResult()));
                    recipesPacket.write(Type.FLOAT, entry.getValue().getExperience());
                    recipesPacket.write(Type.VAR_INT, entry.getValue().getCookingTime());
                    break;
                }
            }
        }
    }

    @Override
    protected void onMappingDataLoaded() {
        super.onMappingDataLoaded();
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
    }

    @Override
    public void init(UserConnection userConnection) {
        userConnection.addEntityTracker(this.getClass(), new EntityTrackerBase(userConnection, EntityTypes1_13.EntityType.PLAYER));
        userConnection.put(new TabCompleteTracker());
        if (!userConnection.has(ClientWorld.class))
            userConnection.put(new ClientWorld());
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
    public MappingData getMappingData() {
        return MAPPINGS;
    }

    @Override
    public MetadataRewriter1_13To1_12_2 getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    public InventoryPackets getItemRewriter() {
        return itemRewriter;
    }

    public ComponentRewriter1_13 getComponentRewriter() {
        return componentRewriter;
    }
}
