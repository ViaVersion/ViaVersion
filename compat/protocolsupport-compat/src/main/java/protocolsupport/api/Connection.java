package protocolsupport.api;

public abstract class Connection {

    public abstract static class PacketListener {

        public void onPacketReceiving(PacketEvent event) {
            throw new UnsupportedOperationException();
        }

        public static class PacketEvent {

            public Object getPacket() {
                throw new UnsupportedOperationException();
            }
        }
    }
}
