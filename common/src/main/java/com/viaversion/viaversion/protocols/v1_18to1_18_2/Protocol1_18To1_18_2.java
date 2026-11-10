/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_18to1_18_2;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ServerboundPackets1_17;
import com.viaversion.viaversion.protocols.v1_17_1to1_18.packet.ClientboundPackets1_18;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.util.TagUtil;

public final class Protocol1_18To1_18_2 extends AbstractProtocol<ClientboundPackets1_18, ClientboundPackets1_18, ServerboundPackets1_17, ServerboundPackets1_17> {

    public Protocol1_18To1_18_2() {
        super(ClientboundPackets1_18.class, ClientboundPackets1_18.class, ServerboundPackets1_17.class, ServerboundPackets1_17.class);
    }

    @Override
    protected void registerPackets() {
        final TagRewriter<ClientboundPackets1_18> tagRewriter = new TagRewriter<>(this);
        tagRewriter.addTagRaw(RegistryType.BLOCK, "minecraft:fall_damage_resetting", 169, 257, 680, 713, 714, 715, 716, 859, 860, 696, 100);
        tagRewriter.registerGeneric(ClientboundPackets1_18.UPDATE_TAGS);

        registerClientbound(ClientboundPackets1_18.UPDATE_MOB_EFFECT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // Entity id
                map(Types.BYTE, Types.VAR_INT); // Effect id
            }
        });

        registerClientbound(ClientboundPackets1_18.REMOVE_MOB_EFFECT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // Entity id
                map(Types.BYTE, Types.VAR_INT); // Effect id
            }
        });

        registerClientbound(ClientboundPackets1_18.LOGIN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); // Entity ID
                map(Types.BOOLEAN); // Hardcore
                map(Types.BYTE); // Gamemode
                map(Types.BYTE); // Previous Gamemode
                map(Types.STRING_ARRAY); // World List
                map(Types.NAMED_COMPOUND_TAG); // Registry
                map(Types.NAMED_COMPOUND_TAG); // Current dimension data
                handler(wrapper -> {
                    final CompoundTag registry = wrapper.get(Types.NAMED_COMPOUND_TAG, 0);
                    final ListTag<CompoundTag> dimensions = TagUtil.getRegistryEntries(registry, "dimension_type");
                    for (final CompoundTag dimension : dimensions) {
                        addTagPrefix(dimension.getCompoundTag("element"));
                    }

                    addTagPrefix(wrapper.get(Types.NAMED_COMPOUND_TAG, 1));
                });
            }
        });

        registerClientbound(ClientboundPackets1_18.RESPAWN, wrapper -> addTagPrefix(wrapper.passthrough(Types.NAMED_COMPOUND_TAG)));
    }

    private void addTagPrefix(CompoundTag tag) {
        final Tag infiniburnTag = tag.get("infiniburn");
        if (infiniburnTag instanceof final StringTag infiniburn) {
            infiniburn.setValue("#" + infiniburn.getValue());
        }
    }
}
