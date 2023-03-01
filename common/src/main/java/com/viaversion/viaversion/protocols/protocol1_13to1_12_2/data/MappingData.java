/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2023 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.io.CharStreams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.data.BiMappings;
import com.viaversion.viaversion.api.data.Int2IntMapBiMappings;
import com.viaversion.viaversion.api.data.IntArrayMappings;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.data.MappingDataLoader;
import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.util.GsonUtil;
import com.viaversion.viaversion.util.Int2IntBiHashMap;
import com.viaversion.viaversion.util.Int2IntBiMap;
import com.viaversion.viaversion.util.Key;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MappingData extends MappingDataBase {
    private final Map<String, Integer[]> blockTags = new HashMap<>();
    private final Map<String, Integer[]> itemTags = new HashMap<>();
    private final Map<String, Integer[]> fluidTags = new HashMap<>();
    private final BiMap<Short, String> oldEnchantmentsIds = HashBiMap.create();
    private final Map<String, String> translateMapping = new HashMap<>();
    private final Map<String, String> mojangTranslation = new HashMap<>();
    private final BiMap<String, String> channelMappings = HashBiMap.create(); // 1.12->1.13

    public MappingData() {
        super("1.12", "1.13");
    }

    @Override
    public void loadExtras(JsonObject unmappedIdentifiers, JsonObject mappedIdentifiers, JsonObject diffMappings) {
        loadTags(blockTags, mappedIdentifiers.getAsJsonObject("block_tags"));
        loadTags(itemTags, mappedIdentifiers.getAsJsonObject("item_tags"));
        loadTags(fluidTags, mappedIdentifiers.getAsJsonObject("fluid_tags"));

        loadEnchantments(oldEnchantmentsIds, unmappedIdentifiers.getAsJsonObject("legacy_enchantments"));
        enchantmentMappings = IntArrayMappings.builder().customEntrySize(72)
                .unmapped(unmappedIdentifiers.getAsJsonObject("legacy_enchantments")).mapped(mappedIdentifiers.getAsJsonArray("enchantments")).build();

        // Map minecraft:snow[layers=1] of 1.12 to minecraft:snow[layers=2] in 1.13
        if (Via.getConfig().isSnowCollisionFix()) {
            blockMappings.setNewId(1248, 3416);
        }

        // Remap infested blocks, as they are instantly breakabale in 1.13+ and can't be broken by those clients on older servers
        if (Via.getConfig().isInfestedBlocksFix()) {
            blockMappings.setNewId(1552, 1); // stone
            blockMappings.setNewId(1553, 14); // cobblestone
            blockMappings.setNewId(1554, 3983); // stone bricks
            blockMappings.setNewId(1555, 3984); // mossy stone bricks
            blockMappings.setNewId(1556, 3985); // cracked stone bricks
            blockMappings.setNewId(1557, 3986); // chiseled stone bricks
        }

        JsonObject object = MappingDataLoader.loadFromDataDir("channelmappings-1.13.json");
        if (object != null) {
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                String oldChannel = entry.getKey();
                String newChannel = entry.getValue().getAsString();
                if (!isValid1_13Channel(newChannel)) {
                    Via.getPlatform().getLogger().warning("Channel '" + newChannel + "' is not a valid 1.13 plugin channel, please check your configuration!");
                    continue;
                }
                channelMappings.put(oldChannel, newChannel);
            }
        }

        Map<String, String> translateData = GsonUtil.getGson().fromJson(
                new InputStreamReader(MappingData.class.getClassLoader().getResourceAsStream("assets/viaversion/data/mapping-lang-1.12-1.13.json")),
                new TypeToken<Map<String, String>>() {
                }.getType());
        try {
            String[] lines;
            try (Reader reader = new InputStreamReader(MappingData.class.getClassLoader()
                    .getResourceAsStream("assets/viaversion/data/en_US.properties"), StandardCharsets.UTF_8)) {
                lines = CharStreams.toString(reader).split("\n");
            }
            for (String line : lines) {
                if (line.isEmpty()) continue;

                String[] keyAndTranslation = line.split("=", 2);
                if (keyAndTranslation.length != 2) continue;

                String key = keyAndTranslation[0];
                if (!translateData.containsKey(key)) {
                    String translation = keyAndTranslation[1].replaceAll("%(\\d\\$)?d", "%$1s");
                    mojangTranslation.put(key, translation);
                } else {
                    String dataValue = translateData.get(key);
                    if (dataValue != null) {
                        translateMapping.put(key, dataValue);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected @Nullable Mappings loadFromArray(final JsonObject unmappedIdentifiers, final JsonObject mappedIdentifiers, @Nullable final JsonObject diffMappings, final String key) {
        if (key.equals("blocks")) {
            // Need to use a custom size since there are larger gaps in ids, also object -> array
            return IntArrayMappings.builder().customEntrySize(4084)
                    .unmapped(unmappedIdentifiers.getAsJsonObject("blocks")).mapped(mappedIdentifiers.getAsJsonArray("blockstates")).build();
        } else if (key.equals("items")) {
            // Object -> array
            return IntArrayMappings.builder()
                    .unmapped(unmappedIdentifiers.getAsJsonObject(key)).mapped(mappedIdentifiers.getAsJsonArray(key)).build();
        } else {
            return super.loadFromArray(unmappedIdentifiers, mappedIdentifiers, diffMappings, key);
        }
    }

    @Override
    protected @Nullable BiMappings loadBiFromArray(final JsonObject unmappedIdentifiers, final JsonObject mappedIdentifiers, @Nullable final JsonObject diffMappings, final String key) {
        if (key.equals("items")) {
            final Int2IntBiMap itemMappings = new Int2IntBiHashMap();
            itemMappings.defaultReturnValue(-1);
            MappingDataLoader.mapIdentifiers(itemMappings, unmappedIdentifiers.getAsJsonObject("items"), toJsonObject(mappedIdentifiers.getAsJsonArray("items")), null, true);
            return Int2IntMapBiMappings.of(itemMappings);
        }
        return super.loadBiFromArray(unmappedIdentifiers, mappedIdentifiers, diffMappings, key);
    }

    private JsonObject toJsonObject(final JsonArray array) {
        final JsonObject object = new JsonObject();
        for (int i = 0; i < array.size(); i++) {
            final JsonElement element = array.get(i);
            object.add(Integer.toString(i), element);
        }
        return object;
    }

    public static String validateNewChannel(String newId) {
        if (!isValid1_13Channel(newId)) {
            return null; // Not valid
        }
        int separatorIndex = newId.indexOf(':');
        // Vanilla parses an empty and a missing namespace as the minecraft namespace
        if (separatorIndex == -1) {
            return "minecraft:" + newId;
        } else if (separatorIndex == 0) {
            return "minecraft" + newId;
        }
        return newId;
    }

    public static boolean isValid1_13Channel(String channelId) {
        return channelId.matches("([0-9a-z_.-]+:)?[0-9a-z_/.-]+");
    }

    private void loadTags(Map<String, Integer[]> output, JsonObject newTags) {
        for (Map.Entry<String, JsonElement> entry : newTags.entrySet()) {
            JsonArray ids = entry.getValue().getAsJsonArray();
            Integer[] idsArray = new Integer[ids.size()];
            for (int i = 0; i < ids.size(); i++) {
                idsArray[i] = ids.get(i).getAsInt();
            }
            output.put(Key.namespaced(entry.getKey()), idsArray);
        }
    }

    private void loadEnchantments(Map<Short, String> output, JsonObject enchantments) {
        for (Map.Entry<String, JsonElement> enchantment : enchantments.entrySet()) {
            output.put(Short.parseShort(enchantment.getKey()), enchantment.getValue().getAsString());
        }
    }

    public Map<String, Integer[]> getBlockTags() {
        return blockTags;
    }

    public Map<String, Integer[]> getItemTags() {
        return itemTags;
    }

    public Map<String, Integer[]> getFluidTags() {
        return fluidTags;
    }

    public BiMap<Short, String> getOldEnchantmentsIds() {
        return oldEnchantmentsIds;
    }

    public Map<String, String> getTranslateMapping() {
        return translateMapping;
    }

    public Map<String, String> getMojangTranslation() {
        return mojangTranslation;
    }

    public BiMap<String, String> getChannelMappings() {
        return channelMappings;
    }
}
