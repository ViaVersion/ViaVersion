/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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
package com.viaversion.viaversion.bukkit.classgenerator;

import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import com.viaversion.viaversion.api.protocol.connection.UserConnection;
import com.viaversion.viaversion.bukkit.handlers.BukkitDecodeHandler;
import com.viaversion.viaversion.bukkit.handlers.BukkitEncodeHandler;

public class BasicHandlerConstructor implements HandlerConstructor {
    @Override
    public BukkitEncodeHandler newEncodeHandler(UserConnection info, MessageToByteEncoder minecraftEncoder) {
        return new BukkitEncodeHandler(info, minecraftEncoder);
    }

    @Override
    public BukkitDecodeHandler newDecodeHandler(UserConnection info, ByteToMessageDecoder minecraftDecoder) {
        return new BukkitDecodeHandler(info, minecraftDecoder);
    }
}
