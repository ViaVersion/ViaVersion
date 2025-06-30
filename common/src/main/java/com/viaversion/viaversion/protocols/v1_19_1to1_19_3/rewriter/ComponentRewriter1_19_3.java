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
package com.viaversion.viaversion.protocols.v1_19_1to1_19_3.rewriter;

import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.protocols.v1_19to1_19_1.packet.ClientboundPackets1_19_1;
import com.viaversion.viaversion.rewriter.text.JsonNBTComponentRewriter;

public final class ComponentRewriter1_19_3 extends JsonNBTComponentRewriter<ClientboundPackets1_19_1> {

    public ComponentRewriter1_19_3(final Protocol<ClientboundPackets1_19_1, ?, ?, ?> protocol) {
        super(protocol, ReadType.JSON);
    }

    @Override
    protected void handleTranslate(final JsonObject object, final String translate) {
        switch (translate) {
            case "commands.locate.poi.invalid" -> object.addProperty("translate", "There is no point of interest with type \"%s\"");
            case "commands.locate.biome.invalid" -> object.addProperty("translate", "There is no biome with type \"%s\"");
            case "multiplayer.disconnect.missing_public_key" -> object.addProperty("translate", "Missing profile public key.\nThis server requires secure profiles.");
            case "multiplayer.disconnect.invalid_public_key" -> object.addProperty("translate", "Invalid signature for profile public key.\nTry restarting your game.");
        }
    }
}
