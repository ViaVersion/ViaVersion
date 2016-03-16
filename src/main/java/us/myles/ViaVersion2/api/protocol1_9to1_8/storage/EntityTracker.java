package us.myles.ViaVersion2.api.protocol1_9to1_8.storage;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.api.boss.BossBar;
import us.myles.ViaVersion.api.boss.BossColor;
import us.myles.ViaVersion.api.boss.BossStyle;
import us.myles.ViaVersion.metadata.NewType;
import us.myles.ViaVersion.util.PacketUtil;
import us.myles.ViaVersion2.api.PacketWrapper;
import us.myles.ViaVersion2.api.data.StoredObject;
import us.myles.ViaVersion2.api.data.UserConnection;
import us.myles.ViaVersion2.api.item.Item;
import us.myles.ViaVersion2.api.metadata.Metadata;
import us.myles.ViaVersion2.api.protocol.base.ProtocolInfo;
import us.myles.ViaVersion2.api.type.Type;

import java.util.*;

@Getter
public class EntityTracker extends StoredObject {
    private final Map<Integer, UUID> uuidMap = new HashMap<>();
    private final Map<Integer, EntityType> clientEntityTypes = new HashMap<>();
    private final Map<Integer, Integer> vehicleMap = new HashMap<>();
    private final Map<Integer, BossBar> bossBarMap = new HashMap<>();
    private final Set<Integer> validBlocking = new HashSet<>();
    private final Set<Integer> knownHolograms = new HashSet<>();
    @Getter
    @Setter
    private boolean blocking = false;
    @Setter
    private int entityID;

    public EntityTracker(UserConnection user) {
        super(user);
    }

    public UUID getEntityUUID(int id) {
        if (uuidMap.containsKey(id)) {
            return uuidMap.get(id);
        } else {
            UUID uuid = UUID.randomUUID();
            uuidMap.put(id, uuid);
            return uuid;
        }
    }

    public void setSecondHand(UserConnection connection, Item item) {
        PacketWrapper wrapper = new PacketWrapper(0x3C, null, connection);
        wrapper.write(Type.VAR_INT, entityID);
        wrapper.write(Type.VAR_INT, 1); // slot
        wrapper.write(Type.ITEM, item);
        try {
            wrapper.send();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeEntity(Integer entityID) {
        clientEntityTypes.remove(entityID);
        vehicleMap.remove(entityID);
        uuidMap.remove(entityID);
        validBlocking.remove(entityID);
        knownHolograms.remove(entityID);

        BossBar bar = bossBarMap.remove(entityID);
        if (bar != null) {
            bar.hide();
        }
    }

    public void handleMetadata(int entityID, List<Metadata> metadataList) {
        if (!clientEntityTypes.containsKey(entityID)) return;

        EntityType type = clientEntityTypes.get(entityID);
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

            if (type == EntityType.PLAYER) {
                if (metadata.getId() == 0) {
                    // Byte
                    byte data = (byte) metadata.getValue();
                    if (entityID != getEntityID() && ((ViaVersionPlugin) ViaVersion.getInstance()).isShieldBlocking()) {
                        if ((data & 0x10) == 0x10) {
                            if (validBlocking.contains(entityID)) {
                                Item shield = new Item((short) 442, (byte) 1, (short) 0, null);
                                setSecondHand(getUser(), shield);
                            }
                        } else {
                            setSecondHand(getUser(), null);
                        }
                    }
                }
            }
            if (type == EntityType.ARMOR_STAND && ((ViaVersionPlugin) ViaVersion.getInstance()).isHologramPatch()) {
                if (metadata.getId() == 0) {
                    byte data = (byte) metadata.getValue();
                    if ((data & 0x20) == 0x20) {
                        if (!knownHolograms.contains(entityID)) {
                            knownHolograms.add(entityID);
                            // Send movement
                            ByteBuf buf = getUser().getChannel().alloc().buffer();
                            PacketUtil.writeVarInt(0x25, buf); // Relative Move Packet
                            PacketUtil.writeVarInt(entityID, buf);
                            buf.writeShort(0);
                            buf.writeShort((short) (128D * (((ViaVersionPlugin) ViaVersion.getInstance()).getHologramYOffset() * 32D)));
                            buf.writeShort(0);
                            buf.writeBoolean(true);
                            getUser().sendRawPacket(buf, false);
                        }
                    }
                }
            }
            Player player = Bukkit.getPlayer(getUser().get(ProtocolInfo.class).getUuid());
            // Boss bar
            if (((ViaVersionPlugin) ViaVersion.getInstance()).isBossbarPatch()) {
                if (type == EntityType.ENDER_DRAGON || type == EntityType.WITHER) {
                    if (metadata.getId() == 2) {
                        BossBar bar = bossBarMap.get(entityID);
                        String title = (String) metadata.getValue();
                        title = title.isEmpty() ? (type == EntityType.ENDER_DRAGON ? "Ender Dragon" : "Wither") : title;
                        if (bar == null) {
                            bar = ViaVersion.getInstance().createBossBar(title, BossColor.PINK, BossStyle.SOLID);
                            bossBarMap.put(entityID, bar);
                            bar.addPlayer(player);
                            bar.show();
                        } else {
                            bar.setTitle(title);
                        }
                    } else if (metadata.getId() == 6 && !((ViaVersionPlugin) ViaVersion.getInstance()).isBossbarAntiflicker()) { // If anti flicker is enabled, don't update health
                        BossBar bar = bossBarMap.get(entityID);
                        // Make health range between 0 and 1
                        float maxHealth = type == EntityType.ENDER_DRAGON ? 200.0f : 300.0f;
                        float health = Math.max(0.0f, Math.min(((float) metadata.getValue()) / maxHealth, 1.0f));
                        if (bar == null) {
                            String title = type == EntityType.ENDER_DRAGON ? "Ender Dragon" : "Wither";
                            bar = ViaVersion.getInstance().createBossBar(title, health, BossColor.PINK, BossStyle.SOLID);
                            bossBarMap.put(entityID, bar);
                            bar.addPlayer(player);
                            bar.show();
                        } else {
                            bar.setHealth(health);
                        }
                    }
                }
            }
        }
    }
}
