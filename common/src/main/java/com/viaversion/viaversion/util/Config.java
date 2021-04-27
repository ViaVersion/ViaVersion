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
package com.viaversion.viaversion.util;

import com.viaversion.viaversion.api.configuration.ConfigurationProvider;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public abstract class Config implements ConfigurationProvider {
    private static final ThreadLocal<Yaml> YAML = ThreadLocal.withInitial(() -> {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(false);
        options.setIndent(2);
        return new Yaml(new YamlConstructor(), new Representer(), options);
    });

    private final CommentStore commentStore = new CommentStore('.', 2);
    private final File configFile;
    private Map<String, Object> config;

    /**
     * Create a new Config instance, this will *not* load the config by default.
     * To load config see {@link #reloadConfig()}
     *
     * @param configFile The location of where the config is loaded/saved.
     */
    public Config(File configFile) {
        this.configFile = configFile;
    }

    public abstract URL getDefaultConfigURL();

    public synchronized Map<String, Object> loadConfig(File location) {
        List<String> unsupported = getUnsupportedOptions();
        URL jarConfigFile = getDefaultConfigURL();
        try {
            commentStore.storeComments(jarConfigFile.openStream());
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
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (config == null) {
            config = new HashMap<>();
        }

        Map<String, Object> defaults = config;
        try (InputStream stream = jarConfigFile.openStream()) {
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
        saveConfig(location, defaults);

        return defaults;
    }

    protected abstract void handleConfig(Map<String, Object> config);

    public synchronized void saveConfig(File location, Map<String, Object> config) {
        try {
            commentStore.writeComments(YAML.get().dump(config), location);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public abstract List<String> getUnsupportedOptions();

    @Override
    public void set(String path, Object value) {
        config.put(path, value);
    }

    @Override
    public void saveConfig() {
        this.configFile.getParentFile().mkdirs();
        saveConfig(this.configFile, this.config);
    }

    @Override
    public void reloadConfig() {
        this.configFile.getParentFile().mkdirs();
        this.config = new ConcurrentSkipListMap<>(loadConfig(this.configFile));
    }

    @Override
    public Map<String, Object> getValues() {
        return this.config;
    }

    public @Nullable <T> T get(String key, Class<T> clazz, T def) {
        Object o = this.config.get(key);
        if (o != null) {
            return (T) o;
        } else {
            return def;
        }
    }

    public boolean getBoolean(String key, boolean def) {
        Object o = this.config.get(key);
        if (o != null) {
            return (boolean) o;
        } else {
            return def;
        }
    }

    public @Nullable String getString(String key, @Nullable String def) {
        final Object o = this.config.get(key);
        if (o != null) {
            return (String) o;
        } else {
            return def;
        }
    }

    public int getInt(String key, int def) {
        Object o = this.config.get(key);
        if (o != null) {
            if (o instanceof Number) {
                return ((Number) o).intValue();
            } else {
                return def;
            }
        } else {
            return def;
        }
    }

    public double getDouble(String key, double def) {
        Object o = this.config.get(key);
        if (o != null) {
            if (o instanceof Number) {
                return ((Number) o).doubleValue();
            } else {
                return def;
            }
        } else {
            return def;
        }
    }

    public List<Integer> getIntegerList(String key) {
        Object o = this.config.get(key);
        if (o != null) {
            return (List<Integer>) o;
        } else {
            return new ArrayList<>();
        }
    }
}
