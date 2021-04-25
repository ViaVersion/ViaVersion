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
package us.myles.ViaVersion.dump;

public class RuntimeInfo {

    private final long maxMemory;
    private final long freeMemory;
    private final long totalMemory;

    public RuntimeInfo() {
        this(Runtime.getRuntime());
    }

    public RuntimeInfo(Runtime runtime) {
        this(runtime.maxMemory(), runtime.freeMemory(), runtime.totalMemory());
    }

    public RuntimeInfo(long maxMemory, long freeMemory, long totalMemory) {
        this.maxMemory = maxMemory;
        this.freeMemory = freeMemory;
        this.totalMemory = totalMemory;
    }

    public long getMaxMemory() {
        return maxMemory;
    }

    public long getFreeMemory() {
        return freeMemory;
    }

    public long getTotalMemory() {
        return totalMemory;
    }

}