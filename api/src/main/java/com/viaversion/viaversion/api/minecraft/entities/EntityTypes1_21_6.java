/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
import com.viaversion.viaversion.util.Key;
import java.util.Locale;
import org.checkerframework.checker.nullness.qual.Nullable;

public enum EntityTypes1_21_6 implements EntityType {

    ENTITY(null, null),

    AREA_EFFECT_CLOUD(ENTITY),
    END_CRYSTAL(ENTITY),
    EVOKER_FANGS(ENTITY),
    EXPERIENCE_ORB(ENTITY),
    EYE_OF_ENDER(ENTITY),
    FALLING_BLOCK(ENTITY),
    ITEM(ENTITY),
    TNT(ENTITY),
    OMINOUS_ITEM_SPAWNER(ENTITY),
    MARKER(ENTITY),
    LIGHTNING_BOLT(ENTITY),
    INTERACTION(ENTITY),

    DISPLAY(ENTITY, null),
    BLOCK_DISPLAY(DISPLAY),
    ITEM_DISPLAY(DISPLAY),
    TEXT_DISPLAY(DISPLAY),

    // Hanging entities
    HANGING_ENTITY(ENTITY, null),
    LEASH_KNOT(HANGING_ENTITY),
    PAINTING(HANGING_ENTITY),
    ITEM_FRAME(HANGING_ENTITY),
    GLOW_ITEM_FRAME(ITEM_FRAME),

    // Projectiles
    PROJECTILE(ENTITY, null),
    ITEM_PROJECTILE(PROJECTILE, null),
    SNOWBALL(ITEM_PROJECTILE),
    ENDER_PEARL(ITEM_PROJECTILE),
    EGG(ITEM_PROJECTILE),
    EXPERIENCE_BOTTLE(ITEM_PROJECTILE),
    FIREWORK_ROCKET(PROJECTILE),
    LLAMA_SPIT(PROJECTILE),
    SHULKER_BULLET(PROJECTILE),
    FISHING_BOBBER(PROJECTILE),
    WITHER_SKULL(PROJECTILE),
    DRAGON_FIREBALL(PROJECTILE), // Doesn't actually inherit fireball

    ABSTRACT_THROWN_POTION(ITEM_PROJECTILE, null),
    SPLASH_POTION(ABSTRACT_THROWN_POTION),
    LINGERING_POTION(ABSTRACT_THROWN_POTION),

    ABSTRACT_ARROW(PROJECTILE, null),
    ARROW(ABSTRACT_ARROW),
    SPECTRAL_ARROW(ABSTRACT_ARROW),
    TRIDENT(ABSTRACT_ARROW),

    ABSTRACT_FIREBALL(ENTITY, null),
    FIREBALL(ABSTRACT_FIREBALL),
    SMALL_FIREBALL(ABSTRACT_FIREBALL),

    ABSTRACT_WIND_CHARGE(PROJECTILE, null),
    WIND_CHARGE(ABSTRACT_WIND_CHARGE),
    BREEZE_WIND_CHARGE(ABSTRACT_WIND_CHARGE),

    // Vehicles
    VEHICLE(ENTITY, null),

    ABSTRACT_MINECART(VEHICLE, null),
    MINECART(ABSTRACT_MINECART),
    FURNACE_MINECART(ABSTRACT_MINECART),
    COMMAND_BLOCK_MINECART(ABSTRACT_MINECART),
    TNT_MINECART(ABSTRACT_MINECART),
    SPAWNER_MINECART(ABSTRACT_MINECART),

    ABSTRACT_MINECART_CONTAINER(ABSTRACT_MINECART, null),
    CHEST_MINECART(ABSTRACT_MINECART_CONTAINER),
    HOPPER_MINECART(ABSTRACT_MINECART_CONTAINER),

