/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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
package us.myles.ViaVersion.protocols.protocol1_9to1_8.storage;

import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;

import java.util.HashMap;
import java.util.Map;

public class InventoryTracker extends StoredObject {
    private String inventory;

    private final Map<Short, Integer> slotToItemIdMap = new HashMap<>();
    private Integer itemIdInCursor = null;
    private boolean dragging = false;

    public InventoryTracker(UserConnection user) {
        super(user);
    }

    public String getInventory() {
        return inventory;
    }

    public Map<Short, Integer> getSlotToItemIdMap() {
        return this.slotToItemIdMap;
    }

    public Integer getItemIdInSlot(short slot) {
        return this.slotToItemIdMap.get(slot);
    }

    public void setInventory(String inventory) {
        this.inventory = inventory;
    }

    /**
     * Handle the window click to track the item position in the inventory of the player
     *
     * @param mode      Inventory operation mode
     * @param hoverSlot The slot number of the current mouse position
     * @param button    The button to use in the click
     */
    public void handleWindowClick(byte mode, short hoverSlot, byte button) {
        EntityTracker1_9 entityTracker = getUser().get(EntityTracker1_9.class);

        // Don't listen to the second hand slot
        if (hoverSlot == 45) {
            return;
        }

        switch (mode) {
            case 0: // Click on slot

                // The cursor is empty, so we can put an item to it
                if (itemIdInCursor == null || itemIdInCursor == 0) {
                    // Move item to cursor
                    this.itemIdInCursor = this.slotToItemIdMap.remove(hoverSlot);
                } else {
                    // Dropping item
                    if (hoverSlot == -999) {
                        this.itemIdInCursor = null;
                    } else {
                        // Clicking on a slot
                        Integer hoverItem = this.slotToItemIdMap.get(hoverSlot);

                        // An item is already in the cursor, can we empty it in this slot?
                        if (hoverItem == null || hoverItem == 0) {
                            // Place item in inventory
                            this.slotToItemIdMap.put(hoverSlot, this.itemIdInCursor);
                            this.itemIdInCursor = null;
                        }
                    }
                }
                break;
            case 2: // Move item using number keys
                short hotkeySlot = (short) (button + 36);

                // Find move direction
                Integer hotkeyItem = this.slotToItemIdMap.get(hotkeySlot);
                boolean isHotKeyEmpty = hotkeyItem == null || hotkeyItem == 0;

                // Move item
                Integer sourceItem = this.slotToItemIdMap.remove(isHotKeyEmpty ? hoverSlot : hotkeySlot);
                this.slotToItemIdMap.put(isHotKeyEmpty ? hotkeySlot : hoverSlot, sourceItem);

                break;
            case 4: // Drop item
                Integer hoverItem = this.slotToItemIdMap.get(hoverSlot);

                if (hoverItem != null && hoverItem != 0) {
                    this.slotToItemIdMap.put(hoverSlot, null);
                }

                break;
            case 5: // Mouse dragging
                switch (button) {
                    case 0: // Start left dragging
                    case 4: // Start right dragging
                        this.dragging = true;
                        break;
                    case 1: // Place item during left dragging
                    case 5: // Place item during right dragging
                        // Check dragging mode and item on cursor
                        if (this.dragging && itemIdInCursor != null && itemIdInCursor != 0) {
                            // Place item on cursor in hovering slot
                            this.slotToItemIdMap.put(hoverSlot, itemIdInCursor);
                            this.itemIdInCursor = null;
                        }
                        break;
                    case 2: // Stop left dragging
                    case 6: // Stop right dragging
                        this.dragging = false;
                        break;
                }

                break;
            default:
                break;
        }

        // Update shield state in offhand
        entityTracker.syncShieldWithSword();
    }
}
