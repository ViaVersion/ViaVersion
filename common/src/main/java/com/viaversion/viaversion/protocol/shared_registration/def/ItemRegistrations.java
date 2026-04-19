/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.protocol.shared_registration.def;

import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.protocol.shared_registration.PacketBound;
import com.viaversion.viaversion.protocol.shared_registration.RegistrationContext;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ServerboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.packet.ClientboundPackets1_14;
import com.viaversion.viaversion.protocols.v1_14_3to1_14_4.packet.ClientboundPackets1_14_4;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.packet.ClientboundPackets1_16;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ServerboundPackets1_17;
import com.viaversion.viaversion.protocols.v1_17to1_17_1.packet.ClientboundPackets1_17_1;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.packet.ServerboundPackets1_19_3;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.v1_19to1_19_1.packet.ClientboundPackets1_19_1;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ClientboundPackets1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ServerboundPackets1_20_3;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ClientboundPackets1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ServerboundPackets1_20_2;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ClientboundPackets1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ServerboundPackets1_21_5;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundConfigurationPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ServerboundPackets1_21_2;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.rewriter.StructuredItemRewriter;
import org.checkerframework.checker.nullness.qual.Nullable;

final class ItemRegistrations {

    static <CU extends ClientboundPacketType, SU extends ServerboundPacketType> void registerItemPackets1_13(final RegistrationContext<CU, SU> ctx, final ItemRewriter<CU, SU, ?> ir) {
        common1_13(ctx, ir);
        ctx.clientbound(ClientboundPackets1_13.SET_EQUIPPED_ITEM, ir::registerSetEquippedItem);
    }

    private static <CU extends ClientboundPacketType, SU extends ServerboundPacketType> void common1_13(final RegistrationContext<CU, SU> ctx, final ItemRewriter<CU, SU, ?> ir) {
        ctx.clientbound(ClientboundPackets1_13.CONTAINER_SET_DATA, ir::registerContainerSetData);
        ctx.clientbound(ClientboundPackets1_13.CONTAINER_SET_SLOT, ir::registerSetSlot);
        ctx.clientbound(ClientboundPackets1_13.CONTAINER_SET_CONTENT, ir::registerSetContent);
        ctx.clientbound(ClientboundPackets1_13.UPDATE_ADVANCEMENTS, ir::registerAdvancements);
        ctx.clientbound(ClientboundPackets1_13.COOLDOWN, ir::registerCooldown);
        ctx.serverbound(ServerboundPackets1_13.CONTAINER_CLICK, ir::registerContainerClick);
        ctx.serverbound(ServerboundPackets1_13.SET_CREATIVE_MODE_SLOT, ir::registerSetCreativeModeSlot);
    }

    static <CU extends ClientboundPacketType, SU extends ServerboundPacketType> void registerItemPackets1_14(final RegistrationContext<CU, SU> ctx, final ItemRewriter<CU, SU, ?> ir) {
        common1_13(ctx, ir);
        ctx.clientbound(ClientboundPackets1_14.SET_EQUIPPED_ITEM, ir::registerSetEquippedItem);
        ctx.clientbound(ClientboundPackets1_14.OPEN_SCREEN, ir::registerOpenScreen);
    }

    static <CU extends ClientboundPacketType, SU extends ServerboundPacketType> void registerItemPackets1_14_4(final RegistrationContext<CU, SU> ctx, final ItemRewriter<CU, SU, ?> ir) {
        common1_13(ctx, ir);
        ctx.clientbound(ClientboundPackets1_14_4.SET_EQUIPPED_ITEM, ir::registerSetEquippedItem, PacketBound.REMOVED_AT_MAX);
        ctx.clientbound(ClientboundPackets1_14_4.OPEN_SCREEN, ir::registerOpenScreen);
        ctx.clientbound(ClientboundPackets1_14_4.MERCHANT_OFFERS, ir::registerMerchantOffers1_14_4);
    }

