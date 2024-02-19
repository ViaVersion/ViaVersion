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
package com.viaversion.viaversion.protocols.protocol1_14to1_13_2.storage;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_14;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class EntityTracker1_14 extends EntityTrackerBase {
    private final Int2ObjectMap<Byte> insentientData = new Int2ObjectOpenHashMap<>();
    // 0x1 = sleeping, 0x2 = riptide
    private final Int2ObjectMap<Byte> sleepingAndRiptideData = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Byte> playerEntityFlags = new Int2ObjectOpenHashMap<>();
    private int latestTradeWindowId;
    private boolean forceSendCenterChunk = true;
    private int chunkCenterX;
    private int chunkCenterZ;

    public EntityTracker1_14(UserConnection user) {
        super(user, EntityTypes1_14.PLAYER);
    }

    @Override
    public void removeEntity(int entityId) {
        super.removeEntity(entityId);

        insentientData.remove(entityId);
        sleepingAndRiptideData.remove(entityId);
        playerEntityFlags.remove(entityId);
    }

    public byte getInsentientData(int entity) {
        Byte val = insentientData.get(entity);
        return val == null ? 0 : val;
    }

    public void setInsentientData(int entity, byte value) {
        insentientData.put(entity, (Byte) value);
    }

    private static byte zeroIfNull(Byte val) {
        if (val == null) return 0;
        return val;
    }

    public boolean isSleeping(int player) {
        return (zeroIfNull(sleepingAndRiptideData.get(player)) & 1) != 0;
    }

    public void setSleeping(int player, boolean value) {
        byte newValue = (byte) ((zeroIfNull(sleepingAndRiptideData.get(player)) & ~1) | (value ? 1 : 0));
        if (newValue == 0) {
            sleepingAndRiptideData.remove(player);
        } else {
            sleepingAndRiptideData.put(player, (Byte) newValue);
        }
    }

    public boolean isRiptide(int player) {
        return (zeroIfNull(sleepingAndRiptideData.get(player)) & 2) != 0;
    }

    public void setRiptide(int player, boolean value) {
        byte newValue = (byte) ((zeroIfNull(sleepingAndRiptideData.get(player)) & ~2) | (value ? 2 : 0));
        if (newValue == 0) {
            sleepingAndRiptideData.remove(player);
        } else {
            sleepingAndRiptideData.put(player, (Byte) newValue);
        }
    }

    public byte getEntityFlags(int player) {
        return zeroIfNull(playerEntityFlags.get(player));
    }

    public void setEntityFlags(int player, byte data) {
        playerEntityFlags.put(player, (Byte) data);
    }

    public int getLatestTradeWindowId() {
        return latestTradeWindowId;
    }

    public void setLatestTradeWindowId(int latestTradeWindowId) {
        this.latestTradeWindowId = latestTradeWindowId;
    }

    public boolean isForceSendCenterChunk() {
        return forceSendCenterChunk;
    }

    public void setForceSendCenterChunk(boolean forceSendCenterChunk) {
        this.forceSendCenterChunk = forceSendCenterChunk;
    }

    public int getChunkCenterX() {
        return chunkCenterX;
    }

    public void setChunkCenterX(int chunkCenterX) {
        this.chunkCenterX = chunkCenterX;
    }

    public int getChunkCenterZ() {
        return chunkCenterZ;
    }

    public void setChunkCenterZ(int chunkCenterZ) {
        this.chunkCenterZ = chunkCenterZ;
    }
}
