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

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.packets.WorldPackets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ParticleRewriter {
    private static final List<NewParticle> particles = new ArrayList<>();

    static {
        add(34); // (0->34) explode -> minecraft:poof
        add(19); // (1->19) largeexplode -> minecraft:explosion
        add(18); // (2->18) hugeexplosion -> minecraft:explosion_emitter
        add(21); // (3->21) fireworksSpark -> minecraft:firework
        add(4); // (4->4) bubble -> minecraft:bubble
        add(43); // (5->43) splash -> minecraft:splash
        add(22); // (6->22) wake -> minecraft:fishing
        add(42); // (7->42) suspended -> minecraft:underwater
        add(32); // (8->32) depthsuspend -> minecraft:mycelium
        add(6); // (9->6) crit -> minecraft:crit
        add(14); // (10->14) magicCrit -> minecraft:enchanted_hit
        add(37); // (11->37) smoke -> minecraft:smoke
        add(30); // (12->30) largesmoke -> minecraft:large_smoke
        add(12); // (13->12) spell -> minecraft:effect
        add(26); // (14->26) instantSpell -> minecraft:instant_effect
        add(17); // (15->17) mobSpell -> minecraft:entity_effect
        add(0); // (16->0) mobSpellAmbient -> minecraft:ambient_entity_effect
        add(44); // (17->44) witchMagic -> minecraft:witch
        add(10); // (18->10) dripWater -> minecraft:dripping_water
        add(9); // (19->9) dripLava -> minecraft:dripping_lava
        add(1); // (20->1) angryVillager -> minecraft:angry_villager
        add(24); // (21->24) happyVillager -> minecraft:happy_villager
        add(32); // (22->32) townaura -> minecraft:mycelium
        add(33); // (23->33) note -> minecraft:note
        add(35); // (24->35) portal -> minecraft:portal
        add(15); // (25->15) enchantmenttable -> minecraft:enchant
        add(23); // (26->23) flame -> minecraft:flame
        add(31); // (27->31) lava -> minecraft:lava
        add(-1); // (28->-1) footstep -> REMOVED
        add(5); // (29->5) cloud -> minecraft:cloud
        add(11, reddustHandler()); // (30->11) reddust -> minecraft:dust
        //    Red	Float	Red value, 0-1
        //    Green	Float	Green value, 0-1
        //    Blue	Float	Blue value, 0-1
        //    Scale	Float	The scale, will be clamped between 0.01 and 4.
        add(29); // (31->29) snowballpoof -> minecraft:item_snowball
        add(34); // (32->34) snowshovel -> minecraft:poof
        add(28); // (33->28) slime -> minecraft:item_slime
        add(25); // (34->25) heart -> minecraft:heart
        add(2); // (35->2) barrier -> minecraft:barrier
        add(27, iconcrackHandler()); // (36->27) iconcrack_(id)_(data) -> minecraft:item
        // Item	Slot	The item that will be used.
        add(3, blockHandler()); // (37->3) blockcrack_(id+(data<<12))   -> minecraft:block
        // BlockState	VarInt	The ID of the block state.
        add(3, blockHandler()); // (38->3) blockdust_(id)               -> minecraft:block
        // BlockState	VarInt	The ID of the block state.
        add(36); // (39->36) droplet -> minecraft:rain
        add(-1); // (40->-1) take -> REMOVED
        add(13); // (41->13) mobappearance -> minecraft:elder_guardian
        add(8); // (42->8) dragonbreath -> minecraft:dragon_breath
        add(16); // (43->16) endrod -> minecraft:end_rod
        add(7); // (44->7) damageindicator -> minecraft:damage_indicator
        add(40); // (45->40) sweepattack -> minecraft:sweep_attack
        add(20, blockHandler()); // (46->20) fallingdust -> minecraft:falling_dust
        // BlockState	VarInt	The ID of the block state.
        add(41); // (47->41) totem -> minecraft:totem_of_undying
        add(38); // (48->38) spit -> minecraft:spit
    }

    public static Particle rewriteParticle(int particleId, Integer[] data) {
        if (particleId >= particles.size()) {
            Via.getPlatform().getLogger().severe("Failed to transform particles with id " + particleId + " and data " + Arrays.toString(data));
            return null;
        }

        NewParticle rewrite = particles.get(particleId);
        return rewrite.handle(new Particle(rewrite.id()), data);
    }

    private static void add(int newId) {
        particles.add(new NewParticle(newId, null));
    }

    private static void add(int newId, ParticleDataHandler dataHandler) {
        particles.add(new NewParticle(newId, dataHandler));
    }

    // Randomized because the previous one was a lot of different colors at once! :)
    private static ParticleDataHandler reddustHandler() {
        return (particle, data) -> {
            particle.add(Type.FLOAT, randomBool() ? 1f : 0f); // Red 0 - 1
            particle.add(Type.FLOAT, 0f); // Green 0 - 1
            particle.add(Type.FLOAT, randomBool() ? 1f : 0f); // Blue 0 - 1
            particle.add(Type.FLOAT, 1f);// Scale 0.01 - 4
            return particle;
        };
    }

    private static boolean randomBool() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    // Rewrite IconCrack items to new format :)
    private static ParticleDataHandler iconcrackHandler() {
        return (particle, data) -> {
            Item item;
            if (data.length == 1) {
                item = new DataItem(data[0], (byte) 1, (short) 0, null);
            } else if (data.length == 2) {
                item = new DataItem(data[0], (byte) 1, data[1].shortValue(), null);
            } else {
                return particle;
            }

            // Transform to new Item
            Via.getManager().getProtocolManager().getProtocol(Protocol1_13To1_12_2.class).getItemRewriter().handleItemToClient(null, item);

            particle.add(Type.ITEM1_13, item); // Item Slot	The item that will be used.
            return particle;
        };
    }

    // Handle (id+(data<<12)) encoded blocks
    private static ParticleDataHandler blockHandler() {
        return (particle, data) -> {
            int value = data[0];
            int combined = (((value & 4095) << 4) | (value >> 12 & 15));
            int newId = WorldPackets.toNewId(combined);

            particle.add(Type.VAR_INT, newId); // BlockState	VarInt	The ID of the block state.
            return particle;
        };
    }

    @FunctionalInterface
    interface ParticleDataHandler {
        Particle handler(Particle particle, Integer[] data);
    }

    private static class NewParticle {
        private final int id;
        private final ParticleDataHandler handler;

        public NewParticle(int id, ParticleDataHandler handler) {
            this.id = id;
            this.handler = handler;
        }

        public Particle handle(Particle particle, Integer[] data) {
            if (handler != null)
                return handler.handler(particle, data);
            return particle;
        }

        public int id() {
            return id;
        }

        public ParticleDataHandler handler() {
            return handler;
        }
    }
}
