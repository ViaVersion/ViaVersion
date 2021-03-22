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
package us.myles.ViaVersion.protocols.protocol1_9to1_8.storage;

import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;

public class PlaceBlockTracker extends StoredObject {
    private long lastPlaceTimestamp = 0;
    private Position lastPlacedPosition = null;

    public PlaceBlockTracker(UserConnection user) {
        super(user);
    }

    /**
     * Check if a certain amount of time has passed
     *
     * @param ms The amount of time in MS
     * @return True if it has passed
     */
    public boolean isExpired(int ms) {
        return System.currentTimeMillis() > (lastPlaceTimestamp + ms);
    }

    /**
     * Set the last place time to the current time
     */
    public void updateTime() {
        lastPlaceTimestamp = System.currentTimeMillis();
    }

    public long getLastPlaceTimestamp() {
        return lastPlaceTimestamp;
    }

    public Position getLastPlacedPosition() {
        return lastPlacedPosition;
    }

    public void setLastPlacedPosition(Position lastPlacedPosition) {
        this.lastPlacedPosition = lastPlacedPosition;
    }
}
