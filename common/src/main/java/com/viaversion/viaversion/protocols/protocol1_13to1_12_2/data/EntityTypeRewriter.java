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

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

public class EntityTypeRewriter {
    private static final Int2IntMap ENTITY_TYPES = new Int2IntOpenHashMap(83, .99F);

    static {
        ENTITY_TYPES.defaultReturnValue(-1);
        registerEntity(1, 32); // item - ajl
        registerEntity(2, 22); // xp_orb - abx
        registerEntity(3, 0); // area_effect_cloud - abp
        registerEntity(4, 15); // elder_guardian - aju
        registerEntity(5, 84); // wither_skeleton - aku
        registerEntity(6, 71); // stray - akq
        registerEntity(7, 74); // egg - alz
        registerEntity(8, 35); // leash_knot - ajb
        registerEntity(9, 49); // painting - ajd
        registerEntity(10, 2); // arrow - all
        registerEntity(11, 67); // snowball - alw
        registerEntity(12, 34); // fireball - alq
        registerEntity(13, 65); // small_fireball - alv
        registerEntity(14, 75); // ender_pearl - ama
        registerEntity(15, 23); // eye_of_ender_signal - alo
        registerEntity(16, 77); // potion - amc
        registerEntity(17, 76); // xp_bottle - amb
        registerEntity(18, 33); // item_frame - aja
        registerEntity(19, 85); // wither_skull - ame
        registerEntity(20, 55); // tnt - ajm
        registerEntity(21, 24); // falling_block - ajk
        registerEntity(22, 25); // fireworks_rocket - alp
        registerEntity(23, 30); // husk - akc
        registerEntity(24, 68); // spectral_arrow - alx
        registerEntity(25, 60); // shulker_bullet - alu
        registerEntity(26, 13); // dragon_fireball - alm
        registerEntity(27, 89); // zombie_villager - akw
        registerEntity(28, 63); // skeleton_horse - aht
        registerEntity(29, 88); // zombie_horse - ahv
        registerEntity(30, 1); // armor_stand - aiy
        registerEntity(31, 11); // donkey - aho
        registerEntity(32, 46); // mule - ahs
        registerEntity(33, 20); // evocation_fangs - aln
        registerEntity(34, 21); // evocation_illager - ajy
        registerEntity(35, 78); // vex - akr
        registerEntity(36, 81); // vindication_illager - aks
        registerEntity(37, 31); // illusion_illager - akd
        registerEntity(40, 41); // commandblock_minecart - aml
        registerEntity(41, 5); // boat - ami
        registerEntity(42, 39); // minecart - amj
        registerEntity(43, 40); // chest_minecart - amk
        registerEntity(44, 42); // furnace_minecart - amm
        registerEntity(45, 45); // tnt_minecart - amp
        registerEntity(46, 43); // hopper_minecart - amn
        registerEntity(47, 44); // spawner_minecart - amo
        registerEntity(50, 10); // creeper - ajs
        registerEntity(51, 62); // skeleton - akm
        registerEntity(52, 69); // spider - akp
        registerEntity(53, 27); // giant - aka
        registerEntity(54, 87); // zombie - akv
        registerEntity(55, 64); // slime - akn
        registerEntity(56, 26); // ghast - ajz
        registerEntity(57, 53); // zombie_pigman - akh
        registerEntity(58, 18); // enderman - ajv
        registerEntity(59, 6); // cave_spider - ajr
        registerEntity(60, 61); // silverfish - akl
        registerEntity(61, 4); // blaze - ajq
        registerEntity(62, 38); // magma_cube - ake
        registerEntity(63, 17); // ender_dragon - aic
        registerEntity(64, 83); // wither - aiw
        registerEntity(65, 3); // bat - agl
        registerEntity(66, 82); // witch - akt
        registerEntity(67, 19); // endermite - ajw
        registerEntity(68, 28); // guardian - akb
        registerEntity(69, 59); // shulker - akk
        registerEntity(200, 16); // ender_crystal - aib
        registerEntity(90, 51); // pig - agy
        registerEntity(91, 58); // sheep - ahd
        registerEntity(92, 9); // cow - ags
        registerEntity(93, 7); // chicken - agq
        registerEntity(94, 70); // squid - ahg
        registerEntity(95, 86); // wolf - ahl
        registerEntity(96, 47); // mooshroom - agv
        registerEntity(97, 66); // snowman - ahf
        registerEntity(98, 48); // ocelot - agw
        registerEntity(99, 80); // villager_golem - ahj
        registerEntity(100, 29); // horse - ahp
        registerEntity(101, 56); // rabbit - ahb
        registerEntity(102, 54); // polar_bear - agz
        registerEntity(103, 36); // llama - ahr
        registerEntity(104, 37); // llama_spit - alr
        registerEntity(105, 50); // parrot - agx
        registerEntity(120, 79); // villager - ala
    }

    private static void registerEntity(int type1_12, int type1_13) {
        ENTITY_TYPES.put(type1_12, type1_13);
    }

    public static int getNewId(int type1_12) {
        return ENTITY_TYPES.getOrDefault(type1_12, type1_12);
    }
}