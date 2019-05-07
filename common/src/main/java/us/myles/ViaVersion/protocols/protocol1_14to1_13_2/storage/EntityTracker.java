package us.myles.ViaVersion.protocols.protocol1_14to1_13_2.storage;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.Setter;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.ExternalJoinGameListener;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_14Types;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.Protocol1_14To1_13_2;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets.WorldPackets;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityTracker extends StoredObject implements ExternalJoinGameListener {
    private final Map<Integer, Entity1_14Types.EntityType> clientEntityTypes = new ConcurrentHashMap<>();
    private final Map<Integer, Byte> insentientData = new ConcurrentHashMap<>();
    // 0x1 = sleeping, 0x2 = riptide
    private final Map<Integer, Byte> sleepingAndRiptideData = new ConcurrentHashMap<>();
    private final Map<Integer, Byte> playerEntityFlags = new ConcurrentHashMap<>();
    @Getter
    @Setter
    private int latestTradeWindowId;
    @Getter
    @Setter
    private int clientEntityId;
    @Getter
    @Setter
    private boolean forceSendCenterChunk = true;
    @Getter
    @Setter
    private int chunkCenterX, chunkCenterZ;

    public EntityTracker(UserConnection user) {
        super(user);
    }

    public void removeEntity(int entityId) {
        clientEntityTypes.remove(entityId);
        insentientData.remove(entityId);
        sleepingAndRiptideData.remove(entityId);
        playerEntityFlags.remove(entityId);
    }

    public void addEntity(int entityId, Entity1_14Types.EntityType type) {
        clientEntityTypes.put(entityId, type);
    }

    public byte getInsentientData(int entity) {
        Byte val = insentientData.get(entity);
        return val == null ? 0 : val;
    }

    public void setInsentientData(int entity, byte value) {
        insentientData.put(entity, value);
    }

    private static byte zeroIfNull(Byte val) {
        if (val == null) return 0;
        return val;
    }

    public boolean isSleeping(int player) {
        return (zeroIfNull(sleepingAndRiptideData.get(player)) & 1) != 0;
    }

    public void setSleeping(int player, boolean value) {
        byte newValue = (byte) ((zeroIfNull(sleepingAndRiptideData.get(player)) & ~1) | (value ? 1 : 0));
        if (newValue == 0) {
            sleepingAndRiptideData.remove(player);
        } else {
            sleepingAndRiptideData.put(player, newValue);
        }
    }

    public boolean isRiptide(int player) {
        return (zeroIfNull(sleepingAndRiptideData.get(player)) & 2) != 0;
    }

    public void setRiptide(int player, boolean value) {
        byte newValue = (byte) ((zeroIfNull(sleepingAndRiptideData.get(player)) & ~2) | (value ? 2 : 0));
        if (newValue == 0) {
            sleepingAndRiptideData.remove(player);
        } else {
            sleepingAndRiptideData.put(player, newValue);
        }
    }

    public boolean has(int entityId) {
        return clientEntityTypes.containsKey(entityId);
    }

    public Optional<Entity1_14Types.EntityType> get(int id) {
        return Optional.fromNullable(clientEntityTypes.get(id));
    }

    @Override
    public void onExternalJoinGame(int playerEntityId) {
        clientEntityId = playerEntityId;
        clientEntityTypes.put(playerEntityId, Entity1_14Types.EntityType.PLAYER);
        PacketWrapper setViewDistance = new PacketWrapper(0x41, null, getUser());
        setViewDistance.write(Type.VAR_INT, WorldPackets.SERVERSIDE_VIEW_DISTANCE);
        try {
            setViewDistance.send(Protocol1_14To1_13_2.class, true, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte getEntityFlags(int player) {
        return zeroIfNull(playerEntityFlags.get(player));
    }

    public void setEntityFlags(int player, byte data) {
        playerEntityFlags.put(player, data);
    }
}
