/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v26_2to26_3.storage;

import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.connection.ProtocolStorablesBase;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ProtocolStorables26_3 extends ProtocolStorablesBase {

    private BlockPosition destroyPosition;
    private short destroyDirection;
    private int destroyTicks;
    private boolean punched;

    public @Nullable BlockPosition destroyPosition() {
        return destroyPosition;
    }

    public int destroyDirection() {
        return destroyDirection;
    }

    public void setDestroyDirection(final short destroyDirection) {
        this.destroyDirection = destroyDirection;
    }

    public void startDestroying(final BlockPosition position, final short direction) {
        this.destroyPosition = position;
        this.destroyDirection = direction;
        this.destroyTicks = 0;
        this.punched = false;
    }

    public void stopDestroying() {
        this.destroyPosition = null;
    }

    public void setPunched() {
        this.punched = true;
    }

    /**
     * Returns the destroy tick that just ended, or -1 if the client did not continue destroying in it.
     *
     * @return destroy tick that just ended, or -1
     */
    public int endDestroyTick() {
        if (destroyPosition == null) {
            return -1;
        }
        if (!punched) {
            destroyPosition = null;
            return -1;
        }

        punched = false;
        return destroyTicks++;
    }
}
