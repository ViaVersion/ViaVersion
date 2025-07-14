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
package com.viaversion.viaversion.protocols.v1_8to1_9;

import com.google.gson.JsonElement;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.ValueTransformer;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ServerboundPackets1_8;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ServerboundPackets1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.provider.BossBarProvider;
import com.viaversion.viaversion.protocols.v1_8to1_9.provider.CommandBlockProvider;
import com.viaversion.viaversion.protocols.v1_8to1_9.provider.CompressionProvider;
import com.viaversion.viaversion.protocols.v1_8to1_9.provider.EntityIdProvider;
import com.viaversion.viaversion.protocols.v1_8to1_9.provider.HandItemProvider;
import com.viaversion.viaversion.protocols.v1_8to1_9.provider.MainHandProvider;
import com.viaversion.viaversion.protocols.v1_8to1_9.provider.MovementTransmitterProvider;
import com.viaversion.viaversion.protocols.v1_8to1_9.rewriter.EntityPacketRewriter1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.rewriter.ItemPacketRewriter1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.rewriter.PlayerPacketRewriter1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.rewriter.SpawnPacketRewriter1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.rewriter.WorldPacketRewriter1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.storage.ClientWorld1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.storage.CommandBlockStorage;
import com.viaversion.viaversion.protocols.v1_8to1_9.storage.EntityTracker1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.storage.InventoryTracker;
import com.viaversion.viaversion.protocols.v1_8to1_9.storage.MovementTracker;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.SerializerVersion;

public class Protocol1_8To1_9 extends AbstractProtocol<ClientboundPackets1_8, ClientboundPackets1_9, ServerboundPackets1_8, ServerboundPackets1_9> {

    public static final ValueTransformer<String, JsonElement> STRING_TO_JSON = new ValueTransformer<>(Types.COMPONENT) {
        @Override
        public JsonElement transform(PacketWrapper wrapper, String line) {
            return ComponentUtil.convertJsonOrEmpty(line, SerializerVersion.V1_8, SerializerVersion.V1_9);
        }
    };
    private final EntityPacketRewriter1_9 entityRewriter = new EntityPacketRewriter1_9(this);
    private final ItemPacketRewriter1_9 itemRewriter = new ItemPacketRewriter1_9(this);

    public Protocol1_8To1_9() {
        super(ClientboundPackets1_8.class, ClientboundPackets1_9.class, ServerboundPackets1_8.class, ServerboundPackets1_9.class);
    }

    public static boolean isSword(int id) {
        switch (id) {
            case 267: // Iron sword
            case 268: // Wooden sword
            case 272: // Stone sword
            case 276: // Diamond sword
            case 283: // Gold sword
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        registerClientbound(State.LOGIN, ClientboundLoginPackets.LOGIN_DISCONNECT, wrapper -> STRING_TO_JSON.write(wrapper, wrapper.read(Types.STRING)));

        // Other Handlers
        SpawnPacketRewriter1_9.register(this);
        PlayerPacketRewriter1_9.register(this);
        WorldPacketRewriter1_9.register(this);
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
        userConnection.addEntityTracker(this.getClass(), new EntityTracker1_9(userConnection));
        userConnection.addClientWorld(this.getClass(), new ClientWorld1_9());

        userConnection.put(new MovementTracker());
        userConnection.put(new InventoryTracker());
        userConnection.put(new CommandBlockStorage());
    }

    @Override
    public EntityPacketRewriter1_9 getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    public ItemPacketRewriter1_9 getItemRewriter() {
        return itemRewriter;
    }
}