    static <CU extends ClientboundPacketType, SU extends ServerboundPacketType> void registerItemPackets1_16(final RegistrationContext<CU, SU> ctx, final ItemRewriter<CU, SU, ?> ir) {
        common1_13(ctx, ir);
        ctx.clientbound(ClientboundPackets1_16.SET_EQUIPMENT, ir::registerSetEquipment, PacketBound.ADDED_AT_MIN);
        ctx.clientbound(ClientboundPackets1_16.OPEN_SCREEN, ir::registerOpenScreen);
        ctx.clientbound(ClientboundPackets1_16.MERCHANT_OFFERS, ir::registerMerchantOffers1_14_4);
    }

    static <CU extends ClientboundPacketType, SU extends ServerboundPacketType> void registerItemPackets1_17_1(final RegistrationContext<CU, SU> ctx, final ItemRewriter<CU, SU, ?> ir) {
        ctx.clientbound(ClientboundPackets1_17_1.CONTAINER_SET_DATA, ir::registerContainerSetData);
        ctx.clientbound(ClientboundPackets1_17_1.CONTAINER_SET_SLOT, ir::registerSetSlot1_17_1);
        ctx.clientbound(ClientboundPackets1_17_1.CONTAINER_SET_CONTENT, ir::registerSetContent1_17_1);
        ctx.clientbound(ClientboundPackets1_17_1.UPDATE_ADVANCEMENTS, ir::registerAdvancements);
        ctx.clientbound(ClientboundPackets1_17_1.COOLDOWN, ir::registerCooldown);
        ctx.serverbound(ServerboundPackets1_17.CONTAINER_CLICK, ir::registerContainerClick1_17_1);
        ctx.serverbound(ServerboundPackets1_17.SET_CREATIVE_MODE_SLOT, ir::registerSetCreativeModeSlot);
        ctx.clientbound(ClientboundPackets1_17_1.SET_EQUIPMENT, ir::registerSetEquipment);
        ctx.clientbound(ClientboundPackets1_17_1.OPEN_SCREEN, ir::registerOpenScreen);
        ctx.clientbound(ClientboundPackets1_17_1.MERCHANT_OFFERS, ir::registerMerchantOffers1_14_4);
    }

    static <CU extends ClientboundPacketType, SU extends ServerboundPacketType> void registerItemPackets1_19_1(final RegistrationContext<CU, SU> ctx, final ItemRewriter<CU, SU, ?> ir) {
        ctx.clientbound(ClientboundPackets1_19_1.CONTAINER_SET_DATA, ir::registerContainerSetData);
        ctx.clientbound(ClientboundPackets1_19_1.CONTAINER_SET_SLOT, ir::registerSetSlot1_17_1);
        ctx.clientbound(ClientboundPackets1_19_1.CONTAINER_SET_CONTENT, ir::registerSetContent1_17_1);
        ctx.clientbound(ClientboundPackets1_19_1.UPDATE_ADVANCEMENTS, ir::registerAdvancements);
        ctx.clientbound(ClientboundPackets1_19_1.COOLDOWN, ir::registerCooldown);
        ctx.serverbound(ServerboundPackets1_19_3.CONTAINER_CLICK, ir::registerContainerClick1_17_1);
        ctx.serverbound(ServerboundPackets1_19_3.SET_CREATIVE_MODE_SLOT, ir::registerSetCreativeModeSlot);
        ctx.clientbound(ClientboundPackets1_19_1.SET_EQUIPMENT, ir::registerSetEquipment);
        ctx.clientbound(ClientboundPackets1_19_1.OPEN_SCREEN, ir::registerOpenScreen);
        ctx.clientbound(ClientboundPackets1_19_1.MERCHANT_OFFERS, ir::registerMerchantOffers1_19);
    }

