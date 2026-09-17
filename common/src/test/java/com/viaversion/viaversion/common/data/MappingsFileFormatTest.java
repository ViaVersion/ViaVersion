/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.common.data;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.data.MappingDataLoader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * A data file generated for an outdated mappings format is only noticed when a protocol using it
 * loads its mapping data, and that failure is logged instead of thrown. The protocol then stays
 * loaded without mappings and silently stops rewriting the packets it is responsible for.
 */
final class MappingsFileFormatTest {

    private static final String DATA_PATH = "assets/viaversion/data";

    private List<String> dataFiles(final String prefix) throws Exception {
        final URL directoryUrl = MappingsFileFormatTest.class.getClassLoader().getResource(DATA_PATH);
        Assertions.assertNotNull(directoryUrl, "Could not find " + DATA_PATH + " on the classpath");

        try (final Stream<Path> files = Files.list(Path.of(directoryUrl.toURI()))) {
            final List<String> fileNames = files.map(file -> file.getFileName().toString())
                .filter(name -> name.startsWith(prefix) && name.endsWith(".nbt"))
                .sorted()
                .toList();
            Assertions.assertFalse(fileNames.isEmpty(), "Found no " + prefix + "*.nbt files in " + DATA_PATH);
            return fileNames;
        }
    }

    @Test
    void mappingsFilesDeclareOneFormatVersion() throws Exception {
        final List<String> fileNames = new ArrayList<>(dataFiles("mappings-"));
        fileNames.addAll(dataFiles("identifiers-"));

        final Map<Integer, List<String>> filesByVersion = new LinkedHashMap<>();
        final List<String> unreadable = new ArrayList<>();
        for (final String fileName : fileNames) {
            final CompoundTag data = MappingDataLoader.INSTANCE.loadNBTFromFile(fileName);
            if (data == null) {
                unreadable.add(fileName);
                continue;
            }

            filesByVersion.computeIfAbsent(data.getInt("version", -1), version -> new ArrayList<>()).add(fileName);
        }

        Assertions.assertTrue(unreadable.isEmpty(), "Could not read mappings files: " + unreadable);
        Assertions.assertFalse(filesByVersion.containsKey(-1), "Mappings files without a format version tag: " + filesByVersion.get(-1));
        Assertions.assertEquals(1, filesByVersion.size(), "Mappings files declare more than one format version: " + filesByVersion);
    }

    @Test
    void identifierFilesAreLoadable() throws Exception {
        final List<String> errors = new ArrayList<>();
        for (final String fileName : dataFiles("identifiers-")) {
            final CompoundTag data = MappingDataLoader.INSTANCE.loadNBTFromFile(fileName);
            Assertions.assertNotNull(data, fileName + " could not be read");

            for (final Map.Entry<String, Tag> entry : data.entrySet()) {
                if (!(entry.getValue() instanceof CompoundTag)) {
                    continue;
                }

                try {
                    MappingDataLoader.INSTANCE.loadMappings(data, entry.getKey());
                } catch (final RuntimeException e) {
                    errors.add(fileName + " (" + entry.getKey() + "): " + e);
                }
            }
        }

        Assertions.assertTrue(errors.isEmpty(), "Unreadable identifier mappings:\n" + String.join("\n", errors));
    }
}
