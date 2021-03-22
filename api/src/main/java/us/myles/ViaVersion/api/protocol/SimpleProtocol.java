package us.myles.ViaVersion.api.protocol;

/**
 * Dummy protocol class when there is no need of any of the
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
