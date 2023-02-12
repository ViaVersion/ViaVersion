/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2023 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_16to1_15_2.packets;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntArrayTag;
import com.github.steveice10.opennbt.tag.builtin.LongArrayTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.google.gson.JsonElement;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.UUIDIntArrayType;
import com.viaversion.viaversion.protocols.protocol1_15to1_14_4.ClientboundPackets1_15;
import com.viaversion.viaversion.protocols.protocol1_15to1_14_4.types.Chunk1_15Type;
import com.viaversion.viaversion.protocols.protocol1_16to1_15_2.Protocol1_16To1_15_2;
import com.viaversion.viaversion.protocols.protocol1_16to1_15_2.types.Chunk1_16Type;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.util.CompactArrayUtil;
import java.util.Map;
import java.util.UUID;

public class WorldPackets {

    public static void register(Protocol1_16To1_15_2 protocol) {
        BlockRewriter<ClientboundPackets1_15> blockRewriter = new BlockRewriter<>(protocol, Type.POSITION1_14);

        blockRewriter.registerBlockAction(ClientboundPackets1_15.BLOCK_ACTION);
        blockRewriter.registerBlockChange(ClientboundPackets1_15.BLOCK_CHANGE);
        blockRewriter.registerMultiBlockChange(ClientboundPackets1_15.MULTI_BLOCK_CHANGE);
        blockRewriter.registerAcknowledgePlayerDigging(ClientboundPackets1_15.ACKNOWLEDGE_PLAYER_DIGGING);

        protocol.registerClientbound(ClientboundPackets1_15.UPDATE_LIGHT, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // x
                map(Type.VAR_INT); // y
                handler(wrapper -> wrapper.write(Type.BOOLEAN, true)); // Take neighbour's light into account as well
            }
        });

        protocol.registerClientbound(ClientboundPackets1_15.CHUNK_DATA, wrapper -> {
            Chunk chunk = wrapper.read(new Chunk1_15Type());
            wrapper.write(new Chunk1_16Type(), chunk);

            chunk.setIgnoreOldLightData(chunk.isFullChunk());

            for (int s = 0; s < chunk.getSections().length; s++) {
                ChunkSection section = chunk.getSections()[s];
                if (section == null) {
                    continue;
                }

                DataPalette palette = section.palette(PaletteType.BLOCKS);
                for (int i = 0; i < palette.size(); i++) {
                    int mappedBlockStateId = protocol.getMappingData().getNewBlockStateId(palette.idByIndex(i));
                    palette.setIdByIndex(i, mappedBlockStateId);
                }
            }

            CompoundTag heightMaps = chunk.getHeightMap();
            for (Tag heightMapTag : heightMaps.values()) {
                LongArrayTag heightMap = (LongArrayTag) heightMapTag;
                int[] heightMapData = new int[256];
                CompactArrayUtil.iterateCompactArray(9, heightMapData.length, heightMap.getValue(), (i, v) -> heightMapData[i] = v);
                heightMap.setValue(CompactArrayUtil.createCompactArrayWithPadding(9, heightMapData.length, i -> heightMapData[i]));
            }

            if (chunk.getBlockEntities() == null) return;
            for (CompoundTag blockEntity : chunk.getBlockEntities()) {
                handleBlockEntity(protocol, blockEntity);
            }
        });

        protocol.registerClientbound(ClientboundPackets1_15.BLOCK_ENTITY_DATA, wrapper -> {
            wrapper.passthrough(Type.POSITION1_14); // Position
            wrapper.passthrough(Type.UNSIGNED_BYTE); // Action
            CompoundTag tag = wrapper.passthrough(Type.NBT);
            handleBlockEntity(protocol, tag);
        });

        blockRewriter.registerEffect(ClientboundPackets1_15.EFFECT, 1010, 2001);
    }

    private static void handleBlockEntity(Protocol1_16To1_15_2 protocol, CompoundTag compoundTag) {
        StringTag idTag = compoundTag.get("id");
        if (idTag == null) return;

        String id = idTag.getValue();
        if (id.equals("minecraft:conduit")) {
            Tag targetUuidTag = compoundTag.remove("target_uuid");
            if (!(targetUuidTag instanceof StringTag)) return;

            // target_uuid -> Target
            UUID targetUuid = UUID.fromString((String) targetUuidTag.getValue());
            compoundTag.put("Target", new IntArrayTag(UUIDIntArrayType.uuidToIntArray(targetUuid)));
        } else if (id.equals("minecraft:skull") && compoundTag.get("Owner") instanceof CompoundTag) {
            CompoundTag ownerTag = compoundTag.remove("Owner");
            StringTag ownerUuidTag = ownerTag.remove("Id");
            if (ownerUuidTag != null) {
                UUID ownerUuid = UUID.fromString(ownerUuidTag.getValue());
                ownerTag.put("Id", new IntArrayTag(UUIDIntArrayType.uuidToIntArray(ownerUuid)));
            }

            // Owner -> SkullOwner
            CompoundTag skullOwnerTag = new CompoundTag();
            for (Map.Entry<String, Tag> entry : ownerTag.entrySet()) {
                skullOwnerTag.put(entry.getKey(), entry.getValue());
            }
            compoundTag.put("SkullOwner", skullOwnerTag);
        } else if (id.equals("minecraft:sign")) {
            for (int i = 1; i <= 4; i++) {
                Tag line = compoundTag.get("Text" + i);
                if (line instanceof StringTag) {
                    JsonElement text = protocol.getComponentRewriter().processText(((StringTag) line).getValue());
                    compoundTag.put("Text" + i, new StringTag(text.toString()));
                }
            }
        } else if (id.equals("minecraft:mob_spawner")) {
            Tag spawnDataTag = compoundTag.get("SpawnData");
            if (spawnDataTag instanceof CompoundTag) {
                Tag spawnDataIdTag = ((CompoundTag) spawnDataTag).get("id");
                if (spawnDataIdTag instanceof StringTag) {
                    StringTag spawnDataIdStringTag = ((StringTag) spawnDataIdTag);
                    if (spawnDataIdStringTag.getValue().equals("minecraft:zombie_pigman")) {
                        spawnDataIdStringTag.setValue("minecraft:zombified_piglin");
                    }
                }
            }
        }
    }
}
