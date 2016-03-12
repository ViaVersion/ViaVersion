package us.myles.ViaVersion2.api.protocol1_9to1_8;

import us.myles.ViaVersion2.api.data.UserConnection;
import us.myles.ViaVersion2.api.metadata.Metadata;
import us.myles.ViaVersion2.api.protocol.Protocol;
import us.myles.ViaVersion2.api.protocol1_9to1_8.packets.SpawnPackets;
import us.myles.ViaVersion2.api.protocol1_9to1_8.storage.EntityTracker;
import us.myles.ViaVersion2.api.protocol1_9to1_8.types.MetadataListType;
import us.myles.ViaVersion2.api.protocol1_9to1_8.types.MetadataType;
import us.myles.ViaVersion2.api.type.Type;

import java.util.List;

public class Protocol1_9TO1_8 extends Protocol {
    public static Type<List<Metadata>> METADATA_LIST = new MetadataListType();

    public static Type<Metadata> METADATA = new MetadataType();

    @Override
    public void registerPackets() {
        // Example PLAY_SPAWN_OBJECT(State.PLAY, Direction.OUTGOING, 0x0E, 0x00),
        SpawnPackets.register(this);
    }

    @Override
    public void init(UserConnection userConnection) {
        // Entity tracker
        userConnection.add(new EntityTracker());
    }
}
