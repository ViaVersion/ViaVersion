/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntArrayTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.io.CharStreams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.data.BiMappings;
import com.viaversion.viaversion.api.data.Int2IntMapBiMappings;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.data.MappingDataLoader;
import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.util.GsonUtil;
import com.viaversion.viaversion.util.Int2IntBiHashMap;
import com.viaversion.viaversion.util.Key;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MappingData extends MappingDataBase {
    private final Map<String, int[]> blockTags = new HashMap<>();
    private final Map<String, int[]> itemTags = new HashMap<>();
    private final Map<String, int[]> fluidTags = new HashMap<>();
    private final BiMap<Short, String> oldEnchantmentsIds = HashBiMap.create();
    private final Map<String, String> translateMapping = new HashMap<>();
    private final Map<String, String> mojangTranslation = new HashMap<>();
    private final BiMap<String, String> channelMappings = HashBiMap.create();

    public MappingData() {
        super("1.12", "1.13");
    }

    @Override
    protected void loadExtras(final CompoundTag data) {
        loadTags(blockTags, data.getCompoundTag("block_tags"));
        loadTags(itemTags, data.getCompoundTag("item_tags"));
        loadTags(fluidTags, data.getCompoundTag("fluid_tags"));

        CompoundTag legacyEnchantments = data.getCompoundTag("legacy_enchantments");
        loadEnchantments(oldEnchantmentsIds, legacyEnchantments);

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

        JsonObject object = MappingDataLoader.INSTANCE.loadFromDataDir("channelmappings-1.13.json");
        if (object != null) {
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                String oldChannel = entry.getKey();
                String newChannel = entry.getValue().getAsString();
                if (!Key.isValid(newChannel)) {
                    Via.getPlatform().getLogger().warning("Channel '" + newChannel + "' is not a valid 1.13 plugin channel, please check your configuration!");
                    continue;
                }
                channelMappings.put(oldChannel, newChannel);
            }
        }

        Map<String, String> translationMappingData = GsonUtil.getGson().fromJson(
                new InputStreamReader(MappingData.class.getClassLoader().getResourceAsStream("assets/viaversion/data/mapping-lang-1.12-1.13.json")),
                new TypeToken<Map<String, String>>() {
                }.getType());

        String[] unmappedTranslationLines;
        try (Reader reader = new InputStreamReader(MappingData.class.getClassLoader()
                .getResourceAsStream("assets/viaversion/data/en_US.properties"), StandardCharsets.UTF_8)) {
            unmappedTranslationLines = CharStreams.toString(reader).split("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (String line : unmappedTranslationLines) {
            if (line.isEmpty()) {
                continue;
            }

            String[] keyAndTranslation = line.split("=", 2);
            if (keyAndTranslation.length != 2) {
                continue;
            }

            String key = keyAndTranslation[0];
            String translation = keyAndTranslation[1].replaceAll("%(\\d\\$)?d", "%$1s").trim();
            mojangTranslation.put(key, translation);

            // Null values in the file mean the key did not change AND the translation has the same amount of placeholders still
            if (translationMappingData.containsKey(key)) {
                String mappedKey = translationMappingData.get(key);
                translateMapping.put(key, mappedKey != null ? mappedKey : key);
            }
        }
    }

    @Override
    protected @Nullable Mappings loadMappings(final CompoundTag data, final String key) {
        // Special cursed case
        if (key.equals("blocks")) {
            return super.loadMappings(data, "blockstates");
        } else if (key.equals("blockstates")) {
            return null;
        } else {
            return super.loadMappings(data, key);
        }
    }

    @Override
    protected @Nullable BiMappings loadBiMappings(final CompoundTag data, final String key) {
        // Special cursed case
        if (key.equals("items")) {
            return (BiMappings) MappingDataLoader.INSTANCE.loadMappings(data, "items", size -> {
                final Int2IntBiHashMap map = new Int2IntBiHashMap(size);
                map.defaultReturnValue(-1);
                return map;
            }, Int2IntBiHashMap::put, (v, mappedSize) -> Int2IntMapBiMappings.of(v));
        } else {
            return super.loadBiMappings(data, key);
        }
    }

    public static String validateNewChannel(String newId) {
        if (!Key.isValid(newId)) {
            return null; // Not valid
        }

        return Key.namespaced(newId);
    }

    private void loadTags(Map<String, int[]> output, CompoundTag newTags) {
        for (Map.Entry<String, Tag> entry : newTags.entrySet()) {
            IntArrayTag ids = (IntArrayTag) entry.getValue();
            output.put(Key.namespaced(entry.getKey()), ids.getValue());
        }
    }

    private void loadEnchantments(Map<Short, String> output, CompoundTag enchantments) {
        for (Map.Entry<String, Tag> enty : enchantments.entrySet()) {
            output.put(Short.parseShort(enty.getKey()), ((StringTag) enty.getValue()).getValue());
        }
    }

    public Map<String, int[]> getBlockTags() {
        return blockTags;
    }

    public Map<String, int[]> getItemTags() {
        return itemTags;
    }

    public Map<String, int[]> getFluidTags() {
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
