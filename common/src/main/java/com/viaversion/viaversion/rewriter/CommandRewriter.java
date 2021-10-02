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
package com.viaversion.viaversion.rewriter;

import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract rewriter for the declare commands packet to handle argument type name and content changes.
 */
public abstract class CommandRewriter {
    protected final Protocol protocol;
    protected final Map<String, CommandArgumentConsumer> parserHandlers = new HashMap<>();

    protected CommandRewriter(Protocol protocol) {
        this.protocol = protocol;

        // Register default parsers
        this.parserHandlers.put("brigadier:double", wrapper -> {
            byte propertyFlags = wrapper.passthrough(Type.BYTE); // Flags
            if ((propertyFlags & 0x01) != 0) wrapper.passthrough(Type.DOUBLE); // Min Value
            if ((propertyFlags & 0x02) != 0) wrapper.passthrough(Type.DOUBLE); // Max Value
        });
        this.parserHandlers.put("brigadier:float", wrapper -> {
            byte propertyFlags = wrapper.passthrough(Type.BYTE); // Flags
            if ((propertyFlags & 0x01) != 0) wrapper.passthrough(Type.FLOAT); // Min Value
            if ((propertyFlags & 0x02) != 0) wrapper.passthrough(Type.FLOAT); // Max Value
        });
        this.parserHandlers.put("brigadier:integer", wrapper -> {
            byte propertyFlags = wrapper.passthrough(Type.BYTE); // Flags
            if ((propertyFlags & 0x01) != 0) wrapper.passthrough(Type.INT); // Min Value
            if ((propertyFlags & 0x02) != 0) wrapper.passthrough(Type.INT); // Max Value
        });
        this.parserHandlers.put("brigadier:long", wrapper -> {
            byte propertyFlags = wrapper.passthrough(Type.BYTE); // Flags
            if ((propertyFlags & 0x01) != 0) wrapper.passthrough(Type.LONG); // Min Value
            if ((propertyFlags & 0x02) != 0) wrapper.passthrough(Type.LONG); // Max Value
        });
        this.parserHandlers.put("brigadier:string", wrapper -> {
            wrapper.passthrough(Type.VAR_INT); // Flags
        });
        this.parserHandlers.put("minecraft:entity", wrapper -> {
            wrapper.passthrough(Type.BYTE); // Flags
        });
        this.parserHandlers.put("minecraft:score_holder", wrapper -> {
            wrapper.passthrough(Type.BYTE); // Flags
        });
    }

    public void handleArgument(PacketWrapper wrapper, String argumentType) throws Exception {
        CommandArgumentConsumer handler = parserHandlers.get(argumentType);
        if (handler != null) {
            handler.accept(wrapper);
        }
    }

    public void registerDeclareCommands(ClientboundPacketType packetType) {
        protocol.registerClientbound(packetType, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    int size = wrapper.passthrough(Type.VAR_INT);
                    for (int i = 0; i < size; i++) {
                        byte flags = wrapper.passthrough(Type.BYTE);
                        wrapper.passthrough(Type.VAR_INT_ARRAY_PRIMITIVE); // Children indices
                        if ((flags & 0x08) != 0) {
                            wrapper.passthrough(Type.VAR_INT); // Redirect node index
                        }

                        byte nodeType = (byte) (flags & 0x03);
                        if (nodeType == 1 || nodeType == 2) { // Literal/argument node
                            wrapper.passthrough(Type.STRING); // Name
                        }

                        if (nodeType == 2) { // Argument node
                            String argumentType = wrapper.read(Type.STRING);
                            String newArgumentType = handleArgumentType(argumentType);
                            if (newArgumentType != null) {
                                wrapper.write(Type.STRING, newArgumentType);
                            }

                            // Always call the handler using the previous name
                            handleArgument(wrapper, argumentType);
                        }

                        if ((flags & 0x10) != 0) {
                            wrapper.passthrough(Type.STRING); // Suggestion type
                        }
                    }

                    wrapper.passthrough(Type.VAR_INT); // Root node index
                });
            }
        });
    }

    /**
     * Can be overridden if needed.
     *
     * @param argumentType argument type
     * @return new argument type, or null if it should be removed
     */
    protected @Nullable String handleArgumentType(String argumentType) {
        return argumentType;
    }

    @FunctionalInterface
    public interface CommandArgumentConsumer {

        void accept(PacketWrapper wrapper) throws Exception;
    }
}
