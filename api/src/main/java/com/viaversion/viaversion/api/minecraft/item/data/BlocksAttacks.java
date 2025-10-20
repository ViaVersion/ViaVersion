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

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.RegistryKey;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.api.type.types.misc.HolderSetType;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public record BlocksAttacks(
    float blockDelaySeconds,
    float disableCooldownScale,
    DamageReduction[] damageReductions,
    ItemDamageFunction itemDamage,
    @Nullable String bypassedByTag,
    @Nullable Holder<SoundEvent> blockSound,
    @Nullable Holder<SoundEvent> disableSound
) implements Rewritable {

    public static final Type<BlocksAttacks> TYPE = new Type<>(BlocksAttacks.class) {

        @Override
        public BlocksAttacks read(final ByteBuf buffer) {
            final float blockDelaySeconds = buffer.readFloat();
            final float disableCooldownScale = buffer.readFloat();
            final DamageReduction[] damageReductions = DamageReduction.ARRAY_TYPE.read(buffer);
            final ItemDamageFunction itemDamage = ItemDamageFunction.TYPE.read(buffer);
            final String bypassedByTag = Types.OPTIONAL_STRING.read(buffer);
            final Holder<SoundEvent> blockSound = Types.OPTIONAL_SOUND_EVENT.read(buffer);
            final Holder<SoundEvent> disableSound = Types.OPTIONAL_SOUND_EVENT.read(buffer);
            return new BlocksAttacks(blockDelaySeconds, disableCooldownScale, damageReductions, itemDamage, bypassedByTag, blockSound, disableSound);
        }

        @Override
        public void write(final ByteBuf buffer, final BlocksAttacks value) {
            buffer.writeFloat(value.blockDelaySeconds());
            buffer.writeFloat(value.disableCooldownScale());
            DamageReduction.ARRAY_TYPE.write(buffer, value.damageReductions());
            ItemDamageFunction.TYPE.write(buffer, value.itemDamage());
            Types.OPTIONAL_STRING.write(buffer, value.bypassedByTag());
            Types.OPTIONAL_SOUND_EVENT.write(buffer, value.blockSound());
            Types.OPTIONAL_SOUND_EVENT.write(buffer, value.disableSound());
        }

        @Override
        public void write(final Ops ops, final BlocksAttacks value) {
            final DamageReduction[] defaultDamageReductions = {new DamageReduction(90, null, 0, 1)};
            final ItemDamageFunction defaultItemDamage = new ItemDamageFunction(1, 0, 1);
            ops.writeMap(map -> map
                .writeOptional("block_delay_seconds", Types.FLOAT, value.blockDelaySeconds(), 0F)
                .writeOptional("disable_cooldown_scale", Types.FLOAT, value.disableCooldownScale(), 1F)
                .writeOptional("damage_reductions", DamageReduction.ARRAY_TYPE, value.damageReductions(), defaultDamageReductions)
                .writeOptional("item_damage", ItemDamageFunction.TYPE, value.itemDamage(), defaultItemDamage)
                .writeOptional("bypassed_by", Types.TAG_KEY, value.bypassedByTag() != null ? Key.of(value.bypassedByTag()) : null)
                .writeOptional("block_sound", Types.SOUND_EVENT, value.blockSound())
                .writeOptional("disabled_sound", Types.SOUND_EVENT, value.disableSound()));
        }
    };

    @Override
    public BlocksAttacks rewrite(final UserConnection connection, final Protocol<?, ?, ?, ?> protocol, final boolean clientbound) {
        final Holder<SoundEvent> blockSound = SoundEvent.rewriteHolder(this.blockSound, Rewritable.soundRewriteFunction(protocol, clientbound));
        final Holder<SoundEvent> disableSound = SoundEvent.rewriteHolder(this.disableSound, Rewritable.soundRewriteFunction(protocol, clientbound));
        return new BlocksAttacks(this.blockDelaySeconds, this.disableCooldownScale, this.damageReductions, this.itemDamage, this.bypassedByTag, blockSound, disableSound);
    }

    public record DamageReduction(float horizontalBlockingAngle, @Nullable HolderSet type, float base, float factor) {

        public static final Type<DamageReduction> TYPE = new Type<>(DamageReduction.class) {

            @Override
            public DamageReduction read(final ByteBuf buffer) {
                final float horizontalBlockingAngle = buffer.readFloat();
                final HolderSet type = Types.OPTIONAL_HOLDER_SET.read(buffer);
                final float base = buffer.readFloat();
                final float factor = buffer.readFloat();
                return new DamageReduction(horizontalBlockingAngle, type, base, factor);
            }

            @Override
            public void write(final ByteBuf buffer, final DamageReduction value) {
                buffer.writeFloat(value.horizontalBlockingAngle());
                Types.OPTIONAL_HOLDER_SET.write(buffer, value.type());
                buffer.writeFloat(value.base());
                buffer.writeFloat(value.factor());
            }

            @Override
            public void write(final Ops ops, final DamageReduction value) {
                ops.writeMap(map -> map
                    .writeOptional("horizontal_blocking_angle", Types.FLOAT, value.horizontalBlockingAngle(), 90F)
                    .writeOptional("type", new HolderSetType(RegistryKey.of("damage_type")), value.type())
                    .write("base", Types.FLOAT, value.base())
                    .write("factor", Types.FLOAT, value.factor()));
            }
        };
        public static final ArrayType<DamageReduction> ARRAY_TYPE = new ArrayType<>(TYPE);
    }

    public record ItemDamageFunction(float threshold, float base, float factor) {

        public static final Type<ItemDamageFunction> TYPE = new Type<>(ItemDamageFunction.class) {

            @Override
            public ItemDamageFunction read(final ByteBuf buffer) {
                final float threshold = buffer.readFloat();
                final float base = buffer.readFloat();
                final float factor = buffer.readFloat();
                return new ItemDamageFunction(threshold, base, factor);
            }

            @Override
            public void write(final ByteBuf buffer, final ItemDamageFunction value) {
                buffer.writeFloat(value.threshold());
                buffer.writeFloat(value.base());
                buffer.writeFloat(value.factor());
            }

            @Override
            public void write(final Ops ops, final ItemDamageFunction value) {
                ops.writeMap(map -> map
                    .write("threshold", Types.FLOAT, value.threshold())
                    .write("base", Types.FLOAT, value.base())
                    .write("factor", Types.FLOAT, value.factor()));
            }
        };
    }
}
