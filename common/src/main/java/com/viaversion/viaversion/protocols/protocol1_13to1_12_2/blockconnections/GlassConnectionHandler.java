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
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.blockconnections;

import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Position;

import java.util.ArrayList;
import java.util.List;

public class GlassConnectionHandler extends AbstractFenceConnectionHandler {

    static List<ConnectionData.ConnectorInitAction> init() {
        List<ConnectionData.ConnectorInitAction> actions = new ArrayList<>(18);
        actions.add(new GlassConnectionHandler("paneConnections").getInitAction("minecraft:white_stained_glass_pane"));
        actions.add(new GlassConnectionHandler("paneConnections").getInitAction("minecraft:orange_stained_glass_pane"));
        actions.add(new GlassConnectionHandler("paneConnections").getInitAction("minecraft:magenta_stained_glass_pane"));
        actions.add(new GlassConnectionHandler("paneConnections").getInitAction("minecraft:light_blue_stained_glass_pane"));
        actions.add(new GlassConnectionHandler("paneConnections").getInitAction("minecraft:yellow_stained_glass_pane"));
        actions.add(new GlassConnectionHandler("paneConnections").getInitAction("minecraft:lime_stained_glass_pane"));
        actions.add(new GlassConnectionHandler("paneConnections").getInitAction("minecraft:pink_stained_glass_pane"));
        actions.add(new GlassConnectionHandler("paneConnections").getInitAction("minecraft:gray_stained_glass_pane"));
        actions.add(new GlassConnectionHandler("paneConnections").getInitAction("minecraft:light_gray_stained_glass_pane"));
        actions.add(new GlassConnectionHandler("paneConnections").getInitAction("minecraft:cyan_stained_glass_pane"));
        actions.add(new GlassConnectionHandler("paneConnections").getInitAction("minecraft:purple_stained_glass_pane"));
        actions.add(new GlassConnectionHandler("paneConnections").getInitAction("minecraft:blue_stained_glass_pane"));
        actions.add(new GlassConnectionHandler("paneConnections").getInitAction("minecraft:brown_stained_glass_pane"));
        actions.add(new GlassConnectionHandler("paneConnections").getInitAction("minecraft:green_stained_glass_pane"));
        actions.add(new GlassConnectionHandler("paneConnections").getInitAction("minecraft:red_stained_glass_pane"));
        actions.add(new GlassConnectionHandler("paneConnections").getInitAction("minecraft:black_stained_glass_pane"));
        actions.add(new GlassConnectionHandler("paneConnections").getInitAction("minecraft:glass_pane"));
        actions.add(new GlassConnectionHandler("paneConnections").getInitAction("minecraft:iron_bars"));
        return actions;
    }

    public GlassConnectionHandler(String blockConnections) {
        super(blockConnections);
    }

    @Override
    protected byte getStates(UserConnection user, Position position, int blockState) {
        byte states = super.getStates(user, position, blockState);
        if (states != 0) return states;

        ProtocolInfo protocolInfo = user.getProtocolInfo();
        return protocolInfo.getServerProtocolVersion() <= 47
                && protocolInfo.getServerProtocolVersion() != -1 ? 0xF : states;
    }
}
