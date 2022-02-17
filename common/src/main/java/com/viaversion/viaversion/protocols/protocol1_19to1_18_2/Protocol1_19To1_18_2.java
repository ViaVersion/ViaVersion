/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2022 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_19to1_18_2;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_19Types;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.rewriter.EntityRewriter;
import com.viaversion.viaversion.api.rewriter.ItemRewriter;
import com.viaversion.viaversion.api.type.types.minecraft.ParticleType;
import com.viaversion.viaversion.api.type.types.version.Types1_19;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.ServerboundPackets1_17;
import com.viaversion.viaversion.protocols.protocol1_18to1_17_1.ClientboundPackets1_18;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.packets.EntityPackets;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.packets.InventoryPackets;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.packets.WorldPackets;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;

public final class Protocol1_19To1_18_2 extends AbstractProtocol<ClientboundPackets1_18, ClientboundPackets1_19, ServerboundPackets1_17, ServerboundPackets1_17> {

    public static final MappingData MAPPINGS = new MappingDataBase("1.18", "1.19");
    private final EntityPackets entityRewriter = new EntityPackets(this);
    private final InventoryPackets itemRewriter = new InventoryPackets(this);

    public Protocol1_19To1_18_2() {
        super(ClientboundPackets1_18.class, ClientboundPackets1_19.class, ServerboundPackets1_17.class, ServerboundPackets1_17.class);
    }

    @Override
    protected void registerPackets() {
        final TagRewriter tagRewriter = new TagRewriter(this);
        tagRewriter.addEmptyTag(RegistryType.BLOCK, "minecraft:fall_damage_resetting"); //TODO
        tagRewriter.addEmptyTags(RegistryType.BLOCK, "minecraft:fall_damage_resetting", "minecraft:sculk_replaceable",
                "minecraft:ancient_city_replaceables", "minecraft:deepslate_blocks", "minecraft:sculk_replaceable_world_gen", "minecraft:skip_occlude_vibration_when_above");
        tagRewriter.addEmptyTag(RegistryType.GAME_EVENT, "minecraft:warden_events_can_listen");
        tagRewriter.registerGeneric(ClientboundPackets1_18.TAGS);

        entityRewriter.register();
        itemRewriter.register();
        WorldPackets.register(this);

        cancelClientbound(ClientboundPackets1_18.ADD_VIBRATION_SIGNAL);

        final SoundRewriter soundRewriter = new SoundRewriter(this);
        soundRewriter.registerSound(ClientboundPackets1_18.SOUND);
        soundRewriter.registerSound(ClientboundPackets1_18.ENTITY_SOUND);
    }

    @Override
    protected void onMappingDataLoaded() {
        Types1_19.PARTICLE.filler(this)
                .reader("block", ParticleType.Readers.BLOCK)
                .reader("block_marker", ParticleType.Readers.BLOCK)
                .reader("dust", ParticleType.Readers.DUST)
                .reader("falling_dust", ParticleType.Readers.BLOCK)
                .reader("dust_color_transition", ParticleType.Readers.DUST_TRANSITION)
                .reader("item", ParticleType.Readers.VAR_INT_ITEM)
                .reader("vibration", ParticleType.Readers.VIBRATION)
                .reader("sculk_charge", ParticleType.Readers.SCULK_CHARGE)
                .reader("shriek", ParticleType.Readers.SHRIEK);
    }

    @Override
    public void init(final UserConnection user) {
        addEntityTracker(user, new EntityTrackerBase(user, Entity1_19Types.PLAYER));
    }

    @Override
    public MappingData getMappingData() {
        return MAPPINGS;
    }

    @Override
    public EntityRewriter getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    public ItemRewriter getItemRewriter() {
        return itemRewriter;
    }
}
