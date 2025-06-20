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
package com.viaversion.viaversion.protocols.v1_18_2to1_19.rewriter;

import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.protocols.v1_17_1to1_18.packet.ClientboundPackets1_18;
import com.viaversion.viaversion.rewriter.text.JsonNBTComponentRewriter;
import java.util.HashMap;
import java.util.Map;

public final class ComponentRewriter1_19 extends JsonNBTComponentRewriter<ClientboundPackets1_18> {

    private final Map<String, String> mappings = new HashMap<>();

    public ComponentRewriter1_19(final Protocol<ClientboundPackets1_18, ?, ?, ?> protocol) {
        super(protocol, ReadType.JSON);

        mappings.put("commands.locate.invalid", "commands.locate.structure.invalid");
        mappings.put("commands.locatebiome.success", "commands.locate.biome.success");
        mappings.put("command.placefeature.invalid", "commands.place.feature.failed");
        mappings.put("commands.locatebiome.invalid", "commands.locate.biome.invalid");
        mappings.put("commands.placefeature.invalid", "commands.place.feature.invalid");
        mappings.put("commands.locatebiome.notFound", "commands.locate.biome.not_found");
        mappings.put("commands.placefeature.success", "commands.place.feature.success");
        mappings.put("commands.locate.failed", "commands.locate.structure.not_found");
        mappings.put("commands.locate.success", "commands.locate.structure.success");
    }

    @Override
    protected void handleTranslate(final JsonObject object, final String translate) {
        String mappedTranslation = mappings.get(translate);
        if (mappedTranslation != null) {
            object.addProperty("translate", mappedTranslation);
        }
    }

}
