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
package com.viaversion.viaversion.protocols.v1_8to1_9.storage;

import com.google.common.cache.CacheBuilder;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.TrackedEntity;
import com.viaversion.viaversion.api.legacy.bossbar.BossBar;
import com.viaversion.viaversion.api.legacy.bossbar.BossColor;
import com.viaversion.viaversion.api.legacy.bossbar.BossStyle;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.GameMode;
import com.viaversion.viaversion.api.minecraft.Vector3d;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_9.EntityType;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_9;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.v1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.provider.BossBarProvider;
import com.viaversion.viaversion.protocols.v1_8to1_9.provider.EntityIdProvider;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.UUIDUtil;
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
import org.checkerframework.checker.nullness.qual.Nullable;

public class EntityTracker1_9 extends EntityTrackerBase {
    public static final String WITHER_TRANSLATABLE = "{\"translate\":\"entity.WitherBoss.name\"}";
    public static final String DRAGON_TRANSLATABLE = "{\"translate\":\"entity.EnderDragon.name\"}";
    // 1.8 Entity#isInRangeToRenderDist: average bounding box edge length * 64
    private static final double WITHER_RENDER_DISTANCE = (0.9 + 3.5 + 0.9) / 3 * 64;
    private static final double DRAGON_RENDER_DISTANCE = (16.0 + 8.0 + 16.0) / 3 * 64;
    private final Int2ObjectMap<UUID> uuidMap = new Int2ObjectOpenHashMap<>();
    private final Int2IntMap vehicleMap = new Int2IntOpenHashMap();
    private final Int2ObjectMap<BossBar> bossBarMap = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Vector3d> bossPositions = new Int2ObjectOpenHashMap<>();
    private BossBar shownBossBar;
    private Vector3d playerPosition = Vector3d.ZERO;
    private final IntSet validBlocking = new IntOpenHashSet();
    private final IntSet knownHolograms = new IntOpenHashSet();
    private final Set<BlockPosition> blockInteractions = Collections.newSetFromMap(CacheBuilder.newBuilder()
        .maximumSize(1000)
        .expireAfterAccess(250, TimeUnit.MILLISECONDS)
        .<BlockPosition, Boolean>build()
        .asMap());
    private boolean blocking;
    private boolean autoTeam;
    private BlockPosition currentlyDigging;
    private boolean teamExists;
    private GameMode gameMode;
    private String currentTeam;
    private int heldItemSlot;
    private Item itemInSecondHand;

    public EntityTracker1_9(UserConnection user) {
        super(user, EntityType.PLAYER);
    }

    public UUID getEntityUUID(int id) {
        return uuidMap.computeIfAbsent(id, k -> UUIDUtil.randomUUID());
    }

    public void setSecondHand(Item item) {
        setSecondHand(clientEntityId(), item);
    }

