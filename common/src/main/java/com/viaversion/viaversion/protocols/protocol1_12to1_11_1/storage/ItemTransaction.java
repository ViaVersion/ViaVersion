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
package com.viaversion.viaversion.protocols.protocol1_12to1_11_1.storage;

public class ItemTransaction {
    private final short windowId;
    private final short slotId;
    private final short actionId;

    public ItemTransaction(short windowId, short slotId, short actionId) {
        this.windowId = windowId;
        this.slotId = slotId;
        this.actionId = actionId;
    }

    public short getWindowId() {
        return windowId;
    }

    public short getSlotId() {
        return slotId;
    }

    public short getActionId() {
        return actionId;
    }

    @Override
    public String toString() {
        return "ItemTransaction{" +
                "windowId=" + windowId +
                ", slotId=" + slotId +
                ", actionId=" + actionId +
                '}';
    }
}