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
package com.viaversion.viaversion.protocols.template;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.data.version.StructuredDataKeys1_21_11;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_11;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes26_1;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.packet.provider.SimplePacketTypesProvider;
import com.viaversion.viaversion.api.type.types.version.Types1_20_5;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.data.item.ItemHasherBase;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPackets26_1;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundConfigurationPackets1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ServerboundConfigurationPackets1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ServerboundPacket1_21_9;
import com.viaversion.viaversion.rewriter.AttributeRewriter;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.RegistryDataRewriter;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.rewriter.text.NBTComponentRewriter;

import static com.viaversion.viaversion.util.ProtocolUtil.packetTypeMap;

// Placeholders to replace (in the entire package):
//   ClientboundPacket26_1
//   ServerboundPacket1_21_9
//   EntityDataTypes26_1 (MAPPED type)
//   VersionedTypes.V26_1
//   98.1, 99.1
final class Protocol98_1To99_1 extends AbstractProtocol<ClientboundPacket26_1, ClientboundPacket26_1, ServerboundPacket1_21_9, ServerboundPacket1_21_9> {

    public static final MappingData MAPPINGS = new MappingDataBase("98.1", "99.1");
    private final EntityPacketRewriter99_1 entityRewriter = new EntityPacketRewriter99_1(this);
    private final BlockItemPacketRewriter99_1 itemRewriter = new BlockItemPacketRewriter99_1(this);
    private final ParticleRewriter<ClientboundPacket26_1> particleRewriter = new ParticleRewriter<>(this);
    private final TagRewriter<ClientboundPacket26_1> tagRewriter = new TagRewriter<>(this);
    private final NBTComponentRewriter<ClientboundPacket26_1> componentRewriter = new ComponentRewriter99_1(this);
    private final RegistryDataRewriter registryDataRewriter = new RegistryDataRewriter(this);

