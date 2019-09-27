package us.myles.ViaVersion.protocols.protocol1_14_1to1_14;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.protocols.protocol1_14_1to1_14.metadata.MetadataRewriter1_14_1To1_14;
import us.myles.ViaVersion.protocols.protocol1_14_1to1_14.packets.EntityPackets;
import us.myles.ViaVersion.protocols.protocol1_14_1to1_14.storage.EntityTracker1_14_1;

public class Protocol1_14_1To1_14 extends Protocol {

    @Override
    protected void registerPackets() {
        new MetadataRewriter1_14_1To1_14(this);

        EntityPackets.register(this);
    }

    @Override
    public void init(UserConnection userConnection) {
        userConnection.put(new EntityTracker1_14_1(userConnection));
    }
}
