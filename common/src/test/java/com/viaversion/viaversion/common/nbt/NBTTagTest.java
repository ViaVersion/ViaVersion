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
package com.viaversion.viaversion.common.nbt;

import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.api.minecraft.nbt.BinaryTagIO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class NBTTagTest {

    @Test
    void test() throws IOException {
        BinaryTagIO.readString("{id:test,test:1}");
        BinaryTagIO.readString("{id:test,test:1,}");

        BinaryTagIO.readString("{id:[1,2,3,]}");

        BinaryTagIO.readString("{id:[I;1,2,3]}");
        BinaryTagIO.readString("{id:[I;1,2,3,]}");

        Assertions.assertTrue(BinaryTagIO.readString("{id:9000b,num:2147483649}").get("num") instanceof StringTag);

        //TODO fix legacy
        // BinaryTagIO.readString("{id:minecraft:stone}");
    }
}
