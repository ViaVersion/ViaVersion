/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2023 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2;

import com.github.steveice10.opennbt.tag.builtin.ByteArrayTag;
import com.github.steveice10.opennbt.tag.builtin.ByteTag;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.DoubleTag;
import com.github.steveice10.opennbt.tag.builtin.FloatTag;
import com.github.steveice10.opennbt.tag.builtin.IntArrayTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.LongArrayTag;
import com.github.steveice10.opennbt.tag.builtin.LongTag;
import com.github.steveice10.opennbt.tag.builtin.NumberTag;
import com.github.steveice10.opennbt.tag.builtin.ShortTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_19_4Types;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.UUIDIntArrayType;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ClientboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ClientboundPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ServerboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ServerboundPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.rewriter.EntityPacketRewriter1_20_3;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class Protocol1_20_3To1_20_2 extends AbstractProtocol<ClientboundPackets1_20_2, ClientboundPackets1_20_2, ServerboundPackets1_20_2, ServerboundPackets1_20_2> {

    public static final MappingData MAPPINGS = new MappingDataBase("1.20.2", "1.20.3");
    private static final Set<String> BOOLEAN_TYPES = new HashSet<>(Arrays.asList(
            "interpret",
            "bold",
            "italic",
            "underlined",
            "strikethrough",
            "obfuscated"
    ));
    private final EntityPacketRewriter1_20_3 entityRewriter = new EntityPacketRewriter1_20_3(this);

    public Protocol1_20_3To1_20_2() {
        super(ClientboundPackets1_20_2.class, ClientboundPackets1_20_2.class, ServerboundPackets1_20_2.class, ServerboundPackets1_20_2.class);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        final SoundRewriter<ClientboundPackets1_20_2> soundRewriter = new SoundRewriter<>(this);
        soundRewriter.register1_19_3Sound(ClientboundPackets1_20_2.SOUND);
        soundRewriter.registerEntitySound(ClientboundPackets1_20_2.ENTITY_SOUND);

        // Components are now (mostly) written as nbt instead of json strings
        registerClientbound(ClientboundPackets1_20_2.ADVANCEMENTS, wrapper -> {
            wrapper.passthrough(Type.BOOLEAN); // Reset/clear
            final int size = wrapper.passthrough(Type.VAR_INT); // Mapping size
            for (int i = 0; i < size; i++) {
                wrapper.passthrough(Type.STRING); // Identifier

                // Parent
                if (wrapper.passthrough(Type.BOOLEAN)) {
                    wrapper.passthrough(Type.STRING);
                }

                // Display data
                if (wrapper.passthrough(Type.BOOLEAN)) {
                    convertComponent(wrapper); // Title
                    convertComponent(wrapper); // Description
                    wrapper.passthrough(Type.ITEM1_20_2); // Icon
                    wrapper.passthrough(Type.VAR_INT); // Frame type
                    final int flags = wrapper.passthrough(Type.INT);
                    if ((flags & 1) != 0) {
                        wrapper.passthrough(Type.STRING); // Background texture
                    }
                    wrapper.passthrough(Type.FLOAT); // X
                    wrapper.passthrough(Type.FLOAT); // Y
                }

                final int requirements = wrapper.passthrough(Type.VAR_INT);
                for (int array = 0; array < requirements; array++) {
                    wrapper.passthrough(Type.STRING_ARRAY);
                }

                wrapper.passthrough(Type.BOOLEAN); // Send telemetry
            }
        });
        registerClientbound(ClientboundPackets1_20_2.TAB_COMPLETE, wrapper -> {
            wrapper.passthrough(Type.VAR_INT); // Transaction id
            wrapper.passthrough(Type.VAR_INT); // Start
            wrapper.passthrough(Type.VAR_INT); // Length

            final int suggestions = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < suggestions; i++) {
                wrapper.passthrough(Type.STRING); // Suggestion
                convertOptionalComponent(wrapper); // Tooltip
            }
        });
        registerClientbound(ClientboundPackets1_20_2.MAP_DATA, wrapper -> {
            wrapper.passthrough(Type.VAR_INT); // Map id
            wrapper.passthrough(Type.BYTE); // Scale
            wrapper.passthrough(Type.BOOLEAN); // Locked
            if (wrapper.passthrough(Type.BOOLEAN)) {
                final int icons = wrapper.passthrough(Type.VAR_INT);
                for (int i = 0; i < icons; i++) {
                    wrapper.passthrough(Type.BYTE); // Type
                    wrapper.passthrough(Type.BYTE); // X
                    wrapper.passthrough(Type.BYTE); // Y
                    wrapper.passthrough(Type.BYTE); // Rotation
                    convertOptionalComponent(wrapper); // Display name
                }
            }
        });
        registerClientbound(ClientboundPackets1_20_2.BOSSBAR, wrapper -> {
            wrapper.passthrough(Type.UUID); // Id

            final int action = wrapper.passthrough(Type.VAR_INT);
            if (action == 0 || action == 3) {
                convertComponent(wrapper);
            }
        });
        registerClientbound(ClientboundPackets1_20_2.PLAYER_CHAT, wrapper -> {
            wrapper.passthrough(Type.UUID); // Sender
            wrapper.passthrough(Type.VAR_INT); // Index
            wrapper.passthrough(Type.OPTIONAL_SIGNATURE_BYTES); // Signature
            wrapper.passthrough(Type.STRING); // Plain content
            wrapper.passthrough(Type.LONG); // Timestamp
            wrapper.passthrough(Type.LONG); // Salt

            final int lastSeen = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < lastSeen; i++) {
                final int index = wrapper.passthrough(Type.VAR_INT);
                if (index == 0) {
                    wrapper.passthrough(Type.SIGNATURE_BYTES);
                }
            }

            convertOptionalComponent(wrapper); // Unsigned content

            final int filterMaskType = wrapper.passthrough(Type.VAR_INT);
            if (filterMaskType == 2) {
                wrapper.passthrough(Type.LONG_ARRAY_PRIMITIVE); // Mask
            }

            wrapper.passthrough(Type.VAR_INT); // Chat type
            convertComponent(wrapper); // Sender
            convertOptionalComponent(wrapper); // Target
        });
        registerClientbound(ClientboundPackets1_20_2.SCOREBOARD_OBJECTIVE, wrapper -> {
            wrapper.passthrough(Type.STRING); // Objective Name
            final byte action = wrapper.passthrough(Type.BYTE); // Mode
            if (action == 0 || action == 2) {
                convertComponent(wrapper); // Display Name
            }
        });
        registerClientbound(ClientboundPackets1_20_2.TEAMS, wrapper -> {
            wrapper.passthrough(Type.STRING); // Team Name
            final byte action = wrapper.passthrough(Type.BYTE); // Mode
            if (action == 0 || action == 2) {
                convertComponent(wrapper); // Display Name
                wrapper.passthrough(Type.BYTE); // Flags
                wrapper.passthrough(Type.STRING); // Name Tag Visibility
                wrapper.passthrough(Type.STRING); // Collision rule
                wrapper.passthrough(Type.VAR_INT); // Color
                convertComponent(wrapper); // Prefix
                convertComponent(wrapper); // Suffix
            }
        });

        registerClientbound(State.CONFIGURATION, ClientboundConfigurationPackets1_20_2.DISCONNECT.getId(), ClientboundConfigurationPackets1_20_2.DISCONNECT.getId(), this::convertComponent);
        registerClientbound(State.CONFIGURATION, ClientboundConfigurationPackets1_20_2.RESOURCE_PACK.getId(), ClientboundConfigurationPackets1_20_2.RESOURCE_PACK.getId(), resourcePackHandler());
        registerClientbound(ClientboundPackets1_20_2.DISCONNECT, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.RESOURCE_PACK, resourcePackHandler());
        registerClientbound(ClientboundPackets1_20_2.SERVER_DATA, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.ACTIONBAR, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.TITLE_TEXT, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.TITLE_SUBTITLE, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.DISGUISED_CHAT, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.SYSTEM_CHAT, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.OPEN_WINDOW, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // Id
                map(Type.VAR_INT); // Window Type
                handler(wrapper -> convertComponent(wrapper));
            }
        });
        registerClientbound(ClientboundPackets1_20_2.TAB_LIST, wrapper -> {
            convertComponent(wrapper);
            convertComponent(wrapper);
        });

        registerClientbound(ClientboundPackets1_20_2.COMBAT_KILL, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // Duration
                handler(wrapper -> convertComponent(wrapper));
            }
        });
        registerClientbound(ClientboundPackets1_20_2.PLAYER_INFO_UPDATE, wrapper -> {
            final BitSet actions = wrapper.passthrough(Type.PROFILE_ACTIONS_ENUM);
            final int entries = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < entries; i++) {
                wrapper.passthrough(Type.UUID);
                if (actions.get(0)) {
                    wrapper.passthrough(Type.STRING); // Player Name

                    final int properties = wrapper.passthrough(Type.VAR_INT);
                    for (int j = 0; j < properties; j++) {
                        wrapper.passthrough(Type.STRING); // Name
                        wrapper.passthrough(Type.STRING); // Value
                        wrapper.passthrough(Type.OPTIONAL_STRING); // Signature
                    }
                }
                if (actions.get(1) && wrapper.passthrough(Type.BOOLEAN)) {
                    wrapper.passthrough(Type.UUID); // Session UUID
                    wrapper.passthrough(Type.PROFILE_KEY);
                }
                if (actions.get(2)) {
                    wrapper.passthrough(Type.VAR_INT); // Gamemode
                }
                if (actions.get(3)) {
                    wrapper.passthrough(Type.BOOLEAN); // Listed
                }
                if (actions.get(4)) {
                    wrapper.passthrough(Type.VAR_INT); // Latency
                }
                if (actions.get(5)) {
                    convertOptionalComponent(wrapper); // Display name
                }
            }
        });
    }

    private PacketHandler resourcePackHandler() {
        return wrapper -> {
            wrapper.passthrough(Type.STRING); // Url
            wrapper.passthrough(Type.STRING); // Hash
            wrapper.passthrough(Type.BOOLEAN); // Required
            convertOptionalComponent(wrapper);
        };
    }

    private void convertComponent(final PacketWrapper wrapper) throws Exception {
        wrapper.write(Type.TAG, jsonComponentToTag(wrapper.read(Type.COMPONENT)));
    }

    private void convertOptionalComponent(final PacketWrapper wrapper) throws Exception {
        wrapper.write(Type.OPTIONAL_TAG, jsonComponentToTag(wrapper.read(Type.OPTIONAL_COMPONENT)));
    }

    public static @Nullable JsonElement tagComponentToJson(@Nullable final Tag tag) {
        try {
            return convertToJson(null, tag);
        } catch (final Exception e) {
            Via.getPlatform().getLogger().severe("Error converting component: " + tag);
            e.printStackTrace();
            return new JsonPrimitive("<error>");
        }
    }

    public static @Nullable Tag jsonComponentToTag(@Nullable final JsonElement component) {
        try {
            return convertToTag(component);
        } catch (final Exception e) {
            Via.getPlatform().getLogger().severe("Error converting component: " + component);
            e.printStackTrace();
            return new StringTag("<error>");
        }
    }

    private static @Nullable Tag convertToTag(final @Nullable JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        } else if (element.isJsonObject()) {
            final CompoundTag tag = new CompoundTag();
            for (final Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                // Not strictly needed, but might as well make it more compact
                convertObjectEntry(entry.getKey(), entry.getValue(), tag);
            }
            return tag;
        } else if (element.isJsonArray()) {
            return convertJsonArray(element);
        } else if (element.isJsonPrimitive()) {
            final JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isString()) {
                return new StringTag(primitive.getAsString());
            } else if (primitive.isBoolean()) {
                return new ByteTag((byte) (primitive.getAsBoolean() ? 1 : 0));
            }

            final Number number = primitive.getAsNumber();
            if (number instanceof Integer) {
                return new IntTag(number.intValue());
            } else if (number instanceof Byte) {
                return new ByteTag(number.byteValue());
            } else if (number instanceof Short) {
                return new ShortTag(number.shortValue());
            } else if (number instanceof Long) {
                return new LongTag(number.longValue());
            } else if (number instanceof Double) {
                return new DoubleTag(number.doubleValue());
            } else if (number instanceof Float) {
                return new FloatTag(number.floatValue());
            }
            return new StringTag(primitive.getAsString()); // ???
        }
        throw new IllegalArgumentException("Unhandled json type " + element.getClass().getSimpleName() + " with value " + element.getAsString());
    }

    private static ListTag convertJsonArray(final JsonElement element) {
        // TODO Number arrays
        final ListTag listTag = new ListTag();
        boolean singleType = true;
        for (final JsonElement entry : element.getAsJsonArray()) {
            final Tag convertedEntryTag = convertToTag(entry);
            if (listTag.getElementType() != null && listTag.getElementType() != convertedEntryTag.getClass()) {
                singleType = false;
                break;
            }

            listTag.add(convertedEntryTag);
        }

        if (singleType) {
            return listTag;
        }

        // Generally, vanilla-esque serializers should not produce this format, so it should be rare
        // Lists are only used for lists of components ("extra" and "with")
        final ListTag processedListTag = new ListTag();
        for (final JsonElement entry : element.getAsJsonArray()) {
            final Tag convertedTag = convertToTag(entry);
            if (convertedTag instanceof CompoundTag) {
                processedListTag.add(listTag);
                continue;
            }

            // Wrap all entries in compound tags as lists can only consist of one type of tag
            final CompoundTag compoundTag = new CompoundTag();
            compoundTag.put("text", new StringTag());
            compoundTag.put("extra", convertedTag);
        }
        return processedListTag;
    }

    private static void convertObjectEntry(final String key, final JsonElement element, final CompoundTag tag) {
        if ((key.equals("contents")) && element.isJsonObject()) {
            // Store show_entity id as int array instead of uuid string
            final JsonObject hoverEvent = element.getAsJsonObject();
            final JsonElement id = hoverEvent.remove("id");
            final UUID uuid;
            if (id != null && id.isJsonPrimitive() && (uuid = parseUUID(id.getAsString())) != null) {
                final CompoundTag convertedTag = (CompoundTag) convertToTag(element);
                convertedTag.put("id", new IntArrayTag(UUIDIntArrayType.uuidToIntArray(uuid)));
                tag.put(key, convertedTag);
                return;
            }
        }

        tag.put(key, convertToTag(element));
    }

    private static @Nullable UUID parseUUID(final String uuidString) {
        try {
            return UUID.fromString(uuidString);
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }

    private static @Nullable JsonElement convertToJson(final @Nullable String key, final @Nullable Tag tag) {
        if (tag == null) {
            return null;
        } else if (tag instanceof CompoundTag) {
            final JsonObject object = new JsonObject();
            for (final Map.Entry<String, Tag> entry : ((CompoundTag) tag).entrySet()) {
                convertCompoundTagEntry(entry.getKey(), entry.getValue(), object);
            }
            return object;
        } else if (tag instanceof ListTag) {
            final ListTag list = (ListTag) tag;
            final JsonArray array = new JsonArray();
            for (final Tag listEntry : list) {
                array.add(convertToJson(null, listEntry));
            }
            return array;
        } else if (tag instanceof NumberTag) {
            final NumberTag numberTag = (NumberTag) tag;
            if (key != null && BOOLEAN_TYPES.contains(key)) {
                // Booleans don't have a direct representation in nbt
                return new JsonPrimitive(numberTag.asBoolean());
            }
            return new JsonPrimitive(numberTag.getValue());
        } else if (tag instanceof StringTag) {
            return new JsonPrimitive(((StringTag) tag).getValue());
        } else if (tag instanceof ByteArrayTag) {
            final ByteArrayTag arrayTag = (ByteArrayTag) tag;
            final JsonArray array = new JsonArray();
            for (final byte num : arrayTag.getValue()) {
                array.add(num);
            }
            return array;
        } else if (tag instanceof IntArrayTag) {
            final IntArrayTag arrayTag = (IntArrayTag) tag;
            final JsonArray array = new JsonArray();
            for (final int num : arrayTag.getValue()) {
                array.add(num);
            }
            return array;
        } else if (tag instanceof LongArrayTag) {
            final LongArrayTag arrayTag = (LongArrayTag) tag;
            final JsonArray array = new JsonArray();
            for (final long num : arrayTag.getValue()) {
                array.add(num);
            }
            return array;
        }
        throw new IllegalArgumentException("Unhandled tag type " + tag.getClass().getSimpleName());
    }

    private static void convertCompoundTagEntry(final String key, final Tag tag, final JsonObject object) {
        if ((key.equals("contents")) && tag instanceof CompoundTag) {
            // Back to a UUID string
            final CompoundTag showEntity = (CompoundTag) tag;
            final Tag idTag = showEntity.remove("id");
            if (idTag instanceof IntArrayTag) {
                final JsonObject convertedElement = (JsonObject) convertToJson(key, tag);
                convertedElement.addProperty("id", uuidIntsToString(((IntArrayTag) idTag).getValue()));
                object.add(key, convertedElement);
                return;
            }
        }

        object.add(key, convertToJson(key, tag));
    }

    private static String uuidIntsToString(final int[] parts) {
        if (parts.length != 4) {
            return new UUID(0, 0).toString();
        }
        return UUIDIntArrayType.uuidFromIntArray(parts).toString();
    }

    @Override
    public void init(final UserConnection connection) {
        addEntityTracker(connection, new EntityTrackerBase(connection, Entity1_19_4Types.PLAYER));
    }

    @Override
    public MappingData getMappingData() {
        return MAPPINGS;
    }

    @Override
    protected ServerboundPacketType serverboundFinishConfigurationPacket() {
        return ServerboundConfigurationPackets1_20_2.FINISH_CONFIGURATION;
    }

    @Override
    protected ClientboundPacketType clientboundFinishConfigurationPacket() {
        return ClientboundConfigurationPackets1_20_2.FINISH_CONFIGURATION;
    }

    @Override
    public EntityPacketRewriter1_20_3 getEntityRewriter() {
        return entityRewriter;
    }
}
