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
package com.viaversion.viaversion.template.protocols;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_19_4Types;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ClientboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ClientboundPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ServerboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ServerboundPackets1_20_2;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.template.protocols.rewriter.EntityPacketRewriter1_99;
import com.viaversion.viaversion.template.protocols.rewriter.BlockItemPacketRewriter1_99;

// Placeholders to replace (in the entire package):
//   Protocol1_99To_98, EntityPacketRewriter1_99, BlockItemPacketRewriter1_99
//   ClientboundPackets1_20_2
//   ServerboundPackets1_20_2
//   ClientboundConfigurationPackets1_20_2
//   ServerboundConfigurationPackets1_20_2
//   Entity1_19_4Types (MAPPED type)
//   1.99, 1.98
public final class Protocol1_99To_98 extends AbstractProtocol<ClientboundPackets1_20_2, ClientboundPackets1_20_2, ServerboundPackets1_20_2, ServerboundPackets1_20_2> {

    public static final MappingData MAPPINGS = new MappingDataBase("1.98", "1.99");
    private final EntityPacketRewriter1_99 entityRewriter = new EntityPacketRewriter1_99(this);
    private final BlockItemPacketRewriter1_99 itemRewriter = new BlockItemPacketRewriter1_99(this);

    public Protocol1_99To_98() {
        // Passing the class types into the super constructor is needed for automatic packet type id remapping, but can otherwise be omitted
        super(ClientboundPackets1_20_2.class, ClientboundPackets1_20_2.class, ServerboundPackets1_20_2.class, ServerboundPackets1_20_2.class);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        // Registers renames etc. as well as registry type id changes
        final TagRewriter<ClientboundPackets1_20_2> tagRewriter = new TagRewriter<>(this);
        tagRewriter.registerGeneric(ClientboundPackets1_20_2.TAGS);

        // Registers sound id changes
        final SoundRewriter<ClientboundPackets1_20_2> soundRewriter = new SoundRewriter<>(this);
        soundRewriter.register1_19_3Sound(ClientboundPackets1_20_2.SOUND);
        soundRewriter.registerSound(ClientboundPackets1_20_2.ENTITY_SOUND);

        // Registers registry type id changes as well as stat id changes if also included in the json mappings
        new StatisticsRewriter<>(this).register(ClientboundPackets1_20_2.STATISTICS);

        // Uncomment if an existing type changed serialization format. Mappings for argument type keys can also be defined in mapping files
        /*final CommandRewriter1_19_4<ClientboundPackets1_20_2> commandRewriter = new CommandRewriter1_19_4<ClientboundPackets1_20_2>(this) {
            @Override
            public void handleArgument(final PacketWrapper wrapper, final String argumentType) throws Exception {
                if (argumentType.equals("minecraft:abc")) {
                    // New argument
                    wrapper.write(Type.INT, 0);
                } else {
                    super.handleArgument(wrapper, argumentType);
                }
            }
        }.registerDeclareCommands1_19(ClientboundPackets1_20_2.DECLARE_COMMANDS);*/
    }

    @Override
    protected void onMappingDataLoaded() {
        super.onMappingDataLoaded(); // Calls load methods on rewriters

        // Uncomment this if the entity types enum has been newly added specificly for this Protocol
        // Entity1_19_4Types.initialize(this);

        // Uncomment if a new particle was added = ids shifted; requires a new Types_ class copied from the last
        /*Types1_19_4.PARTICLE.filler(this)
                .reader("block", ParticleType.Readers.BLOCK)
                .reader("block_marker", ParticleType.Readers.BLOCK)
                .reader("dust", ParticleType.Readers.DUST)
                .reader("falling_dust", ParticleType.Readers.BLOCK)
                .reader("dust_color_transition", ParticleType.Readers.DUST_TRANSITION)
                .reader("item", ParticleType.Readers.VAR_INT_ITEM)
                .reader("vibration", ParticleType.Readers.VIBRATION)
                .reader("sculk_charge", ParticleType.Readers.SCULK_CHARGE)
                .reader("shriek", ParticleType.Readers.SHRIEK);*/
    }

    @Override
    public void init(final UserConnection connection) {
        // Register the entity tracker - used for entity id/metadata rewriting AND for tracking world data sent to the client (then used for chunk data rewriting)
        addEntityTracker(connection, new EntityTrackerBase(connection, Entity1_19_4Types.PLAYER));
    }

    // Overriding these three methods is important as they are relied on various rewriter classes
    @Override
    public MappingData getMappingData() {
        return MAPPINGS;
    }

    @Override
    public EntityPacketRewriter1_99 getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    public BlockItemPacketRewriter1_99 getItemRewriter() {
        return itemRewriter;
    }

    @Override
    protected ClientboundPacketType clientboundFinishConfigurationPacket() {
        return ClientboundConfigurationPackets1_20_2.FINISH_CONFIGURATION;
    }

    @Override
    protected ServerboundPacketType serverboundFinishConfigurationPacket() {
        return ServerboundConfigurationPackets1_20_2.FINISH_CONFIGURATION;
    }
}