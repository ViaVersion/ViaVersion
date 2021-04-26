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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class CommentStore {
    private final Map<String, List<String>> headers = Maps.newConcurrentMap();
    private final char pathSeperator;
    private final int indents;
    private List<String> mainHeader = Lists.newArrayList();

    public CommentStore(char pathSeperator, int indents) {
        this.pathSeperator = pathSeperator;
        this.indents = indents;
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

    public void storeComments(InputStream inputStream) throws IOException {
        InputStreamReader reader = new InputStreamReader(inputStream);
        String contents;
        try {
            contents = CharStreams.toString(reader);
        } finally {
            reader.close();
        }
        StringBuilder memoryData = new StringBuilder();
        // Parse headers
        final String pathSeparator = Character.toString(this.pathSeperator);
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

            int indents = indent / this.indents;
            if (indents <= currentIndents) {
                // Remove last section of key
                String[] array = key.split(Pattern.quote(pathSeparator));
                int backspace = currentIndents - indents + 1;
                key = join(array, this.pathSeperator, 0, array.length - backspace);
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
    }

    public void writeComments(String yaml, File output) throws IOException {
        // Custom save
        final int indentLength = this.indents;
        final String pathSeparator = Character.toString(this.pathSeperator);
        StringBuilder fileData = new StringBuilder();
        int currentIndents = 0;
        String key = "";
        for (String h : mainHeader) {
            // Append main header to top of file
            fileData.append("#> ").append(h).append('\n');
        }

        for (String line : yaml.split("\n")) {
            if (line.isEmpty()) continue; // Skip empty lines
            int indent = getSuccessiveCharCount(line, ' ');
            int indents = indent / indentLength;
            String indentText = indent > 0 ? line.substring(0, indent) : "";
            if (indents <= currentIndents) {
                // Remove last section of key
                String[] array = key.split(Pattern.quote(pathSeparator));
                int backspace = currentIndents - indents + 1;
                key = join(array, this.pathSeperator, 0, array.length - backspace);
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
            writer = new FileWriter(output);
            writer.write(fileData.toString());
            writer.flush();
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
