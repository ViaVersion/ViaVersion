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

import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.viaversion.viaversion.api.minecraft.nbt.BinaryTagIO.readString;

public class NBTTagTest {

    @Test
    void test() throws IOException {
        readString("{id:5}");
        readString("{id:5b}");
        readString("{id:test,test:1,}");
        readString("{id:[3.2,64.5,129.5]}");
        readString("{id:[I;1,2, 3, 4,5]}"); // >=1.11
        readString("{id:1b,b:true}");
        readString("{id:[L;1l,2L,3L]}"); // >=1.11
        readString("{id:[I;1i,2I,3I]}");
        readString("{id:'minecraft:stone'}"); // >=1.13
        readString("{id:1,id:2}");
        readString("{id:-20b,test:3.19f}");
        readString("{id:[I;1,2,3,]}");
        readString("{id:[1,2,3,]}");

        Assertions.assertEquals("0da", readString("{id:0da}").get("id").getValue());
        Assertions.assertEquals("NaNd", readString("{id:NaNd}").get("id").getValue());
        Assertions.assertEquals("Infinityd", readString("{id:Infinityd}").get("id").getValue());
        Assertions.assertEquals("2147483649", readString("{id:9000b,thisisastring:2147483649}").get("thisisastring").getValue());
        Assertions.assertEquals((byte) 1, readString("{thisisabyte:true}").get("thisisabyte").getValue());
        Assertions.assertEquals((byte) 0, readString("{thisisabyte:false}").get("thisisabyte").getValue());

        //TODO fix legacy < 1.12
        // readString("{id:minecraft:stone}");
        // readString("{id:[1,2, 3, 4,5]}");
    }
}
