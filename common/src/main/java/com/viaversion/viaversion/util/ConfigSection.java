/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
package com.viaversion.viaversion.util;

import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ConfigSection {

    protected final Config root;
    protected final String path;
    protected Map<String, Object> values;

    ConfigSection(final Config root, final String path) {
        this(root, path, new HashMap<>());
    }

    ConfigSection(final Config root, final String path, final Map<String, Object> values) {
        this.root = root;
        this.path = path;
        this.values = values;
    }

    public void set(final String path, final Object value) {
        this.values.put(path, value);
    }

    public Map<String, Object> getValues() {
        return this.values;
    }

    public @Nullable <T> T get(final String key) {
        return get(key, null);
    }

    public boolean contains(final String key) {
        return get(key) != null;
    }

    public @Nullable <T> T get(final String key, @Nullable final T def) {
        final String[] parts = key.split("\\.");
        ConfigSection section = this;
        for (int i = 0; i < parts.length - 1; i++) {
            section = section.getSection(parts[i]);
            if (section == null) {
                return def;
            }
        }

        final String valueKey = parts[parts.length - 1];
        final Object o = section.values.get(valueKey);
        //noinspection unchecked
        return o != null ? (T) o : def;
    }

    public boolean getBoolean(final String key, final boolean def) {
        final Object o = this.values.get(key);
        return o instanceof Boolean ? (boolean) o : def;
    }

    public @Nullable String getString(final String key, @Nullable final String def) {
        return this.values.get(key) instanceof final String s ? s : def;
    }

    public int getInt(final String key, final int def) {
        return this.values.get(key) instanceof final Number num ? num.intValue() : def;
    }

    public double getDouble(final String key, final double def) {
        return this.values.get(key) instanceof final Number num ? num.doubleValue() : def;
    }

    public List<Integer> getIntegerList(final String key) {
        final Object o = this.values.get(key);
        return o != null ? (List<Integer>) o : new ArrayList<>();
    }

    public List<String> getStringList(final String key) {
        final Object o = this.values.get(key);
        return o != null ? (List<String>) o : new ArrayList<>();
    }

    public <T> List<T> getListSafe(final String key, final Class<T> type, final String invalidValueMessage) {
        final Object o = this.values.get(key);
        if (o instanceof final List<?> list) {
            final List<T> filteredValues = new ArrayList<>(list.size());
            for (final Object o1 : list) {
                if (type.isInstance(o1)) {
                    filteredValues.add(type.cast(o1));
                } else if (invalidValueMessage != null) {
                    this.logger().warning(String.format(invalidValueMessage, o1));
                }
            }
            return filteredValues;
        }
        return new ArrayList<>();
    }

    public @Nullable JsonElement getSerializedComponent(final String key) {
        final Object o = this.values.get(key);
        if (o != null && !((String) o).isEmpty()) {
            return ComponentUtil.legacyToJson((String) o);
        } else {
            return null;
        }
    }

    public @Nullable ConfigSection getSection(final String key) {
        final Object o = get(key);
        return o instanceof Map ? new ConfigSection(this.root(), fullKeyInPath(key), (Map<String, Object>) o) : null;
    }

    private String fullKeyInPath(final String key) {
        return path.isEmpty() ? key : path + '.' + key;
    }

    public Config root() {
        return this.root;
    }

    protected Logger logger() {
        return this.root().logger();
    }
}
