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
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.blockconnections;

import java.util.ArrayList;
import java.util.List;

public class BasicFenceConnectionHandler extends AbstractFenceConnectionHandler {

    static List<ConnectionData.ConnectorInitAction> init() {
        List<ConnectionData.ConnectorInitAction> actions = new ArrayList<>();
        actions.add(new BasicFenceConnectionHandler("fence").getInitAction("minecraft:oak_fence"));
        actions.add(new BasicFenceConnectionHandler("fence").getInitAction("minecraft:birch_fence"));
        actions.add(new BasicFenceConnectionHandler("fence").getInitAction("minecraft:jungle_fence"));
        actions.add(new BasicFenceConnectionHandler("fence").getInitAction("minecraft:dark_oak_fence"));
        actions.add(new BasicFenceConnectionHandler("fence").getInitAction("minecraft:acacia_fence"));
        actions.add(new BasicFenceConnectionHandler("fence").getInitAction("minecraft:spruce_fence"));
        return actions;
    }

    public BasicFenceConnectionHandler(String blockConnections) {
        super(blockConnections);
    }
}