    static <CU extends ClientboundPacketType, SU extends ServerboundPacketType> void registerItemPackets1_20_3(final RegistrationContext<CU, SU> ctx, final ItemRewriter<CU, SU, ?> ir) {
        ctx.clientbound(ClientboundPackets1_20_3.CONTAINER_SET_DATA, ir::registerContainerSetData);
        ctx.clientbound(ClientboundPackets1_20_3.CONTAINER_SET_SLOT, ir::registerSetSlot1_17_1);
        ctx.clientbound(ClientboundPackets1_20_3.CONTAINER_SET_CONTENT, ir::registerSetContent1_17_1);
        ctx.clientbound(ClientboundPackets1_20_5.UPDATE_ADVANCEMENTS, ir::registerAdvancements1_20_3);
        ctx.clientbound(ClientboundPackets1_20_3.COOLDOWN, ir::registerCooldown);
        ctx.serverbound(ServerboundPackets1_20_3.CONTAINER_CLICK, ir::registerContainerClick1_17_1);
        ctx.serverbound(ServerboundPackets1_20_2.SET_CREATIVE_MODE_SLOT, ir::registerSetCreativeModeSlot);
        ctx.clientbound(ClientboundPackets1_20_5.SET_EQUIPMENT, ir::registerSetEquipment);
        ctx.clientbound(ClientboundPackets1_19_4.OPEN_SCREEN, ir::registerOpenScreen);
        ctx.clientbound(ClientboundPackets1_20_2.MERCHANT_OFFERS, ir::registerMerchantOffers1_19);
    }

    static <CU extends ClientboundPacketType, SU extends ServerboundPacketType> void registerItemPackets1_20_5(final RegistrationContext<CU, SU> ctx, final ItemRewriter<CU, SU, ?> ir) {
        ctx.clientbound(ClientboundPackets1_20_3.CONTAINER_SET_DATA, ir::registerContainerSetData);
        registerItemPackets1_21(ctx, ir);
    }

    static <CU extends ClientboundPacketType, SU extends ServerboundPacketType> void registerItemPackets1_21(final RegistrationContext<CU, SU> ctx, final ItemRewriter<CU, SU, ?> ir) {
        ctx.clientbound(ClientboundPackets1_21.CONTAINER_SET_SLOT, ir::registerSetSlot1_17_1);
        ctx.clientbound(ClientboundPackets1_21.CONTAINER_SET_CONTENT, ir::registerSetContent1_17_1);
        ctx.clientbound(ClientboundPackets1_21.UPDATE_ADVANCEMENTS, ir::registerAdvancements1_20_3);
        ctx.clientbound(ClientboundPackets1_21.COOLDOWN, ir::registerCooldown);
        ctx.serverbound(ServerboundPackets1_20_5.CONTAINER_CLICK, ir::registerContainerClick1_17_1);
        ctx.serverbound(ServerboundPackets1_20_5.SET_CREATIVE_MODE_SLOT, ir::registerSetCreativeModeSlot);
        ctx.clientbound(ClientboundPackets1_21.SET_EQUIPMENT, ir::registerSetEquipment);
        ctx.clientbound(ClientboundPackets1_21.OPEN_SCREEN, ir::registerOpenScreen);
        ctx.clientbound(ClientboundPackets1_21.MERCHANT_OFFERS, ir::registerMerchantOffers1_20_5);
    }