    public Protocol98_1To99_1() {
        // Passing the class types into the super constructor is needed for automatic packet type id remapping, but can otherwise be omitted
        super(ClientboundPacket26_1.class, ClientboundPacket26_1.class, ServerboundPacket1_21_9.class, ServerboundPacket1_21_9.class);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        registerClientbound(ClientboundConfigurationPackets1_21_9.REGISTRY_DATA, registryDataRewriter::handle);

        tagRewriter.registerGeneric(ClientboundPackets26_1.UPDATE_TAGS);
        tagRewriter.registerGeneric(ClientboundConfigurationPackets1_21_9.UPDATE_TAGS);

        // If needed for item or component changes
        componentRewriter.registerOpenScreen1_14(ClientboundPackets26_1.OPEN_SCREEN);
        componentRewriter.registerComponentPacket(ClientboundPackets26_1.SET_ACTION_BAR_TEXT);
        componentRewriter.registerComponentPacket(ClientboundPackets26_1.SET_TITLE_TEXT);
        componentRewriter.registerComponentPacket(ClientboundPackets26_1.SET_SUBTITLE_TEXT);
        componentRewriter.registerBossEvent(ClientboundPackets26_1.BOSS_EVENT);
        componentRewriter.registerComponentPacket(ClientboundPackets26_1.DISCONNECT);
        componentRewriter.registerComponentPacket(ClientboundConfigurationPackets1_21_9.DISCONNECT);
        componentRewriter.registerTabList(ClientboundPackets26_1.TAB_LIST);
        componentRewriter.registerSetPlayerTeam1_21_5(ClientboundPackets26_1.SET_PLAYER_TEAM);
        componentRewriter.registerPlayerCombatKill1_20(ClientboundPackets26_1.PLAYER_COMBAT_KILL);
        componentRewriter.registerPlayerInfoUpdate1_21_4(ClientboundPackets26_1.PLAYER_INFO_UPDATE);
        componentRewriter.registerComponentPacket(ClientboundPackets26_1.SYSTEM_CHAT);
        componentRewriter.registerDisguisedChat(ClientboundPackets26_1.DISGUISED_CHAT);
        componentRewriter.registerPlayerChat1_21_5(ClientboundPackets26_1.PLAYER_CHAT);
        componentRewriter.registerSetObjective(ClientboundPackets26_1.SET_OBJECTIVE);
        componentRewriter.registerSetScore1_20_3(ClientboundPackets26_1.SET_SCORE);
        componentRewriter.registerPing();

        // If needed for any particle, item, or block changes. Extend ParticleRewriter for particle serializer changes
        particleRewriter.registerLevelParticles1_21_4(ClientboundPackets26_1.LEVEL_PARTICLES);
        particleRewriter.registerExplode1_21_9(ClientboundPackets26_1.EXPLODE); // Rewrites the included sound and particles

        final SoundRewriter<ClientboundPacket26_1> soundRewriter = new SoundRewriter<>(this);
        soundRewriter.registerSound1_19_3(ClientboundPackets26_1.SOUND);
        soundRewriter.registerSound1_19_3(ClientboundPackets26_1.SOUND_ENTITY);

        new StatisticsRewriter<>(this).register(ClientboundPackets26_1.AWARD_STATS);
        new AttributeRewriter<>(this).register1_21(ClientboundPackets26_1.UPDATE_ATTRIBUTES);

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
        }.registerDeclareCommands1_19(ClientboundPackets26_1.COMMANDS);*/
    }

    @Override
    protected void onMappingDataLoaded() {
        // Uncomment this if the entity types enum has been newly added specifically for this Protocol
        // EntityTypes1_21_11.initialize(this);

        // Uncomment if a new particle was added = ids shifted; requires a new Types_ class copied from the last
        /*mappedTypes().particle.filler(this)
          .reader("block", ParticleType.Readers.BLOCK)
            .reader("block_marker", ParticleType.Readers.BLOCK)
            .reader("dust_pillar", ParticleType.Readers.BLOCK)
            .reader("falling_dust", ParticleType.Readers.BLOCK)
            .reader("block_crumble", ParticleType.Readers.BLOCK)
            .reader("dust", ParticleType.Readers.DUST1_21_2)
            .reader("dust_color_transition", ParticleType.Readers.DUST_TRANSITION1_21_2)
            .reader("vibration", ParticleType.Readers.VIBRATION1_20_3)
            .reader("sculk_charge", ParticleType.Readers.SCULK_CHARGE)
            .reader("shriek", ParticleType.Readers.SHRIEK)
            .reader("entity_effect", ParticleType.Readers.COLOR)
            .reader("tinted_leaves", ParticleType.Readers.COLOR)
            .reader("trail", ParticleType.Readers.TRAIL1_21_4)
            .reader("dragon_breath", ParticleType.Readers.POWER)
            .reader("effect", ParticleType.Readers.SPELL)
            .reader("instant_effect", ParticleType.Readers.SPELL)
            .reader("flash", ParticleType.Readers.COLOR)
            .reader("item", ParticleType.Readers.item(itemRewriter.mappedItemTemplateType()));*/

        super.onMappingDataLoaded(); // Calls load methods on rewriters. Last in case the rewriters access the above filled data
    }

    @Override
    public void init(final UserConnection connection) {
        // Register the entity tracker - used for entity id/entity data rewriting AND for tracking world data sent to the client (then used for chunk data rewriting)
        addEntityTracker(connection, new EntityTrackerBase(connection, EntityTypes1_21_11.PLAYER));
        addItemHasher(connection, new ItemHasherBase(this, connection));
    }

    // Overriding these methods is important as they are relied on various rewriter classes
    // and have mapping load methods called in AbstractProtocol via the getters
    @Override
    public MappingData getMappingData() {
        return MAPPINGS;
    }

    @Override
    public EntityPacketRewriter99_1 getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    public BlockItemPacketRewriter99_1 getItemRewriter() {
        return itemRewriter;
    }

    @Override
    public RegistryDataRewriter getRegistryDataRewriter() {
        return registryDataRewriter;
    }

    @Override
    public ParticleRewriter<ClientboundPacket26_1> getParticleRewriter() {
        return particleRewriter;
    }

    @Override
    public TagRewriter<ClientboundPacket26_1> getTagRewriter() {
        return tagRewriter;
    }

    @Override
    public NBTComponentRewriter<ClientboundPacket26_1> getComponentRewriter() {
        return componentRewriter;
    }

    @Override
    public Types1_20_5<StructuredDataKeys1_21_11, EntityDataTypes26_1> types() {
        return VersionedTypes.V26_1;
    }

    @Override
    public Types1_20_5<StructuredDataKeys1_21_11, EntityDataTypes26_1> mappedTypes() {
        return VersionedTypes.V26_1;
    }

    @Override
    protected PacketTypesProvider<ClientboundPacket26_1, ClientboundPacket26_1, ServerboundPacket1_21_9, ServerboundPacket1_21_9> createPacketTypesProvider() {
        return new SimplePacketTypesProvider<>(
            packetTypeMap(unmappedClientboundPacketType, ClientboundPackets26_1.class, ClientboundConfigurationPackets1_21_9.class),
            packetTypeMap(mappedClientboundPacketType, ClientboundPackets26_1.class, ClientboundConfigurationPackets1_21_9.class),
            packetTypeMap(mappedServerboundPacketType, ServerboundPackets1_21_6.class, ServerboundConfigurationPackets1_21_9.class),
            packetTypeMap(unmappedServerboundPacketType, ServerboundPackets1_21_6.class, ServerboundConfigurationPackets1_21_9.class)
        );
    }
}
