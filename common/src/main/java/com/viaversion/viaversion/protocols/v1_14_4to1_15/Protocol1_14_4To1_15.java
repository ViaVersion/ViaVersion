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
package com.viaversion.viaversion.protocols.v1_14_4to1_15;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_15;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.packet.ServerboundPackets1_14;
import com.viaversion.viaversion.protocols.v1_14_3to1_14_4.packet.ClientboundPackets1_14_4;
import com.viaversion.viaversion.protocols.v1_14_4to1_15.metadata.MetadataRewriter1_15To1_14_4;
import com.viaversion.viaversion.protocols.v1_14_4to1_15.packet.ClientboundPackets1_15;
import com.viaversion.viaversion.protocols.v1_14_4to1_15.packets.EntityPackets;
import com.viaversion.viaversion.protocols.v1_14_4to1_15.packets.InventoryPackets;
import com.viaversion.viaversion.protocols.v1_14_4to1_15.packets.WorldPackets;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;

public class Protocol1_14_4To1_15 extends AbstractProtocol<ClientboundPackets1_14_4, ClientboundPackets1_15, ServerboundPackets1_14, ServerboundPackets1_14> {

    public static final MappingData MAPPINGS = new MappingDataBase("1.14", "1.15");
    private final MetadataRewriter1_15To1_14_4 metadataRewriter = new MetadataRewriter1_15To1_14_4(this);
    private final InventoryPackets itemRewriter = new InventoryPackets(this);
    private final TagRewriter<ClientboundPackets1_14_4> tagRewriter = new TagRewriter<>(this);

    public Protocol1_14_4To1_15() {
        super(ClientboundPackets1_14_4.class, ClientboundPackets1_15.class, ServerboundPackets1_14.class, ServerboundPackets1_14.class);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        EntityPackets.register(this);
        WorldPackets.register(this);

        SoundRewriter<ClientboundPackets1_14_4> soundRewriter = new SoundRewriter<>(this);
        soundRewriter.registerSound(ClientboundPackets1_14_4.SOUND_ENTITY); // Entity Sound Effect (added somewhere in 1.14)
        soundRewriter.registerSound(ClientboundPackets1_14_4.SOUND);

        new StatisticsRewriter<>(this).register(ClientboundPackets1_14_4.AWARD_STATS);

        registerServerbound(ServerboundPackets1_14.EDIT_BOOK, wrapper -> itemRewriter.handleItemToServer(wrapper.user(), wrapper.passthrough(Type.ITEM1_13_2)));

        tagRewriter.register(ClientboundPackets1_14_4.UPDATE_TAGS, RegistryType.ENTITY);
    }

    @Override
    protected void onMappingDataLoaded() {
        EntityTypes1_15.initialize(this);

        int[] shulkerBoxes = new int[17];
        int shulkerBoxOffset = 501;
        for (int i = 0; i < 17; i++) {
            shulkerBoxes[i] = shulkerBoxOffset + i;
        }
        tagRewriter.addTag(RegistryType.BLOCK, "minecraft:shulker_boxes", shulkerBoxes);

        super.onMappingDataLoaded();
    }

    @Override
    public void init(UserConnection connection) {
        addEntityTracker(connection, new EntityTrackerBase(connection, EntityTypes1_15.PLAYER));
    }

    @Override
    public MappingData getMappingData() {
        return MAPPINGS;
    }

    @Override
    public MetadataRewriter1_15To1_14_4 getEntityRewriter() {
        return metadataRewriter;
    }

    @Override
    public InventoryPackets getItemRewriter() {
        return itemRewriter;
    }

    @Override
    public TagRewriter<ClientboundPackets1_14_4> getTagRewriter() {
        return tagRewriter;
    }
}
