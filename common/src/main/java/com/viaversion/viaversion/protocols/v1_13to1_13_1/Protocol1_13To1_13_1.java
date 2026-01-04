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
package com.viaversion.viaversion.protocols.v1_13to1_13_1;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_13;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.remapper.ValueTransformer;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ServerboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_13to1_13_1.rewriter.EntityPacketRewriter1_13_1;
import com.viaversion.viaversion.protocols.v1_13to1_13_1.rewriter.ItemPacketRewriter1_13_1;
import com.viaversion.viaversion.protocols.v1_13to1_13_1.rewriter.WorldPacketRewriter1_13_1;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;

public class Protocol1_13To1_13_1 extends AbstractProtocol<ClientboundPackets1_13, ClientboundPackets1_13, ServerboundPackets1_13, ServerboundPackets1_13> {

    public static final MappingData MAPPINGS = new MappingDataBase("1.13", "1.13.2");
    private final EntityPacketRewriter1_13_1 entityRewriter = new EntityPacketRewriter1_13_1(this);
    private final ItemPacketRewriter1_13_1 itemRewriter = new ItemPacketRewriter1_13_1(this);
    private final ParticleRewriter<ClientboundPackets1_13> particleRewriter = new ParticleRewriter<>(this);
    private final TagRewriter<ClientboundPackets1_13> tagRewriter = new TagRewriter<>(this);

    public Protocol1_13To1_13_1() {
        super(ClientboundPackets1_13.class, ClientboundPackets1_13.class, ServerboundPackets1_13.class, ServerboundPackets1_13.class);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        WorldPacketRewriter1_13_1.register(this);

        registerServerbound(ServerboundPackets1_13.COMMAND_SUGGESTION, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT);
                map(Types.STRING, new ValueTransformer<>(Types.STRING) {
                    @Override
                    public String transform(PacketWrapper wrapper, String inputValue) {
                        // 1.13 starts sending slash at start, so we remove it for compatibility
                        return inputValue.startsWith("/") ? inputValue.substring(1) : inputValue;
                    }
                });
            }
        });

        registerServerbound(ServerboundPackets1_13.EDIT_BOOK, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.ITEM1_13);
                map(Types.BOOLEAN);
                handler(wrapper -> {
                    Item item = wrapper.get(Types.ITEM1_13, 0);
                    itemRewriter.handleItemToServer(wrapper.user(), item);
                });
                handler(wrapper -> {
                    int hand = wrapper.read(Types.VAR_INT);
                    if (hand == 1) {
                        wrapper.cancel();
                    }
                });
            }
        });

        registerClientbound(ClientboundPackets1_13.COMMAND_SUGGESTIONS, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // Transaction id
                map(Types.VAR_INT); // Start
                map(Types.VAR_INT); // Length
                map(Types.VAR_INT); // Count
                handler(wrapper -> {
                    int start = wrapper.get(Types.VAR_INT, 1);
                    wrapper.set(Types.VAR_INT, 1, start + 1); // Offset by +1 to take into account / at beginning
                    // Passthrough suggestions
                    int count = wrapper.get(Types.VAR_INT, 3);
                    for (int i = 0; i < count; i++) {
                        wrapper.passthrough(Types.STRING);
                        wrapper.passthrough(Types.OPTIONAL_COMPONENT); // Tooltip
                    }
                });
            }
        });

        registerClientbound(ClientboundPackets1_13.BOSS_EVENT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.UUID);
                map(Types.VAR_INT);
                handler(wrapper -> {
                    int action = wrapper.get(Types.VAR_INT, 0);
                    if (action == 0) {
                        wrapper.passthrough(Types.COMPONENT);
                        wrapper.passthrough(Types.FLOAT);
                        wrapper.passthrough(Types.VAR_INT);
                        wrapper.passthrough(Types.VAR_INT);
                        short flags = wrapper.read(Types.BYTE);
                        if ((flags & 0x02) != 0) flags |= 0x04;
                        wrapper.write(Types.UNSIGNED_BYTE, flags);
                    }
                });
            }
        });

        tagRewriter.register(ClientboundPackets1_13.UPDATE_TAGS, RegistryType.ITEM);
        particleRewriter.registerLevelParticles1_13(ClientboundPackets1_13.LEVEL_PARTICLES, Types.FLOAT);

        new StatisticsRewriter<>(this).register(ClientboundPackets1_13.AWARD_STATS);
    }

    @Override
    public void init(UserConnection userConnection) {
        userConnection.addEntityTracker(this.getClass(), new EntityTrackerBase(userConnection, EntityTypes1_13.EntityType.PLAYER));
        userConnection.addClientWorld(this.getClass(), new ClientWorld());
    }

    @Override
    public MappingData getMappingData() {
        return MAPPINGS;
    }

    @Override
    public EntityPacketRewriter1_13_1 getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    public ItemPacketRewriter1_13_1 getItemRewriter() {
        return itemRewriter;
    }

    @Override
    public ParticleRewriter<ClientboundPackets1_13> getParticleRewriter() {
        return particleRewriter;
    }

    @Override
    public TagRewriter<ClientboundPackets1_13> getTagRewriter() {
        return tagRewriter;
    }
}
