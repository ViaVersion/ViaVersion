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

public enum EntityTypes1_19_3 implements EntityType {

    ENTITY(null, null),

    AREA_EFFECT_CLOUD(ENTITY),
    END_CRYSTAL(ENTITY),
    EVOKER_FANGS(ENTITY),
    EXPERIENCE_ORB(ENTITY),
    EYE_OF_ENDER(ENTITY),
    FALLING_BLOCK(ENTITY),
    FIREWORK_ROCKET(ENTITY),
    ITEM(ENTITY),
    LLAMA_SPIT(ENTITY),
    TNT(ENTITY),
    SHULKER_BULLET(ENTITY),
    FISHING_BOBBER(ENTITY),

    LIVINGENTITY(ENTITY, null),
    ARMOR_STAND(LIVINGENTITY),
    MARKER(ENTITY),
    PLAYER(LIVINGENTITY),

    ABSTRACT_INSENTIENT(LIVINGENTITY, null),
    ENDER_DRAGON(ABSTRACT_INSENTIENT),

    BEE(ABSTRACT_INSENTIENT),

    ABSTRACT_CREATURE(ABSTRACT_INSENTIENT, null),

    ABSTRACT_AGEABLE(ABSTRACT_CREATURE, null),
    VILLAGER(ABSTRACT_AGEABLE),
    WANDERING_TRADER(ABSTRACT_AGEABLE),

    // Animals
    ABSTRACT_ANIMAL(ABSTRACT_AGEABLE, null),
    AXOLOTL(ABSTRACT_ANIMAL),
    DOLPHIN(ABSTRACT_INSENTIENT),
    CHICKEN(ABSTRACT_ANIMAL),
    COW(ABSTRACT_ANIMAL),
    MOOSHROOM(COW),
    PANDA(ABSTRACT_INSENTIENT),
    PIG(ABSTRACT_ANIMAL),
    POLAR_BEAR(ABSTRACT_ANIMAL),
    RABBIT(ABSTRACT_ANIMAL),
    SHEEP(ABSTRACT_ANIMAL),
    TURTLE(ABSTRACT_ANIMAL),
    FOX(ABSTRACT_ANIMAL),
    FROG(ABSTRACT_ANIMAL),
    GOAT(ABSTRACT_ANIMAL),

    ABSTRACT_TAMEABLE_ANIMAL(ABSTRACT_ANIMAL, null),
    CAT(ABSTRACT_TAMEABLE_ANIMAL),
    OCELOT(ABSTRACT_TAMEABLE_ANIMAL),
    WOLF(ABSTRACT_TAMEABLE_ANIMAL),

    ABSTRACT_PARROT(ABSTRACT_TAMEABLE_ANIMAL, null),
    PARROT(ABSTRACT_PARROT),

    // Horses
    ABSTRACT_HORSE(ABSTRACT_ANIMAL, null),
    CHESTED_HORSE(ABSTRACT_HORSE, null),
    DONKEY(CHESTED_HORSE),
    MULE(CHESTED_HORSE),
    LLAMA(CHESTED_HORSE),
    TRADER_LLAMA(CHESTED_HORSE),
    HORSE(ABSTRACT_HORSE),
    SKELETON_HORSE(ABSTRACT_HORSE),
    ZOMBIE_HORSE(ABSTRACT_HORSE),
    CAMEL(ABSTRACT_HORSE),

    // Golem
    ABSTRACT_GOLEM(ABSTRACT_CREATURE, null),
    SNOW_GOLEM(ABSTRACT_GOLEM),
    IRON_GOLEM(ABSTRACT_GOLEM),
    SHULKER(ABSTRACT_GOLEM),

    // Fish
    ABSTRACT_FISHES(ABSTRACT_CREATURE, null),
    COD(ABSTRACT_FISHES),
    PUFFERFISH(ABSTRACT_FISHES),
    SALMON(ABSTRACT_FISHES),
    TROPICAL_FISH(ABSTRACT_FISHES),

    // Monsters
    ABSTRACT_MONSTER(ABSTRACT_CREATURE, null),
    BLAZE(ABSTRACT_MONSTER),
    CREEPER(ABSTRACT_MONSTER),
    ENDERMITE(ABSTRACT_MONSTER),
    ENDERMAN(ABSTRACT_MONSTER),
    GIANT(ABSTRACT_MONSTER),
    SILVERFISH(ABSTRACT_MONSTER),
    VEX(ABSTRACT_MONSTER),
    WITCH(ABSTRACT_MONSTER),
    WITHER(ABSTRACT_MONSTER),
    RAVAGER(ABSTRACT_MONSTER),

    ABSTRACT_PIGLIN(ABSTRACT_MONSTER, null),

    PIGLIN(ABSTRACT_PIGLIN),
    PIGLIN_BRUTE(ABSTRACT_PIGLIN),