    ABSTRACT_BOAT(VEHICLE, null),
    ABSTRACT_CHEST_BOAT(ABSTRACT_BOAT, null),
    ACACIA_BOAT(ABSTRACT_BOAT),
    ACACIA_CHEST_BOAT(ABSTRACT_CHEST_BOAT),
    BAMBOO_CHEST_RAFT(ABSTRACT_CHEST_BOAT),
    BAMBOO_RAFT(ABSTRACT_BOAT),
    BIRCH_BOAT(ABSTRACT_BOAT),
    BIRCH_CHEST_BOAT(ABSTRACT_CHEST_BOAT),
    CHERRY_BOAT(ABSTRACT_BOAT),
    CHERRY_CHEST_BOAT(ABSTRACT_CHEST_BOAT),
    DARK_OAK_BOAT(ABSTRACT_BOAT),
    DARK_OAK_CHEST_BOAT(ABSTRACT_CHEST_BOAT),
    JUNGLE_BOAT(ABSTRACT_BOAT),
    JUNGLE_CHEST_BOAT(ABSTRACT_CHEST_BOAT),
    MANGROVE_BOAT(ABSTRACT_BOAT),
    MANGROVE_CHEST_BOAT(ABSTRACT_CHEST_BOAT),
    OAK_BOAT(ABSTRACT_BOAT),
    OAK_CHEST_BOAT(ABSTRACT_CHEST_BOAT),
    PALE_OAK_BOAT(ABSTRACT_BOAT),
    PALE_OAK_CHEST_BOAT(ABSTRACT_CHEST_BOAT),
    SPRUCE_BOAT(ABSTRACT_BOAT),
    SPRUCE_CHEST_BOAT(ABSTRACT_CHEST_BOAT),

    // Living entities as a larger subclass
    LIVING_ENTITY(ENTITY, null),
    ARMOR_STAND(LIVING_ENTITY),
    PLAYER(LIVING_ENTITY),

    // Mobs as a larger subclass
    MOB(LIVING_ENTITY, null),
    ENDER_DRAGON(MOB),

    SLIME(MOB),
    MAGMA_CUBE(SLIME),

    // Ambient mobs
    AMBIENT_CREATURE(MOB, null),
    BAT(AMBIENT_CREATURE),

    // Flying mobs
    FLYING_MOB(MOB, null),
    GHAST(FLYING_MOB),
    PHANTOM(FLYING_MOB),

    // Pathfinder mobs and its subclasses
    PATHFINDER_MOB(MOB, null),
    ALLAY(PATHFINDER_MOB),

    ABSTRACT_GOLEM(PATHFINDER_MOB, null),
    SNOW_GOLEM(ABSTRACT_GOLEM),
    IRON_GOLEM(ABSTRACT_GOLEM),
    SHULKER(ABSTRACT_GOLEM),

    // Ageable mobs and (tamable) animals
    ABSTRACT_AGEABLE(PATHFINDER_MOB, null),
    ABSTRACT_VILLAGER(ABSTRACT_AGEABLE, null),
    VILLAGER(ABSTRACT_VILLAGER),
    WANDERING_TRADER(ABSTRACT_VILLAGER),

    ABSTRACT_ANIMAL(ABSTRACT_AGEABLE, null),
    AXOLOTL(ABSTRACT_ANIMAL),
    CHICKEN(ABSTRACT_ANIMAL),
    PANDA(ABSTRACT_ANIMAL),
    PIG(ABSTRACT_ANIMAL),
    POLAR_BEAR(ABSTRACT_ANIMAL),
    RABBIT(ABSTRACT_ANIMAL),
    SHEEP(ABSTRACT_ANIMAL),
    BEE(ABSTRACT_ANIMAL),
    TURTLE(ABSTRACT_ANIMAL),
    FOX(ABSTRACT_ANIMAL),
    FROG(ABSTRACT_ANIMAL),
    GOAT(ABSTRACT_ANIMAL),
    HOGLIN(ABSTRACT_ANIMAL),
    STRIDER(ABSTRACT_ANIMAL),
    SNIFFER(ABSTRACT_ANIMAL),
    ARMADILLO(ABSTRACT_ANIMAL),
    HAPPY_GHAST(ABSTRACT_ANIMAL),

    ABSTRACT_COW(ABSTRACT_ANIMAL, null),
    COW(ABSTRACT_COW),
    MOOSHROOM(ABSTRACT_COW),

