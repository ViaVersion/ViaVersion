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
package com.viaversion.viaversion.protocols.v1_17_1to1_18.rewriter;

import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.protocols.v1_17to1_17_1.packet.ClientboundPackets1_17_1;
import com.viaversion.viaversion.rewriter.text.JsonNBTComponentRewriter;

public final class ComponentRewriter1_18 extends JsonNBTComponentRewriter<ClientboundPackets1_17_1> {

    public ComponentRewriter1_18(final Protocol<ClientboundPackets1_17_1, ?, ?, ?> protocol) {
        super(protocol, ReadType.JSON);
    }

    @Override
    protected void handleTranslate(final JsonObject object, final String translate) {
        if (translate.equals("commands.scoreboard.objectives.add.longName")) {
            object.addProperty("translate", "Objective names cannot be longer than %s characters");
        } else if (translate.equals("commands.team.add.longName")) {
            object.addProperty("translate", "Team names cannot be longer than %s characters");
        }
    }
}
