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

package com.viaversion.viaversion.rewriter.entitydata;

import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;

@FunctionalInterface
public interface EntityDataHandler {

    /**
     * Handles an entity data entry of.
     *
     * @param event entity data event
     * @param data  entity data, convenience parameter for {@link EntityDataHandlerEvent#data()}
     */
    void handle(EntityDataHandlerEvent event, EntityData data);
}
