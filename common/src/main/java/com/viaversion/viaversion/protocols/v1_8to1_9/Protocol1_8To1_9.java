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
package com.viaversion.viaversion.protocols.v1_8to1_9;

import com.google.gson.JsonElement;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.ValueTransformer;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.v1_8.packet.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.v1_8.packet.ServerboundPackets1_8;
import com.viaversion.viaversion.protocols.v1_8to1_9.metadata.MetadataRewriter1_9To1_8;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ServerboundPackets1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.packets.EntityPackets;
import com.viaversion.viaversion.protocols.v1_8to1_9.packets.InventoryPackets;
import com.viaversion.viaversion.protocols.v1_8to1_9.packets.PlayerPackets;
import com.viaversion.viaversion.protocols.v1_8to1_9.packets.SpawnPackets;
import com.viaversion.viaversion.protocols.v1_8to1_9.packets.WorldPackets;
import com.viaversion.viaversion.protocols.v1_8to1_9.providers.BossBarProvider;
import com.viaversion.viaversion.protocols.v1_8to1_9.providers.CommandBlockProvider;
import com.viaversion.viaversion.protocols.v1_8to1_9.providers.CompressionProvider;
import com.viaversion.viaversion.protocols.v1_8to1_9.providers.EntityIdProvider;
import com.viaversion.viaversion.protocols.v1_8to1_9.providers.HandItemProvider;
import com.viaversion.viaversion.protocols.v1_8to1_9.providers.MainHandProvider;
import com.viaversion.viaversion.protocols.v1_8to1_9.providers.MovementTransmitterProvider;
import com.viaversion.viaversion.protocols.v1_8to1_9.storage.ClientChunks;
import com.viaversion.viaversion.protocols.v1_8to1_9.storage.CommandBlockStorage;
import com.viaversion.viaversion.protocols.v1_8to1_9.storage.EntityTracker1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.storage.InventoryTracker;
import com.viaversion.viaversion.protocols.v1_8to1_9.storage.MovementTracker;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.SerializerVersion;

public class Protocol1_8To1_9 extends AbstractProtocol<ClientboundPackets1_8, ClientboundPackets1_9, ServerboundPackets1_8, ServerboundPackets1_9> {

    public static final ValueTransformer<String, JsonElement> STRING_TO_JSON = new ValueTransformer<>(Type.COMPONENT) {
        @Override
        public JsonElement transform(PacketWrapper wrapper, String line) {
            return ComponentUtil.convertJsonOrEmpty(line, SerializerVersion.V1_8, SerializerVersion.V1_9);
        }
    };
    private final MetadataRewriter1_9To1_8 metadataRewriter = new MetadataRewriter1_9To1_8(this);

    public Protocol1_8To1_9() {
        super(ClientboundPackets1_8.class, ClientboundPackets1_9.class, ServerboundPackets1_8.class, ServerboundPackets1_9.class);
    }

    public static Item getHandItem(final UserConnection info) {
        return Via.getManager().getProviders().get(HandItemProvider.class).getHandItem(info);
    }

    public static boolean isSword(int id) {
        return switch (id) {
            case 267, // Iron sword
                 268, // Wooden sword
                 272, // Stone sword
                 276, // Diamond sword
                 283  // Gold sword
                -> true;
            default -> false;
        };
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        registerClientbound(State.LOGIN, ClientboundLoginPackets.LOGIN_DISCONNECT.getId(), ClientboundLoginPackets.LOGIN_DISCONNECT.getId(), wrapper -> {
            if (wrapper.isReadable(Type.COMPONENT, 0)) {
                // Already written as component in the base protocol
                return;
            }

            STRING_TO_JSON.write(wrapper, wrapper.read(Type.STRING));
        });

        // Other Handlers
        SpawnPackets.register(this);
        InventoryPackets.register(this);
        EntityPackets.register(this);
        PlayerPackets.register(this);
        WorldPackets.register(this);
    }

    @Override
    public void register(ViaProviders providers) {
        providers.register(HandItemProvider.class, new HandItemProvider());
        providers.register(CommandBlockProvider.class, new CommandBlockProvider());
        providers.register(EntityIdProvider.class, new EntityIdProvider());
        providers.register(BossBarProvider.class, new BossBarProvider());
        providers.register(MainHandProvider.class, new MainHandProvider());
        providers.register(CompressionProvider.class, new CompressionProvider());
        providers.register(MovementTransmitterProvider.class, new MovementTransmitterProvider());
    }

    @Override
    public void init(UserConnection userConnection) {
        // Entity tracker
        userConnection.addEntityTracker(this.getClass(), new EntityTracker1_9(userConnection));
        // Chunk tracker
        userConnection.put(new ClientChunks());
        // Movement tracker
        userConnection.put(new MovementTracker());
        // Inventory tracker
        userConnection.put(new InventoryTracker());
        // CommandBlock storage
        userConnection.put(new CommandBlockStorage());

        if (!userConnection.has(ClientWorld.class)) {
            userConnection.put(new ClientWorld());
        }
    }

    @Override
    public MetadataRewriter1_9To1_8 getEntityRewriter() {
        return metadataRewriter;
    }
}
