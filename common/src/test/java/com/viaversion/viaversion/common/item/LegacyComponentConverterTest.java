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
package com.viaversion.viaversion.common.item;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.api.minecraft.item.data.Equippable;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.common.PlatformTestBase;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.Protocol1_21To1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.rewriter.LegacyComponentConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LegacyComponentConverterTest extends PlatformTestBase {

    private static Protocol<?, ?, ?, ?> protocol;

    @BeforeAll
    static void loadProtocol() {
        protocol = Via.getManager().getProtocolManager().getProtocol(Protocol1_21To1_21_2.class);
    }

    private static Item itemWithComponentNbt(final CompoundTag components) {
        final Item item = new StructuredItem(1, 1);
        item.dataContainer().setIdLookup(protocol, true);

        final CompoundTag customData = new CompoundTag();
        customData.put(LegacyComponentConverter.COMPONENTS_TAG, components);
        item.dataContainer().set(StructuredDataKey.CUSTOM_DATA, customData);
        return item;
    }

    private static CompoundTag equippableTag(final String slot, final String assetId) {
        final CompoundTag equippable = new CompoundTag();
        if (slot != null) {
            equippable.putString("slot", slot);
        }
        if (assetId != null) {
            equippable.putString("asset_id", assetId);
        }

        final CompoundTag components = new CompoundTag();
        components.put("equippable", equippable);
        return components;
    }

    @Test
    void testEquippableDefaults() {
        final Item item = itemWithComponentNbt(equippableTag("chest", "example:test_armor"));
        LegacyComponentConverter.convertLegacyComponents(item);

        final Equippable equippable = item.dataContainer().get(StructuredDataKey.EQUIPPABLE1_21_2);
        Assertions.assertNotNull(equippable, "equippable was not converted");
        Assertions.assertEquals(3, equippable.equipmentSlot(), "chest should map to slot 3");
        Assertions.assertEquals("example:test_armor", equippable.model(), "asset_id mismatch");
        Assertions.assertTrue(equippable.dispensable());
        Assertions.assertTrue(equippable.swappable());
        Assertions.assertTrue(equippable.damageOnHurt());
        Assertions.assertFalse(equippable.equipOnInteract());
        Assertions.assertFalse(equippable.canBeSheared());
        Assertions.assertTrue(equippable.soundEvent().isDirect(), "equip sound should be a direct holder");
        Assertions.assertEquals("minecraft:item.armor.equip_generic", equippable.soundEvent().value().identifier());
    }

    @Test
    void testExplicitFields() {
        final CompoundTag components = equippableTag("head", "example:test_helmet");
        final CompoundTag equippableTag = components.getCompoundTag("equippable");
        equippableTag.putBoolean("dispensable", false);
        equippableTag.putBoolean("equip_on_interact", true);
        equippableTag.putString("equip_sound", "minecraft:item.armor.equip_iron");
        equippableTag.putString("camera_overlay", "example:overlay");

        final Item item = itemWithComponentNbt(components);
        LegacyComponentConverter.convertLegacyComponents(item);

        final Equippable equippable = item.dataContainer().get(StructuredDataKey.EQUIPPABLE1_21_2);
        Assertions.assertNotNull(equippable);
        Assertions.assertEquals(4, equippable.equipmentSlot(), "head should map to slot 4");
        Assertions.assertFalse(equippable.dispensable());
        Assertions.assertTrue(equippable.equipOnInteract());
        Assertions.assertEquals("minecraft:item.armor.equip_iron", equippable.soundEvent().value().identifier());
        Assertions.assertEquals("example:overlay", equippable.cameraOverlay());
    }

    @Test
    void testUnknownSlotIsIgnored() {
        // idFromName falls back to the first entry, so an unknown slot must not silently become mainhand
        final Item item = itemWithComponentNbt(equippableTag("definitely_not_a_slot", "example:test_armor"));
        LegacyComponentConverter.convertLegacyComponents(item);

        Assertions.assertNull(item.dataContainer().get(StructuredDataKey.EQUIPPABLE1_21_2), "unknown slot should be ignored");
    }

    @Test
    void testInvalidAssetIdIsIgnored() {
        final Item item = itemWithComponentNbt(equippableTag("chest", "Not A Valid Key"));
        LegacyComponentConverter.convertLegacyComponents(item);

        Assertions.assertNull(item.dataContainer().get(StructuredDataKey.EQUIPPABLE1_21_2), "invalid asset_id should be ignored");
    }

    @Test
    void testMissingSlotIsIgnored() {
        final Item item = itemWithComponentNbt(equippableTag(null, "example:test_armor"));
        LegacyComponentConverter.convertLegacyComponents(item);

        Assertions.assertNull(item.dataContainer().get(StructuredDataKey.EQUIPPABLE1_21_2), "missing slot should be ignored");
    }

    @Test
    void testUnknownComponentIsIgnored() {
        final CompoundTag components = new CompoundTag();
        components.put("definitely_not_a_component", new CompoundTag());

        final Item item = itemWithComponentNbt(components);
        Assertions.assertDoesNotThrow(() -> LegacyComponentConverter.convertLegacyComponents(item));
    }

    @Test
    void testUnnamespacedTagIsIgnored() {
        // Only the namespaced key is read; a plain "components" tag is not part of the contract
        final Item item = new StructuredItem(1, 1);
        item.dataContainer().setIdLookup(protocol, true);

        final CompoundTag customData = new CompoundTag();
        customData.put("components", equippableTag("chest", "example:test_armor"));
        item.dataContainer().set(StructuredDataKey.CUSTOM_DATA, customData);

        LegacyComponentConverter.convertLegacyComponents(item);

        Assertions.assertNull(item.dataContainer().get(StructuredDataKey.EQUIPPABLE1_21_2),
            "only " + LegacyComponentConverter.COMPONENTS_TAG + " should be read");
    }

    @Test
    void testItemWithoutCustomDataIsUntouched() {
        final Item item = new StructuredItem(1, 1);
        item.dataContainer().setIdLookup(protocol, true);

        Assertions.assertDoesNotThrow(() -> LegacyComponentConverter.convertLegacyComponents(item));
        Assertions.assertNull(item.dataContainer().get(StructuredDataKey.EQUIPPABLE1_21_2));
    }

    @Test
    void testComponentNbtIsLeftInCustomData() {
        // The legacy nbt is handed back to the backend server as-is, so it must survive the conversion
        final Item item = itemWithComponentNbt(equippableTag("legs", "example:test_leggings"));
        LegacyComponentConverter.convertLegacyComponents(item);

        final CompoundTag customData = item.dataContainer().get(StructuredDataKey.CUSTOM_DATA);
        Assertions.assertNotNull(customData);
        Assertions.assertNotNull(customData.getCompoundTag(LegacyComponentConverter.COMPONENTS_TAG),
            "the component nbt must stay in custom_data for the serverbound path");
    }
}
