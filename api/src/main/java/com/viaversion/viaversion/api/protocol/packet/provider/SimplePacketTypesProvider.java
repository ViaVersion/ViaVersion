/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.viaversion.viaversion.api.protocol.packet.provider;

import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.State;
import java.util.Map;

public final class SimplePacketTypesProvider<CU extends ClientboundPacketType, CM extends ClientboundPacketType, SM extends ServerboundPacketType, SU extends ServerboundPacketType> implements PacketTypesProvider<CU, CM, SM, SU> {
    private final Map<State, PacketTypeMap<CU>> unmappedClientboundPacketTypes;
    private final Map<State, PacketTypeMap<CM>> mappedClientboundPacketTypes;
    private final Map<State, PacketTypeMap<SM>> mappedServerboundPacketTypes;
    private final Map<State, PacketTypeMap<SU>> unmappedServerboundPacketTypes;

    public SimplePacketTypesProvider(
            Map<State, PacketTypeMap<CU>> unmappedClientboundPacketTypes,
            Map<State, PacketTypeMap<CM>> mappedClientboundPacketTypes,
            Map<State, PacketTypeMap<SM>> mappedServerboundPacketTypes,
            Map<State, PacketTypeMap<SU>> unmappedServerboundPacketTypes
    ) {
        this.unmappedClientboundPacketTypes = unmappedClientboundPacketTypes;
        this.mappedClientboundPacketTypes = mappedClientboundPacketTypes;
        this.mappedServerboundPacketTypes = mappedServerboundPacketTypes;
        this.unmappedServerboundPacketTypes = unmappedServerboundPacketTypes;
    }

    @Override
    public Map<State, PacketTypeMap<CU>> unmappedClientboundPacketTypes() {
        return unmappedClientboundPacketTypes;
    }

    @Override
    public Map<State, PacketTypeMap<CM>> mappedClientboundPacketTypes() {
        return mappedClientboundPacketTypes;
    }

    @Override
    public Map<State, PacketTypeMap<SM>> mappedServerboundPacketTypes() {
        return mappedServerboundPacketTypes;
    }

    @Override
    public Map<State, PacketTypeMap<SU>> unmappedServerboundPacketTypes() {
        return unmappedServerboundPacketTypes;
    }
}
