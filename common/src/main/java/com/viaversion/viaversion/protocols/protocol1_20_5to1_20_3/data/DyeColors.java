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
package com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.data;

public final class DyeColors {

    public static String colorById(final int id) {
        switch (id) {
            case 1:
                return "orange";
            case 2:
                return "magenta";
            case 3:
                return "light_blue";
            case 4:
                return "yellow";
            case 5:
                return "lime";
            case 6:
                return "pink";
            case 7:
                return "gray";
            case 8:
                return "light_gray";
            case 9:
                return "cyan";
            case 10:
                return "purple";
            case 11:
                return "blue";
            case 12:
                return "brown";
            case 13:
                return "green";
            case 14:
                return "red";
            case 15:
                return "black";
            default:
                return "white";
        }
    }
}
