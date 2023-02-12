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
package com.viaversion.viaversion.protocols.protocol1_18_2to1_18;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.ServerboundPackets1_17;
import com.viaversion.viaversion.protocols.protocol1_18to1_17_1.ClientboundPackets1_18;
import com.viaversion.viaversion.rewriter.TagRewriter;

public final class Protocol1_18_2To1_18 extends AbstractProtocol<ClientboundPackets1_18, ClientboundPackets1_18, ServerboundPackets1_17, ServerboundPackets1_17> {

    public Protocol1_18_2To1_18() {
        super(ClientboundPackets1_18.class, ClientboundPackets1_18.class, ServerboundPackets1_17.class, ServerboundPackets1_17.class);
    }

    @Override
    protected void registerPackets() {
        final TagRewriter<ClientboundPackets1_18> tagRewriter = new TagRewriter<>(this);
        tagRewriter.addEmptyTag(RegistryType.BLOCK, "minecraft:fall_damage_resetting");
        tagRewriter.registerGeneric(ClientboundPackets1_18.TAGS);

        registerClientbound(ClientboundPackets1_18.ENTITY_EFFECT, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // Entity id
                map(Type.BYTE, Type.VAR_INT); // Effect id
            }
        });

        registerClientbound(ClientboundPackets1_18.REMOVE_ENTITY_EFFECT, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // Entity id
                map(Type.BYTE, Type.VAR_INT); // Effect id
            }
        });

        registerClientbound(ClientboundPackets1_18.JOIN_GAME, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); // Entity ID
                map(Type.BOOLEAN); // Hardcore
                map(Type.UNSIGNED_BYTE); // Gamemode
                map(Type.BYTE); // Previous Gamemode
                map(Type.STRING_ARRAY); // World List
                map(Type.NBT); // Registry
                map(Type.NBT); // Current dimension data
                handler(wrapper -> {
                    final CompoundTag registry = wrapper.get(Type.NBT, 0);
                    final CompoundTag dimensionsHolder = registry.get("minecraft:dimension_type");
                    final ListTag dimensions = dimensionsHolder.get("value");
                    for (final Tag dimension : dimensions) {
                        addTagPrefix(((CompoundTag) dimension).get("element"));
                    }

                    addTagPrefix(wrapper.get(Type.NBT, 1));
                });
            }
        });

        registerClientbound(ClientboundPackets1_18.RESPAWN, wrapper -> addTagPrefix(wrapper.passthrough(Type.NBT)));
    }

    private void addTagPrefix(CompoundTag tag) {
        final Tag infiniburnTag = tag.get("infiniburn");
        if (infiniburnTag instanceof StringTag) {
            final StringTag infiniburn = (StringTag) infiniburnTag;
            infiniburn.setValue("#" + infiniburn.getValue());
        }
    }
}
