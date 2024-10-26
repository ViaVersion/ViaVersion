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
package com.viaversion.viaversion.protocols.template;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_20_5;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.packet.provider.SimplePacketTypesProvider;
import com.viaversion.viaversion.api.rewriter.ComponentRewriter;
import com.viaversion.viaversion.api.type.types.version.Types1_21_2;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPacket1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundConfigurationPackets1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPacket1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import com.viaversion.viaversion.rewriter.AttributeRewriter;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;

import static com.viaversion.viaversion.util.ProtocolUtil.packetTypeMap;

// Placeholders to replace (in the entire package):
//   Protocol1_99To_98, EntityPacketRewriter1_99, BlockItemPacketRewriter1_99 - move the latter two to a rewriter package
//   ClientboundPacket1_21
//   ServerboundPacket1_20_5
//   EntityTypes1_20_5 (MAPPED type)
//   Types1_21_2.PARTICLE
//   1.99, 1.98
final class Protocol1_99To_98 extends AbstractProtocol<ClientboundPacket1_21, ClientboundPacket1_21, ServerboundPacket1_20_5, ServerboundPacket1_20_5> {

    public static final MappingData MAPPINGS = new MappingDataBase("1.98", "1.99");
    private final EntityPacketRewriter1_99 entityRewriter = new EntityPacketRewriter1_99(this);
    private final BlockItemPacketRewriter1_99 itemRewriter = new BlockItemPacketRewriter1_99(this);
    private final ParticleRewriter<ClientboundPacket1_21> particleRewriter = new ParticleRewriter<>(this, /*Types1_OLD.PARTICLE,*/ Types1_21_2.PARTICLE);
    private final TagRewriter<ClientboundPacket1_21> tagRewriter = new TagRewriter<>(this);
    private final ComponentRewriter1_99 componentRewriter = new ComponentRewriter1_99(this);

    public Protocol1_99To_98() {
        // Passing the class types into the super constructor is needed for automatic packet type id remapping, but can otherwise be omitted
        super(ClientboundPacket1_21.class, ClientboundPacket1_21.class, ServerboundPacket1_20_5.class, ServerboundPacket1_20_5.class);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        tagRewriter.registerGeneric(ClientboundPackets1_21.UPDATE_TAGS);
        tagRewriter.registerGeneric(ClientboundConfigurationPackets1_21.UPDATE_TAGS);

        // If needed for item or component changes
        componentRewriter.registerOpenScreen(ClientboundPackets1_21.OPEN_SCREEN);
        componentRewriter.registerComponentPacket(ClientboundPackets1_21.SET_ACTION_BAR_TEXT);
        componentRewriter.registerComponentPacket(ClientboundPackets1_21.SET_TITLE_TEXT);
        componentRewriter.registerComponentPacket(ClientboundPackets1_21.SET_SUBTITLE_TEXT);
        componentRewriter.registerBossEvent(ClientboundPackets1_21.BOSS_EVENT);
        componentRewriter.registerComponentPacket(ClientboundPackets1_21.DISCONNECT);
        componentRewriter.registerTabList(ClientboundPackets1_21.TAB_LIST);
        componentRewriter.registerPlayerCombatKill1_20(ClientboundPackets1_21.PLAYER_COMBAT_KILL);
        componentRewriter.registerComponentPacket(ClientboundPackets1_21.SYSTEM_CHAT);
        componentRewriter.registerComponentPacket(ClientboundPackets1_21.DISGUISED_CHAT);
        componentRewriter.registerPlayerInfoUpdate1_21_2(ClientboundPackets1_21.PLAYER_INFO_UPDATE);
        componentRewriter.registerPing();

        // If needed for any particle, item, or block changes. Extend ParticleRewriter for particle serializer changes
        particleRewriter.registerLevelParticles1_20_5(ClientboundPackets1_21.LEVEL_PARTICLES);
        particleRewriter.registerExplode1_21_2(ClientboundPackets1_21.EXPLODE); // Rewrites the included sound and particles

        final SoundRewriter<ClientboundPacket1_21> soundRewriter = new SoundRewriter<>(this);
        soundRewriter.registerSound1_19_3(ClientboundPackets1_21.SOUND);
        soundRewriter.registerSound1_19_3(ClientboundPackets1_21.SOUND_ENTITY);

        new StatisticsRewriter<>(this).register(ClientboundPackets1_21.AWARD_STATS);
        new AttributeRewriter<>(this).register1_21(ClientboundPackets1_21.UPDATE_ATTRIBUTES);

        // Uncomment if an existing type changed serialization format. Mappings for argument type keys can also be defined in mapping files
        /*new CommandRewriter1_19_4<>(this) {
            @Override
            public void handleArgument(final PacketWrapper wrapper, final String argumentType) {
                if (argumentType.equals("minecraft:abc")) {
                    // New argument
                    wrapper.write(Types.INT, 0);
                } else {
                    super.handleArgument(wrapper, argumentType);
                }
            }
        }.registerDeclareCommands1_19(ClientboundPackets1_21.COMMANDS);*/
    }

