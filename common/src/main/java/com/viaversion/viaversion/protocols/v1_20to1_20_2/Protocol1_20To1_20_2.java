/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_20to1_20_2;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_19_4;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.rewriter.EntityRewriter;
import com.viaversion.viaversion.api.rewriter.ItemRewriter;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.exception.CancelException;
import com.viaversion.viaversion.exception.InformativeException;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ServerboundPackets1_19_4;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ClientboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ClientboundPackets1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ServerboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ServerboundPackets1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.rewriter.BlockItemPacketRewriter1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.rewriter.EntityPacketRewriter1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.storage.ConfigurationState;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.storage.ConfigurationState.BridgePhase;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.storage.LastResourcePack;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.storage.LastTags;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.util.Key;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class Protocol1_20To1_20_2 extends AbstractProtocol<ClientboundPackets1_19_4, ClientboundPackets1_20_2, ServerboundPackets1_19_4, ServerboundPackets1_20_2> {

    public static final MappingData MAPPINGS = new MappingDataBase("1.20", "1.20.2");
    private final EntityPacketRewriter1_20_2 entityPacketRewriter = new EntityPacketRewriter1_20_2(this);
    private final BlockItemPacketRewriter1_20_2 itemPacketRewriter = new BlockItemPacketRewriter1_20_2(this);
    private final ParticleRewriter<ClientboundPackets1_19_4> particleRewriter = new ParticleRewriter<>(this);
    private final TagRewriter<ClientboundPackets1_19_4> tagRewriter = new TagRewriter<>(this);

    public Protocol1_20To1_20_2() {
        super(ClientboundPackets1_19_4.class, ClientboundPackets1_20_2.class, ServerboundPackets1_19_4.class, ServerboundPackets1_20_2.class);
    }

    @Override
    protected void registerPackets() {
        // Close your eyes and turn around while you still can
        super.registerPackets();

        final SoundRewriter<ClientboundPackets1_19_4> soundRewriter = new SoundRewriter<>(this);
        soundRewriter.registerSound1_19_3(ClientboundPackets1_19_4.SOUND);
        soundRewriter.registerSound1_19_3(ClientboundPackets1_19_4.SOUND_ENTITY);

        particleRewriter.registerLevelParticles1_19(ClientboundPackets1_19_4.LEVEL_PARTICLES);

        registerClientbound(ClientboundPackets1_19_4.CUSTOM_PAYLOAD, new PacketHandlers() {
            @Override
            protected void register() {
                map(Types.STRING); // Channel
                handlerSoftFail(Protocol1_20To1_20_2::sanitizeCustomPayload);
            }
        });
        registerServerbound(ServerboundPackets1_20_2.CUSTOM_PAYLOAD, wrapper -> {
            wrapper.passthrough(Types.STRING); // Channel
            sanitizeCustomPayload(wrapper);
        });

        registerClientbound(ClientboundPackets1_19_4.SYSTEM_CHAT, wrapper -> {
            if (wrapper.user().isClientSide() || Via.getPlatform().isProxy()) {
                return;
            }

            // Workaround for GH-3438, where chat messages are sent before the login has completed, usually during proxy server switches
            // The server will unnecessarily send an error text message, thinking a packet ran into an error, when it was just cancelled and re-queued
            final JsonElement component = wrapper.passthrough(Types.COMPONENT);
            if (component instanceof JsonObject object && object.has("translate")) {
                final JsonElement translate = object.get("translate");
                if (translate != null && translate.getAsString().equals("multiplayer.message_not_delivered")) {
                    wrapper.cancel();
                }
            }
        });

        registerClientbound(ClientboundPackets1_19_4.RESOURCE_PACK, wrapper -> {
            final String url = wrapper.passthrough(Types.STRING);
            final String hash = wrapper.passthrough(Types.STRING);
            final boolean required = wrapper.passthrough(Types.BOOLEAN);
            final JsonElement prompt = wrapper.passthrough(Types.OPTIONAL_COMPONENT);
            wrapper.user().put(new LastResourcePack(url, hash, required, prompt));
        });

        registerClientbound(ClientboundPackets1_19_4.UPDATE_TAGS, wrapper -> {
            tagRewriter.handleGeneric(wrapper);
            wrapper.resetReader();
            wrapper.user().put(new LastTags(wrapper));
        });
        registerClientbound(State.CONFIGURATION, ClientboundConfigurationPackets1_20_2.UPDATE_TAGS, this::handleConfigTags);

        registerClientbound(ClientboundPackets1_19_4.SET_DISPLAY_OBJECTIVE, wrapper -> {
            final byte slot = wrapper.read(Types.BYTE);
            wrapper.write(Types.VAR_INT, (int) slot);
        });

        registerServerbound(State.LOGIN, ServerboundLoginPackets.HELLO, wrapper -> {
            wrapper.passthrough(Types.STRING); // Name

            final UUID uuid = wrapper.read(Types.UUID);
            wrapper.write(Types.OPTIONAL_UUID, uuid);
        });

        // Deal with the new CONFIGURATION protocol state the client expects
        // After the game profile is received by the client, it will send its login ack,
        // switch to the configuration protocol state and send its brand.
        // We need to wait for it send the login ack before actually sending the play login,
        // hence packets are added to a queue. With the data from the login packet, we sent what is needed
        // during the configuration phase before finally transitioning to the play state with the client as well.
        registerClientbound(State.LOGIN, ClientboundLoginPackets.LOGIN_FINISHED, wrapper -> {
            wrapper.user().get(ConfigurationState.class).setBridgePhase(BridgePhase.PROFILE_SENT);
            wrapper.user().getProtocolInfo().setServerState(State.PLAY);
        });

        registerServerbound(State.LOGIN, ServerboundLoginPackets.LOGIN_ACKNOWLEDGED.getId(), -1, wrapper -> {
            wrapper.cancel();

            // Overwrite the state set in the base protocol to what the server actually keeps sending
            wrapper.user().getProtocolInfo().setServerState(State.PLAY);

            final ConfigurationState configurationState = wrapper.user().get(ConfigurationState.class);
            configurationState.setBridgePhase(BridgePhase.CONFIGURATION);
            configurationState.sendQueuedPackets(wrapper.user());
        });

        registerServerbound(State.CONFIGURATION, ServerboundConfigurationPackets1_20_2.FINISH_CONFIGURATION.getId(), -1, wrapper -> {
            wrapper.cancel();

            wrapper.user().getProtocolInfo().setClientState(State.PLAY);

            final ConfigurationState configurationState = wrapper.user().get(ConfigurationState.class);
            configurationState.setBridgePhase(BridgePhase.NONE);
            configurationState.sendQueuedPackets(wrapper.user());
            configurationState.clear();
        });
        registerServerbound(State.CONFIGURATION, ServerboundConfigurationPackets1_20_2.CLIENT_INFORMATION.getId(), -1, wrapper -> {
            final ConfigurationState.ClientInformation clientInformation = new ConfigurationState.ClientInformation(
                wrapper.read(Types.STRING), // Language
                wrapper.read(Types.BYTE), // View distance
                wrapper.read(Types.VAR_INT), // Chat visibility
                wrapper.read(Types.BOOLEAN), // Chat colors
                wrapper.read(Types.UNSIGNED_BYTE), // Model customization
                wrapper.read(Types.VAR_INT), // Main hand
                wrapper.read(Types.BOOLEAN), // Text filtering enabled
                wrapper.read(Types.BOOLEAN) // Allow listing in server list preview
            );

            // Store it to re-send it when another ClientboundLoginPacket is sent, since the client will only send it
            // once per connection right after the handshake
            final ConfigurationState configurationState = wrapper.user().get(ConfigurationState.class);
            configurationState.setClientInformation(clientInformation);
            wrapper.cancel();
        });

        // If these are not queued, they may be received before the server switched its listener state to play
        registerServerbound(State.CONFIGURATION, ServerboundConfigurationPackets1_20_2.CUSTOM_PAYLOAD.getId(), -1, queueServerboundPacket(ServerboundPackets1_20_2.CUSTOM_PAYLOAD));
        registerServerbound(State.CONFIGURATION, ServerboundConfigurationPackets1_20_2.KEEP_ALIVE.getId(), -1, queueServerboundPacket(ServerboundPackets1_20_2.KEEP_ALIVE));
        registerServerbound(State.CONFIGURATION, ServerboundConfigurationPackets1_20_2.PONG.getId(), -1, queueServerboundPacket(ServerboundPackets1_20_2.PONG));

        // Cancel this, as it will always just be the response to a re-sent pack from us
        registerServerbound(State.CONFIGURATION, ServerboundConfigurationPackets1_20_2.RESOURCE_PACK.getId(), -1, PacketWrapper::cancel);

        cancelClientbound(ClientboundPackets1_19_4.UPDATE_ENABLED_FEATURES); // TODO Sad emoji
        registerServerbound(ServerboundPackets1_20_2.CONFIGURATION_ACKNOWLEDGED, null, wrapper -> {
            wrapper.cancel();

            final ConfigurationState configurationState = wrapper.user().get(ConfigurationState.class);
            if (configurationState.bridgePhase() != BridgePhase.REENTERING_CONFIGURATION) {
                return;
            }

            // Reenter the configuration state
            wrapper.user().getProtocolInfo().setClientState(State.CONFIGURATION);
            configurationState.setBridgePhase(BridgePhase.CONFIGURATION);

            final LastResourcePack lastResourcePack = wrapper.user().get(LastResourcePack.class);
            sendConfigurationPackets(wrapper.user(), configurationState.lastDimensionRegistry(), lastResourcePack);
        });
        cancelServerbound(ServerboundPackets1_20_2.CHUNK_BATCH_RECEIVED);

        registerServerbound(ServerboundPackets1_20_2.PING_REQUEST, null, wrapper -> {
            wrapper.cancel();
            final long time = wrapper.read(Types.LONG);

            final PacketWrapper responsePacket = wrapper.create(ClientboundPackets1_20_2.PONG_RESPONSE);
            responsePacket.write(Types.LONG, time);
            responsePacket.sendFuture(Protocol1_20To1_20_2.class);
        });
    }

    private void handleConfigTags(final PacketWrapper wrapper) {
        tagRewriter.handleGeneric(wrapper);
        wrapper.resetReader();

        final LastTags lastTags = new LastTags(wrapper);
        lastTags.setSentDuringConfigPhase(true);
        wrapper.user().put(lastTags);
    }

    private static void sanitizeCustomPayload(final PacketWrapper wrapper) {
        final String channel = Key.namespaced(wrapper.get(Types.STRING, 0));
        if (channel.equals("minecraft:brand")) {
            wrapper.passthrough(Types.STRING);
            wrapper.clearInputBuffer();
        } else if (channel.equals("minecraft:debug/game_test_add_marker")) {
            wrapper.passthrough(Types.BLOCK_POSITION1_14);
            wrapper.passthrough(Types.INT);
            wrapper.passthrough(Types.STRING);
            wrapper.passthrough(Types.INT);
            wrapper.clearInputBuffer();
        } else if (channel.equals("minecraft:debug/game_test_clear")) {
            wrapper.clearInputBuffer();
        }
    }

    @Override
    public void transform(final Direction direction, final State state, final PacketWrapper packetWrapper) throws InformativeException, CancelException {
        if (direction == Direction.SERVERBOUND) {
            // The client will have the correct state set
            super.transform(direction, state, packetWrapper);
            return;
        }

        final ConfigurationState configurationBridge = packetWrapper.user().get(ConfigurationState.class);
        if (configurationBridge == null) {
            // Bad state during an unexpected disconnect
            return;
        }

        final BridgePhase phase = configurationBridge.bridgePhase();
        if (phase == BridgePhase.NONE) {
            super.transform(direction, state, packetWrapper);
            return;
        }

        final int unmappedId = packetWrapper.getId();
        if (phase == BridgePhase.PROFILE_SENT || phase == BridgePhase.REENTERING_CONFIGURATION) {
            if (unmappedId == ClientboundPackets1_19_4.UPDATE_TAGS.getId()) {
                // Don't re-send old tags during config phase
                packetWrapper.user().remove(LastTags.class);
            }

            // Queue packets sent by the server while we wait for the client to transition to the configuration state
            configurationBridge.addPacketToQueue(packetWrapper, true);
            throw CancelException.generate();
        }

        if (packetWrapper.getPacketType() == null || packetWrapper.getPacketType().state() != State.CONFIGURATION) {
            // Map some of them to their configuration state counterparts, but make sure to let join game through
            if (unmappedId == ClientboundPackets1_19_4.LOGIN.getId()) {
                super.transform(direction, State.PLAY, packetWrapper);
                return;
            }

            if (configurationBridge.queuedOrSentJoinGame()) {
                final ProtocolVersion serverProtocolVersion = Via.getAPI().getServerVersion().lowestSupportedProtocolVersion();

                if (serverProtocolVersion.newerThanOrEqualTo(ProtocolVersion.v1_13)) {
                    if (!packetWrapper.user().isClientSide() && !Via.getPlatform().isProxy() && unmappedId == ClientboundPackets1_19_4.SYSTEM_CHAT.getId()) {
                        // Cancelling this on a 1.13+ Vanilla server will cause it to exceptionally resend a message
                        // Assume that we have already sent the login packet and just let it through
                        super.transform(direction, State.PLAY, packetWrapper);
                        return;
                    }
                }

                configurationBridge.addPacketToQueue(packetWrapper, true);
                throw CancelException.generate();
            }

            if (unmappedId == ClientboundPackets1_19_4.CUSTOM_PAYLOAD.getId()) {
                packetWrapper.setPacketType(ClientboundConfigurationPackets1_20_2.CUSTOM_PAYLOAD);
            } else if (unmappedId == ClientboundPackets1_19_4.DISCONNECT.getId()) {
                packetWrapper.setPacketType(ClientboundConfigurationPackets1_20_2.DISCONNECT);
            } else if (unmappedId == ClientboundPackets1_19_4.KEEP_ALIVE.getId()) {
                packetWrapper.setPacketType(ClientboundConfigurationPackets1_20_2.KEEP_ALIVE);
            } else if (unmappedId == ClientboundPackets1_19_4.PING.getId()) {
                packetWrapper.setPacketType(ClientboundConfigurationPackets1_20_2.PING);
            } else if (unmappedId == ClientboundPackets1_19_4.UPDATE_ENABLED_FEATURES.getId()) {
                packetWrapper.setPacketType(ClientboundConfigurationPackets1_20_2.UPDATE_ENABLED_FEATURES);
            } else if (unmappedId == ClientboundPackets1_19_4.UPDATE_TAGS.getId()) {
                handleConfigTags(packetWrapper); // Manually put through handler
                packetWrapper.setPacketType(ClientboundConfigurationPackets1_20_2.UPDATE_TAGS);
            } else {
                // Not a packet that can be mapped to the configuration protocol
                // Includes resource pack packets to make sure it is not applied sooner than the server expects
                configurationBridge.addPacketToQueue(packetWrapper, true);
                throw CancelException.generate();
            }
            return;
        }

        // Redirect packets during the fake configuration phase
        // This might mess up people using Via API/other protocols down the line, but such is life. We can't have different states for server and client
        super.transform(direction, State.CONFIGURATION, packetWrapper);
    }

    public static void sendConfigurationPackets(final UserConnection connection, final CompoundTag dimensionRegistry, @Nullable final LastResourcePack lastResourcePack) {
        final ProtocolInfo protocolInfo = connection.getProtocolInfo();
        protocolInfo.setServerState(State.CONFIGURATION);

        final PacketWrapper registryDataPacket = PacketWrapper.create(ClientboundConfigurationPackets1_20_2.REGISTRY_DATA, connection);
        registryDataPacket.write(Types.COMPOUND_TAG, dimensionRegistry);
        registryDataPacket.send(Protocol1_20To1_20_2.class);

        // If we tracked enables features, they'd be sent here
        // The client includes vanilla as the default feature when initially leaving the login phase

        final LastTags lastTags = connection.get(LastTags.class);
        if (lastTags != null) {
            if (lastTags.sentDuringConfigPhase()) {
                lastTags.setSentDuringConfigPhase(false);
            } else {
                // The server might still follow up with a tags packet, but we wouldn't know
                lastTags.sendLastTags(connection);
            }
        }

        if (lastResourcePack != null && connection.getProtocolInfo().protocolVersion() == ProtocolVersion.v1_20_2) {
            // The client for some reason drops the resource pack when reentering the configuration state
            final PacketWrapper resourcePackPacket = PacketWrapper.create(ClientboundConfigurationPackets1_20_2.RESOURCE_PACK, connection);
            resourcePackPacket.write(Types.STRING, lastResourcePack.url());
            resourcePackPacket.write(Types.STRING, lastResourcePack.hash());
            resourcePackPacket.write(Types.BOOLEAN, lastResourcePack.required());
            resourcePackPacket.write(Types.OPTIONAL_COMPONENT, lastResourcePack.prompt());
            resourcePackPacket.send(Protocol1_20To1_20_2.class);
        }

        final PacketWrapper finishConfigurationPacket = PacketWrapper.create(ClientboundConfigurationPackets1_20_2.FINISH_CONFIGURATION, connection);
        finishConfigurationPacket.send(Protocol1_20To1_20_2.class);

        protocolInfo.setServerState(State.PLAY);
    }

    private PacketHandler queueServerboundPacket(final ServerboundPackets1_20_2 packetType) {
        return wrapper -> {
            wrapper.setPacketType(packetType);
            wrapper.user().get(ConfigurationState.class).addPacketToQueue(wrapper, false);
            wrapper.cancel();
        };
    }

    @Override
    public MappingData getMappingData() {
        return MAPPINGS;
    }

    @Override
    protected void registerConfigurationChangeHandlers() {
        // Don't handle it in the transitioning protocol
    }

    @Override
    public void init(final UserConnection user) {
        user.put(new ConfigurationState());
        addEntityTracker(user, new EntityTrackerBase(user, EntityTypes1_19_4.PLAYER));
    }

    @Override
    public EntityRewriter<Protocol1_20To1_20_2> getEntityRewriter() {
        return entityPacketRewriter;
    }

    @Override
    public ItemRewriter<Protocol1_20To1_20_2> getItemRewriter() {
        return itemPacketRewriter;
    }

    @Override
    public ParticleRewriter<ClientboundPackets1_19_4> getParticleRewriter() {
        return particleRewriter;
    }

    @Override
    public TagRewriter<ClientboundPackets1_19_4> getTagRewriter() {
        return tagRewriter;
    }
}
