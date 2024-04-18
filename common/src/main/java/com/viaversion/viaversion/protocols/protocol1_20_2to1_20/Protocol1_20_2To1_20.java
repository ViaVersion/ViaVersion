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
package com.viaversion.viaversion.protocols.protocol1_20_2to1_20;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.google.gson.JsonElement;
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
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.exception.CancelException;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.ServerboundPackets1_19_4;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ClientboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ClientboundPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ServerboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ServerboundPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.rewriter.BlockItemPacketRewriter1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.rewriter.EntityPacketRewriter1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.storage.ConfigurationState;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.storage.ConfigurationState.BridgePhase;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.storage.LastResourcePack;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.storage.LastTags;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import java.util.UUID;
import com.viaversion.viaversion.util.Key;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class Protocol1_20_2To1_20 extends AbstractProtocol<ClientboundPackets1_19_4, ClientboundPackets1_20_2, ServerboundPackets1_19_4, ServerboundPackets1_20_2> {

    public static final MappingData MAPPINGS = new MappingDataBase("1.20", "1.20.2");
    private final EntityPacketRewriter1_20_2 entityPacketRewriter = new EntityPacketRewriter1_20_2(this);
    private final BlockItemPacketRewriter1_20_2 itemPacketRewriter = new BlockItemPacketRewriter1_20_2(this);
    private final TagRewriter<ClientboundPackets1_19_4> tagRewriter = new TagRewriter<>(this);

    public Protocol1_20_2To1_20() {
        super(ClientboundPackets1_19_4.class, ClientboundPackets1_20_2.class, ServerboundPackets1_19_4.class, ServerboundPackets1_20_2.class);
    }

    @Override
    protected void registerPackets() {
        // Close your eyes and turn around while you still can
        super.registerPackets();

        final SoundRewriter<ClientboundPackets1_19_4> soundRewriter = new SoundRewriter<>(this);
        soundRewriter.register1_19_3Sound(ClientboundPackets1_19_4.SOUND);
        soundRewriter.register1_19_3Sound(ClientboundPackets1_19_4.ENTITY_SOUND);

        final PacketHandlers sanitizeCustomPayload = new PacketHandlers() {
            @Override
            protected void register() {
                map(Type.STRING); // Channel
                handlerSoftFail(wrapper -> {
                    final String channel = Key.namespaced(wrapper.get(Type.STRING, 0));
                    if (channel.equals("minecraft:brand")) {
                        wrapper.passthrough(Type.STRING);
                        wrapper.clearInputBuffer();
                    }
                });
            }
        };
        registerClientbound(ClientboundPackets1_19_4.PLUGIN_MESSAGE, sanitizeCustomPayload);
        registerServerbound(ServerboundPackets1_20_2.PLUGIN_MESSAGE, sanitizeCustomPayload);

        registerClientbound(ClientboundPackets1_19_4.RESOURCE_PACK, wrapper -> {
            final String url = wrapper.passthrough(Type.STRING);
            final String hash = wrapper.passthrough(Type.STRING);
            final boolean required = wrapper.passthrough(Type.BOOLEAN);
            final JsonElement prompt = wrapper.passthrough(Type.OPTIONAL_COMPONENT);
            wrapper.user().put(new LastResourcePack(url, hash, required, prompt));
        });

        registerClientbound(ClientboundPackets1_19_4.TAGS, wrapper -> {
            tagRewriter.getGenericHandler().handle(wrapper);
            wrapper.resetReader();
            wrapper.user().put(new LastTags(wrapper));
        });
        registerClientbound(State.CONFIGURATION, ClientboundConfigurationPackets1_20_2.UPDATE_TAGS.getId(), ClientboundConfigurationPackets1_20_2.UPDATE_TAGS.getId(), wrapper -> {
            tagRewriter.getGenericHandler().handle(wrapper);
            wrapper.resetReader();
            wrapper.user().put(new LastTags(wrapper));
        });

        registerClientbound(ClientboundPackets1_19_4.DISPLAY_SCOREBOARD, wrapper -> {
            final byte slot = wrapper.read(Type.BYTE);
            wrapper.write(Type.VAR_INT, (int) slot);
        });

        registerServerbound(State.LOGIN, ServerboundLoginPackets.HELLO.getId(), ServerboundLoginPackets.HELLO.getId(), wrapper -> {
            wrapper.passthrough(Type.STRING); // Name

            final UUID uuid = wrapper.read(Type.UUID);
            wrapper.write(Type.OPTIONAL_UUID, uuid);
        });

        // Deal with the new CONFIGURATION protocol state the client expects
        // After the game profile is received by the client, it will send its login ack,
        // switch to the configuration protocol state and send its brand.
        // We need to wait for it send the login ack before actually sending the play login,
        // hence packets are added to a queue. With the data from the login packet, we sent what is needed
        // during the configuration phase before finally transitioning to the play state with the client as well.
        registerClientbound(State.LOGIN, ClientboundLoginPackets.GAME_PROFILE.getId(), ClientboundLoginPackets.GAME_PROFILE.getId(), wrapper -> {
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
                    wrapper.read(Type.STRING), // Language
                    wrapper.read(Type.BYTE), // View distance
                    wrapper.read(Type.VAR_INT), // Chat visibility
                    wrapper.read(Type.BOOLEAN), // Chat colors
                    wrapper.read(Type.UNSIGNED_BYTE), // Model customization
                    wrapper.read(Type.VAR_INT), // Main hand
                    wrapper.read(Type.BOOLEAN), // Text filtering enabled
                    wrapper.read(Type.BOOLEAN) // Allow listing in server list preview
            );

            // Store it to re-send it when another ClientboundLoginPacket is sent, since the client will only send it
            // once per connection right after the handshake
            final ConfigurationState configurationState = wrapper.user().get(ConfigurationState.class);
            configurationState.setClientInformation(clientInformation);
            wrapper.cancel();
        });

        // If these are not queued, they may be received before the server switched its listener state to play
        registerServerbound(State.CONFIGURATION, ServerboundConfigurationPackets1_20_2.CUSTOM_PAYLOAD.getId(), -1, queueServerboundPacket(ServerboundPackets1_20_2.PLUGIN_MESSAGE));
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
            final long time = wrapper.read(Type.LONG);

            final PacketWrapper responsePacket = wrapper.create(ClientboundPackets1_20_2.PONG_RESPONSE);
            responsePacket.write(Type.LONG, time);
            responsePacket.sendFuture(Protocol1_20_2To1_20.class);
        });
    }

    @Override
    public void transform(final Direction direction, final State state, final PacketWrapper packetWrapper) throws Exception {
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
            if (unmappedId == ClientboundPackets1_19_4.TAGS.getId()) {
                // Don't re-send old tags during config phase
                packetWrapper.user().remove(LastTags.class);
            }

            // Queue packets sent by the server while we wait for the client to transition to the configuration state
            configurationBridge.addPacketToQueue(packetWrapper, true);
            throw CancelException.generate();
        }

        if (packetWrapper.getPacketType() == null || packetWrapper.getPacketType().state() != State.CONFIGURATION) {
            // Map some of them to their configuration state counterparts, but make sure to let join game through
            if (unmappedId == ClientboundPackets1_19_4.JOIN_GAME.getId()) {
                super.transform(direction, State.PLAY, packetWrapper);
                return;
            }

            if (configurationBridge.queuedOrSentJoinGame()) {
                if (!packetWrapper.user().isClientSide() && !Via.getPlatform().isProxy() && unmappedId == ClientboundPackets1_19_4.SYSTEM_CHAT.getId()) {
                    // Cancelling this on the Vanilla server will cause it to exceptionally resend a message
                    // Assume that we have already sent the login packet and just let it through
                    super.transform(direction, State.PLAY, packetWrapper);
                    return;
                }

                configurationBridge.addPacketToQueue(packetWrapper, true);
                throw CancelException.generate();
            }

            if (unmappedId == ClientboundPackets1_19_4.PLUGIN_MESSAGE.getId()) {
                packetWrapper.setPacketType(ClientboundConfigurationPackets1_20_2.CUSTOM_PAYLOAD);
            } else if (unmappedId == ClientboundPackets1_19_4.DISCONNECT.getId()) {
                packetWrapper.setPacketType(ClientboundConfigurationPackets1_20_2.DISCONNECT);
            } else if (unmappedId == ClientboundPackets1_19_4.KEEP_ALIVE.getId()) {
                packetWrapper.setPacketType(ClientboundConfigurationPackets1_20_2.KEEP_ALIVE);
            } else if (unmappedId == ClientboundPackets1_19_4.PING.getId()) {
                packetWrapper.setPacketType(ClientboundConfigurationPackets1_20_2.PING);
            } else if (unmappedId == ClientboundPackets1_19_4.UPDATE_ENABLED_FEATURES.getId()) {
                packetWrapper.setPacketType(ClientboundConfigurationPackets1_20_2.UPDATE_ENABLED_FEATURES);
            } else if (unmappedId == ClientboundPackets1_19_4.TAGS.getId()) {
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

    public static void sendConfigurationPackets(final UserConnection connection, final CompoundTag dimensionRegistry, @Nullable final LastResourcePack lastResourcePack) throws Exception {
        final ProtocolInfo protocolInfo = connection.getProtocolInfo();
        protocolInfo.setServerState(State.CONFIGURATION);

        final PacketWrapper registryDataPacket = PacketWrapper.create(ClientboundConfigurationPackets1_20_2.REGISTRY_DATA, connection);
        registryDataPacket.write(Type.COMPOUND_TAG, dimensionRegistry);
        registryDataPacket.send(Protocol1_20_2To1_20.class);

        // If we tracked enables features, they'd be sent here
        // The client includes vanilla as the default feature when initially leaving the login phase

        final LastTags lastTags = connection.get(LastTags.class);
        if (lastTags != null) {
            // The server might still follow up with a tags packet, but we wouldn't know
            lastTags.sendLastTags(connection);
        }

        if (lastResourcePack != null && connection.getProtocolInfo().protocolVersion() == ProtocolVersion.v1_20_2) {
            // The client for some reason drops the resource pack when reentering the configuration state
            final PacketWrapper resourcePackPacket = PacketWrapper.create(ClientboundConfigurationPackets1_20_2.RESOURCE_PACK, connection);
            resourcePackPacket.write(Type.STRING, lastResourcePack.url());
            resourcePackPacket.write(Type.STRING, lastResourcePack.hash());
            resourcePackPacket.write(Type.BOOLEAN, lastResourcePack.required());
            resourcePackPacket.write(Type.OPTIONAL_COMPONENT, lastResourcePack.prompt());
            resourcePackPacket.send(Protocol1_20_2To1_20.class);
        }

        final PacketWrapper finishConfigurationPacket = PacketWrapper.create(ClientboundConfigurationPackets1_20_2.FINISH_CONFIGURATION, connection);
        finishConfigurationPacket.send(Protocol1_20_2To1_20.class);

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
    public EntityRewriter<Protocol1_20_2To1_20> getEntityRewriter() {
        return entityPacketRewriter;
    }

    @Override
    public ItemRewriter<Protocol1_20_2To1_20> getItemRewriter() {
        return itemPacketRewriter;
    }

    @Override
    public TagRewriter<ClientboundPackets1_19_4> getTagRewriter() {
        return tagRewriter;
    }
}
