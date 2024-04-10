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
package com.viaversion.viaversion.util;

import com.google.gson.JsonElement;
import com.viaversion.viaversion.compatibility.YamlCompat;
import com.viaversion.viaversion.compatibility.unsafe.Yaml1Compat;
import com.viaversion.viaversion.compatibility.unsafe.Yaml2Compat;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

@SuppressWarnings("VulnerableCodeUsages")
public abstract class Config {
    protected static final Logger LOGGER = Logger.getLogger("ViaVersion Config");
    private static final YamlCompat YAMP_COMPAT = YamlCompat.isVersion1() ? new Yaml1Compat() : new Yaml2Compat();
    private static final ThreadLocal<Yaml> YAML = ThreadLocal.withInitial(() -> {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(false);
        options.setIndent(2);
        return new Yaml(YAMP_COMPAT.createSafeConstructor(), YAMP_COMPAT.createRepresenter(options), options);
    });

    private final CommentStore commentStore = new CommentStore('.', 2);
    private final File configFile;
    private Map<String, Object> config;

    /**
     * Create a new Config instance, this will *not* load the config by default.
     * To load config see {@link #reload()}
     *
     * @param configFile The location of where the config is loaded/saved.
     */
    protected Config(File configFile) {
        this.configFile = configFile;
    }

    public URL getDefaultConfigURL() {
        return getClass().getClassLoader().getResource("assets/viaversion/config.yml");
    }

    public InputStream getDefaultConfigInputStream() {
        return getClass().getClassLoader().getResourceAsStream("assets/viaversion/config.yml");
    }

    public Map<String, Object> loadConfig(File location) {
        final URL defaultConfigUrl = getDefaultConfigURL();
        if (defaultConfigUrl != null) {
            return loadConfig(location, defaultConfigUrl);
        }
        return loadConfig(location, this::getDefaultConfigInputStream);
    }

    public synchronized Map<String, Object> loadConfig(File location, URL jarConfigFile) {
        return loadConfig(location, jarConfigFile::openStream);
    }

    private synchronized Map<String, Object> loadConfig(File location, InputStreamSupplier configSupplier) {
        List<String> unsupported = getUnsupportedOptions();
        try {
            commentStore.storeComments(configSupplier.get());
            for (String option : unsupported) {
                List<String> comments = commentStore.header(option);
                if (comments != null) {
                    comments.clear();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, Object> config = null;
        if (location.exists()) {
            try (FileInputStream input = new FileInputStream(location)) {
                config = (Map<String, Object>) YAML.get().load(input);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (config == null) {
            config = new HashMap<>();
        }

        Map<String, Object> defaults = config;
        try (InputStream stream = configSupplier.get()) {
            defaults = (Map<String, Object>) YAML.get().load(stream);
            for (String option : unsupported) {
                defaults.remove(option);
            }
            // Merge with defaultLoader
            for (Map.Entry<String, Object> entry : config.entrySet()) {
                // Set option in new conf if exists
                if (defaults.containsKey(entry.getKey()) && !unsupported.contains(entry.getKey())) {
                    defaults.put(entry.getKey(), entry.getValue());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Call Handler
        handleConfig(defaults);
        // Save
        save(location, defaults);

        return defaults;
    }

    protected abstract void handleConfig(Map<String, Object> config);

    public synchronized void save(File location, Map<String, Object> config) {
        try {
            commentStore.writeComments(YAML.get().dump(config), location);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public abstract List<String> getUnsupportedOptions();

    public void set(String path, Object value) {
        config.put(path, value);
    }

    public void save() {
        if (this.configFile.getParentFile() != null) {
            this.configFile.getParentFile().mkdirs();
        }
        save(this.configFile, this.config);
    }

    public void save(final File file) {
        save(file, this.config);
    }

    public void reload() {
        if (this.configFile.getParentFile() != null) {
            this.configFile.getParentFile().mkdirs();
        }
        this.config = new ConcurrentSkipListMap<>(loadConfig(this.configFile));
    }

    public Map<String, Object> getValues() {
        return this.config;
    }

    public @Nullable <T> T get(String key, T def) {
        Object o = this.config.get(key);
        if (o != null) {
            return (T) o;
        } else {
            return def;
        }
    }

    public boolean getBoolean(String key, boolean def) {
        Object o = this.config.get(key);
        if (o instanceof Boolean) {
            return (boolean) o;
        } else {
            return def;
        }
    }

    public @Nullable String getString(String key, @Nullable String def) {
        final Object o = this.config.get(key);
        if (o instanceof String) {
            return (String) o;
        } else {
            return def;
        }
    }

    public int getInt(String key, int def) {
        Object o = this.config.get(key);
        if (o instanceof Number) {
            return ((Number) o).intValue();
        } else {
            return def;
        }
    }

    public double getDouble(String key, double def) {
        Object o = this.config.get(key);
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        } else {
            return def;
        }
    }

    public List<Integer> getIntegerList(String key) {
        Object o = this.config.get(key);
        return o != null ? (List<Integer>) o : new ArrayList<>();
    }

    public List<String> getStringList(String key) {
        Object o = this.config.get(key);
        return o != null ? (List<String>) o : new ArrayList<>();
    }

    public <T> List<T> getListSafe(String key, Class<T> type, String invalidValueMessage) {
        Object o = this.config.get(key);
        if (o instanceof List) {
            List<?> list = (List<?>) o;
            List<T> filteredValues = new ArrayList<>();
            for (Object o1 : list) {
                if (type.isInstance(o1)) {
                    filteredValues.add(type.cast(o1));
                } else if (invalidValueMessage != null) {
                    LOGGER.warning(String.format(invalidValueMessage, o1));
                }
            }
            return filteredValues;
        }
        return new ArrayList<>();
    }

    public @Nullable JsonElement getSerializedComponent(String key) {
        final Object o = this.config.get(key);
        if (o != null && !((String) o).isEmpty()) {
            return ComponentUtil.legacyToJson((String) o);
        } else {
            return null;
        }
    }
}