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
package com.viaversion.viaversion.classgenerator.generated;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.bukkit.handlers.BukkitDecodeHandler;
import com.viaversion.viaversion.bukkit.handlers.BukkitEncodeHandler;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;

public interface HandlerSupplier {

    MessageToMessageEncoder<ByteBuf> newEncodeHandler(UserConnection connection);

    MessageToMessageDecoder<ByteBuf> newDecodeHandler(UserConnection connection);

    final class DefaultHandlerSupplier implements HandlerSupplier {
        @Override
        public MessageToMessageEncoder<ByteBuf> newEncodeHandler(final UserConnection connection) {
            return new BukkitEncodeHandler(connection);
        }

        @Override
        public MessageToMessageDecoder<ByteBuf> newDecodeHandler(final UserConnection connection) {
            return new BukkitDecodeHandler(connection);
        }
    }
}
