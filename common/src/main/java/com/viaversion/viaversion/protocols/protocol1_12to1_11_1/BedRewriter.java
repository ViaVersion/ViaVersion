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
package com.viaversion.viaversion.protocols.protocol1_12to1_11_1;

import com.viaversion.viaversion.api.minecraft.item.Item;

public class BedRewriter {

    public static void toClientItem(Item item) {
        if (item == null) return;
        if (item.identifier() == 355 && item.data() == 0) {
            item.setData((short) 14);
        }
    }

    public static void toServerItem(Item item) {
        if (item == null) return;
        if (item.identifier() == 355 && item.data() == 14) {
            item.setData((short) 0);
        }
    }
}