    static <CU extends ClientboundPacketType, SU extends ServerboundPacketType> void registerItemPackets1_21_2(final RegistrationContext<CU, SU> ctx, final ItemRewriter<CU, SU, ?> ir) {
        ctx.clientbound(ClientboundPackets1_21_2.CONTAINER_SET_SLOT, ir::registerSetSlot1_21_2);
        ctx.clientbound(ClientboundPackets1_21_2.CONTAINER_SET_CONTENT, ir::registerSetContent1_21_2);
        ctx.clientbound(ClientboundPackets1_21_2.UPDATE_ADVANCEMENTS, ir::registerAdvancements1_20_3);
        ctx.clientbound(ClientboundPackets1_21_2.COOLDOWN, ir::registerCooldown1_21_2);
        ctx.serverbound(ServerboundPackets1_21_2.CONTAINER_CLICK, ir::registerContainerClick1_21_2);
        ctx.serverbound(ServerboundPackets1_21_2.SET_CREATIVE_MODE_SLOT, ir::registerSetCreativeModeSlot);
        ctx.clientbound(ClientboundPackets1_21_2.SET_EQUIPMENT, ir::registerSetEquipment);
        ctx.clientbound(ClientboundPackets1_21_2.OPEN_SCREEN, ir::registerOpenScreen);
        ctx.clientbound(ClientboundPackets1_21_2.MERCHANT_OFFERS, ir::registerMerchantOffers1_20_5);
        ctx.clientbound(ClientboundPackets1_21_2.SET_PLAYER_INVENTORY, ir::registerSetPlayerInventory, PacketBound.ADDED_AT_MIN);
        ctx.clientbound(ClientboundPackets1_21_2.SET_CURSOR_ITEM, ir::registerSetCursorItem, PacketBound.ADDED_AT_MIN);
    }

    static <CU extends ClientboundPacketType, SU extends ServerboundPacketType> void registerItemPackets1_21_5(final RegistrationContext<CU, SU> ctx, final StructuredItemRewriter<CU, SU, ?> ir) {
        ctx.clientbound(ClientboundPackets1_21_5.CONTAINER_SET_SLOT, ir::registerSetSlot1_21_2);
        ctx.clientbound(ClientboundPackets1_21_5.CONTAINER_SET_CONTENT, ir::registerSetContent1_21_2);
        ctx.clientbound(ClientboundPackets1_21_5.UPDATE_ADVANCEMENTS, ir::registerAdvancements1_20_3);
        ctx.clientbound(ClientboundPackets1_21_5.COOLDOWN, ir::registerCooldown1_21_2);
        ctx.serverbound(ServerboundPackets1_21_5.CONTAINER_CLICK, ir::registerContainerClick1_21_5);
        ctx.serverbound(ServerboundPackets1_21_5.SET_CREATIVE_MODE_SLOT, ir::registerSetCreativeModeSlot1_21_5);
        ctx.clientbound(ClientboundPackets1_21_5.SET_EQUIPMENT, ir::registerSetEquipment);
        ctx.clientbound(ClientboundPackets1_21_5.OPEN_SCREEN, ir::registerOpenScreen);
        ctx.clientbound(ClientboundPackets1_21_5.MERCHANT_OFFERS, ir::registerMerchantOffers1_20_5);
        ctx.clientbound(ClientboundPackets1_21_5.SET_PLAYER_INVENTORY, ir::registerSetPlayerInventory);
        ctx.clientbound(ClientboundPackets1_21_5.SET_CURSOR_ITEM, ir::registerSetCursorItem);
    }

    static <CU extends ClientboundPacketType, SU extends ServerboundPacketType> void registerItemPackets1_21_6(final RegistrationContext<CU, SU> ctx, final StructuredItemRewriter<CU, SU, ?> ir) {
        registerItemPackets1_21_5(ctx, ir);
        ctx.clientbound(ClientboundPackets1_21_6.SHOW_DIALOG, ir::registerShowDialog, PacketBound.ADDED_AT_MIN);
        ctx.clientbound(ClientboundConfigurationPackets1_21_6.SHOW_DIALOG, ir::registerShowDialogDirect, PacketBound.ADDED_AT_MIN);
    }

    static @Nullable <CU extends ClientboundPacketType, SU extends ServerboundPacketType> ItemRewriter<CU, SU, ?> item(final RegistrationContext<CU, SU> ctx) {
        return (ItemRewriter<CU, SU, ?>) ctx.protocol().getItemRewriter();
    }

    static @Nullable <CU extends ClientboundPacketType, SU extends ServerboundPacketType> StructuredItemRewriter<CU, SU, ?> structuredItem(final RegistrationContext<CU, SU> ctx) {
        return (StructuredItemRewriter<CU, SU, ?>) ctx.protocol().getItemRewriter();
    }
}
