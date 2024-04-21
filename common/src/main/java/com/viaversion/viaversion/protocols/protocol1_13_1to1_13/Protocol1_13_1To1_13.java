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
package com.viaversion.viaversion.protocols.protocol1_13_1to1_13;

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
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.protocol1_13_1to1_13.metadata.MetadataRewriter1_13_1To1_13;
import com.viaversion.viaversion.protocols.protocol1_13_1to1_13.packets.EntityPackets;
import com.viaversion.viaversion.protocols.protocol1_13_1to1_13.packets.InventoryPackets;
import com.viaversion.viaversion.protocols.protocol1_13_1to1_13.packets.WorldPackets;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ServerboundPackets1_13;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;

public class Protocol1_13_1To1_13 extends AbstractProtocol<ClientboundPackets1_13, ClientboundPackets1_13, ServerboundPackets1_13, ServerboundPackets1_13> {

    public static final MappingData MAPPINGS = new MappingDataBase("1.13", "1.13.2");
    private final MetadataRewriter1_13_1To1_13 entityRewriter = new MetadataRewriter1_13_1To1_13(this);
    private final InventoryPackets itemRewriter = new InventoryPackets(this);
    private final TagRewriter<ClientboundPackets1_13> tagRewriter = new TagRewriter<>(this);

    public Protocol1_13_1To1_13() {
        super(ClientboundPackets1_13.class, ClientboundPackets1_13.class, ServerboundPackets1_13.class, ServerboundPackets1_13.class);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        EntityPackets.register(this);
        WorldPackets.register(this);

        registerServerbound(ServerboundPackets1_13.TAB_COMPLETE, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT);
                map(Type.STRING, new ValueTransformer<String, String>(Type.STRING) {
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
                map(Type.ITEM1_13);
                map(Type.BOOLEAN);
                handler(wrapper -> {
                    Item item = wrapper.get(Type.ITEM1_13, 0);
                    itemRewriter.handleItemToServer(wrapper.user(), item);
                });
                handler(wrapper -> {
                    int hand = wrapper.read(Type.VAR_INT);
                    if (hand == 1) {
                        wrapper.cancel();
                    }
                });
            }
        });

        registerClientbound(ClientboundPackets1_13.TAB_COMPLETE, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // Transaction id
                map(Type.VAR_INT); // Start
                map(Type.VAR_INT); // Length
                map(Type.VAR_INT); // Count
                handler(wrapper -> {
                    int start = wrapper.get(Type.VAR_INT, 1);
                    wrapper.set(Type.VAR_INT, 1, start + 1); // Offset by +1 to take into account / at beginning
                    // Passthrough suggestions
                    int count = wrapper.get(Type.VAR_INT, 3);
                    for (int i = 0; i < count; i++) {
                        wrapper.passthrough(Type.STRING);
                        wrapper.passthrough(Type.OPTIONAL_COMPONENT); // Tooltip
                    }
                });
            }
        });

        registerClientbound(ClientboundPackets1_13.BOSSBAR, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.UUID);
                map(Type.VAR_INT);
                handler(wrapper -> {
                    int action = wrapper.get(Type.VAR_INT, 0);
                    if (action == 0) {
                        wrapper.passthrough(Type.COMPONENT);
                        wrapper.passthrough(Type.FLOAT);
                        wrapper.passthrough(Type.VAR_INT);
                        wrapper.passthrough(Type.VAR_INT);
                        short flags = wrapper.read(Type.BYTE);
                        if ((flags & 0x02) != 0) flags |= 0x04;
                        wrapper.write(Type.UNSIGNED_BYTE, flags);
                    }
                });
            }
        });

        tagRewriter.register(ClientboundPackets1_13.TAGS, RegistryType.ITEM);
        new StatisticsRewriter<>(this).register(ClientboundPackets1_13.STATISTICS);
    }

    @Override
    public void init(UserConnection userConnection) {
        userConnection.addEntityTracker(this.getClass(), new EntityTrackerBase(userConnection, EntityTypes1_13.EntityType.PLAYER));
        if (!userConnection.has(ClientWorld.class)) {
            userConnection.put(new ClientWorld());
        }
    }

    @Override
    public MappingData getMappingData() {
        return MAPPINGS;
    }

    @Override
    public MetadataRewriter1_13_1To1_13 getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    public InventoryPackets getItemRewriter() {
        return itemRewriter;
    }

    @Override
    public TagRewriter<ClientboundPackets1_13> getTagRewriter() {
        return tagRewriter;
    }
}