    @Override
    protected void onMappingDataLoaded() {
        // Uncomment this if the entity types enum has been newly added specifically for this Protocol
        // EntityTypes1_20_5.initialize(this);

        // Uncomment if a new particle was added = ids shifted; requires a new Types_ class copied from the last
        /*Types1_21_2.PARTICLE.filler(this)
            .reader("block", ParticleType.Readers.BLOCK)
            .reader("block_marker", ParticleType.Readers.BLOCK)
            .reader("dust", ParticleType.Readers.DUST1_21_2)
            .reader("dust_pillar", ParticleType.Readers.BLOCK)
            .reader("falling_dust", ParticleType.Readers.BLOCK)
            .reader("dust_color_transition", ParticleType.Readers.DUST_TRANSITION1_21_2)
            .reader("vibration", ParticleType.Readers.VIBRATION1_20_3)
            .reader("sculk_charge", ParticleType.Readers.SCULK_CHARGE)
            .reader("shriek", ParticleType.Readers.SHRIEK)
            .reader("entity_effect", ParticleType.Readers.COLOR)
            .reader("trail", ParticleType.Readers.TRAIL)
            .reader("item", ParticleType.Readers.item(itemRewriter.mappedItemType()));*/

        super.onMappingDataLoaded(); // Calls load methods on rewriters. Last in case the rewriters access the above filled data
    }

    @Override
    public void init(final UserConnection connection) {
        // Register the entity tracker - used for entity id/entity data rewriting AND for tracking world data sent to the client (then used for chunk data rewriting)
        addEntityTracker(connection, new EntityTrackerBase(connection, EntityTypes1_20_5.PLAYER));
    }

    // Overriding these four methods is important as they are relied on various rewriter classes
    // and have mapping load methods called in AbstractProtocol via the getters
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
    public ParticleRewriter<ClientboundPacket1_21> getParticleRewriter() {
        return particleRewriter;
    }

    @Override
    public TagRewriter<ClientboundPacket1_21> getTagRewriter() {
        return tagRewriter;
    }

    @Override
    public ComponentRewriter getComponentRewriter() {
        return componentRewriter;
    }

    @Override
    protected PacketTypesProvider<ClientboundPacket1_21, ClientboundPacket1_21, ServerboundPacket1_20_5, ServerboundPacket1_20_5> createPacketTypesProvider() {
        return new SimplePacketTypesProvider<>(
            packetTypeMap(unmappedClientboundPacketType, ClientboundPackets1_21.class, ClientboundConfigurationPackets1_21.class),
            packetTypeMap(mappedClientboundPacketType, ClientboundPackets1_21.class, ClientboundConfigurationPackets1_21.class),
            packetTypeMap(mappedServerboundPacketType, ServerboundPackets1_20_5.class, ServerboundConfigurationPackets1_20_5.class),
            packetTypeMap(unmappedServerboundPacketType, ServerboundPackets1_20_5.class, ServerboundConfigurationPackets1_20_5.class)
        );
    }
}
