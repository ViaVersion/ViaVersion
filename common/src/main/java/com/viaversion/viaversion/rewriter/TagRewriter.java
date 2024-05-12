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
package com.viaversion.viaversion.rewriter;

import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.TagData;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Key;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

public class TagRewriter<C extends ClientboundPacketType> implements com.viaversion.viaversion.api.rewriter.TagRewriter {
    private static final int[] EMPTY_ARRAY = {};
    private final Protocol<C, ?, ?, ?> protocol;
    private final Map<RegistryType, List<TagData>> newTags = new EnumMap<>(RegistryType.class);
    private final Map<RegistryType, Map<String, String>> toRename = new EnumMap<>(RegistryType.class);
    private final Set<String> toRemove = new HashSet<>();

    public TagRewriter(final Protocol<C, ?, ?, ?> protocol) {
        this.protocol = protocol;
    }

    @Override
    public void onMappingDataLoaded() {
        if (protocol.getMappingData() == null) {
            return;
        }

        for (RegistryType type : RegistryType.getValues()) {
            List<TagData> tags = protocol.getMappingData().getTags(type);
            if (tags != null) {
                getOrComputeNewTags(type).addAll(tags);
            }
        }
    }

    @Override
    public void removeTags(final String registryKey) {
        toRemove.add(registryKey);
    }

    @Override
    public void renameTag(final RegistryType type, final String registryKey, final String renameTo) {
        toRename.computeIfAbsent(type, t -> new HashMap<>()).put(registryKey, renameTo);
    }

    @Override
    public void addEmptyTag(RegistryType tagType, String tagId) {
        getOrComputeNewTags(tagType).add(new TagData(tagId, EMPTY_ARRAY));
    }

    @Override
    public void addEmptyTags(RegistryType tagType, String... tagIds) {
        List<TagData> tagList = getOrComputeNewTags(tagType);
        for (String id : tagIds) {
            tagList.add(new TagData(id, EMPTY_ARRAY));
        }
    }

    @Override
    public void addEntityTag(String tagId, EntityType... entities) {
        int[] ids = new int[entities.length];
        for (int i = 0; i < entities.length; i++) {
            ids[i] = entities[i].getId();
        }
        addTagRaw(RegistryType.ENTITY, tagId, ids);
    }

    @Override
    public void addTag(RegistryType tagType, String tagId, int... unmappedIds) {
        List<TagData> newTags = getOrComputeNewTags(tagType);
        IdRewriteFunction rewriteFunction = getRewriter(tagType);
        if (rewriteFunction != null) {
            for (int i = 0; i < unmappedIds.length; i++) {
                int unmappedId = unmappedIds[i];
                unmappedIds[i] = rewriteFunction.rewrite(unmappedId);
            }
        }
        newTags.add(new TagData(tagId, unmappedIds));
    }

    @Override
    public void addTagRaw(RegistryType tagType, String tagId, int... ids) {
        getOrComputeNewTags(tagType).add(new TagData(tagId, ids));
    }

    public void register(C packetType, @Nullable RegistryType readUntilType) {
        protocol.registerClientbound(packetType, getHandler(readUntilType));
    }

    public void registerGeneric(C packetType) {
        protocol.registerClientbound(packetType, getGenericHandler());
    }

    public PacketHandler getHandler(@Nullable RegistryType readUntilType) {
        return wrapper -> {
            for (RegistryType type : RegistryType.getValues()) {
                handle(wrapper, getRewriter(type), getNewTags(type), toRename.get(type));

                // Stop iterating
                if (type == readUntilType) {
                    break;
                }
            }
        };
    }

    public PacketHandler getGenericHandler() {
        return wrapper -> {
            final int length = wrapper.passthrough(Types.VAR_INT);
            int editedLength = length;
            for (int i = 0; i < length; i++) {
                String registryKey = wrapper.read(Types.STRING);
                if (toRemove.contains(registryKey)) {
                    wrapper.set(Types.VAR_INT, 0, --editedLength);
                    int tagsSize = wrapper.read(Types.VAR_INT);
                    for (int j = 0; j < tagsSize; j++) {
                        wrapper.read(Types.STRING);
                        wrapper.read(Types.VAR_INT_ARRAY_PRIMITIVE);
                    }
                    continue;
                }

                wrapper.write(Types.STRING, registryKey);
                registryKey = Key.stripMinecraftNamespace(registryKey);

                RegistryType type = RegistryType.getByKey(registryKey);
                if (type != null) {
                    handle(wrapper, getRewriter(type), getNewTags(type), toRename.get(type));
                } else {
                    handle(wrapper, null, null, null);
                }
            }
        };
    }

    public void handle(PacketWrapper wrapper, @Nullable IdRewriteFunction rewriteFunction, @Nullable List<TagData> newTags) {
        handle(wrapper, rewriteFunction, newTags, null);
    }

    public void handle(PacketWrapper wrapper, @Nullable IdRewriteFunction rewriteFunction, @Nullable List<TagData> newTags, @Nullable Map<String, String> tagsToRename) {
        int tagsSize = wrapper.read(Types.VAR_INT);
        wrapper.write(Types.VAR_INT, newTags != null ? tagsSize + newTags.size() : tagsSize); // add new tags count

        for (int i = 0; i < tagsSize; i++) {
            String key = wrapper.read(Types.STRING);
            if (tagsToRename != null) {
                String renamedKey = tagsToRename.get(key);
                if (renamedKey != null) {
                    key = renamedKey;
                }
            }
            wrapper.write(Types.STRING, key);

            int[] ids = wrapper.read(Types.VAR_INT_ARRAY_PRIMITIVE);
            if (rewriteFunction != null) {
                // Map ids and filter out new blocks
                IntList idList = new IntArrayList(ids.length);
                for (int id : ids) {
                    int mappedId = rewriteFunction.rewrite(id);
                    if (mappedId != -1) {
                        idList.add(mappedId);
                    }
                }

                wrapper.write(Types.VAR_INT_ARRAY_PRIMITIVE, idList.toArray(EMPTY_ARRAY));
            } else {
                // Write the original array
                wrapper.write(Types.VAR_INT_ARRAY_PRIMITIVE, ids);
            }
        }

        // Send new tags if present
        if (newTags != null) {
            for (TagData tag : newTags) {
                wrapper.write(Types.STRING, tag.identifier());
                wrapper.write(Types.VAR_INT_ARRAY_PRIMITIVE, tag.entries());
            }
        }
    }

    @Override
    public @Nullable List<TagData> getNewTags(RegistryType tagType) {
        return newTags.get(tagType);
    }

    @Override
    public List<TagData> getOrComputeNewTags(RegistryType tagType) {
        return newTags.computeIfAbsent(tagType, type -> new ArrayList<>());
    }

    public @Nullable IdRewriteFunction getRewriter(RegistryType tagType) {
        MappingData mappingData = protocol.getMappingData();
        return switch (tagType) {
            case BLOCK ->
                    mappingData != null && mappingData.getBlockMappings() != null ? mappingData::getNewBlockId : null;
            case ITEM ->
                    mappingData != null && mappingData.getItemMappings() != null ? mappingData::getNewItemId : null;
            case ENTITY ->
                    protocol.getEntityRewriter() != null ? id -> protocol.getEntityRewriter().newEntityId(id) : null;
            case FLUID, GAME_EVENT -> null;
        };
    }
}
