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
package com.viaversion.viaversion.protocols.v1_20_5to1_21.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.viaversion.viaversion.util.Key;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class AttributeModifierMappings1_21 {
    private static final Map<UUID, String> ATTRIBUTE_MODIFIER_IDS = new HashMap<>();
    private static final Map<String, UUID> ATTRIBUTE_MODIFIER_INVERSE_IDS = new HashMap<>();
    private static final BiMap<String, String> ATTRIBUTE_MODIFIER_NAMES = HashBiMap.create(Map.of(
        "Random spawn bonus", "random_spawn_bonus",
        "Random zombie-spawn bonus", "zombie_random_spawn_bonus",
        "Leader zombie bonus", "leader_zombie_bonus",
        "Zombie reinforcement callee charge", "reinforcement_callee_charge",
        "Zombie reinforcement caller charge", "reinforcement_caller_charge"
    ));

    static {
        add(-4483571535397864886L, -5989644940537681742L, "armor.body");
        add(8144722948526719024L, -7778190119041365872L, "effect.slowness");
        add(6967552254039378640L, -9116175396973475259L, "enchantment.aqua_affinity");
        add(5279725409867744698L, -5150363631200102632L, "attacking");
        add(148071211714102867L, -7685811609035173472L, "attacking");
        add(6196088217904236654L, -7493791321850887290L, "effect.minining_fatigue");
        add(-5084161844451524480L, -8859020046251006329L, "enchantment.soul_speed");
        add(-7907339078496465106L, -8112074600724210224L, "enchantment.swift_sneak");
        add(6688265815086220243L, -6545541163342161890L, "drinking");
        add(8315164243412860989L, -6631520853640075966L, "creative_mode_block_range");
        add(4389663563256579765L, -4827163546944004714L, "enchantment.efficiency");
        add(6732612758648800940L, -5145707699103688244L, "effect.health_boost");
        add(9079981369298536661L, -6728494925450710401L, "covered");
        add(-1521481167610687786L, -8630419745734927691L, "effect.absorption");
        add(-7473408062188862076L, -5872005994337939597L, "creative_mode_entity_range");
        add(-3721396875562958315L, -5317020504214661337L, "effect.unluck");
        add(-2861585646493481178L, -6113244764726669811L, "armor.leggings");
        add(6718535547217657911L, -5386630269401489641L, "enchantment.sweeping_edge");
        add(-7949229004988660584L, -7828611303000832459L, "effect.speed");
        add(-8650171790042118250L, -5749650997644763080L, "enchantment.soul_speed");
        add(551224288813600377L, -8734740027710371860L, "enchantment.respiration");
        add(-7046399332347654691L, -6723081531683397260L, "suffocating");
        add(7361814797886573596L, -8641397326606817395L, "sprinting");
        add(-6972338111383059132L, -8978659762232839026L, "armor.chestplate");
        add(-5371971015925809039L, -6062243582569928137L, "enchantment.fire_protection");
        add(7245570952092733273L, -8449101711440750679L, "effect.strength");
        add(-422425648963762075L, -5756800103171642205L, "base_attack_speed");
        add(-4607081316629330256L, -7008565754814018370L, "effect.jump_boost");
        add(271280981090454338L, -8746077033958322898L, "effect.luck");
        add(2211131075215181206L, -5513857709499300658L, "powder_snow");
        add(-8908768238899017377L, -8313820693701227669L, "armor.boots");
        add(-5797418877589107702L, -6181652684028920077L, "effect.haste");
        add(3086076556416732775L, -5150312587563650736L, "armor.helmet");
        add(-5082757096938257406L, -4891139119377885130L, "baby");
        add(2478452629826324956L, -7247530463494186011L, "effect.weakness");
        add(4659420831966187055L, -5191473055587376048L, "enchantment.blast_protection");
        add(7301951777949303281L, -6753860660653972126L, "evil");
        add(8533189226688352746L, -8254757081029716377L, "baby");
        add(1286946037536540352L, -5768092872487507967L, "enchantment.depth_strider");
        add(-3801225194067177672L, -6586624321849018929L, "base_attack_damage");
    }

    public static @Nullable String uuidToId(final UUID uuid) {
        return ATTRIBUTE_MODIFIER_IDS.get(uuid);
    }

    public static @Nullable UUID idToUuid(final String id) {
        return ATTRIBUTE_MODIFIER_INVERSE_IDS.get(Key.stripMinecraftNamespace(id));
    }

    public static @Nullable String nameToId(final String name) {
        return ATTRIBUTE_MODIFIER_NAMES.get(name);
    }

    public static @Nullable String idToName(final String id) {
        return ATTRIBUTE_MODIFIER_NAMES.inverse().get(Key.stripMinecraftNamespace(id));
    }

    private static void add(final long msb, final long lsb, final String id) {
        final UUID uuid = new UUID(msb, lsb);
        ATTRIBUTE_MODIFIER_IDS.put(uuid, id);
        ATTRIBUTE_MODIFIER_INVERSE_IDS.put(id, uuid);
    }
}
