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
package com.viaversion.viaversion.protocols.v1_21to1_21_2;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_20_5;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.packet.provider.SimplePacketTypesProvider;
import com.viaversion.viaversion.api.rewriter.ComponentRewriter;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import com.viaversion.viaversion.api.type.types.version.Types1_21_2;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPacket1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundConfigurationPackets1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPacket1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPacket1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ServerboundPacket1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ServerboundPackets1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.rewriter.BlockItemPacketRewriter1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.rewriter.ComponentRewriter1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.rewriter.EntityPacketRewriter1_21_2;
import com.viaversion.viaversion.rewriter.AttributeRewriter;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;

import static com.viaversion.viaversion.util.ProtocolUtil.packetTypeMap;

public final class Protocol1_21To1_21_2 extends AbstractProtocol<ClientboundPacket1_21, ClientboundPacket1_21_2, ServerboundPacket1_20_5, ServerboundPacket1_21_2> {

    public static final MappingData MAPPINGS = new MappingDataBase("1.21", "1.21.2");
    private final EntityPacketRewriter1_21_2 entityRewriter = new EntityPacketRewriter1_21_2(this);
    private final BlockItemPacketRewriter1_21_2 itemRewriter = new BlockItemPacketRewriter1_21_2(this);
    private final TagRewriter<ClientboundPacket1_21> tagRewriter = new TagRewriter<>(this);
    private final ComponentRewriter1_21_2 componentRewriter = new ComponentRewriter1_21_2(this);

    public Protocol1_21To1_21_2() {
        super(ClientboundPacket1_21.class, ClientboundPacket1_21_2.class, ServerboundPacket1_20_5.class, ServerboundPacket1_21_2.class);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        tagRewriter.registerGeneric(ClientboundPackets1_21.UPDATE_TAGS);
        tagRewriter.registerGeneric(ClientboundConfigurationPackets1_21.UPDATE_TAGS);

        final SoundRewriter<ClientboundPacket1_21> soundRewriter = new SoundRewriter<>(this);
        soundRewriter.registerSound1_19_3(ClientboundPackets1_21.SOUND);
        soundRewriter.registerSound1_19_3(ClientboundPackets1_21.SOUND_ENTITY);

        new StatisticsRewriter<>(this).register(ClientboundPackets1_21.AWARD_STATS);
        new AttributeRewriter<>(this).register1_21(ClientboundPackets1_21.UPDATE_ATTRIBUTES);

        registerServerbound(ServerboundPackets1_21_2.CLIENT_INFORMATION, this::clientInformation);
        registerServerbound(ServerboundConfigurationPackets1_20_5.CLIENT_INFORMATION, this::clientInformation);

        cancelServerbound(ServerboundPackets1_21_2.BUNDLE_ITEM_SELECTED);
        cancelServerbound(ServerboundPackets1_21_2.CLIENT_TICK_END);
    }

    private void clientInformation(final PacketWrapper wrapper) {
        wrapper.passthrough(Types.STRING); // Locale
        wrapper.passthrough(Types.BYTE); // View distance
        wrapper.passthrough(Types.VAR_INT); // Chat visibility
        wrapper.passthrough(Types.BOOLEAN); // Chat colors
        wrapper.passthrough(Types.BYTE); // Skin parts
        wrapper.passthrough(Types.VAR_INT); // Main hand
        wrapper.passthrough(Types.BOOLEAN); // Text filtering enabled
        wrapper.passthrough(Types.BOOLEAN); // Allow listing
        wrapper.read(Types.VAR_INT); // Particle status
    }

