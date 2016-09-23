package us.myles.ViaVersion.util;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class Configuration extends YamlConfiguration {
    private final Map<String, List<String>> headers = Maps.newConcurrentMap();
    private final File file;
    private List<String> mainHeader = Lists.newArrayList();
    private boolean loadHeaders;

    public Configuration(File file) {
        this.file = file;
    }

    /**
     * Set the main header displayed at top of config.
     *
     * @param header header
     */
    public void mainHeader(String... header) {
        mainHeader = Arrays.asList(header);
    }

    /**
     * Get main header displayed at top of config.
     *
     * @return header
     */
    public List<String> mainHeader() {
        return mainHeader;
    }

    /**
     * Set option header.
     *
     * @param key    of option (or section)
     * @param header of option (or section)
     */
    public void header(String key, String... header) {
//        String value = Joiner.on('\n').join(header);
        headers.put(key, Arrays.asList(header));
    }

    /**
     * Get header of option
     *
     * @param key of option (or section)
     * @return Header
     */
    public List<String> header(String key) {
        return headers.get(key);
    }

    public <T> T get(String key, Class<T> type) {
        return type.cast(get(key));
    }

    /**
     * Reload config from file.
     */
    public void reload() {
        reload(headers.isEmpty());
    }

    /**
     * Reload config from file.
     *
     * @param loadHeaders Whether or not to load headers.
     */
    public void reload(boolean loadHeaders) {
        this.loadHeaders = loadHeaders;
        try {
            load(file);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "failed to reload file", e);
        }
    }

    @Override
    public void loadFromString(String contents) throws InvalidConfigurationException {
        if (!loadHeaders) {
            super.loadFromString(contents);
            return;
        }

        StringBuilder memoryData = new StringBuilder();

        // Parse headers
        final int indentLength = options().indent();
        final String pathSeparator = Character.toString(options().pathSeparator());
        int currentIndents = 0;
        String key = "";
        List<String> headers = Lists.newArrayList();
        for (String line : contents.split("\n")) {
            if (line.isEmpty()) continue; // Skip empty lines
            int indent = getSuccessiveCharCount(line, ' ');
            String subline = indent > 0 ? line.substring(indent) : line;
            if (subline.startsWith("#")) {
                if (subline.startsWith("#>")) {
                    String txt = subline.startsWith("#> ") ? subline.substring(3) : subline.substring(2);
                    mainHeader.add(txt);
                    continue; // Main header, handled by bukkit
                }

                // Add header to list
                String txt = subline.startsWith("# ") ? subline.substring(2) : subline.substring(1);
                headers.add(txt);
                continue;
            }

            int indents = indent / indentLength;
            if (indents <= currentIndents) {
                // Remove last section of key
                String[] array = key.split(Pattern.quote(pathSeparator));
                int backspace = currentIndents - indents + 1;
                key = join(array, options().pathSeparator(), 0, array.length - backspace);
            }

            // Add new section to key
            String separator = key.length() > 0 ? pathSeparator : "";
            String lineKey = line.contains(":") ? line.split(Pattern.quote(":"))[0] : line;
            key += separator + lineKey.substring(indent);

            currentIndents = indents;

            memoryData.append(line).append('\n');
            if (!headers.isEmpty()) {
                this.headers.put(key, headers);
                headers = Lists.newArrayList();
            }
        }

        // Parse remaining text
        super.loadFromString(memoryData.toString());

        // Clear bukkit header
        options().header(null);
    }

    /**
     * Save config to file
     */
    public void save() {
        if (headers.isEmpty() && mainHeader.isEmpty()) {
            try {
                super.save(file);
            } catch (IOException e) {
                Bukkit.getLogger().log(Level.WARNING, "Failed to save file", e);
            }
            return;
        }

        // Custom save
        final int indentLength = options().indent();
        final String pathSeparator = Character.toString(options().pathSeparator());
        String content = saveToString();
        StringBuilder fileData = new StringBuilder(buildHeader());
        int currentIndents = 0;
        String key = "";
        for (String h : mainHeader) {
            // Append main header to top of file
            fileData.append("#> ").append(h).append('\n');
        }

        for (String line : content.split("\n")) {
            if (line.isEmpty()) continue; // Skip empty lines
            int indent = getSuccessiveCharCount(line, ' ');
            int indents = indent / indentLength;
            String indentText = indent > 0 ? line.substring(0, indent) : "";
            if (indents <= currentIndents) {
                // Remove last section of key
                String[] array = key.split(Pattern.quote(pathSeparator));
                int backspace = currentIndents - indents + 1;
                key = join(array, options().pathSeparator(), 0, array.length - backspace);
            }

            // Add new section to key
            String separator = key.length() > 0 ? pathSeparator : "";
            String lineKey = line.contains(":") ? line.split(Pattern.quote(":"))[0] : line;
            key += separator + lineKey.substring(indent);

            currentIndents = indents;

            List<String> header = headers.get(key);
            String headerText = header != null ? addHeaderTags(header, indentText) : "";
            fileData.append(headerText).append(line).append('\n');
        }

        // Write data to file
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(fileData.toString());
            writer.flush();
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to save file", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private String addHeaderTags(List<String> header, String indent) {
        StringBuilder builder = new StringBuilder();
        for (String line : header) {
            builder.append(indent).append("# ").append(line).append('\n');
        }
        return builder.toString();
    }

    private String join(String[] array, char joinChar, int start, int length) {
        String[] copy = new String[length - start];
        System.arraycopy(array, start, copy, 0, length - start);
        return Joiner.on(joinChar).join(copy);
    }

    private int getSuccessiveCharCount(String text, char key) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == key) {
                count += 1;
            } else {
                break;
            }
        }
        return count;
    }
}
