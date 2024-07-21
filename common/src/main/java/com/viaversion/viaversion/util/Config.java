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
    protected final Logger logger;
    private Map<String, Object> config;

    /**
     * Create a new Config instance, this will *not* load the config by default.
     * To load config see {@link #reload()}
     *
     * @param configFile The location of where the config is loaded/saved.
     * @param logger     The logger to use.
     */
    protected Config(File configFile, Logger logger) {
        this.configFile = configFile;
        this.logger = logger;
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
        try (InputStream inputStream = configSupplier.get()) {
            commentStore.storeComments(inputStream);
            for (String option : unsupported) {
                List<String> comments = commentStore.header(option);
                if (comments != null) {
                    comments.clear();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load default config comments", e);
        }

        // Load actual file data
        Map<String, Object> config = null;
        if (location.exists()) {
            try (FileInputStream input = new FileInputStream(location)) {
                //noinspection unchecked
                config = (Map<String, Object>) YAML.get().load(input);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load config", e);
            } catch (Exception e) {
                logger.severe("Failed to load config, make sure your input is valid");
                throw new RuntimeException("Failed to load config (malformed input?)", e);
            }
        }
        if (config == null) {
            config = new HashMap<>();
        }

        // Load default config and merge with current to make sure we always have an up-to-date/correct config
        Map<String, Object> mergedConfig;
        try (InputStream stream = configSupplier.get()) {
            //noinspection unchecked
            mergedConfig = (Map<String, Object>) YAML.get().load(stream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load default config", e);
        }
        for (String option : unsupported) {
            mergedConfig.remove(option);
        }
        for (Map.Entry<String, Object> entry : config.entrySet()) {
            // Set option in new conf if it exists
            mergedConfig.computeIfPresent(entry.getKey(), (key, value) -> entry.getValue());
        }

        handleConfig(mergedConfig);
        save(location, mergedConfig);

        return mergedConfig;
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
        if (o instanceof List<?> list) {
            List<T> filteredValues = new ArrayList<>();
            for (Object o1 : list) {
                if (type.isInstance(o1)) {
                    filteredValues.add(type.cast(o1));
                } else if (invalidValueMessage != null) {
                    logger.warning(String.format(invalidValueMessage, o1));
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