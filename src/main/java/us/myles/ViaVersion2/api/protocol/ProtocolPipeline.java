package us.myles.ViaVersion2.api.protocol;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.CancelException;
import us.myles.ViaVersion.packets.Direction;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion2.api.PacketWrapper;
import us.myles.ViaVersion2.api.data.UserConnection;
import us.myles.ViaVersion2.api.protocol.base.BaseProtocol;
import us.myles.ViaVersion2.api.protocol.base.ProtocolInfo;

import java.util.ArrayList;
import java.util.LinkedList;

public class ProtocolPipeline extends Protocol {
    LinkedList<Protocol> protocolList = new LinkedList<>();

    @Override
    protected void registerPackets() {
        // This is a pipeline so we register basic pipes
        protocolList.addLast(new BaseProtocol());
    }

    @Override
    public void init(UserConnection userConnection) {
        ProtocolInfo protocolInfo = new ProtocolInfo();
        protocolInfo.setPipeline(this);

        userConnection.put(protocolInfo);

        /* Init through all our pipes */
        for (Protocol protocol : protocolList) {
            protocol.init(userConnection);
        }
    }

    @Override
    public void transform(Direction direction, State state, PacketWrapper packetWrapper) throws Exception {

        for (Protocol protocol : new ArrayList<>(protocolList)) { // Copy to prevent from removal.
            protocol.transform(direction, state, packetWrapper);
            // Reset the reader for the packetWrapper (So it can be recycled across packets)
            packetWrapper.resetReader();
        }
        super.transform(direction, state, packetWrapper);
    }
}
