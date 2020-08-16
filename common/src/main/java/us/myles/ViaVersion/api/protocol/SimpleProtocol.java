package us.myles.ViaVersion.api.protocol;

/**
 * Dummy protocol class for when you do not need any of the
 * existing packet type enums or automated channel mappings.
 *
 * @see Protocol
 */
public abstract class SimpleProtocol extends Protocol<SimpleProtocol.DummyPacketTypes, SimpleProtocol.DummyPacketTypes, SimpleProtocol.DummyPacketTypes, SimpleProtocol.DummyPacketTypes> {

    protected SimpleProtocol() {
    }

    public enum DummyPacketTypes implements ClientboundPacketType, ServerboundPacketType {
    }
}
