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
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public record Equippable(int equipmentSlot, Holder<SoundEvent> soundEvent, @Nullable String model,
                         @Nullable String cameraOverlay, @Nullable HolderSet allowedEntities, boolean dispensable,
                         boolean swappable, boolean damageOnHurt, boolean equipOnInteract,
                         boolean canBeSheared, Holder<SoundEvent> shearingSound) implements Rewritable {

    public Equippable(final int equipmentSlot, final Holder<SoundEvent> soundEvent, @Nullable final String model, @Nullable final String cameraOverlay,
                      @Nullable final HolderSet allowedEntities, final boolean dispensable, final boolean swappable, final boolean damageOnHurt) {
        this(equipmentSlot, soundEvent, model, cameraOverlay, allowedEntities, dispensable, swappable, damageOnHurt, true);
    }

    public Equippable(final int equipmentSlot, final Holder<SoundEvent> soundEvent, @Nullable final String model, @Nullable final String cameraOverlay,
                      @Nullable final HolderSet allowedEntities, final boolean dispensable, final boolean swappable, final boolean damageOnHurt, final boolean equipOnInteract) {
        this(equipmentSlot, soundEvent, model, cameraOverlay, allowedEntities, dispensable, swappable, damageOnHurt, equipOnInteract, false, Holder.of(0));
    }

    public static final Type<Equippable> TYPE1_21_2 = new Type<>(Equippable.class) {
        @Override
        public Equippable read(final ByteBuf buffer) {
            final int equipmentSlot = Types.VAR_INT.readPrimitive(buffer);
            final Holder<SoundEvent> soundEvent = Types.SOUND_EVENT.read(buffer);
            final String model = Types.OPTIONAL_STRING.read(buffer);
            final String cameraOverlay = Types.OPTIONAL_STRING.read(buffer);
            final HolderSet allowedEntities = Types.OPTIONAL_HOLDER_SET.read(buffer);
            final boolean dispensable = buffer.readBoolean();
            final boolean swappable = buffer.readBoolean();
            final boolean damageOnHurt = buffer.readBoolean();
            return new Equippable(equipmentSlot, soundEvent, model, cameraOverlay, allowedEntities, dispensable, swappable, damageOnHurt);
        }

        @Override
        public void write(final ByteBuf buffer, final Equippable value) {
            Types.VAR_INT.writePrimitive(buffer, value.equipmentSlot());
            Types.SOUND_EVENT.write(buffer, value.soundEvent());
            Types.OPTIONAL_STRING.write(buffer, value.model());
            Types.OPTIONAL_STRING.write(buffer, value.cameraOverlay());
            Types.OPTIONAL_HOLDER_SET.write(buffer, value.allowedEntities());
            buffer.writeBoolean(value.dispensable());
            buffer.writeBoolean(value.swappable());
            buffer.writeBoolean(value.damageOnHurt());
        }
    };
    public static final Type<Equippable> TYPE1_21_5 = new Type<>(Equippable.class) {
        @Override
        public Equippable read(final ByteBuf buffer) {
            final int equipmentSlot = Types.VAR_INT.readPrimitive(buffer);
            final Holder<SoundEvent> soundEvent = Types.SOUND_EVENT.read(buffer);
            final String model = Types.OPTIONAL_STRING.read(buffer);
            final String cameraOverlay = Types.OPTIONAL_STRING.read(buffer);
            final HolderSet allowedEntities = Types.OPTIONAL_HOLDER_SET.read(buffer);
            final boolean dispensable = buffer.readBoolean();
            final boolean swappable = buffer.readBoolean();
            final boolean damageOnHurt = buffer.readBoolean();
            final boolean equipOnInteract = buffer.readBoolean();
            return new Equippable(equipmentSlot, soundEvent, model, cameraOverlay, allowedEntities, dispensable, swappable, damageOnHurt, equipOnInteract);
        }

        @Override
        public void write(final ByteBuf buffer, final Equippable value) {
            Types.VAR_INT.writePrimitive(buffer, value.equipmentSlot());
            Types.SOUND_EVENT.write(buffer, value.soundEvent());
            Types.OPTIONAL_STRING.write(buffer, value.model());
            Types.OPTIONAL_STRING.write(buffer, value.cameraOverlay());
            Types.OPTIONAL_HOLDER_SET.write(buffer, value.allowedEntities());
            buffer.writeBoolean(value.dispensable());
            buffer.writeBoolean(value.swappable());
            buffer.writeBoolean(value.damageOnHurt());
            buffer.writeBoolean(value.equipOnInteract());
        }
    };
    public static final Type<Equippable> TYPE1_21_6 = new Type<>(Equippable.class) {
        @Override
        public Equippable read(final ByteBuf buffer) {
            final int equipmentSlot = Types.VAR_INT.readPrimitive(buffer);
            final Holder<SoundEvent> soundEvent = Types.SOUND_EVENT.read(buffer);
            final String model = Types.OPTIONAL_STRING.read(buffer);
            final String cameraOverlay = Types.OPTIONAL_STRING.read(buffer);
            final HolderSet allowedEntities = Types.OPTIONAL_HOLDER_SET.read(buffer);
            final boolean dispensable = buffer.readBoolean();
            final boolean swappable = buffer.readBoolean();
            final boolean damageOnHurt = buffer.readBoolean();
            final boolean equipOnInteract = buffer.readBoolean();
            final boolean canBeSheared = buffer.readBoolean();
            final Holder<SoundEvent> shearingSound = Types.SOUND_EVENT.read(buffer);
            return new Equippable(equipmentSlot, soundEvent, model, cameraOverlay, allowedEntities, dispensable, swappable, damageOnHurt, equipOnInteract, canBeSheared, shearingSound);
        }

        @Override
        public void write(final ByteBuf buffer, final Equippable value) {
            Types.VAR_INT.writePrimitive(buffer, value.equipmentSlot());
            Types.SOUND_EVENT.write(buffer, value.soundEvent());
            Types.OPTIONAL_STRING.write(buffer, value.model());
            Types.OPTIONAL_STRING.write(buffer, value.cameraOverlay());
            Types.OPTIONAL_HOLDER_SET.write(buffer, value.allowedEntities());
            buffer.writeBoolean(value.dispensable());
            buffer.writeBoolean(value.swappable());
            buffer.writeBoolean(value.damageOnHurt());
            buffer.writeBoolean(value.equipOnInteract());
            buffer.writeBoolean(value.canBeSheared());
            Types.SOUND_EVENT.write(buffer, value.shearingSound());
        }
    };

    @Override
    public Equippable rewrite(final UserConnection connection, final Protocol<?, ?, ?, ?> protocol, final boolean clientbound) {
        final Holder<SoundEvent> soundEvent = this.soundEvent.updateId(Rewritable.soundRewriteFunction(protocol, clientbound));
        final Holder<SoundEvent> shearingSound = this.shearingSound.updateId(Rewritable.soundRewriteFunction(protocol, clientbound));
        return soundEvent == this.soundEvent && shearingSound == this.shearingSound ? this : new Equippable(equipmentSlot, soundEvent, model, cameraOverlay, allowedEntities, dispensable, swappable, damageOnHurt, equipOnInteract, canBeSheared, shearingSound);
    }
}