    @Override
    protected void onMappingDataLoaded() {
        Types1_21_2.PARTICLE.filler(this)
            .reader("block", ParticleType.Readers.BLOCK)
            .reader("block_marker", ParticleType.Readers.BLOCK)
            .reader("dust", ParticleType.Readers.DUST)
            .reader("falling_dust", ParticleType.Readers.BLOCK)
            .reader("dust_color_transition", ParticleType.Readers.DUST_TRANSITION)
            .reader("vibration", ParticleType.Readers.VIBRATION1_20_3)
            .reader("sculk_charge", ParticleType.Readers.SCULK_CHARGE)
            .reader("shriek", ParticleType.Readers.SHRIEK)
            .reader("item", ParticleType.Readers.item(Types1_21_2.ITEM));
        Types1_21_2.STRUCTURED_DATA.filler(this)
            .add(StructuredDataKey.CUSTOM_DATA).add(StructuredDataKey.MAX_STACK_SIZE).add(StructuredDataKey.MAX_DAMAGE)
            .add(StructuredDataKey.DAMAGE).add(StructuredDataKey.UNBREAKABLE).add(StructuredDataKey.RARITY)
            .add(StructuredDataKey.HIDE_TOOLTIP).add(StructuredDataKey.FIRE_RESISTANT)
            .add(StructuredDataKey.CUSTOM_NAME).add(StructuredDataKey.LORE).add(StructuredDataKey.ENCHANTMENTS)
            .add(StructuredDataKey.CAN_PLACE_ON).add(StructuredDataKey.CAN_BREAK)
            .add(StructuredDataKey.CUSTOM_MODEL_DATA).add(StructuredDataKey.HIDE_ADDITIONAL_TOOLTIP).add(StructuredDataKey.REPAIR_COST)
            .add(StructuredDataKey.CREATIVE_SLOT_LOCK).add(StructuredDataKey.ENCHANTMENT_GLINT_OVERRIDE).add(StructuredDataKey.INTANGIBLE_PROJECTILE)
            .add(StructuredDataKey.STORED_ENCHANTMENTS).add(StructuredDataKey.DYED_COLOR).add(StructuredDataKey.MAP_COLOR)
            .add(StructuredDataKey.MAP_ID).add(StructuredDataKey.MAP_DECORATIONS).add(StructuredDataKey.MAP_POST_PROCESSING)
            .add(StructuredDataKey.POTION_CONTENTS)
            .add(StructuredDataKey.SUSPICIOUS_STEW_EFFECTS).add(StructuredDataKey.WRITABLE_BOOK_CONTENT).add(StructuredDataKey.WRITTEN_BOOK_CONTENT)
            .add(StructuredDataKey.TRIM).add(StructuredDataKey.DEBUG_STICK_STATE).add(StructuredDataKey.ENTITY_DATA)
            .add(StructuredDataKey.BUCKET_ENTITY_DATA).add(StructuredDataKey.BLOCK_ENTITY_DATA).add(StructuredDataKey.INSTRUMENT1_21_2)
            .add(StructuredDataKey.RECIPES).add(StructuredDataKey.LODESTONE_TRACKER).add(StructuredDataKey.FIREWORK_EXPLOSION)
            .add(StructuredDataKey.FIREWORKS).add(StructuredDataKey.PROFILE).add(StructuredDataKey.NOTE_BLOCK_SOUND)
            .add(StructuredDataKey.BANNER_PATTERNS).add(StructuredDataKey.BASE_COLOR).add(StructuredDataKey.POT_DECORATIONS)
            .add(StructuredDataKey.BLOCK_STATE).add(StructuredDataKey.BEES)
            .add(StructuredDataKey.LOCK).add(StructuredDataKey.CONTAINER_LOOT).add(StructuredDataKey.TOOL)
            .add(StructuredDataKey.ITEM_NAME).add(StructuredDataKey.OMINOUS_BOTTLE_AMPLIFIER)
            .add(StructuredDataKey.FOOD1_21_2).add(StructuredDataKey.JUKEBOX_PLAYABLE).add(StructuredDataKey.ATTRIBUTE_MODIFIERS1_21)
            .add(StructuredDataKey.REPAIRABLE).add(StructuredDataKey.ENCHANTABLE).add(StructuredDataKey.CONSUMABLE1_21_2)
            .add(StructuredDataKey.USE_REMAINDER).add(StructuredDataKey.USE_COOLDOWN)
            .add(StructuredDataKey.CHARGED_PROJECTILES1_21_2).add(StructuredDataKey.BUNDLE_CONTENTS1_21_2).add(StructuredDataKey.CONTAINER1_21_2);
        super.onMappingDataLoaded();
    }

    @Override
    public void init(final UserConnection connection) {
        addEntityTracker(connection, new EntityTrackerBase(connection, EntityTypes1_20_5.PLAYER));
    }

    @Override
    public MappingData getMappingData() {
        return MAPPINGS;
    }

    @Override
    public EntityPacketRewriter1_21_2 getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    public BlockItemPacketRewriter1_21_2 getItemRewriter() {
        return itemRewriter;
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
    protected PacketTypesProvider<ClientboundPacket1_21, ClientboundPacket1_21_2, ServerboundPacket1_20_5, ServerboundPacket1_21_2> createPacketTypesProvider() {
        return new SimplePacketTypesProvider<>(
            packetTypeMap(unmappedClientboundPacketType, ClientboundPackets1_21.class, ClientboundConfigurationPackets1_21.class),
            packetTypeMap(mappedClientboundPacketType, ClientboundPackets1_21_2.class, ClientboundConfigurationPackets1_21.class),
            packetTypeMap(mappedServerboundPacketType, ServerboundPackets1_20_5.class, ServerboundConfigurationPackets1_20_5.class),
            packetTypeMap(unmappedServerboundPacketType, ServerboundPackets1_21_2.class, ServerboundConfigurationPackets1_20_5.class)
        );
    }
}
