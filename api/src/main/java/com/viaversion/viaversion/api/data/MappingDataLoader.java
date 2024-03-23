/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.viaversion.viaversion.api.data;

import com.github.steveice10.opennbt.tag.builtin.ByteTag;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntArrayTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.io.NBTIO;
import com.github.steveice10.opennbt.tag.io.TagReader;
import com.google.common.annotations.Beta;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.util.GsonUtil;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MappingDataLoader {

    private final static TagReader<CompoundTag> MAPPINGS_READER = NBTIO.reader(CompoundTag.class).named();
    private static final byte DIRECT_ID = 0;
    private static final byte SHIFTS_ID = 1;
    private static final byte CHANGES_ID = 2;
    private static final byte IDENTITY_ID = 3;

    public static final MappingDataLoader INSTANCE = new MappingDataLoader();

    private final Map<String, CompoundTag> mappingsCache = new HashMap<>();
    private boolean cacheValid = true;

    public void clearCache() {
        mappingsCache.clear();
        cacheValid = false;
    }

    /**
     * Loads the file from the plugin folder if present, else from the bundled resources.
     *
     * @return loaded json object, or null if not found or invalid
     */
    public @Nullable JsonObject loadFromDataDir(final String name) {
        final File file = new File(Via.getPlatform().getDataFolder(), name);
        if (!file.exists()) {
            return loadData(name);
        }

        // Load the file from the platform's directory if present
        try (final FileReader reader = new FileReader(file)) {
            return GsonUtil.getGson().fromJson(reader, JsonObject.class);
        } catch (final JsonSyntaxException e) {
            // Users might mess up the format, so let's catch the syntax error
            Via.getPlatform().getLogger().warning(name + " is badly formatted!");
            throw new RuntimeException(e);
        } catch (final IOException | JsonIOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads the file from the bundled resources.
     *
     * @return loaded json object from bundled resources if present
     */
    public @Nullable JsonObject loadData(final String name) {
        final InputStream stream = getResource(name);
        if (stream == null) {
            return null;
        }

        try (final InputStreamReader reader = new InputStreamReader(stream)) {
            return GsonUtil.getGson().fromJson(reader, JsonObject.class);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public @Nullable CompoundTag loadNBT(final String name, final boolean cache) {
        if (!cacheValid) {
            return loadNBTFromFile(name);
        }

        CompoundTag data = mappingsCache.get(name);
        if (data != null) {
            return data;
        }

        data = loadNBTFromFile(name);

        if (cache && data != null) {
            mappingsCache.put(name, data);
        }
        return data;
    }

    public @Nullable CompoundTag loadNBT(final String name) {
        return loadNBT(name, false);
    }

    public @Nullable CompoundTag loadNBTFromFile(final String name) {
        final InputStream resource = getResource(name);
        if (resource == null) {
            return null;
        }

        try (final InputStream stream = resource) {
            return MAPPINGS_READER.read(stream);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public @Nullable Mappings loadMappings(final CompoundTag mappingsTag, final String key) {
        return loadMappings(mappingsTag, key, size -> {
            final int[] array = new int[size];
            Arrays.fill(array, -1);
            return array;
        }, (array, id, mappedId) -> array[id] = mappedId, IntArrayMappings::of);
    }

    @Beta
    public <M extends Mappings, V> @Nullable Mappings loadMappings(
        final CompoundTag mappingsTag,
        final String key,
        final MappingHolderSupplier<V> holderSupplier,
        final AddConsumer<V> addConsumer,
        final MappingsSupplier<M, V> mappingsSupplier
    ) {
        final CompoundTag tag = mappingsTag.getCompoundTag(key);
        if (tag == null) {
            return null;
        }

        final ByteTag serializationStragetyTag = tag.getUnchecked("id");
        final IntTag mappedSizeTag = tag.getUnchecked("mappedSize");
        final byte strategy = serializationStragetyTag.asByte();
        final V mappings;
        if (strategy == DIRECT_ID) {
            final IntArrayTag valuesTag = tag.getIntArrayTag("val");
            return IntArrayMappings.of(valuesTag.getValue(), mappedSizeTag.asInt());
        } else if (strategy == SHIFTS_ID) {
            final IntArrayTag shiftsAtTag = tag.getIntArrayTag("at");
            final IntArrayTag shiftsTag = tag.getIntArrayTag("to");
            final IntTag sizeTag = tag.getUnchecked("size");
            final int[] shiftsAt = shiftsAtTag.getValue();
            final int[] shiftsTo = shiftsTag.getValue();
            final int size = sizeTag.asInt();
            mappings = holderSupplier.get(size);

            // Handle values until first shift
            if (shiftsAt[0] != 0) {
                final int to = shiftsAt[0];
                for (int id = 0; id < to; id++) {
                    addConsumer.addTo(mappings, id, id);
                }
            }

            // Handle shifts
            for (int i = 0; i < shiftsAt.length; i++) {
                final int from = shiftsAt[i];
                final int to = i == shiftsAt.length - 1 ? size : shiftsAt[i + 1];
                int mappedId = shiftsTo[i];
                for (int id = from; id < to; id++) {
                    addConsumer.addTo(mappings, id, mappedId++);
                }
            }
        } else if (strategy == CHANGES_ID) {
            final IntArrayTag changesAtTag = tag.getIntArrayTag("at");
            final IntArrayTag valuesTag = tag.getIntArrayTag("val");
            final IntTag sizeTag = tag.getUnchecked("size");
            final boolean fillBetween = tag.get("nofill") == null;
            final int[] changesAt = changesAtTag.getValue();
            final int[] values = valuesTag.getValue();
            mappings = holderSupplier.get(sizeTag.asInt());

            for (int i = 0; i < changesAt.length; i++) {
                final int id = changesAt[i];
                if (fillBetween) {
                    // Fill from after the last change to before this change with unchanged ids
                    final int previousId = i != 0 ? changesAt[i - 1] + 1 : 0;
                    for (int identity = previousId; identity < id; identity++) {
                        addConsumer.addTo(mappings, identity, identity);
                    }
                }

                // Assign the changed value
                addConsumer.addTo(mappings, id, values[i]);
            }
        } else if (strategy == IDENTITY_ID) {
            final IntTag sizeTag = tag.getUnchecked("size");
            return new IdentityMappings(sizeTag.asInt(), mappedSizeTag.asInt());
        } else {
            throw new IllegalArgumentException("Unknown serialization strategy: " + strategy);
        }
        return mappingsSupplier.create(mappings, mappedSizeTag.asInt());
    }

    public FullMappings loadFullMappings(final CompoundTag mappingsTag, final CompoundTag unmappedIdentifiers, final CompoundTag mappedIdentifiers, final String key) {
        final ListTag<StringTag> unmappedElements = unmappedIdentifiers.getListTag(key, StringTag.class);
        final ListTag<StringTag> mappedElements = mappedIdentifiers.getListTag(key, StringTag.class);
        if (unmappedElements == null || mappedElements == null) {
            return null;
        }

        Mappings mappings = loadMappings(mappingsTag, key);
        if (mappings == null) {
            mappings = new IdentityMappings(unmappedElements.size(), mappedElements.size());
        }

        return new FullMappingsBase(
            unmappedElements.stream().map(StringTag::getValue).collect(Collectors.toList()),
            mappedElements.stream().map(StringTag::getValue).collect(Collectors.toList()),
            mappings
        );
    }

    /**
     * Returns a map of the object entries hashed by their id value.
     *
     * @param object json object
     * @return map with indexes hashed by their id value
     */
    public Object2IntMap<String> indexedObjectToMap(final JsonObject object) {
        final Object2IntMap<String> map = new Object2IntOpenHashMap<>(object.size(), .99F);
        map.defaultReturnValue(-1);
        for (final Map.Entry<String, JsonElement> entry : object.entrySet()) {
            map.put(entry.getValue().getAsString(), Integer.parseInt(entry.getKey()));
        }
        return map;
    }

    /**
     * Returns a map of the array entries hashed by their id value.
     *
     * @param array json array
     * @return map with indexes hashed by their id value
     */
    public Object2IntMap<String> arrayToMap(final JsonArray array) {
        final Object2IntMap<String> map = new Object2IntOpenHashMap<>(array.size(), .99F);
        map.defaultReturnValue(-1);
        for (int i = 0; i < array.size(); i++) {
            map.put(array.get(i).getAsString(), i);
        }
        return map;
    }

    /**
     * Returns the resource as an input stream.
     * Platforms can override this method to load resources from their own directories by creating a new instance of this class.
     */
    public @Nullable InputStream getResource(final String name) {
        return MappingDataLoader.class.getClassLoader().getResourceAsStream("assets/viaversion/data/" + name);
    }

    @FunctionalInterface
    public interface AddConsumer<T> {

        void addTo(T holder, int id, int mappedId);
    }

    @FunctionalInterface
    public interface MappingHolderSupplier<T> {

        T get(int expectedSize);
    }

    @FunctionalInterface
    public interface MappingsSupplier<T extends Mappings, V> {

        T create(V mappings, int mappedSize);
    }
}
