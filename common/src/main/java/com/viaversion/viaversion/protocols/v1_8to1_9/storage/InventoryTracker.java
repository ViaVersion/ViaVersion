/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_8to1_9.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.protocols.v1_8to1_9.Protocol1_8To1_9;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.Map;

public class InventoryTracker implements StorableObject {
    private String inventory;

    private final Int2ObjectMap<Map<Short, Integer>> windowItemCache = new Int2ObjectOpenHashMap<>();
    private int itemIdInCursor;
    private boolean dragging;

    public String getInventory() {
        return inventory;
    }

    public void setInventory(String inventory) {
        this.inventory = inventory;
    }

    public void resetInventory(int windowId) {
        // Reset the cursor state of the inventory
        if (inventory == null) {
            this.itemIdInCursor = 0;
            this.dragging = false;

            // Remove window from cache (Except players window)
            if (windowId != 0) {
                this.windowItemCache.remove(windowId);
            }
        }
    }

    public int getItemId(int windowId, short slot) {
        Map<Short, Integer> itemMap = this.windowItemCache.get(windowId);
        if (itemMap == null) {
            return 0;
        }

        return itemMap.getOrDefault(slot, 0);
    }

    public void setItemId(int windowId, short slot, int itemId) {
        if (windowId == -1 && slot == -1) {
            // Set the cursor item
            this.itemIdInCursor = itemId;
        } else {
            // Set item in normal inventory
            this.windowItemCache.computeIfAbsent(windowId, k -> new HashMap<>()).put(slot, itemId);
        }
    }

    /**
     * Handle the window click to track the position of the sword
     *
     * @param windowId  Id of the current inventory
     * @param mode      Inventory operation mode
     * @param hoverSlot The slot number of the current mouse position
     * @param button    The button to use in the click
     */
    public void handleWindowClick(UserConnection user, int windowId, byte mode, short hoverSlot, byte button) {
        EntityTracker1_9 entityTracker = user.getEntityTracker(Protocol1_8To1_9.class);

        // Skip inventory background clicks
        if (hoverSlot == -1) {
            return;
        }

        // Interaction with the offhand slot
        if (hoverSlot == 45) {
            entityTracker.setSecondHand(null); // Remove it so we know that we can update it on ITEM_USE
            return;
        }

        // It is not possible to put a sword into the armor or crafting result slot
        boolean isArmorOrResultSlot = hoverSlot >= 5 && hoverSlot <= 8 || hoverSlot == 0;

        switch (mode) {
            case 0: // Click on slot

                // The cursor is empty, so we can put an item to it
                if (this.itemIdInCursor == 0) {
                    // Move item to cursor
                    this.itemIdInCursor = getItemId(windowId, hoverSlot);

                    // Remove item in slot
                    setItemId(windowId, hoverSlot, 0);
                } else {
                    // Dropping item
                    if (hoverSlot == -999) {
                        this.itemIdInCursor = 0;
                    } else if (!isArmorOrResultSlot) {
                        int previousItem = getItemId(windowId, hoverSlot);

                        // Place item in inventory
                        setItemId(windowId, hoverSlot, this.itemIdInCursor);

                        // Pick up the other item
                        this.itemIdInCursor = previousItem;
                    }
                }
                break;
            case 2: // Move item using number keys
                if (!isArmorOrResultSlot) {
                    short hotkeySlot = (short) (button + 36);

                    // Get items to swap
                    int sourceItem = getItemId(windowId, hoverSlot);
                    int destinationItem = getItemId(windowId, hotkeySlot);

                    // Swap
                    setItemId(windowId, hotkeySlot, sourceItem);
                    setItemId(windowId, hoverSlot, destinationItem);
                }

                break;
            case 4: // Drop item
                int hoverItem = getItemId(windowId, hoverSlot);

                if (hoverItem != 0) {
                    setItemId(windowId, hoverSlot, 0);
                }

                break;
            case 5: // Mouse dragging
                switch (button) {
                    case 0, 4: // Start left/right dragging
                        this.dragging = true;
                        break;
                    case 1, 5: // Place item during left/right dragging
                        // Check dragging mode and item on cursor
                        if (this.dragging && this.itemIdInCursor != 0 && !isArmorOrResultSlot) {
                            int previousItem = getItemId(windowId, hoverSlot);

                            // Place item on cursor in hovering slot
                            setItemId(windowId, hoverSlot, this.itemIdInCursor);

                            // Pick up the other item
                            this.itemIdInCursor = previousItem;
                        }
                        break;
                    case 2, 6: // Stop left/right dragging
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