    TAMABLE_ANIMAL(ABSTRACT_ANIMAL, null),
    CAT(TAMABLE_ANIMAL),
    OCELOT(TAMABLE_ANIMAL),
    WOLF(TAMABLE_ANIMAL),
    PARROT(TAMABLE_ANIMAL),

    ABSTRACT_HORSE(ABSTRACT_ANIMAL, null),
    HORSE(ABSTRACT_HORSE),
    SKELETON_HORSE(ABSTRACT_HORSE),
    ZOMBIE_HORSE(ABSTRACT_HORSE),
    CAMEL(ABSTRACT_HORSE),

    ABSTRACT_CHESTED_HORSE(ABSTRACT_HORSE, null),
    DONKEY(ABSTRACT_CHESTED_HORSE),
    MULE(ABSTRACT_CHESTED_HORSE),
    LLAMA(ABSTRACT_CHESTED_HORSE),
    TRADER_LLAMA(LLAMA),

    // Monsters
    ABSTRACT_MONSTER(PATHFINDER_MOB, null),
    BLAZE(ABSTRACT_MONSTER),
    CREEPER(ABSTRACT_MONSTER),
    ENDERMITE(ABSTRACT_MONSTER),
    ENDERMAN(ABSTRACT_MONSTER),
    GIANT(ABSTRACT_MONSTER),
    SILVERFISH(ABSTRACT_MONSTER),
    VEX(ABSTRACT_MONSTER),
    WITHER(ABSTRACT_MONSTER),
    BREEZE(ABSTRACT_MONSTER),
    ZOGLIN(ABSTRACT_MONSTER),
    WARDEN(ABSTRACT_MONSTER),
    CREAKING(ABSTRACT_MONSTER),

    ABSTRACT_SKELETON(ABSTRACT_MONSTER, null),
    SKELETON(ABSTRACT_SKELETON),
    STRAY(ABSTRACT_SKELETON),
    WITHER_SKELETON(ABSTRACT_SKELETON),
    BOGGED(ABSTRACT_SKELETON),

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

    // Water mobs
    AGEABLE_WATER_CREATURE(ABSTRACT_AGEABLE, null),
    DOLPHIN(AGEABLE_WATER_CREATURE),

    SQUID(AGEABLE_WATER_CREATURE),
    GLOW_SQUID(SQUID),

    WATER_ANIMAL(PATHFINDER_MOB, null),
    ABSTRACT_FISH(WATER_ANIMAL, null),
    PUFFERFISH(ABSTRACT_FISH),
    TADPOLE(ABSTRACT_FISH),

    ABSTRACT_SCHOOLING_FISH(ABSTRACT_FISH, null),
    COD(ABSTRACT_SCHOOLING_FISH),
    SALMON(ABSTRACT_SCHOOLING_FISH),
    TROPICAL_FISH(ABSTRACT_SCHOOLING_FISH),

    // Raiders
    ABSTRACT_RAIDER(ABSTRACT_MONSTER, null),
    WITCH(ABSTRACT_RAIDER),
    RAVAGER(ABSTRACT_RAIDER),

    ABSTRACT_ILLAGER(ABSTRACT_RAIDER, null),
    SPELLCASTER_ILLAGER(ABSTRACT_ILLAGER, null),
    VINDICATOR(ABSTRACT_ILLAGER),
    PILLAGER(ABSTRACT_ILLAGER),
    EVOKER(SPELLCASTER_ILLAGER),
    ILLUSIONER(SPELLCASTER_ILLAGER);

    private static final EntityType[] TYPES = EntityTypeUtil.createSizedArray(values());
    private final EntityType parent;
    private final String identifier;
    private int id = -1;

    EntityTypes1_21_6(final EntityType parent) {
        this.parent = parent;
        this.identifier = Key.namespaced(name().toLowerCase(Locale.ROOT));
    }

    EntityTypes1_21_6(final EntityType parent, @Nullable final String identifier) {
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
        return EntityTypeUtil.getTypeFromId(TYPES, typeId, PIG);
    }

    public static void initialize(final Protocol<?, ?, ?, ?> protocol) {
        EntityTypeUtil.initialize(values(), TYPES, protocol, (type, id) -> type.id = id);
    }
}
