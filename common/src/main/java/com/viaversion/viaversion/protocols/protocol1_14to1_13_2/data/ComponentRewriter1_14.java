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
package us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data;

import com.google.gson.JsonObject;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.ComponentRewriter1_13;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets.InventoryPackets;

public class ComponentRewriter1_14 extends ComponentRewriter1_13 {

    public ComponentRewriter1_14(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected void handleItem(Item item) {
        InventoryPackets.toClient(item);
    }

    @Override
    protected void handleTranslate(JsonObject object, String translate) {
        // Nothing
    }
}
