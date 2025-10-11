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

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class EntityTypes1_12 {

    public enum EntityType implements com.viaversion.viaversion.api.minecraft.entities.EntityType {

        ENTITY,

        AREA_EFFECT_CLOUD(3, ENTITY),
        END_CRYSTAL(200, ENTITY),
        EVOKER_FANGS(33, ENTITY),
        EXPERIENCE_ORB(2, ENTITY),
        EYE_OF_ENDER(15, ENTITY),
        FALLING_BLOCK(21, ENTITY),
        ITEM(1, ENTITY),
        TNT(20, ENTITY),
        LIGHTNING_BOLT(ENTITY), // Needed for entity (un)tracking

        // Hanging entities
        HANGING_ENTITY(ENTITY),
        LEASH_KNOT(8, HANGING_ENTITY),
        ITEM_FRAME(18, HANGING_ENTITY),
        PAINTING(9, HANGING_ENTITY),

        // Projectiles
        PROJECTILE(ENTITY),
        FIREWORK_ROCKET(22, ENTITY),
        LLAMA_SPIT(104, ENTITY),
        SHULKER_BULLET(25, ENTITY),
        SNOWBALL(11, PROJECTILE),
        ENDER_PEARL(14, PROJECTILE),
        EGG(7, PROJECTILE),
        EXPERIENCE_BOTTLE(17, PROJECTILE),
        POTION(16, PROJECTILE),
        FISHING_HOOK(ENTITY),

        ABSTRACT_ARROW(ENTITY),
        ARROW(10, ABSTRACT_ARROW),
        SPECTRAL_ARROW(24, ABSTRACT_ARROW),

        HURTING_PROJECTILE(ENTITY),
        DRAGON_FIREBALL(26, HURTING_PROJECTILE),
        FIREBALL(12, HURTING_PROJECTILE),
        SMALL_FIREBALL(13, HURTING_PROJECTILE),
        WITHER_SKULL(19, HURTING_PROJECTILE),

        // Vehicles
        BOAT(41, ENTITY),

        ABSTRACT_MINECART(ENTITY),
        MINECART(42, ABSTRACT_MINECART),
        FURNACE_MINECART(44, ABSTRACT_MINECART),
        COMMAND_BLOCK_MINECART(40, ABSTRACT_MINECART),
        TNT_MINECART(45, ABSTRACT_MINECART),
        SPAWNER_MINECART(47, ABSTRACT_MINECART),

        ABSTRACT_MINECART_CONTAINER(ABSTRACT_MINECART),
        CHEST_MINECART(43, ABSTRACT_MINECART_CONTAINER),
        HOPPER_MINECART(46, ABSTRACT_MINECART_CONTAINER),

        LIVING_ENTITY_BASE(ENTITY),
        ARMOR_STAND(30, LIVING_ENTITY_BASE),
        PLAYER(LIVING_ENTITY_BASE), // Needed for entity (un)tracking

        // Living entities as a larger subclass
        LIVING_ENTITY(LIVING_ENTITY_BASE),
        ENDER_DRAGON(63, LIVING_ENTITY),
        ABSTRACT_CREATURE(LIVING_ENTITY),
        SLIME(55, LIVING_ENTITY),
        MAGMA_CUBE(62, SLIME),

        // Flying entities
        FLYING_MOB(LIVING_ENTITY),
        GHAST(56, FLYING_MOB),

        AMBIENT_CREATURE(LIVING_ENTITY),
        BAT(65, AMBIENT_CREATURE),

        ABSTRACT_GOLEM(ABSTRACT_CREATURE),
        SNOW_GOLEM(97, ABSTRACT_GOLEM),
        IRON_GOLEM(99, ABSTRACT_GOLEM),
        SHULKER(69, ABSTRACT_GOLEM),

        WATER_ANIMAL(ABSTRACT_CREATURE),
        SQUID(94, WATER_ANIMAL),

        // Ageable mobs and (tamable) animals
        ABSTRACT_AGEABLE(ABSTRACT_CREATURE),
        VILLAGER(120, ABSTRACT_AGEABLE),

        ABSTRACT_ANIMAL(ABSTRACT_AGEABLE),
        CHICKEN(93, ABSTRACT_ANIMAL),
        COW(92, ABSTRACT_ANIMAL),
        MOOSHROOM(96, COW),
        PIG(90, ABSTRACT_ANIMAL),
        POLAR_BEAR(102, ABSTRACT_ANIMAL),
        RABBIT(101, ABSTRACT_ANIMAL),
        SHEEP(91, ABSTRACT_ANIMAL),

        TAMABLE_ANIMAL(ABSTRACT_ANIMAL),
        OCELOT(98, TAMABLE_ANIMAL),
        WOLF(95, TAMABLE_ANIMAL),

        ABSTRACT_SHOULDER_RIDING(TAMABLE_ANIMAL),
        PARROT(105, ABSTRACT_SHOULDER_RIDING),

        // Horses
        ABSTRACT_HORSE(ABSTRACT_ANIMAL),
        HORSE(100, ABSTRACT_HORSE),
        SKELETON_HORSE(28, ABSTRACT_HORSE),
        ZOMBIE_HORSE(29, ABSTRACT_HORSE),

        CHESTED_HORSE(ABSTRACT_HORSE),
        DONKEY(31, CHESTED_HORSE),
        MULE(32, CHESTED_HORSE),
        LLAMA(103, CHESTED_HORSE),

        // Monsters
        ABSTRACT_MONSTER(ABSTRACT_CREATURE),
        BLAZE(61, ABSTRACT_MONSTER),
        CREEPER(50, ABSTRACT_MONSTER),
        ENDERMITE(67, ABSTRACT_MONSTER),
        ENDERMAN(58, ABSTRACT_MONSTER),
        GIANT(53, ABSTRACT_MONSTER),
        SILVERFISH(60, ABSTRACT_MONSTER),
        VEX(35, ABSTRACT_MONSTER),
        WITCH(66, ABSTRACT_MONSTER),
        WITHER(64, ABSTRACT_MONSTER),

        ABSTRACT_SKELETON(ABSTRACT_MONSTER),
        SKELETON(51, ABSTRACT_SKELETON),
        STRAY(6, ABSTRACT_SKELETON),
        WITHER_SKELETON(5, ABSTRACT_SKELETON),

        ZOMBIE(54, ABSTRACT_MONSTER),
        HUSK(23, ZOMBIE),
        ZOMBIE_PIGMEN(57, ZOMBIE),
        ZOMBIE_VILLAGER(27, ZOMBIE),

        GUARDIAN(68, ABSTRACT_MONSTER),
        ELDER_GUARDIAN(4, GUARDIAN),
        SPIDER(52, ABSTRACT_MONSTER),
        CAVE_SPIDER(59, ABSTRACT_MONSTER),

        // Illagers
        ABSTRACT_ILLAGER(ABSTRACT_MONSTER),
        SPELLCASTER_ILLAGER(ABSTRACT_ILLAGER),
        VINDICATOR(36, ABSTRACT_ILLAGER),
        EVOKER(34, SPELLCASTER_ILLAGER),
        ILLUSIONER(37, SPELLCASTER_ILLAGER);

        private static final Int2ObjectMap<EntityType> TYPES;

        private final int id;
        private final EntityType parent;

        EntityType() {
            this.id = -1;
            this.parent = null;
        }

        EntityType(EntityType parent) {
            this.id = -1;
            this.parent = parent;
        }

        EntityType(int id, EntityType parent) {
            this.id = id;
            this.parent = parent;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public EntityType getParent() {
            return parent;
        }

        @Override
        public String identifier() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isAbstractType() {
            return id != -1;
        }

        static {
            final EntityType[] values = EntityType.values();
            TYPES = new Int2ObjectOpenHashMap<>(values.length);
            for (EntityType type : values) {
                TYPES.put(type.id, type);
            }
        }

        public static EntityType findById(final int id) {
            if (id == -1) {
                return null;
            }
            return TYPES.get(id);
        }
    }

    public enum ObjectType implements com.viaversion.viaversion.api.minecraft.entities.ObjectType {
        BOAT(1, EntityType.BOAT),
        ITEM(2, EntityType.ITEM),
        AREA_EFFECT_CLOUD(3, EntityType.AREA_EFFECT_CLOUD),
        MINECART(10, EntityType.MINECART),
        CHEST_MINECART(10, 1, EntityType.CHEST_MINECART),
        FURNACE_MINECART(10, 2, EntityType.FURNACE_MINECART),
        TNT_MINECART(10, 3, EntityType.TNT_MINECART),
        SPAWNER_MINECART(10, 4, EntityType.SPAWNER_MINECART),
        HOPPER_MINECART(10, 5, EntityType.HOPPER_MINECART),
        COMMAND_BLOCK_MINECART(10, 6, EntityType.COMMAND_BLOCK_MINECART),
        TNT_PRIMED(50, EntityType.TNT),
        ENDER_CRYSTAL(51, EntityType.END_CRYSTAL),
        TIPPED_ARROW(60, EntityType.ARROW),
        SNOWBALL(61, EntityType.SNOWBALL),
        EGG(62, EntityType.EGG),
        FIREBALL(63, EntityType.FIREBALL),
        SMALL_FIREBALL(64, EntityType.SMALL_FIREBALL),
        ENDER_PEARL(65, EntityType.ENDER_PEARL),
        WITHER_SKULL(66, EntityType.WITHER_SKULL),
        SHULKER_BULLET(67, EntityType.SHULKER_BULLET),
        LLAMA_SPIT(68, EntityType.LLAMA_SPIT),
        FALLING_BLOCK(70, EntityType.FALLING_BLOCK),
        ITEM_FRAME(71, EntityType.ITEM_FRAME),
        EYE_OF_ENDER(72, EntityType.EYE_OF_ENDER),
        POTION(73, EntityType.POTION),
        EXPERIENCE_BOTTLE(75, EntityType.EXPERIENCE_BOTTLE),
        FIREWORK_ROCKET(76, EntityType.FIREWORK_ROCKET),
        LEASH(77, EntityType.LEASH_KNOT),
        ARMOR_STAND(78, EntityType.ARMOR_STAND),
        EVOKER_FANGS(79, EntityType.EVOKER_FANGS),
        FISHIHNG_HOOK(90, EntityType.FISHING_HOOK),
        SPECTRAL_ARROW(91, EntityType.SPECTRAL_ARROW),
        DRAGON_FIREBALL(93, EntityType.DRAGON_FIREBALL);

        private static final Int2ObjectMap<Int2ObjectMap<ObjectType>> TYPES = new Int2ObjectOpenHashMap<>();

        private final int id;
        private final int data;
        private final EntityType type;

        static {
            for (ObjectType type : ObjectType.values()) {
                TYPES.computeIfAbsent(type.id, k -> new Int2ObjectOpenHashMap<>()).put(type.data, type);
            }
        }

        ObjectType(int id, EntityType type) {
            this(id, 0, type);
        }

        ObjectType(int id, int data, EntityType type) {
            this.id = id;
            this.data = data;
            this.type = type;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public int getData() {
            return data;
        }

        @Override
        public EntityType getType() {
            return type;
        }

        public static ObjectType findById(final int id, final int data) {
            final Int2ObjectMap<ObjectType> types = TYPES.get(id);
            if (types == null) {
                return null;
            }

            final ObjectType type = types.get(data);
            return type != null ? type : types.get(0);
        }

        public static EntityType getEntityType(final int id, final int data) {
            final ObjectType objectType = findById(id, data);
            return objectType != null ? objectType.type : null;
        }
    }
}
