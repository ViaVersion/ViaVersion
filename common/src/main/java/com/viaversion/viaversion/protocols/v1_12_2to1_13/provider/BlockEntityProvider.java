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
package com.viaversion.viaversion.protocols.v1_12_2to1_13.provider;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.platform.providers.Provider;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.Protocol1_12_2To1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.provider.blockentities.BannerHandler;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.provider.blockentities.BedHandler;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.provider.blockentities.CommandBlockHandler;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.provider.blockentities.FlowerPotHandler;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.provider.blockentities.SkullHandler;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.provider.blockentities.SpawnerHandler;
import com.viaversion.viaversion.util.ComponentUtil;
import java.util.HashMap;
import java.util.Map;

public class BlockEntityProvider implements Provider {
    private final Map<String, BlockEntityHandler> handlers = new HashMap<>();

    public BlockEntityProvider() {
        handlers.put("minecraft:flower_pot", new FlowerPotHandler());
        handlers.put("minecraft:bed", new BedHandler());
        handlers.put("minecraft:banner", new BannerHandler());
        handlers.put("minecraft:skull", new SkullHandler());
        handlers.put("minecraft:mob_spawner", new SpawnerHandler());
        handlers.put("minecraft:command_block", new CommandBlockHandler());

        final BlockEntityHandler customNameHandler = (user, tag) -> {
            final StringTag name = tag.getStringTag("CustomName");
            if (name != null) {
                name.setValue(ComponentUtil.legacyToJsonString(name.getValue()));
            }
            return -1;
        };
        handlers.put("minecraft:chest", customNameHandler);
        handlers.put("minecraft:dispenser", customNameHandler);
        handlers.put("minecraft:dropper", customNameHandler);
        handlers.put("minecraft:enchanting_table", customNameHandler);
        handlers.put("minecraft:furnace", customNameHandler);
        handlers.put("minecraft:hopper", customNameHandler);
        handlers.put("minecraft:shulker_box", customNameHandler);
    }

    /**
     * Transforms the BlockEntities to blocks!
     *
     * @param user       UserConnection instance
     * @param position   Block Position - WARNING: Position is null when called from a chunk
     * @param tag        BlockEntity NBT
     * @param sendUpdate send a block change update
     * @return new block id
     */
    public int transform(UserConnection user, BlockPosition position, CompoundTag tag, boolean sendUpdate) {
        StringTag idTag = tag.getStringTag("id");
        if (idTag == null) return -1;

        BlockEntityHandler handler = handlers.get(idTag.getValue());
        if (handler == null) {
            if (Via.getManager().isDebug()) {
                Protocol1_12_2To1_13.LOGGER.warning("Unhandled BlockEntity " + idTag.getValue() + " full tag: " + tag);
            }
            return -1;
        }

        int newBlock = handler.transform(user, tag);

        if (sendUpdate && newBlock != -1) {
            sendBlockChange(user, position, newBlock);
        }

        return newBlock;
    }

    private void sendBlockChange(UserConnection user, BlockPosition position, int blockId) {
        PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_13.BLOCK_UPDATE, null, user);
        wrapper.write(Types.BLOCK_POSITION1_8, position);
        wrapper.write(Types.VAR_INT, blockId);

        wrapper.send(Protocol1_12_2To1_13.class);
    }

    @FunctionalInterface
    public interface BlockEntityHandler {

        int transform(UserConnection user, CompoundTag tag);
    }
}
