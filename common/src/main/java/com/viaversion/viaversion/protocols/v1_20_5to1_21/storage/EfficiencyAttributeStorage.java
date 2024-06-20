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
package com.viaversion.viaversion.protocols.v1_20_5to1_21.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.Protocol1_20_5To1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import java.util.List;

public final class EfficiencyAttributeStorage implements StorableObject {

    private static final EnchantAttributeModifier EFFICIENCY = new EnchantAttributeModifier("minecraft:enchantment.efficiency/mainhand", 19, 0, level -> (level * level) + 1);
    private static final EnchantAttributeModifier SOUL_SPEED = new EnchantAttributeModifier("minecraft:enchantment.soul_speed", 21, 0.1, level -> 0.04D + ((level - 1) * 0.01D));
    private static final EnchantAttributeModifier SWIFT_SNEAK = new EnchantAttributeModifier("minecraft:enchantment.swift_sneak", 25, 0.3, level -> level * 0.15D);
    private static final EnchantAttributeModifier AQUA_AFFINITY = new EnchantAttributeModifier("minecraft:enchantment.aqua_affinity", 28, 0.2, level -> level * 4, (byte) 2);
    private static final EnchantAttributeModifier DEPTH_STRIDER = new EnchantAttributeModifier("minecraft:enchantment.depth_strider", 30, 0, level -> level / 3D);
    private static final ActiveEnchants DEFAULT = new ActiveEnchants(-1,
        new ActiveEnchant(EFFICIENCY, 0),
        new ActiveEnchant(SOUL_SPEED, 0),
        new ActiveEnchant(SWIFT_SNEAK, 0),
        new ActiveEnchant(AQUA_AFFINITY, 0),
        new ActiveEnchant(DEPTH_STRIDER, 0)
    );
    private final Object lock = new Object();
    private volatile boolean attributesSent = true;
    private volatile boolean loginSent;
    private ActiveEnchants activeEnchants = DEFAULT;

    public void setEnchants(final int entityId, final UserConnection connection, final int efficiency, final int soulSpeed,
                            final int swiftSneak, final int aquaAffinity, final int depthStrider) {
        // Always called from the main thread
        if (efficiency == activeEnchants.efficiency.level
            && soulSpeed == activeEnchants.soulSpeed.level
            && swiftSneak == activeEnchants.swiftSneak.level
            && aquaAffinity == activeEnchants.aquaAffinity.level
            && depthStrider == activeEnchants.depthStrider.level) {
            return;
        }

        synchronized (lock) {
            this.activeEnchants = new ActiveEnchants(entityId,
                new ActiveEnchant(EFFICIENCY, efficiency),
                new ActiveEnchant(SOUL_SPEED, soulSpeed),
                new ActiveEnchant(SWIFT_SNEAK, swiftSneak),
                new ActiveEnchant(AQUA_AFFINITY, aquaAffinity),
                new ActiveEnchant(DEPTH_STRIDER, depthStrider)
            );
            this.attributesSent = false;
        }
        sendAttributesPacket(connection);
    }

    public ActiveEnchants activeEnchants() {
        return activeEnchants;
    }

    public void onLoginSent(final UserConnection connection) {
        // Always called from the netty thread
        this.loginSent = true;
        sendAttributesPacket(connection);
    }

    private void sendAttributesPacket(final UserConnection connection) {
        final ActiveEnchants enchants;
        synchronized (lock) {
            // Older servers (and often Bungee) will send world state packets before sending the login packet
            if (!loginSent || attributesSent) {
                return;
            }

            enchants = this.activeEnchants;
            attributesSent = true;
        }

        final PacketWrapper attributesPacket = PacketWrapper.create(ClientboundPackets1_21.UPDATE_ATTRIBUTES, connection);
        attributesPacket.write(Types.VAR_INT, enchants.entityId());

        final List<ActiveEnchant> list = List.of(enchants.efficiency(), enchants.soulSpeed(), enchants.swiftSneak(), enchants.depthStrider());
        attributesPacket.write(Types.VAR_INT, list.size());
        for (final ActiveEnchant enchant : list) {
            final EnchantAttributeModifier modifier = enchant.modifier;
            attributesPacket.write(Types.VAR_INT, modifier.attributeId);
            attributesPacket.write(Types.DOUBLE, modifier.baseValue);

            if (enchant.level > 0) {
                attributesPacket.write(Types.VAR_INT, 1); // Modifiers
                attributesPacket.write(Types.STRING, modifier.key);
                attributesPacket.write(Types.DOUBLE, enchant.modifier.modifierFunction.get(enchant.level));
                attributesPacket.write(Types.BYTE, modifier.operation);
            } else {
                attributesPacket.write(Types.VAR_INT, 0); // Modifiers
            }
        }

        attributesPacket.scheduleSend(Protocol1_20_5To1_21.class);
    }

    public record ActiveEnchants(int entityId, ActiveEnchant efficiency, ActiveEnchant aquaAffinity,
                                 ActiveEnchant soulSpeed, ActiveEnchant swiftSneak, ActiveEnchant depthStrider) {
    }

    public record ActiveEnchant(EnchantAttributeModifier modifier, int level) {
    }

    public record EnchantAttributeModifier(String key, int attributeId, double baseValue, LevelToModifier modifierFunction, byte operation) {

        private EnchantAttributeModifier(String key, int attributeId, double baseValue, LevelToModifier modifierFunction) {
            this(key, attributeId, baseValue, modifierFunction, (byte) 0);
        }
    }

    @FunctionalInterface
    private interface LevelToModifier {

        double get(int level);
    }
}
