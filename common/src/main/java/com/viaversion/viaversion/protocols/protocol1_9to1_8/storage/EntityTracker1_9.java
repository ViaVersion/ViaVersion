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
package com.viaversion.viaversion.protocols.protocol1_9to1_8.storage;

import com.google.common.cache.CacheBuilder;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.legacy.bossbar.BossBar;
import com.viaversion.viaversion.api.legacy.bossbar.BossColor;
import com.viaversion.viaversion.api.legacy.bossbar.BossStyle;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10.EntityType;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_9;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.chat.GameMode;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.providers.BossBarProvider;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.providers.EntityIdProvider;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class EntityTracker1_9 extends EntityTrackerBase {
    public static final String WITHER_TRANSLATABLE = "{\"translate\":\"entity.WitherBoss.name\"}";
    public static final String DRAGON_TRANSLATABLE = "{\"translate\":\"entity.EnderDragon.name\"}";
    private final Int2ObjectMap<UUID> uuidMap = new Int2ObjectOpenHashMap<>();
    private final Int2IntMap vehicleMap = new Int2IntOpenHashMap();
    private final Int2ObjectMap<BossBar> bossBarMap = new Int2ObjectOpenHashMap<>();
    private final IntSet validBlocking = new IntOpenHashSet();
    private final IntSet knownHolograms = new IntOpenHashSet();
    private final Set<Position> blockInteractions = Collections.newSetFromMap(CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(250, TimeUnit.MILLISECONDS)
            .<Position, Boolean>build()
            .asMap());
    private boolean blocking = false;
    private boolean autoTeam = false;
    private Position currentlyDigging = null;
    private boolean teamExists = false;
    private GameMode gameMode;
    private String currentTeam;
    private int heldItemSlot;
    private Item itemInSecondHand = null;

    public EntityTracker1_9(UserConnection user) {
        super(user, EntityType.PLAYER);
    }

    public UUID getEntityUUID(int id) {
        UUID uuid = uuidMap.get(id);
        if (uuid == null) {
            uuid = UUID.randomUUID();
            uuidMap.put(id, uuid);
        }

        return uuid;
    }

    public void setSecondHand(Item item) {
        setSecondHand(clientEntityId(), item);
    }

    public void setSecondHand(int entityID, Item item) {
        PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_9.ENTITY_EQUIPMENT, null, user());
        wrapper.write(Type.VAR_INT, entityID);
        wrapper.write(Type.VAR_INT, 1); // slot
        wrapper.write(Type.ITEM1_8, this.itemInSecondHand = item);
        try {
            wrapper.scheduleSend(Protocol1_9To1_8.class);
        } catch (Exception e) {
            Via.getPlatform().getLogger().log(Level.WARNING, "Failed to send second hand item", e);
        }
    }

    public Item getItemInSecondHand() {
        return itemInSecondHand;
    }

    /**
     * It will set a shield to the offhand if a sword is in the main hand.
     * The item in the offhand will be cleared if there is no sword in the main hand.
     */
    public void syncShieldWithSword() {
        boolean swordInHand = hasSwordInHand();

        // Update if there is no sword in the main hand or if the player has no shield in the second hand but a sword in the main hand
        if (!swordInHand || this.itemInSecondHand == null) {

            // Update shield in off-hand depending on whether a sword is in the main hand
            setSecondHand(swordInHand ? new DataItem(442, (byte) 1, (short) 0, null) : null);
        }
    }

    /**
     * Returns true if the item in the held inventory slot is a sword.
     *
     * @return player has a sword in the main hand
     */
    public boolean hasSwordInHand() {
        InventoryTracker inventoryTracker = user().get(InventoryTracker.class);

        // Get item in new selected slot
        int inventorySlot = this.heldItemSlot + 36; // Hotbar slot index to inventory slot
        int itemIdentifier = inventoryTracker.getItemId((short) 0, (short) inventorySlot);

        return Protocol1_9To1_8.isSword(itemIdentifier);
    }

    @Override
    public void removeEntity(int entityId) {
        super.removeEntity(entityId);

        vehicleMap.remove(entityId);
        uuidMap.remove(entityId);
        validBlocking.remove(entityId);
        knownHolograms.remove(entityId);

        BossBar bar = bossBarMap.remove(entityId);
        if (bar != null) {
            bar.hide();
            // Send to provider
            Via.getManager().getProviders().get(BossBarProvider.class).handleRemove(user(), bar.getId());
        }
    }

    public boolean interactedBlockRecently(int x, int y, int z) {
        return blockInteractions.contains(new Position(x, y, z));
    }

    public void addBlockInteraction(Position p) {
        blockInteractions.add(p);
    }

    public void handleMetadata(int entityId, List<Metadata> metadataList) {
        com.viaversion.viaversion.api.minecraft.entities.EntityType type = entityType(entityId);
        if (type == null) {
            return;
        }

        for (Metadata metadata : new ArrayList<>(metadataList)) {
            if (type == EntityType.SKELETON) {
                if ((getMetaByIndex(metadataList, 12)) == null) {
                    metadataList.add(new Metadata(12, MetaType1_9.Boolean, true));
                }
            }

            // 1.8 can handle out of range values and will just not show any armor, 1.9+ clients will get
            // exceptions and won't render the entity at all
            if (type == EntityType.HORSE && metadata.id() == 16) {
                final int value = metadata.value();
                if (value < 0 || value > 3) { // no armor, iron armor, gold armor and diamond armor
                    metadata.setValue(0);
                }
            }

            if (type == EntityType.PLAYER) {
                if (metadata.id() == 0) {
                    // Byte
                    byte data = (byte) metadata.getValue();
                    if (entityId != getProvidedEntityId() && Via.getConfig().isShieldBlocking()) {
                        if ((data & 0x10) == 0x10) {
                            if (validBlocking.contains(entityId)) {
                                Item shield = new DataItem(442, (byte) 1, (short) 0, null);
                                setSecondHand(entityId, shield);
                            } else {
                                setSecondHand(entityId, null);
                            }
                        } else {
                            setSecondHand(entityId, null);
                        }
                    }
                }
                if (metadata.id() == 12 && Via.getConfig().isLeftHandedHandling()) { // Player model
                    metadataList.add(new Metadata(
                            13, // Main hand
                            MetaType1_9.Byte,
                            (byte) (((((byte) metadata.getValue()) & 0x80) != 0) ? 0 : 1)
                    ));
                }
            }
            if (type == EntityType.ARMOR_STAND && Via.getConfig().isHologramPatch()) {
                if (metadata.id() == 0 && getMetaByIndex(metadataList, 10) != null) {
                    Metadata meta = getMetaByIndex(metadataList, 10); //Only happens if the armorstand is small
                    byte data = (byte) metadata.getValue();
                    // Check invisible | Check small | Check if custom name is empty | Check if custom name visible is true
                    Metadata displayName;
                    Metadata displayNameVisible;
                    if ((data & 0x20) == 0x20 && ((byte) meta.getValue() & 0x01) == 0x01
                            && (displayName = getMetaByIndex(metadataList, 2)) != null && !((String) displayName.getValue()).isEmpty()
                            && (displayNameVisible = getMetaByIndex(metadataList, 3)) != null && (boolean) displayNameVisible.getValue()) {
                        if (!knownHolograms.contains(entityId)) {
                            knownHolograms.add(entityId);
                            try {
                                // Send movement
                                PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_9.ENTITY_POSITION, null, user());
                                wrapper.write(Type.VAR_INT, entityId);
                                wrapper.write(Type.SHORT, (short) 0);
                                wrapper.write(Type.SHORT, (short) (128D * (Via.getConfig().getHologramYOffset() * 32D)));
                                wrapper.write(Type.SHORT, (short) 0);
                                wrapper.write(Type.BOOLEAN, true);
                                wrapper.scheduleSend(Protocol1_9To1_8.class);
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            }
            // Boss bar
            if (Via.getConfig().isBossbarPatch()) {
                if (type == EntityType.ENDER_DRAGON || type == EntityType.WITHER) {
                    if (metadata.id() == 2) {
                        BossBar bar = bossBarMap.get(entityId);
                        String title = (String) metadata.getValue();
                        title = title.isEmpty() ? (type == EntityType.ENDER_DRAGON ? DRAGON_TRANSLATABLE : WITHER_TRANSLATABLE) : title;
                        if (bar == null) {
                            bar = Via.getAPI().legacyAPI().createLegacyBossBar(title, BossColor.PINK, BossStyle.SOLID);
                            bossBarMap.put(entityId, bar);
                            bar.addConnection(user());
                            bar.show();

                            // Send to provider
                            Via.getManager().getProviders().get(BossBarProvider.class).handleAdd(user(), bar.getId());
                        } else {
                            bar.setTitle(title);
                        }
                    } else if (metadata.id() == 6 && !Via.getConfig().isBossbarAntiflicker()) { // If anti flicker is enabled, don't update health
                        BossBar bar = bossBarMap.get(entityId);
                        // Make health range between 0 and 1
                        float maxHealth = type == EntityType.ENDER_DRAGON ? 200.0f : 300.0f;
                        float health = Math.max(0.0f, Math.min(((float) metadata.getValue()) / maxHealth, 1.0f));
                        if (bar == null) {
                            String title = type == EntityType.ENDER_DRAGON ? DRAGON_TRANSLATABLE : WITHER_TRANSLATABLE;
                            bar = Via.getAPI().legacyAPI().createLegacyBossBar(title, health, BossColor.PINK, BossStyle.SOLID);
                            bossBarMap.put(entityId, bar);
                            bar.addConnection(user());
                            bar.show();
                            // Send to provider
                            Via.getManager().getProviders().get(BossBarProvider.class).handleAdd(user(), bar.getId());
                        } else {
                            bar.setHealth(health);
                        }
                    }
                }
            }
        }
    }

    public Metadata getMetaByIndex(List<Metadata> list, int index) {
        for (Metadata meta : list)
            if (index == meta.id()) {
                return meta;
            }
        return null;
    }

    public void sendTeamPacket(boolean add, boolean now) {
        PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_9.TEAMS, null, user());
        wrapper.write(Type.STRING, "viaversion"); // Use viaversion as name
        if (add) {
            // add
            if (!teamExists) {
                wrapper.write(Type.BYTE, (byte) 0); // make team
                wrapper.write(Type.STRING, "viaversion");
                wrapper.write(Type.STRING, "§f"); // prefix
                wrapper.write(Type.STRING, ""); // suffix
                wrapper.write(Type.BYTE, (byte) 0); // friendly fire
                wrapper.write(Type.STRING, ""); // nametags
                wrapper.write(Type.STRING, "never"); // collision rule :)
                wrapper.write(Type.BYTE, (byte) 15); // color
            } else {
                wrapper.write(Type.BYTE, (byte) 3);
            }
            wrapper.write(Type.STRING_ARRAY, new String[]{user().getProtocolInfo().getUsername()});
        } else {
            wrapper.write(Type.BYTE, (byte) 1); // remove team
        }
        teamExists = add;
        try {
            if (now) {
                wrapper.send(Protocol1_9To1_8.class);
            } else {
                wrapper.scheduleSend(Protocol1_9To1_8.class);
            }
        } catch (Exception e) {
            Via.getPlatform().getLogger().log(Level.WARNING, "Failed to send team packet", e);
        }
    }

    public int getProvidedEntityId() {
        try {
            return Via.getManager().getProviders().get(EntityIdProvider.class).getEntityId(user());
        } catch (Exception e) {
            return clientEntityId();
        }
    }

    public Int2ObjectMap<UUID> getUuidMap() {
        return uuidMap;
    }

    public Int2IntMap getVehicleMap() {
        return vehicleMap;
    }

    public Int2ObjectMap<BossBar> getBossBarMap() {
        return bossBarMap;
    }

    public IntSet getValidBlocking() {
        return validBlocking;
    }

    public IntSet getKnownHolograms() {
        return knownHolograms;
    }

    public Set<Position> getBlockInteractions() {
        return blockInteractions;
    }

    public boolean isBlocking() {
        return blocking;
    }

    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }

    public boolean isAutoTeam() {
        return autoTeam;
    }

    public void setAutoTeam(boolean autoTeam) {
        this.autoTeam = autoTeam;
    }

    public Position getCurrentlyDigging() {
        return currentlyDigging;
    }

    public void setCurrentlyDigging(Position currentlyDigging) {
        this.currentlyDigging = currentlyDigging;
    }

    public boolean isTeamExists() {
        return teamExists;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public String getCurrentTeam() {
        return currentTeam;
    }

    public void setCurrentTeam(String currentTeam) {
        this.currentTeam = currentTeam;
    }

    public void setHeldItemSlot(int heldItemSlot) {
        this.heldItemSlot = heldItemSlot;
    }
}
