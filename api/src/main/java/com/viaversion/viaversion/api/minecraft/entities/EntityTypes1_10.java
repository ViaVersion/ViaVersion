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

import com.viaversion.viaversion.api.Via;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// 1.10 Entity / Object ids
public class EntityTypes1_10 {

    public static EntityType getTypeFromId(int typeID, boolean isObject) {
        Optional<EntityType> type;

        if (isObject)
            type = ObjectType.getPCEntity(typeID);
        else
            type = EntityType.findById(typeID);

        if (!type.isPresent()) {
            Via.getPlatform().getLogger().severe("Could not find 1.10 type id " + typeID + " isObject=" + isObject);
            return EntityType.ENTITY; // Fall back to the basic ENTITY
        }

        return type.get();
    }

    public enum EntityType implements com.viaversion.viaversion.api.minecraft.entities.EntityType {
        ENTITY(-1),
        DROPPED_ITEM(1, ENTITY),
        EXPERIENCE_ORB(2, ENTITY),
        LEASH_HITCH(8, ENTITY), // Actually entity hanging but it doesn't make a lot of difference for metadata
        PAINTING(9, ENTITY), // Actually entity hanging but it doesn't make a lot of difference for metadata
        ARROW(10, ENTITY),
        SNOWBALL(11, ENTITY), // Actually EntityProjectile
        FIREBALL(12, ENTITY),
        SMALL_FIREBALL(13, ENTITY),
        ENDER_PEARL(14, ENTITY), // Actually EntityProjectile
        ENDER_SIGNAL(15, ENTITY),
        THROWN_EXP_BOTTLE(17, ENTITY),
        ITEM_FRAME(18, ENTITY), // Actually EntityHanging
        WITHER_SKULL(19, ENTITY),
        PRIMED_TNT(20, ENTITY),
        FALLING_BLOCK(21, ENTITY),
        FIREWORK(22, ENTITY),
        TIPPED_ARROW(23, ARROW),
        SPECTRAL_ARROW(24, ARROW),
        SHULKER_BULLET(25, ENTITY),
        DRAGON_FIREBALL(26, FIREBALL),

        ENTITY_LIVING(-1, ENTITY),
        ENTITY_INSENTIENT(-1, ENTITY_LIVING),
        ENTITY_AGEABLE(-1, ENTITY_INSENTIENT),
        ENTITY_TAMEABLE_ANIMAL(-1, ENTITY_AGEABLE),
        ENTITY_HUMAN(-1, ENTITY_LIVING),

        ARMOR_STAND(30, ENTITY_LIVING),

        // Vehicles
        MINECART_ABSTRACT(-1, ENTITY),
        MINECART_COMMAND(40, MINECART_ABSTRACT),
        BOAT(41, ENTITY),
        MINECART_RIDEABLE(42, MINECART_ABSTRACT),
        MINECART_CHEST(43, MINECART_ABSTRACT),
        MINECART_FURNACE(44, MINECART_ABSTRACT),
        MINECART_TNT(45, MINECART_ABSTRACT),
        MINECART_HOPPER(46, MINECART_ABSTRACT),
        MINECART_MOB_SPAWNER(47, MINECART_ABSTRACT),

        CREEPER(50, ENTITY_INSENTIENT),
        SKELETON(51, ENTITY_INSENTIENT),
        SPIDER(52, ENTITY_INSENTIENT),
        GIANT(53, ENTITY_INSENTIENT),
        ZOMBIE(54, ENTITY_INSENTIENT),
        SLIME(55, ENTITY_INSENTIENT),
        GHAST(56, ENTITY_INSENTIENT),
        PIG_ZOMBIE(57, ZOMBIE),
        ENDERMAN(58, ENTITY_INSENTIENT),
        CAVE_SPIDER(59, SPIDER),
        SILVERFISH(60, ENTITY_INSENTIENT),
        BLAZE(61, ENTITY_INSENTIENT),
        MAGMA_CUBE(62, SLIME),
        ENDER_DRAGON(63, ENTITY_INSENTIENT),
        WITHER(64, ENTITY_INSENTIENT),
        BAT(65, ENTITY_INSENTIENT),
        WITCH(66, ENTITY_INSENTIENT),
        ENDERMITE(67, ENTITY_INSENTIENT),
        GUARDIAN(68, ENTITY_INSENTIENT),
        IRON_GOLEM(99, ENTITY_INSENTIENT), // moved up to avoid illegal forward references
        SHULKER(69, IRON_GOLEM),
        PIG(90, ENTITY_AGEABLE),
        SHEEP(91, ENTITY_AGEABLE),
        COW(92, ENTITY_AGEABLE),
        CHICKEN(93, ENTITY_AGEABLE),
        SQUID(94, ENTITY_INSENTIENT),
        WOLF(95, ENTITY_TAMEABLE_ANIMAL),
        MUSHROOM_COW(96, COW),
        SNOWMAN(97, IRON_GOLEM),
        OCELOT(98, ENTITY_TAMEABLE_ANIMAL),
        HORSE(100, ENTITY_AGEABLE),
        RABBIT(101, ENTITY_AGEABLE),
        POLAR_BEAR(102, ENTITY_AGEABLE),
        VILLAGER(120, ENTITY_AGEABLE),
        ENDER_CRYSTAL(200, ENTITY),
        SPLASH_POTION(-1, ENTITY),
        LINGERING_POTION(-1, SPLASH_POTION),
        AREA_EFFECT_CLOUD(-1, ENTITY),
        EGG(-1, ENTITY),
        FISHING_HOOK(-1, ENTITY),
        LIGHTNING(-1, ENTITY),
        WEATHER(-1, ENTITY),
        PLAYER(-1, ENTITY_HUMAN),
        COMPLEX_PART(-1, ENTITY);

