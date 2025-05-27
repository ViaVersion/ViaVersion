/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.type.types.EnumType;
import com.viaversion.viaversion.api.type.types.EnumType.Fallback;
import com.viaversion.viaversion.api.type.types.FakeEnumType;
import com.viaversion.viaversion.api.type.types.RegistryValueType;
import java.util.List;

import static com.viaversion.viaversion.api.type.types.FakeEnumType.Entry.of;

/**
 * Enum types that aren't associated with any specific item data in this package.
 */
public final class EnumTypes {

    public static final EnumType DYE_COLOR = new EnumType("white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black");
    public static final EnumType RARITY = new EnumType("common", "uncommon", "rare", "epic");
    public static final EnumType FOX_VARIANT = new EnumType("red", "snow");
    public static final EnumType SALMON_VARIANT = new EnumType(Fallback.CLAMP, "small", "medium", "large");
    public static final EnumType PARROT_VARIANT = new EnumType(Fallback.CLAMP, "red_blue", "blue", "green", "yellow_blue", "gray");
    public static final EnumType MUSHROOM_COW_VARIANT = new EnumType(Fallback.CLAMP, "red", "brown");
    public static final EnumType HORSE_VARIANT = new EnumType(Fallback.WRAP, "white", "creamy", "chestnut", "brown", "black", "gray", "dark_brown");
    public static final EnumType LLAMA_VARIANT = new EnumType(Fallback.CLAMP, "creamy", "white", "brown", "gray");
    public static final EnumType AXOLOTL_VARIANT = new EnumType("lucy", "wild", "gold", "cyan", "blue");
    // Enums with non-ordinal ids
    public static final FakeEnumType RABBIT_VARIANT = new FakeEnumType(List.of("brown", "white", "black", "white_splotched", "gold", "salt"), of(99, "evil"));
    // Pretty much enums, but with a resource location
    public static final RegistryValueType VILLAGER_TYPE = new RegistryValueType("desert", "jungle", "plains", "savanna", "snow", "swamp", "taiga");
}
