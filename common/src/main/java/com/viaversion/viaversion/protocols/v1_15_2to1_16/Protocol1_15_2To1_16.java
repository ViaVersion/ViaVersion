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
package com.viaversion.viaversion.protocols.v1_15_2to1_16;

import com.google.common.base.Joiner;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_16;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import com.viaversion.viaversion.api.type.types.version.Types1_16;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.base.ClientboundStatusPackets;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.packet.ServerboundPackets1_14;
import com.viaversion.viaversion.protocols.v1_14_4to1_15.packet.ClientboundPackets1_15;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.packet.ClientboundPackets1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.packet.ServerboundPackets1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.provider.PlayerAbilitiesProvider;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.rewriter.ComponentRewriter1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.rewriter.EntityPacketRewriter1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.rewriter.ItemPacketRewriter1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.rewriter.WorldPacketRewriter1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.storage.InventoryTracker1_16;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.util.GsonUtil;
import com.viaversion.viaversion.util.Key;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Protocol1_15_2To1_16 extends AbstractProtocol<ClientboundPackets1_15, ClientboundPackets1_16, ServerboundPackets1_14, ServerboundPackets1_16> {

    private static final UUID ZERO_UUID = new UUID(0, 0);
    public static final MappingData MAPPINGS = new MappingDataBase("1.15", "1.16");
    private final EntityPacketRewriter1_16 entityRewriter = new EntityPacketRewriter1_16(this);
    private final ItemPacketRewriter1_16 itemRewriter = new ItemPacketRewriter1_16(this);
    private final ComponentRewriter1_16 componentRewriter = new ComponentRewriter1_16(this);
    private final TagRewriter<ClientboundPackets1_15> tagRewriter = new TagRewriter<>(this);

    public Protocol1_15_2To1_16() {
        super(ClientboundPackets1_15.class, ClientboundPackets1_16.class, ServerboundPackets1_14.class, ServerboundPackets1_16.class);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        WorldPacketRewriter1_16.register(this);

        tagRewriter.register(ClientboundPackets1_15.UPDATE_TAGS, RegistryType.ENTITY);

        new StatisticsRewriter<>(this).register(ClientboundPackets1_15.AWARD_STATS);

        // Login Success
        registerClientbound(State.LOGIN, ClientboundLoginPackets.LOGIN_FINISHED, wrapper -> {
            // Transform string to a uuid
            UUID uuid = UUID.fromString(wrapper.read(Types.STRING));
            wrapper.write(Types.UUID, uuid);
        });

        // Motd Status - line breaks are no longer allowed for player samples
        registerClientbound(State.STATUS, ClientboundStatusPackets.STATUS_RESPONSE, wrapper -> {
            String original = wrapper.passthrough(Types.STRING);
            JsonObject object = GsonUtil.getGson().fromJson(original, JsonObject.class);
            JsonObject players = object.getAsJsonObject("players");
            if (players == null) return;

            JsonArray sample = players.getAsJsonArray("sample");
            if (sample == null) return;

            JsonArray splitSamples = new JsonArray();
            for (JsonElement element : sample) {
                JsonObject playerInfo = element.getAsJsonObject();
                String name = playerInfo.getAsJsonPrimitive("name").getAsString();
                if (name.indexOf('\n') == -1) {
                    splitSamples.add(playerInfo);
                    continue;
                }

                String id = playerInfo.getAsJsonPrimitive("id").getAsString();
                for (String s : name.split("\n")) {
                    JsonObject newSample = new JsonObject();
                    newSample.addProperty("name", s);
                    newSample.addProperty("id", id);
                    splitSamples.add(newSample);
                }
            }

            // Replace data if changed
            if (splitSamples.size() != sample.size()) {
                players.add("sample", splitSamples);
                wrapper.set(Types.STRING, 0, object.toString());
            }
        });

        // Handle (relevant) component cases for translatable and score changes
        registerClientbound(ClientboundPackets1_15.CHAT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.COMPONENT);
                map(Types.BYTE);
                handler(wrapper -> {
                    componentRewriter.processText(wrapper.user(), wrapper.get(Types.COMPONENT, 0));
                    wrapper.write(Types.UUID, ZERO_UUID); // Sender uuid - always send as 'system'
                });
            }
        });
        componentRewriter.registerBossEvent(ClientboundPackets1_15.BOSS_EVENT);
        componentRewriter.registerTitle(ClientboundPackets1_15.SET_TITLES);
        componentRewriter.registerPlayerCombat(ClientboundPackets1_15.PLAYER_COMBAT);

        SoundRewriter<ClientboundPackets1_15> soundRewriter = new SoundRewriter<>(this);
        soundRewriter.registerSound(ClientboundPackets1_15.SOUND);
        soundRewriter.registerSound(ClientboundPackets1_15.SOUND_ENTITY);

        registerServerbound(ServerboundPackets1_16.INTERACT, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Entity Id
            int action = wrapper.passthrough(Types.VAR_INT);
            if (action == 0 || action == 2) {
                if (action == 2) {
                    // Location
                    wrapper.passthrough(Types.FLOAT);
                    wrapper.passthrough(Types.FLOAT);
                    wrapper.passthrough(Types.FLOAT);
                }

                wrapper.passthrough(Types.VAR_INT); // Hand
            }

            // New boolean: Whether the client is sneaking/pressing shift
            wrapper.read(Types.BOOLEAN);
        });

        if (Via.getConfig().isIgnoreLong1_16ChannelNames()) {
            registerServerbound(ServerboundPackets1_16.CUSTOM_PAYLOAD, new PacketHandlers() {
                @Override
                public void register() {
                    map(Types.STRING); // Channel
                    handler(wrapper -> {
                        final String channel = wrapper.get(Types.STRING, 0);
                        final String namespacedChannel = Key.namespaced(channel);
                        if (channel.length() > 32) {
                            if (!Via.getConfig().isSuppressConversionWarnings()) {
                                getLogger().warning("Ignoring serverbound plugin channel, as it is longer than 32 characters: " + channel);
                            }
                            wrapper.cancel();
                        } else if (namespacedChannel.equals("minecraft:register") || namespacedChannel.equals("minecraft:unregister")) {
                            String[] channels = new String(wrapper.read(Types.REMAINING_BYTES), StandardCharsets.UTF_8).split("\0");
                            List<String> checkedChannels = new ArrayList<>(channels.length);
                            for (String registeredChannel : channels) {
                                if (registeredChannel.length() > 32) {
                                    if (!Via.getConfig().isSuppressConversionWarnings()) {
                                        getLogger().warning("Ignoring serverbound plugin channel register of '" + registeredChannel + "', as it is longer than 32 characters");
                                    }
                                    continue;
                                }

                                checkedChannels.add(registeredChannel);
                            }

                            if (checkedChannels.isEmpty()) {
                                wrapper.cancel();
                                return;
                            }

                            wrapper.write(Types.REMAINING_BYTES, Joiner.on('\0').join(checkedChannels).getBytes(StandardCharsets.UTF_8));
                        }
                    });
                }
            });
        }

        registerServerbound(ServerboundPackets1_16.PLAYER_ABILITIES, wrapper -> {
            wrapper.passthrough(Types.BYTE); // Flags

            final PlayerAbilitiesProvider playerAbilities = Via.getManager().getProviders().get(PlayerAbilitiesProvider.class);
            wrapper.write(Types.FLOAT, playerAbilities.getFlyingSpeed(wrapper.user()));
            wrapper.write(Types.FLOAT, playerAbilities.getWalkingSpeed(wrapper.user()));
        });

        cancelServerbound(ServerboundPackets1_16.JIGSAW_GENERATE);
        cancelServerbound(ServerboundPackets1_16.SET_JIGSAW_BLOCK);
    }

    @Override
    protected void onMappingDataLoaded() {
        EntityTypes1_16.initialize(this);
        Types1_16.PARTICLE.filler(this)
            .reader("block", ParticleType.Readers.BLOCK)
            .reader("dust", ParticleType.Readers.DUST)
            .reader("falling_dust", ParticleType.Readers.BLOCK)
            .reader("item", ParticleType.Readers.ITEM1_13_2);

        tagRewriter.addEmptyTags(RegistryType.ITEM, "minecraft:crimson_stems", "minecraft:non_flammable_wood", "minecraft:piglin_loved",
            "minecraft:piglin_repellents", "minecraft:soul_fire_base_blocks", "minecraft:warped_stems");
        tagRewriter.addEmptyTags(RegistryType.BLOCK, "minecraft:crimson_stems", "minecraft:guarded_by_piglins", "minecraft:hoglin_repellents",
            "minecraft:non_flammable_wood", "minecraft:nylium", "minecraft:piglin_repellents", "minecraft:soul_fire_base_blocks", "minecraft:soul_speed_blocks",
            "minecraft:strider_warm_blocks", "minecraft:warped_stems");

        super.onMappingDataLoaded();
    }

    @Override
    public void register(ViaProviders providers) {
        providers.register(PlayerAbilitiesProvider.class, new PlayerAbilitiesProvider());
    }

    @Override
    public void init(UserConnection userConnection) {
        userConnection.addEntityTracker(this.getClass(), new EntityTrackerBase(userConnection, EntityTypes1_16.PLAYER));
        userConnection.put(new InventoryTracker1_16());
    }

    @Override
    public MappingData getMappingData() {
        return MAPPINGS;
    }

    @Override
    public EntityPacketRewriter1_16 getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    public ItemPacketRewriter1_16 getItemRewriter() {
        return itemRewriter;
    }

    public ComponentRewriter1_16 getComponentRewriter() {
        return componentRewriter;
    }

    @Override
    public TagRewriter<ClientboundPackets1_15> getTagRewriter() {
        return tagRewriter;
    }
}
