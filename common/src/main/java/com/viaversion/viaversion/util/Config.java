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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

public abstract class Config extends ConfigSection {
    private static final ThreadLocal<Yaml> YAML = ThreadLocal.withInitial(() -> {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(false);
        options.setIndent(2);
        return new Yaml(new CustomSafeConstructor(), new Representer(options), options);
    });

    private static final class CustomSafeConstructor extends SafeConstructor {

        public CustomSafeConstructor() {
            super(new LoaderOptions());
            yamlClassConstructors.put(NodeId.mapping, new ConstructYamlMap());
            yamlConstructors.put(Tag.OMAP, new ConstructYamlOmap());
        }
    }

    private final CommentStore commentStore = new CommentStore('.', 2);
    private final File configFile;
    protected final Logger logger;
    private ConfigSection originalRoot;

    /**
     * Create a new Config instance, this will *not* load the config by default.
     * To load config see {@link #reload()}
     *
     * @param configFile The location of where the config is loaded/saved.
     * @param logger     The logger to use.
     */
    protected Config(File configFile, Logger logger) {
        super(null, "");
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
        originalRoot = null;

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
        Map<String, Object> existingConfig = null;
        if (location.exists()) {
            try (FileInputStream input = new FileInputStream(location)) {
                existingConfig = YAML.get().load(input);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load config", e);
            } catch (Exception e) {
                logger.severe("Failed to load config, make sure your input is valid");
                throw new RuntimeException("Failed to load config (malformed input?)", e);
            }
        }

        // Load default config and merge with current to make sure we always have an up-to-date/correct config
        Map<String, Object> mergedConfig;
        try (InputStream stream = configSupplier.get()) {
            mergedConfig = YAML.get().load(stream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load default config", e);
        }
        for (String option : unsupported) {
            mergedConfig.remove(option);
        }

        if (existingConfig != null) {
            merge(null, existingConfig, mergedConfig);
        }

        handleConfig(mergedConfig);

        if (!mergedConfig.equals(existingConfig)) {
            originalRoot = existingConfig != null ? new ConfigSection(this, "", existingConfig) : null;

            // Also updates comments once values need to be saved
            save(location, mergedConfig);
        }

        return mergedConfig;
    }

    private void merge(@Nullable String currentSectionKey, Map<String, Object> loadedConfig, Map<String, Object> mergedConfig) {
        for (Map.Entry<String, Object> entry : loadedConfig.entrySet()) {
            // Set option in new config if it exists
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> mapValue && mergedConfig.get(key) instanceof Map<?, ?> mergedMapValue) {
                //noinspection unchecked
                merge(key, (Map<String, Object>) mapValue, (Map<String, Object>) mergedMapValue);
            } else if (currentSectionKey != null && getSectionsWithModifiableKeys().contains(currentSectionKey)) {
                mergedConfig.put(key, value);
            } else {
                mergedConfig.computeIfPresent(key, (k, v) -> value);
            }
        }
    }

    protected abstract void handleConfig(Map<String, Object> config);

    public synchronized void save(File location, Map<String, Object> config) {
        try {
            commentStore.writeComments(YAML.get().dump(config), location);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a list of options to remove from the config.
     * This can be unused or unsupported options for this particular platform.
     *
     * @return list of options to remove from the config
     */
    public abstract List<String> getUnsupportedOptions();

    /**
     * Returns a set of section keys that will have custom entry keys.
     * These values will be kept when merging into a new config.
     *
     * @return set of section keys that will have custom entry keys
     */
    public Set<String> getSectionsWithModifiableKeys() {
        return Set.of();
    }

    public void save() {
        if (this.configFile.getParentFile() != null) {
            this.configFile.getParentFile().mkdirs();
        }
        save(this.configFile, this.values);
    }

    public void save(final File file) {
        save(file, this.values);
    }

    public void reload() {
        if (this.configFile.getParentFile() != null) {
            this.configFile.getParentFile().mkdirs();
        }
        this.values = new ConcurrentSkipListMap<>(loadConfig(this.configFile));
    }

    @Override
    public Config root() {
        return this;
    }

    @Override
    public Logger logger() {
        return logger;
    }

    /**
     * Returns the pre-modification root section of the config if present.
     *
     * @return pre-modification root section, or null if no config existed or it did not require changes on load
     */
    public @Nullable ConfigSection originalRootSection() {
        return originalRoot;
    }
}
