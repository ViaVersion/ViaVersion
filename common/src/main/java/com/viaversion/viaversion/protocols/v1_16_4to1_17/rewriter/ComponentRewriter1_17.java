/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_16_4to1_17.rewriter;

import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.packet.ClientboundPackets1_16_2;
import com.viaversion.viaversion.rewriter.text.JsonNBTComponentRewriter;
import java.util.Map;

public final class ComponentRewriter1_17 extends JsonNBTComponentRewriter<ClientboundPackets1_16_2> {

    private final Map<String, String> mappings;

    public ComponentRewriter1_17(final Protocol<ClientboundPackets1_16_2, ?, ?, ?> protocol) {
        super(protocol, ReadType.JSON);
        final MappingData mappingData = protocol.getMappingData();
        mappings = mappingData != null ? mappingData.getTranslationMappings() : Map.of();
    }

    @Override
    protected void handleTranslate(final JsonObject object, final String translate) {
        String mappedTranslation = mappings.get(translate);
        if (mappedTranslation != null) {
            object.addProperty("translate", mappedTranslation);
        }
    }
}
