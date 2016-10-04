package us.myles.ViaVersion.api.configuration;

import java.util.Map;

public interface ConfigurationProvider {

    /**
     * Sets the specified path to the given value.
     *
     * @param path  Path of the object to set.
     * @param value New value to set the path to
     */
    void set(String path, Object value);

    /**
     * Saves the config
     */
    void saveConfig();

    /**
     * Reloads the config
     */
    void reloadConfig();

    /**
     * Get all the configuration values
     *
     * @return Map with key-values
     */
    Map<String, Object> getValues();
}