        private static final Map<Integer, EntityType> TYPES = new HashMap<>();

        private final int id;
        private final EntityType parent;

        EntityType(int id) {
            this.id = id;
            this.parent = null;
        }

        EntityType(int id, EntityType parent) {
            this.id = id;
            this.parent = parent;
        }

        static {
            for (EntityType type : EntityType.values()) {
                TYPES.put(type.id, type);
            }
        }

        public static Optional<EntityType> findById(int id) {
            if (id == -1)  // Check if this is called
                return Optional.empty();
            return Optional.ofNullable(TYPES.get(id));
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
    }

    public enum ObjectType implements com.viaversion.viaversion.api.minecraft.entities.ObjectType {
        BOAT(1, EntityType.BOAT),
        ITEM(2, EntityType.DROPPED_ITEM),
        AREA_EFFECT_CLOUD(3, EntityType.AREA_EFFECT_CLOUD),
        MINECART(10, EntityType.MINECART_RIDEABLE),
        TNT_PRIMED(50, EntityType.PRIMED_TNT),
        ENDER_CRYSTAL(51, EntityType.ENDER_CRYSTAL),
        TIPPED_ARROW(60, EntityType.TIPPED_ARROW),
        SNOWBALL(61, EntityType.SNOWBALL),
        EGG(62, EntityType.EGG),
        FIREBALL(63, EntityType.FIREBALL),
        SMALL_FIREBALL(64, EntityType.SMALL_FIREBALL),
        ENDER_PEARL(65, EntityType.ENDER_PEARL),
        WITHER_SKULL(66, EntityType.WITHER_SKULL),
        SHULKER_BULLET(67, EntityType.SHULKER_BULLET),
        FALLING_BLOCK(70, EntityType.FALLING_BLOCK),
        ITEM_FRAME(71, EntityType.ITEM_FRAME),
        ENDER_SIGNAL(72, EntityType.ENDER_SIGNAL),
        POTION(73, EntityType.SPLASH_POTION),
        THROWN_EXP_BOTTLE(75, EntityType.THROWN_EXP_BOTTLE),
        FIREWORK(76, EntityType.FIREWORK),
        LEASH(77, EntityType.LEASH_HITCH),
        ARMOR_STAND(78, EntityType.ARMOR_STAND),
        FISHIHNG_HOOK(90, EntityType.FISHING_HOOK),
        SPECTRAL_ARROW(91, EntityType.SPECTRAL_ARROW),
        DRAGON_FIREBALL(93, EntityType.DRAGON_FIREBALL);

        private static final Map<Integer, ObjectType> TYPES = new HashMap<>();

        private final int id;
        private final EntityType type;

        static {
            for (ObjectType type : ObjectType.values()) {
                TYPES.put(type.id, type);
            }
        }

        ObjectType(int id, EntityType type) {
            this.id = id;
            this.type = type;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public EntityType getType() {
            return type;
        }

        public static Optional<ObjectType> findById(int id) {
            if (id == -1)
                return Optional.empty();
            return Optional.ofNullable(TYPES.get(id));
        }

        public static Optional<EntityType> getPCEntity(int id) {
            Optional<ObjectType> output = findById(id);

            if (!output.isPresent())
                return Optional.empty();
            return Optional.of(output.get().type);
        }
    }
}
