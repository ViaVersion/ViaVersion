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

import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class CommentStore {
    private final Map<String, List<String>> headers = new HashMap<>();
    private final String pathSeparator;
    private final String pathSeparatorQuoted;
    private final int indents;
    private List<String> mainHeader = new ArrayList<>();

    public CommentStore(final char pathSeparator, final int indents) {
        this.pathSeparator = Character.toString(pathSeparator);
        this.pathSeparatorQuoted = Pattern.quote(this.pathSeparator);
        this.indents = indents;
    }

    /**
     * Set the main header displayed at top of config.
     *
     * @param header header
     */
    public void mainHeader(final String... header) {
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
    public void header(final String key, final String... header) {
        headers.put(key, Arrays.asList(header));
    }

    /**
     * Get header of option
     *
     * @param key of option (or section)
     * @return Header
     */
    public List<String> header(final String key) {
        return headers.get(key);
    }

    public void storeComments(final InputStream inputStream) throws IOException {
        mainHeader.clear();
        headers.clear();

        final String data;
        try (final InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            data = CharStreams.toString(reader);
        }

        final List<String> currentComments = new ArrayList<>();
        boolean header = true;
        boolean multiLineValue = false;
        int currentIndents = 0;
        String key = "";
        for (final String line : data.split("\n")) {
            final String s = line.trim();
            // It's a comment!
            if (s.startsWith("#")) {
                currentComments.add(s);
                continue;
            }

            // Header is over - save it!
            if (header) {
                if (!currentComments.isEmpty()) {
                    currentComments.add("");
                    mainHeader.addAll(currentComments);
                    currentComments.clear();
                }
                header = false;
            }

            // Save empty lines as well
            if (s.isEmpty()) {
                currentComments.add(s);
                continue;
            }

            // Multi line values?
            if (s.startsWith("- |")) {
                multiLineValue = true;
                continue;
            }

            final int indent = getIndents(line);
            final int indents = indent / this.indents;
            // Check if the multi line value is over
            if (multiLineValue) {
                if (indents > currentIndents) continue;

                multiLineValue = false;
            }

            // Check if this is a level lower
            if (indents <= currentIndents) {
                final String[] array = key.split(pathSeparatorQuoted);
                final int backspace = currentIndents - indents + 1;
                final int delta = array.length - backspace;
                key = delta >= 0 ? join(array, delta) : key;
            }

            // Finish current key
            final String separator = key.isEmpty() ? "" : this.pathSeparator;
            final String lineKey = line.indexOf(':') != -1 ? line.split(Pattern.quote(":"))[0] : line;
            key += separator + lineKey.substring(indent);
            currentIndents = indents;

            if (!currentComments.isEmpty()) {
                headers.put(key, new ArrayList<>(currentComments));
                currentComments.clear();
            }
        }
    }

    public void writeComments(final String rawYaml, final File output) throws IOException {
        final StringBuilder fileData = new StringBuilder();
        for (final String mainHeaderLine : mainHeader) {
            fileData.append(mainHeaderLine).append('\n');
        }

        // Remove last new line
        fileData.deleteCharAt(fileData.length() - 1);

        int currentKeyIndents = 0;
        String key = "";
        for (final String line : rawYaml.lines().toList()) {
            if (line.isEmpty()) {
                continue;
            }

            final int indent = getIndents(line);
            final int indents = indent / this.indents;
            final boolean keyLine;
            final String substring = line.substring(indent);
            if (substring.trim().isEmpty() || substring.charAt(0) == '-') {
                keyLine = false;
            } else if (indents <= currentKeyIndents) {
                final String[] array = key.split(this.pathSeparatorQuoted);
                final int backspace = currentKeyIndents - indents + 1;
                key = join(array, array.length - backspace);
                keyLine = true;
            } else {
                keyLine = line.indexOf(':') != -1;
            }

            if (!keyLine) {
                // Nothing to do, go to next line
                fileData.append(line).append('\n');
                continue;
            }

            final String newKey = substring.split(Pattern.quote(":"))[0]; // Not sure about the quote thing, so I'll just keep it :aaa:
            if (!key.isEmpty()) {
                key += this.pathSeparator;
            }
            key += newKey;

            // Add comments
            final List<String> strings = headers.get(key);
            if (strings != null && !strings.isEmpty()) {
                final String indentText = indent > 0 ? line.substring(0, indent) : "";
                for (final String comment : strings) {
                    if (comment.isEmpty()) {
                        fileData.append('\n');
                    } else {
                        fileData.append(indentText).append(comment).append('\n');
                    }
                }
            }

            currentKeyIndents = indents;
            fileData.append(line).append('\n');
        }

        // Write data to file
        Files.write(fileData.toString(), output, StandardCharsets.UTF_8);
    }

    private int getIndents(final String line) {
        int count = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) != ' ') {
                break;
            }

            count++;
        }
        return count;
    }

    private String join(final String[] array, final int length) {
        final String[] copy = new String[length];
        System.arraycopy(array, 0, copy, 0, length);
        return String.join(this.pathSeparator, copy);
    }
}
