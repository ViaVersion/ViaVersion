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
package com.viaversion.viaversion.api.minecraft.entities;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.util.EntityTypeUtil;
import java.util.Locale;
import org.checkerframework.checker.nullness.qual.Nullable;

public enum EntityTypes1_16_2 implements EntityType {

    ENTITY(null, null),

    AREA_EFFECT_CLOUD(ENTITY),
    END_CRYSTAL(ENTITY),
    EVOKER_FANGS(ENTITY),
    EXPERIENCE_ORB(ENTITY),
    EYE_OF_ENDER(ENTITY),
    FALLING_BLOCK(ENTITY),
    ITEM(ENTITY),
    TNT(ENTITY),
    LIGHTNING_BOLT(ENTITY),

    // Hanging entities
    ABSTRACT_HANGING(ENTITY, null),
    LEASH_KNOT(ABSTRACT_HANGING),
    PAINTING(ABSTRACT_HANGING),
    ITEM_FRAME(ABSTRACT_HANGING),

    // Projectiles
    PROJECTILE_ABSTRACT(ENTITY, null), // Not actually its own abstract type, but useful for categorizing
    SNOWBALL(PROJECTILE_ABSTRACT),
    ENDER_PEARL(PROJECTILE_ABSTRACT),
    EGG(PROJECTILE_ABSTRACT),
    POTION(PROJECTILE_ABSTRACT),
    EXPERIENCE_BOTTLE(PROJECTILE_ABSTRACT),
    FIREWORK_ROCKET(PROJECTILE_ABSTRACT),
    LLAMA_SPIT(PROJECTILE_ABSTRACT),
    SHULKER_BULLET(PROJECTILE_ABSTRACT),
    FISHING_BOBBER(PROJECTILE_ABSTRACT),
    WITHER_SKULL(PROJECTILE_ABSTRACT),
    DRAGON_FIREBALL(PROJECTILE_ABSTRACT), // Doesn't actually inherit fireball

    ABSTRACT_ARROW(PROJECTILE_ABSTRACT, null),
    ARROW(ABSTRACT_ARROW),
    SPECTRAL_ARROW(ABSTRACT_ARROW),
    TRIDENT(ABSTRACT_ARROW),

    ABSTRACT_FIREBALL(ENTITY, null),
    FIREBALL(ABSTRACT_FIREBALL),
    SMALL_FIREBALL(ABSTRACT_FIREBALL),

    // Vehicles
    VEHICLE(ENTITY, null),
    BOAT(VEHICLE),

    MINECART_ABSTRACT(VEHICLE, null),
    MINECART(MINECART_ABSTRACT),
    FURNACE_MINECART(MINECART_ABSTRACT),
    COMMAND_BLOCK_MINECART(MINECART_ABSTRACT),
    TNT_MINECART(MINECART_ABSTRACT),
    SPAWNER_MINECART(MINECART_ABSTRACT),

    CHESTED_MINECART_ABSTRACT(MINECART_ABSTRACT, null),
    CHEST_MINECART(CHESTED_MINECART_ABSTRACT),
    HOPPER_MINECART(CHESTED_MINECART_ABSTRACT),

    // Living entities as a larger subclass
    LIVINGENTITY(ENTITY, null),
    ARMOR_STAND(LIVINGENTITY),
    PLAYER(LIVINGENTITY),

    // Mobs as a larger subclass
    ABSTRACT_INSENTIENT(LIVINGENTITY, null),
    ENDER_DRAGON(ABSTRACT_INSENTIENT),

    SLIME(ABSTRACT_INSENTIENT),
    MAGMA_CUBE(SLIME),

    // Ambient mobs
    ABSTRACT_AMBIENT(ABSTRACT_INSENTIENT, null),
    BAT(ABSTRACT_AMBIENT),

    // Flying mobs
    ABSTRACT_FLYING(ABSTRACT_INSENTIENT, null),
    GHAST(ABSTRACT_FLYING),
    PHANTOM(ABSTRACT_FLYING),

    // Pathfinder mobs and its subclasses
    ABSTRACT_CREATURE(ABSTRACT_INSENTIENT, null),

    ABSTRACT_GOLEM(ABSTRACT_CREATURE, null),
    SNOW_GOLEM(ABSTRACT_GOLEM),
    IRON_GOLEM(ABSTRACT_GOLEM),
    SHULKER(ABSTRACT_GOLEM),

    // Water mobs
    ABSTRACT_WATERMOB(ABSTRACT_CREATURE, null),
    DOLPHIN(ABSTRACT_WATERMOB),
    SQUID(ABSTRACT_WATERMOB),

    ABSTRACT_FISHES(ABSTRACT_WATERMOB, null),
    PUFFERFISH(ABSTRACT_FISHES),

    ABSTRACT_SCHOOLING_FISH(ABSTRACT_FISHES, null),
    COD(ABSTRACT_SCHOOLING_FISH),
    SALMON(ABSTRACT_SCHOOLING_FISH),
    TROPICAL_FISH(ABSTRACT_SCHOOLING_FISH),

    // Ageable mobs and (tamable) animals
    ABSTRACT_AGEABLE(ABSTRACT_CREATURE, null),
    ABSTRACT_VILLAGER(ABSTRACT_AGEABLE, null),
    VILLAGER(ABSTRACT_VILLAGER),
    WANDERING_TRADER(ABSTRACT_VILLAGER),

