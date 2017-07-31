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
    private static ThreadLocal<Yaml> yaml = new ThreadLocal<Yaml>() {
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

    public Config(File configFile) {
        this.configFile = configFile;
        reloadConfig();
    }

    public synchronized Map<String, Object> loadConfig(File location) {
        List<String> unsupported = getUnsupportedOptions();
        URL jarConfigFile = Config.class.getClassLoader().getResource("config.yml");
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
                config = (Map<String, Object>) yaml.get().load(input);
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
            defaults = (Map<String, Object>) yaml.get().load(stream);
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
            commentStore.writeComments(yaml.get().dump(config), location);
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
        if (this.config.containsKey(key)) {
            return (T) this.config.get(key);
        } else {
            return def;
        }
    }

    public boolean getBoolean(String key, boolean def) {
        if (this.config.containsKey(key)) {
            return (boolean) this.config.get(key);
        } else {
            return def;
        }
    }

    public String getString(String key, String def) {
        if (this.config.containsKey(key)) {
            return (String) this.config.get(key);
        } else {
            return def;
        }
    }

    public int getInt(String key, int def) {
        if (this.config.containsKey(key)) {
            if (this.config.get(key) instanceof Number) {
                return ((Number) this.config.get(key)).intValue();
            } else {
                return def;
            }
        } else {
            return def;
        }
    }

    public double getDouble(String key, double def) {
        if (this.config.containsKey(key)) {
            if (this.config.get(key) instanceof Number) {
                return ((Number) this.config.get(key)).doubleValue();
            } else {
                return def;
            }
        } else {
            return def;
        }
    }

    public List<Integer> getIntegerList(String key) {
        if (this.config.containsKey(key)) {
            return (List<Integer>) this.config.get(key);
        } else {
            return new ArrayList<>();
        }
    }
}
