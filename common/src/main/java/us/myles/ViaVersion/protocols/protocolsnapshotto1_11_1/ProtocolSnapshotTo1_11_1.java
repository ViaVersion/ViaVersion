package us.myles.ViaVersion.protocols.protocolsnapshotto1_11_1;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.packets.State;

public class ProtocolSnapshotTo1_11_1 extends Protocol {

    @Override
    protected void registerPackets() {
        // As of 17w13b

        // Outgoing
        // New packet at 0x08
        registerOutgoing(State.PLAY, 0x08, 0x09);
        registerOutgoing(State.PLAY, 0x09, 0x0a);
        registerOutgoing(State.PLAY, 0x0a, 0x0b);
        registerOutgoing(State.PLAY, 0x0b, 0x0c);
        // error here, 0x0c
        registerOutgoing(State.PLAY, 0x0c, 0x0d);
        registerOutgoing(State.PLAY, 0x0d, 0x0e);
        registerOutgoing(State.PLAY, 0x0e, 0x0f);
        registerOutgoing(State.PLAY, 0x0f, 0x10);
        registerOutgoing(State.PLAY, 0x10, 0x11);
        registerOutgoing(State.PLAY, 0x11, 0x12);
        registerOutgoing(State.PLAY, 0x12, 0x13);
        registerOutgoing(State.PLAY, 0x13, 0x14);
        registerOutgoing(State.PLAY, 0x14, 0x15);
        registerOutgoing(State.PLAY, 0x15, 0x16);
        registerOutgoing(State.PLAY, 0x16, 0x17);
        registerOutgoing(State.PLAY, 0x17, 0x18);
        registerOutgoing(State.PLAY, 0x18, 0x19);
        registerOutgoing(State.PLAY, 0x19, 0x1a);
        registerOutgoing(State.PLAY, 0x1a, 0x1b);
        registerOutgoing(State.PLAY, 0x1b, 0x1c);
        registerOutgoing(State.PLAY, 0x1c, 0x1d);
        registerOutgoing(State.PLAY, 0x1d, 0x1e);
        registerOutgoing(State.PLAY, 0x1e, 0x1f);
        registerOutgoing(State.PLAY, 0x1f, 0x20);
        registerOutgoing(State.PLAY, 0x20, 0x21);
        registerOutgoing(State.PLAY, 0x21, 0x22);
        registerOutgoing(State.PLAY, 0x22, 0x23);
        registerOutgoing(State.PLAY, 0x23, 0x24);
        registerOutgoing(State.PLAY, 0x24, 0x25);
        registerOutgoing(State.PLAY, 0x25, 0x26);
        registerOutgoing(State.PLAY, 0x26, 0x27);
        registerOutgoing(State.PLAY, 0x27, 0x28);
        registerOutgoing(State.PLAY, 0x28, 0x29);
        registerOutgoing(State.PLAY, 0x29, 0x2a);
        registerOutgoing(State.PLAY, 0x2a, 0x2b);
        registerOutgoing(State.PLAY, 0x2b, 0x2c);
        registerOutgoing(State.PLAY, 0x2c, 0x2d);
        registerOutgoing(State.PLAY, 0x2d, 0x2e);
        registerOutgoing(State.PLAY, 0x2e, 0x2f);
        registerOutgoing(State.PLAY, 0x2f, 0x30);
        registerOutgoing(State.PLAY, 0x30, 0x32);
        // New packet at 0x31
        registerOutgoing(State.PLAY, 0x31, 0x33);
        registerOutgoing(State.PLAY, 0x32, 0x34);
        registerOutgoing(State.PLAY, 0x33, 0x35);
        registerOutgoing(State.PLAY, 0x34, 0x36);
        registerOutgoing(State.PLAY, 0x35, 0x37);
        registerOutgoing(State.PLAY, 0x36, 0x38);
        registerOutgoing(State.PLAY, 0x37, 0x39);
        registerOutgoing(State.PLAY, 0x38, 0x3a);
        registerOutgoing(State.PLAY, 0x39, 0x3b);
        registerOutgoing(State.PLAY, 0x3a, 0x3c);
        registerOutgoing(State.PLAY, 0x3b, 0x3d);
        registerOutgoing(State.PLAY, 0x3c, 0x3e);
        registerOutgoing(State.PLAY, 0x3d, 0x3f);
        registerOutgoing(State.PLAY, 0x3e, 0x40);
        registerOutgoing(State.PLAY, 0x3f, 0x41);
        registerOutgoing(State.PLAY, 0x40, 0x42);
        registerOutgoing(State.PLAY, 0x41, 0x43);
        registerOutgoing(State.PLAY, 0x42, 0x44);
        registerOutgoing(State.PLAY, 0x43, 0x45);
        registerOutgoing(State.PLAY, 0x44, 0x46);
        registerOutgoing(State.PLAY, 0x45, 0x47);
        registerOutgoing(State.PLAY, 0x46, 0x48);
        registerOutgoing(State.PLAY, 0x47, 0x49);
        registerOutgoing(State.PLAY, 0x48, 0x4a);
        registerOutgoing(State.PLAY, 0x49, 0x4b);
        registerOutgoing(State.PLAY, 0x4a, 0x4c);
        registerOutgoing(State.PLAY, 0x4b, 0x4d);

        // Incoming
        // New packet at 0x01
        registerIncoming(State.PLAY, -1, 0x01);
        registerIncoming(State.PLAY, 0x01, 0x02);
        registerIncoming(State.PLAY, 0x02, 0x03);
        registerIncoming(State.PLAY, 0x03, 0x04);
        registerIncoming(State.PLAY, 0x04, 0x05);
        registerIncoming(State.PLAY, 0x05, 0x06);
        registerIncoming(State.PLAY, 0x06, 0x07);
        registerIncoming(State.PLAY, 0x07, 0x08);
        registerIncoming(State.PLAY, 0x08, 0x09);
        registerIncoming(State.PLAY, 0x09, 0x0a);
        registerIncoming(State.PLAY, 0x0a, 0x0b);
        registerIncoming(State.PLAY, 0x0b, 0x0c);
        registerIncoming(State.PLAY, 0x0c, 0x0d);
        registerIncoming(State.PLAY, 0x0d, 0x0e);
        registerIncoming(State.PLAY, 0x0e, 0x0f);
        registerIncoming(State.PLAY, 0x0f, 0x10);
        registerIncoming(State.PLAY, 0x10, 0x11);
        registerIncoming(State.PLAY, 0x11, 0x12);
        registerIncoming(State.PLAY, 0x12, 0x13);
        registerIncoming(State.PLAY, 0x13, 0x14);
        registerIncoming(State.PLAY, 0x14, 0x15);
        registerIncoming(State.PLAY, 0x15, 0x16);
        // New packet at 0x17
        registerIncoming(State.PLAY, 0x17, -1, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler(){

                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        wrapper.cancel();
                    }
                });
            }
        });
        registerIncoming(State.PLAY, 0x16, 0x18);
        registerIncoming(State.PLAY, 0x17, 0x19);
        registerIncoming(State.PLAY, 0x18, 0x1a);
        registerIncoming(State.PLAY, 0x19, 0x1b);
        registerIncoming(State.PLAY, 0x1a, 0x1c);
        registerIncoming(State.PLAY, 0x1b, 0x1d);
        registerIncoming(State.PLAY, 0x1c, 0x1e);
        registerIncoming(State.PLAY, 0x1d, 0x1f);
    }


    @Override
    public void init(UserConnection userConnection) {

    }
}
