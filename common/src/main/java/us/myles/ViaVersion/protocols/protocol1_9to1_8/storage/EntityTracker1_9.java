package us.myles.ViaVersion.protocols.protocol1_9to1_8.storage;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.boss.BossBar;
import us.myles.ViaVersion.api.boss.BossColor;
import us.myles.ViaVersion.api.boss.BossStyle;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_10Types.EntityType;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_9;
import us.myles.ViaVersion.api.storage.EntityTracker;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.version.Types1_9;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.chat.GameMode;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.metadata.MetadataRewriter1_9To1_8;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.BossBarProvider;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.EntityIdProvider;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Getter
public class EntityTracker1_9 extends EntityTracker {
    private final Map<Integer, UUID> uuidMap = new ConcurrentHashMap<>();
    private final Map<Integer, List<Metadata>> metadataBuffer = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> vehicleMap = new ConcurrentHashMap<>();
    private final Map<Integer, BossBar> bossBarMap = new ConcurrentHashMap<>();
    private final Set<Integer> validBlocking = Sets.newConcurrentHashSet();
    private final Set<Integer> knownHolograms = Sets.newConcurrentHashSet();
    private final Set<Position> blockInteractions = Collections.newSetFromMap(CacheBuilder.newBuilder()
            .maximumSize(10)
            .expireAfterAccess(250, TimeUnit.MILLISECONDS)
            .<Position, Boolean>build()
            .asMap());
    @Setter
    private boolean blocking = false;
    @Setter
    private boolean autoTeam = false;
    @Setter
    private Position currentlyDigging = null;
    private boolean teamExists = false;
    @Setter
    private GameMode gameMode;
    @Setter
    private String currentTeam;

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
        setSecondHand(getClientEntityId(), item);
    }

    public void setSecondHand(int entityID, Item item) {
        PacketWrapper wrapper = new PacketWrapper(0x3C, null, getUser());
        wrapper.write(Type.VAR_INT, entityID);
        wrapper.write(Type.VAR_INT, 1); // slot
        wrapper.write(Type.ITEM, item);
        try {
            wrapper.send(Protocol1_9To1_8.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeEntity(int entityId) {
        super.removeEntity(entityId);

        vehicleMap.remove(entityId);
        uuidMap.remove(entityId);
        validBlocking.remove(entityId);
        knownHolograms.remove(entityId);
        metadataBuffer.remove(entityId);

        BossBar bar = bossBarMap.remove(entityId);
        if (bar != null) {
            bar.hide();
            // Send to provider
            Via.getManager().getProviders().get(BossBarProvider.class).handleRemove(getUser(), bar.getId());
        }
    }

    public boolean interactedBlockRecently(int x, int y, int z) {
        return blockInteractions.contains(new Position(x, (short) y , z));
    }

    public void addBlockInteraction(Position p) {
        blockInteractions.add(p);
    }

    public void handleMetadata(int entityId, List<Metadata> metadataList) {
        us.myles.ViaVersion.api.entities.EntityType type = getEntity(entityId);
        if (type == null) {
            return;
        }

        for (Metadata metadata : new ArrayList<>(metadataList)) {
            // Fix: wither (crash fix)
            if (type == EntityType.WITHER) {
                if (metadata.getId() == 10) {
                    metadataList.remove(metadata);
                    //metadataList.add(new Metadata(10, NewType.Byte.getTypeID(), Type.BYTE, 0));
                }
            }
            // Fix: enderdragon (crash fix)
            if (type == EntityType.ENDER_DRAGON) {
                if (metadata.getId() == 11) {
                    metadataList.remove(metadata);
                    //   metadataList.add(new Metadata(11, NewType.Byte.getTypeID(), Type.VAR_INT, 0));
                }
            }

            if (type == EntityType.SKELETON) {
                if ((getMetaByIndex(metadataList, 12)) == null) {
                    metadataList.add(new Metadata(12, MetaType1_9.Boolean, true));
                }
            }

            //ECHOPET Patch
            if (type == EntityType.HORSE) {
                // Wrong metadata value from EchoPet, patch since it's discontinued. (https://github.com/DSH105/EchoPet/blob/06947a8b08ce40be9a518c2982af494b3b99d140/modules/API/src/main/java/com/dsh105/echopet/compat/api/entity/HorseArmour.java#L22)
                if (metadata.getId() == 16 && (int) metadata.getValue() == Integer.MIN_VALUE)
                    metadata.setValue(0);
            }

            if (type == EntityType.PLAYER) {
                if (metadata.getId() == 0) {
                    // Byte
                    byte data = (byte) metadata.getValue();
                    if (entityId != getProvidedEntityId() && Via.getConfig().isShieldBlocking()) {
                        if ((data & 0x10) == 0x10) {
                            if (validBlocking.contains(entityId)) {
                                Item shield = new Item(442, (byte) 1, (short) 0, null);
                                setSecondHand(entityId, shield);
                            } else {
                                setSecondHand(entityId, null);
                            }
                        } else {
                            setSecondHand(entityId, null);
                        }
                    }
                }
                if (metadata.getId() == 12 && Via.getConfig().isLeftHandedHandling()) { // Player model
                    metadataList.add(new Metadata(
                            13, // Main hand
                            MetaType1_9.Byte,
                            (byte) (((((byte) metadata.getValue()) & 0x80) != 0) ? 0 : 1)
                    ));
                }
            }
            if (type == EntityType.ARMOR_STAND && Via.getConfig().isHologramPatch()) {
                if (metadata.getId() == 0 && getMetaByIndex(metadataList, 10) != null) {
                    Metadata meta = getMetaByIndex(metadataList, 10); //Only happens if the armorstand is small
                    byte data = (byte) metadata.getValue();
                    // Check invisible | Check small | Check if custom name is empty | Check if custom name visible is true
                    if ((data & 0x20) == 0x20 && ((byte) meta.getValue() & 0x01) == 0x01
                            && !((String) getMetaByIndex(metadataList, 2).getValue()).isEmpty() && (boolean) getMetaByIndex(metadataList, 3).getValue()) {
                        if (!knownHolograms.contains(entityId)) {
                            knownHolograms.add(entityId);
                            try {
                                // Send movement
                                PacketWrapper wrapper = new PacketWrapper(0x25, null, getUser());
                                wrapper.write(Type.VAR_INT, entityId);
                                wrapper.write(Type.SHORT, (short) 0);
                                wrapper.write(Type.SHORT, (short) (128D * (Via.getConfig().getHologramYOffset() * 32D)));
                                wrapper.write(Type.SHORT, (short) 0);
                                wrapper.write(Type.BOOLEAN, true);
                                wrapper.send(Protocol1_9To1_8.class, true, false);
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            }
            UUID uuid = getUser().get(ProtocolInfo.class).getUuid();
            // Boss bar
            if (Via.getConfig().isBossbarPatch()) {
                if (type == EntityType.ENDER_DRAGON || type == EntityType.WITHER) {
                    if (metadata.getId() == 2) {
                        BossBar bar = bossBarMap.get(entityId);
                        String title = (String) metadata.getValue();
                        title = title.isEmpty() ? (type == EntityType.ENDER_DRAGON ? "Ender Dragon" : "Wither") : title;
                        if (bar == null) {
                            bar = Via.getAPI().createBossBar(title, BossColor.PINK, BossStyle.SOLID);
                            bossBarMap.put(entityId, bar);
                            bar.addPlayer(uuid);
                            bar.show();

                            // Send to provider
                            Via.getManager().getProviders().get(BossBarProvider.class).handleAdd(getUser(), bar.getId());
                        } else {
                            bar.setTitle(title);
                        }
                    } else if (metadata.getId() == 6 && !Via.getConfig().isBossbarAntiflicker()) { // If anti flicker is enabled, don't update health
                        BossBar bar = bossBarMap.get(entityId);
                        // Make health range between 0 and 1
                        float maxHealth = type == EntityType.ENDER_DRAGON ? 200.0f : 300.0f;
                        float health = Math.max(0.0f, Math.min(((float) metadata.getValue()) / maxHealth, 1.0f));
                        if (bar == null) {
                            String title = type == EntityType.ENDER_DRAGON ? "Ender Dragon" : "Wither";
                            bar = Via.getAPI().createBossBar(title, health, BossColor.PINK, BossStyle.SOLID);
                            bossBarMap.put(entityId, bar);
                            bar.addPlayer(uuid);
                            bar.show();
                            // Send to provider
                            Via.getManager().getProviders().get(BossBarProvider.class).handleAdd(getUser(), bar.getId());
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
            if (index == meta.getId())
                return meta;
        return null;
    }

    public void sendTeamPacket(boolean add, boolean now) {
        PacketWrapper wrapper = new PacketWrapper(0x41, null, getUser());
        wrapper.write(Type.STRING, "viaversion"); // Use viaversion as name
        if (add) {
            // add
            if (!teamExists) {
                wrapper.write(Type.BYTE, (byte) 0); // make team
                wrapper.write(Type.STRING, "viaversion");
                wrapper.write(Type.STRING, ""); // prefix
                wrapper.write(Type.STRING, ""); // suffix
                wrapper.write(Type.BYTE, (byte) 0); // friendly fire
                wrapper.write(Type.STRING, ""); // nametags
                wrapper.write(Type.STRING, "never"); // collision rule :)
                wrapper.write(Type.BYTE, (byte) 0); // color
            } else {
                wrapper.write(Type.BYTE, (byte) 3);
            }
            wrapper.write(Type.STRING_ARRAY, new String[]{getUser().get(ProtocolInfo.class).getUsername()});
        } else {
            wrapper.write(Type.BYTE, (byte) 1); // remove team
        }
        teamExists = add;
        try {
            wrapper.send(Protocol1_9To1_8.class, true, now);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addMetadataToBuffer(int entityID, List<Metadata> metadataList) {
        final List<Metadata> metadata = metadataBuffer.get(entityID);
        if (metadata != null) {
            metadata.addAll(metadataList);
        } else {
            metadataBuffer.put(entityID, metadataList);
        }
    }

    public void sendMetadataBuffer(int entityId) {
        List<Metadata> metadataList = metadataBuffer.get(entityId);
        if (metadataList != null) {
            PacketWrapper wrapper = new PacketWrapper(0x39, null, getUser());
            wrapper.write(Type.VAR_INT, entityId);
            wrapper.write(Types1_9.METADATA_LIST, metadataList);
            getUser().get(ProtocolInfo.class).getPipeline().getProtocol(Protocol1_9To1_8.class).get(MetadataRewriter1_9To1_8.class)
                    .handleMetadata(entityId, metadataList, getUser());
            handleMetadata(entityId, metadataList);
            if (!metadataList.isEmpty()) {
                try {
                    wrapper.send(Protocol1_9To1_8.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            metadataBuffer.remove(entityId);
        }
    }

    public int getProvidedEntityId() {
        try {
            return Via.getManager().getProviders().get(EntityIdProvider.class).getEntityId(getUser());
        } catch (Exception e) {
            return getClientEntityId();
        }
    }
}