    HOGLIN(ABSTRACT_ANIMAL),
    STRIDER(ABSTRACT_ANIMAL),
    TADPOLE(ABSTRACT_FISHES),
    ZOGLIN(ABSTRACT_MONSTER),
    WARDEN(ABSTRACT_MONSTER),

    // Illagers
    ABSTRACT_ILLAGER_BASE(ABSTRACT_MONSTER, null),
    ABSTRACT_EVO_ILLU_ILLAGER(ABSTRACT_ILLAGER_BASE, null),
    EVOKER(ABSTRACT_EVO_ILLU_ILLAGER),
    ILLUSIONER(ABSTRACT_EVO_ILLU_ILLAGER),
    VINDICATOR(ABSTRACT_ILLAGER_BASE),
    PILLAGER(ABSTRACT_ILLAGER_BASE),

    // Skeletons
    ABSTRACT_SKELETON(ABSTRACT_MONSTER, null),
    SKELETON(ABSTRACT_SKELETON),
    STRAY(ABSTRACT_SKELETON),
    WITHER_SKELETON(ABSTRACT_SKELETON),

    // Guardians
    GUARDIAN(ABSTRACT_MONSTER),
    ELDER_GUARDIAN(GUARDIAN),

    // Spiders
    SPIDER(ABSTRACT_MONSTER),
    CAVE_SPIDER(SPIDER),

    // Zombies
    ZOMBIE(ABSTRACT_MONSTER),
    DROWNED(ZOMBIE),
    HUSK(ZOMBIE),
    ZOMBIFIED_PIGLIN(ZOMBIE),
    ZOMBIE_VILLAGER(ZOMBIE),

    // Flying entities
    ABSTRACT_FLYING(ABSTRACT_INSENTIENT, null),
    GHAST(ABSTRACT_FLYING),
    PHANTOM(ABSTRACT_FLYING),

    ABSTRACT_AMBIENT(ABSTRACT_INSENTIENT, null),
    BAT(ABSTRACT_AMBIENT),
    ALLAY(ABSTRACT_CREATURE),

    ABSTRACT_WATERMOB(ABSTRACT_INSENTIENT, null),
    SQUID(ABSTRACT_WATERMOB),
    GLOW_SQUID(SQUID),

    // Slimes
    SLIME(ABSTRACT_INSENTIENT),
    MAGMA_CUBE(SLIME),

    // Hangable objects
    ABSTRACT_HANGING(ENTITY, null),
    LEASH_KNOT(ABSTRACT_HANGING),
    ITEM_FRAME(ABSTRACT_HANGING),
    GLOW_ITEM_FRAME(ITEM_FRAME),
    PAINTING(ABSTRACT_HANGING),

    ABSTRACT_LIGHTNING(ENTITY, null),
    LIGHTNING_BOLT(ABSTRACT_LIGHTNING),

    // Arrows
    ABSTRACT_ARROW(ENTITY, null),
    ARROW(ABSTRACT_ARROW),
    SPECTRAL_ARROW(ABSTRACT_ARROW),
    TRIDENT(ABSTRACT_ARROW),

    // Fireballs
    ABSTRACT_FIREBALL(ENTITY, null),
    DRAGON_FIREBALL(ABSTRACT_FIREBALL),
    FIREBALL(ABSTRACT_FIREBALL),
    SMALL_FIREBALL(ABSTRACT_FIREBALL),
    WITHER_SKULL(ABSTRACT_FIREBALL),

    // Projectiles
    PROJECTILE_ABSTRACT(ENTITY, null),
    SNOWBALL(PROJECTILE_ABSTRACT),
    ENDER_PEARL(PROJECTILE_ABSTRACT),
    EGG(PROJECTILE_ABSTRACT),
    POTION(PROJECTILE_ABSTRACT),
    EXPERIENCE_BOTTLE(PROJECTILE_ABSTRACT),

    // Vehicles
    MINECART_ABSTRACT(ENTITY, null),
    CHESTED_MINECART_ABSTRACT(MINECART_ABSTRACT, null),
    CHEST_MINECART(CHESTED_MINECART_ABSTRACT),
    HOPPER_MINECART(CHESTED_MINECART_ABSTRACT),
    MINECART(MINECART_ABSTRACT),
    FURNACE_MINECART(MINECART_ABSTRACT),
    COMMAND_BLOCK_MINECART(MINECART_ABSTRACT),
    TNT_MINECART(MINECART_ABSTRACT),
    SPAWNER_MINECART(MINECART_ABSTRACT),
    BOAT(ENTITY),
    CHEST_BOAT(BOAT);

    private static final EntityType[] TYPES = EntityTypeUtil.createSizedArray(values());
    private final EntityType parent;
    private final String identifier;
    private int id = -1;

    EntityTypes1_19_3(final EntityType parent) {
        this.parent = parent;
        this.identifier = "minecraft:" + name().toLowerCase(Locale.ROOT);
    }

    EntityTypes1_19_3(final EntityType parent, @Nullable final String identifier) {
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
