package us.myles.ViaVersion.util;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;
import us.myles.ViaVersion.api.configuration.ConfigurationProvider;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public abstract class Config implements ConfigurationProvider {
    private static final ThreadLocal<Yaml> YAML = new ThreadLocal<Yaml>() {
        @Override
        protected Yaml initialValue() {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(false);
            options.setIndent(2);
            return new Yaml(new YamlConstructor(), new Representer(), options);
        }
    };

    private CommentStore commentStore = new CommentStore('.', 2);
    private final File configFile;
    private ConcurrentSkipListMap<String, Object> config;

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
            for (Object key : config.keySet()) {
                // Set option in new conf if exists
                if (defaults.containsKey(key) && !unsupported.contains(key.toString())) {
                    defaults.put((String) key, config.get(key));
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

    public <T> T get(String key, Class<T> clazz, T def) {
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

    public String getString(String key, String def) {
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
