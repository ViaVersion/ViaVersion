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
package com.viaversion.viaversion.protocols.v26_2to26_3.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.protocols.v26_2to26_3.Protocol26_2To26_3;
import com.viaversion.viaversion.rewriter.RegistryDataRewriter;
import com.viaversion.viaversion.util.Key;
import java.util.function.UnaryOperator;

public final class RegistryDataRewriter26_3 extends RegistryDataRewriter {

    public RegistryDataRewriter26_3(final Protocol26_2To26_3 protocol) {
        super(protocol);
    }

    @Override
    public void updateEnchantmentTerm(final CompoundTag term) {
        final StringTag condition = term.removeUnchecked("condition");
        if (condition != null) {
            term.put("type", condition);
            updateNumberProviders(term, condition);
        }

        if (condition != null && Key.equals(condition.getValue(), "damage_source_properties")) {
            final CompoundTag predicate = term.getCompoundTag("predicate");
            if (predicate != null) {
                final ListTag<CompoundTag> tags = predicate.getListTag("tags", CompoundTag.class);
                if (tags != null) {
                    updateTagKey(tags);
                }
            }
        }

        super.updateEnchantmentTerm(term);

        // has to run after super, which maps the block id under the old key
        if (condition != null && Key.equals(condition.getValue(), "block_state_property")) {
            condition.setValue("minecraft:match_block");
            term.put("blocks", term.remove("block"));

            final Tag properties = term.remove("properties");
            if (properties != null) {
                term.put("state", properties);
            }
        }
    }

    // Number providers have been split into int and float providers
    private static void updateNumberProviders(final CompoundTag term, final StringTag condition) {
        if (Key.equals(condition.getValue(), "value_check")) {
            condition.setValue("int_value_check");
            convert(term, "value", "value", RegistryDataRewriter26_3::toIntProvider);
            convert(term, "range", "test", RegistryDataRewriter26_3::updateIntRange);
        } else if (Key.equals(condition.getValue(), "time_check")) {
            convert(term, "value", "value", RegistryDataRewriter26_3::updateIntRange);
        } else if (Key.equals(condition.getValue(), "random_chance")) {
            convert(term, "chance", "chance", RegistryDataRewriter26_3::toFloatProvider);
        }
    }

    private static Tag updateIntRange(final Tag range) {
        if (range instanceof CompoundTag rangeTag) {
            convert(rangeTag, "min", "min", RegistryDataRewriter26_3::toIntProvider);
            convert(rangeTag, "max", "max", RegistryDataRewriter26_3::toIntProvider);
        }
        return range;
    }

    // Old providers were floats, with getInt rounding the value
    private static Tag toIntProvider(final Tag tag) {
        if (tag instanceof NumberTag numberTag) {
            return new IntTag(Math.round(numberTag.asFloat()));
        } else if (tag instanceof CompoundTag provider) {
            final String type = provider.getString("type");
            if (Key.equals(type, "binomial")) {
                convert(provider, "n", "n", RegistryDataRewriter26_3::toIntProvider);
                convert(provider, "p", "p", RegistryDataRewriter26_3::toFloatProvider);
                return provider;
            } else if (Key.equals(type, "score") && provider.getFloat("scale", 1) == 1) {
                provider.remove("scale");
                return provider;
            }
        }
        return unary("from_float", unary("round", toFloatProvider(tag)));
    }

    private static Tag toFloatProvider(final Tag tag) {
        if (!(tag instanceof CompoundTag provider)) {
            return tag;
        }

        // Untyped providers used to fall back to uniform
        switch (Key.stripMinecraftNamespace(provider.getString("type", "uniform"))) {
            case "uniform" -> {
                provider.putString("type", "minecraft:uniform");
                convert(provider, "min", "min", RegistryDataRewriter26_3::toFloatProvider);
                convert(provider, "max", "max", RegistryDataRewriter26_3::toFloatProvider);
            }
            case "sum" -> {
                provider.putString("type", "minecraft:add");
                convert(provider, "summands", "inputs", summands -> {
                    final ListTag<CompoundTag> inputs = new ListTag<>(CompoundTag.class);
                    if (summands instanceof ListTag<?> summandsList) {
                        for (final Tag summand : summandsList) {
                            inputs.add(toFloatProviderCompound(summand));
                        }
                    }
                    return inputs;
                });
            }
            case "binomial" -> {
                return unary("from_int", toIntProvider(provider));
            }
            case "score" -> {
                final Tag scale = provider.remove("scale");
                final CompoundTag score = unary("from_int", provider);
                if (scale == null) {
                    return score;
                }

                final ListTag<CompoundTag> inputs = new ListTag<>(CompoundTag.class);
                inputs.add(score);
                inputs.add(toFloatProviderCompound(scale));
                final CompoundTag product = new CompoundTag();
                product.putString("type", "minecraft:mul");
                product.put("inputs", inputs);
                return product;
            }
        }
        return provider;
    }

    private static CompoundTag toFloatProviderCompound(final Tag tag) {
        final Tag provider = toFloatProvider(tag);
        if (provider instanceof CompoundTag compoundTag) {
            return compoundTag;
        }

        final CompoundTag constant = new CompoundTag();
        constant.putString("type", "minecraft:constant");
        constant.put("value", provider);
        return constant;
    }

    private static CompoundTag unary(final String type, final Tag input) {
        final CompoundTag provider = new CompoundTag();
        provider.putString("type", "minecraft:" + type);
        provider.put("input", input);
        return provider;
    }

    private static void convert(final CompoundTag tag, final String key, final String newKey, final UnaryOperator<Tag> converter) {
        final Tag value = tag.remove(key);
        if (value != null) {
            tag.put(newKey, converter.apply(value));
        }
    }

    @Override
    public boolean updateBlockStateProvider(final CompoundTag tag) {
        final StringTag typeTag = tag.getStringTag("type");
        final String strippedName = Key.stripMinecraftNamespace(typeTag.getValue());
        typeTag.setValue(switch (strippedName) {
            case "randomized_int_state_provider" -> "randomized_int";
            case "rotated_block_provider" -> "rotated";
            case "rule_based_state_provider" -> "rule_based";
            case "simple_state_provider" -> "simple";
            case "weighted_state_provider" -> "weighted";
            default -> strippedName.replace("_provider", "");
        });
        return super.updateBlockStateProvider(tag);
    }

    @Override
    protected boolean updateBlockState(final Tag blockStateTag) {
        if (blockStateTag instanceof CompoundTag compoundTag) { // can only be a compound tag pre-26.3
            final Tag id = compoundTag.remove("Name");
            if (id != null) {
                compoundTag.put("id", id);

                final Tag properties = compoundTag.remove("Properties");
                if (properties != null) {
                    compoundTag.put("properties", properties);
                }
            }
        }
        return super.updateBlockState(blockStateTag);
    }

    private void updateTagKey(final ListTag<CompoundTag> tags) {
        for (final CompoundTag tag : tags) {
            final StringTag tagKey = tag.getStringTag("id");
            tagKey.setValue("#" + tagKey.getValue());
        }
    }
}