    public void setSecondHand(int entityID, Item item) {
        PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_9.SET_EQUIPPED_ITEM, null, user());
        wrapper.write(Types.VAR_INT, entityID);
        wrapper.write(Types.VAR_INT, 1); // slot
        wrapper.write(Types.ITEM1_8, this.itemInSecondHand = item);
        wrapper.scheduleSend(Protocol1_8To1_9.class);
    }

    public Item getItemInSecondHand() {
        return itemInSecondHand;
    }

    /**
     * It will set a shield to the offhand if a sword is in the main hand.
     * The item in the offhand will be cleared if there is no sword in the main hand.
     */
    public void syncShieldWithSword() {
        if (user().getProtocolInfo().protocolVersion().newerThanOrEqualTo(ProtocolVersion.v1_21_4)) {
            // If sword blocking is done through consumables, don't add a shield.
            return;
        }

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
        int itemIdentifier = inventoryTracker.getItemId(0, (short) inventorySlot);

        return Protocol1_8To1_9.isSword(itemIdentifier);
    }

    @Override
    public @Nullable TrackedEntity removeEntity(int entityId) {
        vehicleMap.remove(entityId);
        uuidMap.remove(entityId);
        validBlocking.remove(entityId);
        knownHolograms.remove(entityId);

        this.bossPositions.remove(entityId);
        BossBar bar = this.bossBarMap.remove(entityId);
        if (bar != null && bar == this.shownBossBar) {
            this.updateShownBossBar();
        }

        return super.removeEntity(entityId);
    }

    public void setBossPosition(final int entityId, final Vector3d position) {
        final com.viaversion.viaversion.api.minecraft.entities.EntityType type = this.entityType(entityId);
        if (type != EntityType.WITHER && type != EntityType.ENDER_DRAGON) {
            return;
        }

        this.bossPositions.put(entityId, position);
        this.updateShownBossBar();
    }

    public void moveBoss(final int entityId, final double deltaX, final double deltaY, final double deltaZ) {
        final Vector3d position = this.bossPositions.get(entityId);
        if (position != null) {
            this.setBossPosition(entityId, new Vector3d(position.x() + deltaX, position.y() + deltaY, position.z() + deltaZ));
        }
    }

    public Vector3d getPlayerPosition() {
        return this.playerPosition;
    }

    public void setPlayerPosition(final Vector3d playerPosition) {
        this.playerPosition = playerPosition;
        this.updateShownBossBar();
    }

    private void updateShownBossBar() {
        BossBar best = null;
        int bestSectionDistance = -1;
        for (final Int2ObjectMap.Entry<BossBar> entry : this.bossBarMap.int2ObjectEntrySet()) {
            final Vector3d position = this.bossPositions.get(entry.getIntKey());
            if (position == null) {
                continue;
            }

            final double deltaX = position.x() - this.playerPosition.x();
            final double deltaY = position.y() - this.playerPosition.y();
            final double deltaZ = position.z() - this.playerPosition.z();
            final double range = this.entityType(entry.getIntKey()) == EntityType.ENDER_DRAGON ? DRAGON_RENDER_DISTANCE : WITHER_RENDER_DISTANCE;
            if (deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ >= range * range) {
                continue;
            }

            final int distanceX = Math.abs(sectionCoord(position.x()) - sectionCoord(this.playerPosition.x()));
            final int distanceY = Math.abs(sectionCoord(position.y()) - sectionCoord(this.playerPosition.y()));
            final int distanceZ = Math.abs(sectionCoord(position.z()) - sectionCoord(this.playerPosition.z()));
            final int sectionDistance = distanceX + distanceY + distanceZ;
            // Prefer the currently shown bar on ties to avoid needless switching
            if (sectionDistance > bestSectionDistance || (sectionDistance == bestSectionDistance && entry.getValue() == shownBossBar)) {
                best = entry.getValue();
                bestSectionDistance = sectionDistance;
            }
        }
        this.showBossBar(best);
    }

    private static int sectionCoord(double coord) {
        return (int) Math.floor(coord) >> 4;
    }

    private void showBossBar(@Nullable final BossBar bar) {
        if (bar == this.shownBossBar) {
            return;
        }

        final BossBarProvider provider = Via.getManager().getProviders().get(BossBarProvider.class);
        if (this.shownBossBar != null) {
            this.shownBossBar.removeConnection(user());
            provider.handleRemove(user(), this.shownBossBar.getId());
        }
        this.shownBossBar = bar;
        if (bar != null) {
            bar.addConnection(user());
            provider.handleAdd(user(), bar.getId());
        }
    }

    public boolean interactedBlockRecently(final int x, final int y, final int z) {
        for (final BlockPosition position : blockInteractions) {
            if (Math.abs(position.x() - x) <= 1 && Math.abs(position.y() - y) <= 1 && Math.abs(position.z() - z) <= 1) {
                return true;
            }
        }
        return false;
    }

    public void addBlockInteraction(BlockPosition p) {
        blockInteractions.add(p);
    }

    public void handleEntityData(int entityId, List<EntityData> entityDataList) {
        com.viaversion.viaversion.api.minecraft.entities.EntityType type = entityType(entityId);
        if (type == null) {
            return;
        }

        for (EntityData entityData : new ArrayList<>(entityDataList)) {
            if (type == EntityType.SKELETON) {
                if ((getDataByIndex(entityDataList, 12)) == null) {
                    entityDataList.add(new EntityData(12, EntityDataTypes1_9.BOOLEAN, true));
                }
            }

            // 1.8 can handle out of range values and will just not show any armor, 1.9+ clients will get
            // exceptions and won't render the entity at all
            if (type == EntityType.HORSE && entityData.id() == 16) {
                final int value = entityData.value();
                if (value < 0 || value > 3) { // no armor, iron armor, gold armor and diamond armor
                    entityData.setValue(0);
                }
            }

            if (type == EntityType.PLAYER) {
                if (entityData.id() == 0) {
                    // Byte
                    byte data = (byte) entityData.getValue();
                    // If sword blocking is done through consumables (1.21.4+), don't add a shield.
                    if (entityId != getProvidedEntityId() && Via.getConfig().isShieldBlocking()
                        && user().getProtocolInfo().protocolVersion().olderThan(ProtocolVersion.v1_21_4)) {
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
                if (entityData.id() == 12 && Via.getConfig().isLeftHandedHandling()) { // Player model
                    entityDataList.add(new EntityData(
                        13, // Main hand
                        EntityDataTypes1_9.BYTE,
                        (byte) (((((byte) entityData.getValue()) & 0x80) != 0) ? 0 : 1)
                    ));
                }
            }
            if (type == EntityType.ARMOR_STAND && Via.getConfig().isHologramPatch()) {
                if (entityData.id() == 0 && getDataByIndex(entityDataList, 10) != null) {
                    EntityData data = getDataByIndex(entityDataList, 10); //Only happens if the armorstand is small
                    byte value = (byte) entityData.getValue();
                    // Check invisible | Check small | Check if custom name is empty | Check if custom name visible is true
                    EntityData displayName;
                    EntityData displayNameVisible;
                    if ((value & 0x20) == 0x20 && ((byte) data.getValue() & 0x01) == 0x01
                        && (displayName = getDataByIndex(entityDataList, 2)) != null && !((String) displayName.getValue()).isEmpty()
                        && (displayNameVisible = getDataByIndex(entityDataList, 3)) != null && (boolean) displayNameVisible.getValue()) {
                        if (!knownHolograms.contains(entityId)) {
                            knownHolograms.add(entityId);
                            // Send movement
                            PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_9.MOVE_ENTITY_POS, null, user());
                            wrapper.write(Types.VAR_INT, entityId);
                            wrapper.write(Types.SHORT, (short) 0);
                            wrapper.write(Types.SHORT, (short) (128D * (Via.getConfig().getHologramYOffset() * 32D)));
                            wrapper.write(Types.SHORT, (short) 0);
                            wrapper.write(Types.BOOLEAN, true);
                            wrapper.scheduleSend(Protocol1_8To1_9.class);
                        }
                    }
                }
            }
            // Boss bar
            if (Via.getConfig().isBossbarPatch()) {
                if (type == EntityType.ENDER_DRAGON || type == EntityType.WITHER) {
                    if (entityData.id() == 2) {
                        BossBar bar = bossBarMap.get(entityId);
                        String title = (String) entityData.getValue();
                        if (title.isEmpty()) {
                            title = type == EntityType.ENDER_DRAGON ? DRAGON_TRANSLATABLE : WITHER_TRANSLATABLE;
                        } else {
                            title = ComponentUtil.plainToJson(title).toString();
                        }
                        if (bar == null) {
                            bar = Via.getAPI().legacyAPI().createLegacyBossBar(title, BossColor.PINK, BossStyle.SOLID);
                            bossBarMap.put(entityId, bar);
                            updateShownBossBar();
                        } else {
                            bar.setTitle(title);
                        }
                    } else if (entityData.id() == 6 && !Via.getConfig().isBossbarAntiflicker()) { // If anti flicker is enabled, don't update health
                        BossBar bar = bossBarMap.get(entityId);
                        // Make health range between 0 and 1
                        float maxHealth = type == EntityType.ENDER_DRAGON ? 200.0f : 300.0f;
                        float health = Math.max(0.0f, Math.min(((float) entityData.getValue()) / maxHealth, 1.0f));
                        if (bar == null) {
                            String title = type == EntityType.ENDER_DRAGON ? DRAGON_TRANSLATABLE : WITHER_TRANSLATABLE;
                            bar = Via.getAPI().legacyAPI().createLegacyBossBar(title, health, BossColor.PINK, BossStyle.SOLID);
                            bossBarMap.put(entityId, bar);
                            updateShownBossBar();
                        } else {
                            bar.setHealth(health);
                        }
                    }
                }
            }
        }
    }

    public EntityData getDataByIndex(List<EntityData> list, int index) {
        for (EntityData data : list)
            if (index == data.id()) {
                return data;
            }
        return null;
    }

    public void sendTeamPacket(boolean add, boolean now) {
        PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_9.SET_PLAYER_TEAM, null, user());
        wrapper.write(Types.STRING, "viaversion"); // Use viaversion as name
        if (add) {
            // add
            if (!teamExists) {
                wrapper.write(Types.BYTE, (byte) 0); // make team
                wrapper.write(Types.STRING, "viaversion");
                wrapper.write(Types.STRING, "§f"); // prefix
                wrapper.write(Types.STRING, ""); // suffix
                wrapper.write(Types.BYTE, (byte) 0); // friendly fire
                wrapper.write(Types.STRING, ""); // nametags
                wrapper.write(Types.STRING, "never"); // collision rule :)
                wrapper.write(Types.BYTE, (byte) 15); // color
            } else {
                wrapper.write(Types.BYTE, (byte) 3);
            }
            wrapper.write(Types.STRING_ARRAY, new String[]{user().getProtocolInfo().getUsername()});
        } else {
            wrapper.write(Types.BYTE, (byte) 1); // remove team
        }
        teamExists = add;
        if (now) {
            wrapper.send(Protocol1_8To1_9.class);
        } else {
            wrapper.scheduleSend(Protocol1_8To1_9.class);
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

    public Set<BlockPosition> getBlockInteractions() {
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

    public BlockPosition getCurrentlyDigging() {
        return currentlyDigging;
    }

    public void setCurrentlyDigging(BlockPosition currentlyDigging) {
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