    ABSTRACT_ANIMAL(ABSTRACT_AGEABLE, null),
    CHICKEN(ABSTRACT_ANIMAL),
    PANDA(ABSTRACT_ANIMAL),
    PIG(ABSTRACT_ANIMAL),
    POLAR_BEAR(ABSTRACT_ANIMAL),
    RABBIT(ABSTRACT_ANIMAL),
    SHEEP(ABSTRACT_ANIMAL),
    BEE(ABSTRACT_ANIMAL),
    TURTLE(ABSTRACT_ANIMAL),
    FOX(ABSTRACT_ANIMAL),
    HOGLIN(ABSTRACT_ANIMAL),
    STRIDER(ABSTRACT_ANIMAL),

    COW(ABSTRACT_ANIMAL),
    MOOSHROOM(COW),

    ABSTRACT_TAMEABLE_ANIMAL(ABSTRACT_ANIMAL, null),
    CAT(ABSTRACT_TAMEABLE_ANIMAL),
    OCELOT(ABSTRACT_TAMEABLE_ANIMAL),
    WOLF(ABSTRACT_TAMEABLE_ANIMAL),
    PARROT(ABSTRACT_TAMEABLE_ANIMAL),

    ABSTRACT_HORSE(ABSTRACT_ANIMAL, null),
    HORSE(ABSTRACT_HORSE),
    SKELETON_HORSE(ABSTRACT_HORSE),
    ZOMBIE_HORSE(ABSTRACT_HORSE),

    CHESTED_HORSE(ABSTRACT_HORSE, null),
    DONKEY(CHESTED_HORSE),
    MULE(CHESTED_HORSE),
    LLAMA(CHESTED_HORSE),
    TRADER_LLAMA(LLAMA),

    // Monsters
    ABSTRACT_MONSTER(ABSTRACT_CREATURE, null),
    BLAZE(ABSTRACT_MONSTER),
    CREEPER(ABSTRACT_MONSTER),
    ENDERMITE(ABSTRACT_MONSTER),
    ENDERMAN(ABSTRACT_MONSTER),
    GIANT(ABSTRACT_MONSTER),
    SILVERFISH(ABSTRACT_MONSTER),
    VEX(ABSTRACT_MONSTER),
    WITHER(ABSTRACT_MONSTER),
    ZOGLIN(ABSTRACT_MONSTER),

    ABSTRACT_SKELETON(ABSTRACT_MONSTER, null),
    SKELETON(ABSTRACT_SKELETON),
    STRAY(ABSTRACT_SKELETON),
    WITHER_SKELETON(ABSTRACT_SKELETON),

    ZOMBIE(ABSTRACT_MONSTER),
    DROWNED(ZOMBIE),
    HUSK(ZOMBIE),
    ZOMBIFIED_PIGLIN(ZOMBIE),
    ZOMBIE_VILLAGER(ZOMBIE),

    GUARDIAN(ABSTRACT_MONSTER),
    ELDER_GUARDIAN(GUARDIAN),

    SPIDER(ABSTRACT_MONSTER),
    CAVE_SPIDER(SPIDER),

    ABSTRACT_PIGLIN(ABSTRACT_MONSTER, null),
    PIGLIN(ABSTRACT_PIGLIN),
    PIGLIN_BRUTE(ABSTRACT_PIGLIN),

    // Raiders
    ABSTRACT_RAIDER(ABSTRACT_MONSTER, null),
    WITCH(ABSTRACT_RAIDER),
    RAVAGER(ABSTRACT_RAIDER),

    ABSTRACT_ILLAGER_BASE(ABSTRACT_RAIDER, null),
    ABSTRACT_EVO_ILLU_ILLAGER(ABSTRACT_ILLAGER_BASE, null),
    VINDICATOR(ABSTRACT_ILLAGER_BASE),
    PILLAGER(ABSTRACT_ILLAGER_BASE),
    EVOKER(ABSTRACT_EVO_ILLU_ILLAGER),
    ILLUSIONER(ABSTRACT_EVO_ILLU_ILLAGER);

    private static final EntityType[] TYPES = EntityTypeUtil.createSizedArray(values());
    private final EntityType parent;
    private final String identifier;
    private int id = -1;

    EntityTypes1_16_2(final EntityType parent) {
        this.parent = parent;
        this.identifier = "minecraft:" + name().toLowerCase(Locale.ROOT);
    }

    EntityTypes1_16_2(final EntityType parent, @Nullable final String identifier) {
        this.parent = parent;
        this.identifier = identifier;
    }

    @Override
    public int getId() {
        if (id == -1) {
            throw new IllegalStateException("Ids have not been initialized yet (type " + name() + ")");
        }
        return id;
    }

    @Override
    public String identifier() {
        Preconditions.checkArgument(identifier != null, "Called identifier method on abstract type");
        return identifier;
    }

    @Override
    public @Nullable EntityType getParent() {
        return parent;
    }

    @Override
    public boolean isAbstractType() {
        return identifier == null;
    }

    public static EntityType getTypeFromId(final int typeId) {
        return EntityTypeUtil.getTypeFromId(TYPES, typeId, ENTITY);
    }

    public static void initialize(final Protocol<?, ?, ?, ?> protocol) {
        EntityTypeUtil.initialize(values(), TYPES, protocol, (type, id) -> type.id = id);
    }
}
