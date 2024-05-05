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
package com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.data;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;

public final class DamageTypes1_20_3 {

    private static final Object2ObjectMap<String, CompoundTag> DAMAGE_TYPES = new Object2ObjectOpenHashMap<>();

    static {
        DAMAGE_TYPES.put("in_fire", create("inFire", 0.1F));
        DAMAGE_TYPES.put("lightning_bolt", create("lightningBolt", 0.1F));
        DAMAGE_TYPES.put("on_fire", create("onFire", 0.0F, "burning"));
        DAMAGE_TYPES.put("lava", create("lava", 0.1F, "burning"));
        DAMAGE_TYPES.put("hot_floor", create("hotFloor", 0.1F, "burning"));
        DAMAGE_TYPES.put("in_wall", create("inWall", 0.0F));
        DAMAGE_TYPES.put("cramming", create("cramming", 0.0F));
        DAMAGE_TYPES.put("drown", create("drown", 0.0F, "drowning"));
        DAMAGE_TYPES.put("starve", create("starve", 0.0F));
        DAMAGE_TYPES.put("cactus", create("cactus", 0.1F));
        DAMAGE_TYPES.put("fall", create("fall", "when_caused_by_living_non_player", 0.0F, null, "fall_variants"));
        DAMAGE_TYPES.put("fly_into_wall", create("flyIntoWall", 0.0F));
        DAMAGE_TYPES.put("out_of_world", create("outOfWorld", 0.0F));
        DAMAGE_TYPES.put("generic", create("generic", 0.0F));
        DAMAGE_TYPES.put("magic", create("magic", 0.0F));
        DAMAGE_TYPES.put("wither", create("wither", 0.0F));
        DAMAGE_TYPES.put("dragon_breath", create("dragonBreath", 0.0F));
        DAMAGE_TYPES.put("dry_out", create("dryout", 0.1F));
        DAMAGE_TYPES.put("sweet_berry_bush", create("sweetBerryBush", 0.1F, "poking"));
        DAMAGE_TYPES.put("freeze", create("freeze", 0.0F, "freezing"));
        DAMAGE_TYPES.put("stalagmite", create("stalagmite", 0.0F));
        DAMAGE_TYPES.put("falling_block", create("fallingBlock", 0.1F));
        DAMAGE_TYPES.put("falling_anvil", create("anvil", 0.1F));
        DAMAGE_TYPES.put("falling_stalactite", create("fallingStalactite", 0.1F));
        DAMAGE_TYPES.put("sting", create("sting", 0.1F));
        DAMAGE_TYPES.put("mob_attack", create("mob", 0.1F));
        DAMAGE_TYPES.put("mob_attack_no_aggro", create("mob", 0.1F));
        DAMAGE_TYPES.put("player_attack", create("player", 0.1F));
        DAMAGE_TYPES.put("arrow", create("arrow", 0.1F));
        DAMAGE_TYPES.put("trident", create("trident", 0.1F));
        DAMAGE_TYPES.put("mob_projectile", create("mob", 0.1F));
        DAMAGE_TYPES.put("fireworks", create("fireworks", 0.1F));
        DAMAGE_TYPES.put("unattributed_fireball", create("onFire", 0.1F, "burning"));
        DAMAGE_TYPES.put("fireball", create("fireball", 0.1F, "burning"));
        DAMAGE_TYPES.put("wither_skull", create("witherSkull", 0.1F));
        DAMAGE_TYPES.put("thrown", create("thrown", 0.1F));
        DAMAGE_TYPES.put("indirect_magic", create("indirectMagic", 0.0F));
        DAMAGE_TYPES.put("thorns", create("thorns", 0.1F, "thorns"));
        DAMAGE_TYPES.put("explosion", create("explosion", "always", 0.1F));
        DAMAGE_TYPES.put("player_explosion", create("explosion.player", "always", 0.1F));
        DAMAGE_TYPES.put("sonic_boom", create("sonic_boom", "always", 0.0F));
        DAMAGE_TYPES.put("bad_respawn_point", create("badRespawnPoint", "always", 0.1F, "hurt", "intentional_game_design"));
        DAMAGE_TYPES.put("outside_border", create("outsideBorder", 0.0F));
        DAMAGE_TYPES.put("generic_kill", create("genericKill", 0.0F));
    }

    public static CompoundTag get(String key) {
        return DAMAGE_TYPES.get(key);
    }

    public static ObjectSet<String> keys() {
        return DAMAGE_TYPES.keySet();
    }

    private static CompoundTag create(String messageId, String scaling, float exhaustion) {
        return create(messageId, scaling, exhaustion, null, null);
    }

    private static CompoundTag create(String messageId, float exhaustion, String effects) {
        return create(messageId, "when_caused_by_living_non_player", exhaustion, effects, null);
    }

    private static CompoundTag create(String messageId, float exhaustion) {
        return create(messageId, "when_caused_by_living_non_player", exhaustion);
    }

    private static CompoundTag create(String messageId, String scaling, float exhaustion, String damageEffects, String deathMessageType) {
        final CompoundTag tag = new CompoundTag();
        tag.putString("message_id", messageId);
        tag.putString("scaling", scaling);
        tag.putFloat("exhaustion", exhaustion);
        if (damageEffects != null) {
            tag.putString("effects", damageEffects);
        }
        if (deathMessageType != null) {
            tag.putString("death_message_type", deathMessageType);
        }

        return tag;
    }
}
