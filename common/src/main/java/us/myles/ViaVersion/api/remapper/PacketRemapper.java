package us.myles.ViaVersion.api.remapper;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Pair;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.exception.InformativeException;

import java.util.ArrayList;
import java.util.List;

public abstract class PacketRemapper {
    private List<Pair<ValueReader, ValueWriter>> valueRemappers = new ArrayList<>();

    public PacketRemapper() {
        registerMap();
    }

    /**
     * Map a type to the same type.
     *
     * @param type Type to map
     */
    public void map(Type type) {
        TypeRemapper remapper = new TypeRemapper(type);
        map(remapper, remapper);
    }

    /**
     * Map a type from an old type to a new type
     *
     * @param oldType The old type
     * @param newType The new type
     */
    public void map(Type oldType, Type newType) {
        map(new TypeRemapper(oldType), new TypeRemapper(newType));
    }

    /**
     * Map a type from an old type to a transformed new type.
     *
     * @param oldType     The old type
     * @param <T1>        The old return type.
     * @param transformer The transformer to use to produce the new type.
     * @param <T2>        The new return type.
     */
    public <T1, T2> void map(Type<T1> oldType, ValueTransformer<T1, T2> transformer) {
        map(new TypeRemapper(oldType), transformer);
    }

    /**
     * Map a type using a basic ValueReader to a ValueWriter
     *
     * @param inputReader  The reader to read with.
     * @param outputWriter The writer to write with
     * @param <T>          The return type
     */
    public <T> void map(ValueReader<T> inputReader, ValueWriter<T> outputWriter) {
        valueRemappers.add(new Pair<ValueReader, ValueWriter>(inputReader, outputWriter));
    }

    /**
     * Create a value
     *
     * @param creator The creator to used to make the value(s).
     */
    public void create(ValueCreator creator) {
        map(new TypeRemapper(Type.NOTHING), creator);
    }

    /**
     * Create a handler
     *
     * @param handler The handler to use to handle the current packet.
     */
    public void handler(PacketHandler handler) {
        map(new TypeRemapper(Type.NOTHING), handler);
    }

    /**
     * Register the mappings for this packet
     */
    public abstract void registerMap();

    /**
     * Remap a packet wrapper
     *
     * @param packetWrapper The wrapper to remap
     * @throws Exception Throws if it fails to write / read to the packet.
     */
    public void remap(PacketWrapper packetWrapper) throws Exception {
        try {
            // Read all the current values
            for (Pair<ValueReader, ValueWriter> valueRemapper : valueRemappers) {
                Object object = valueRemapper.getKey().read(packetWrapper);
                // Convert object to write type :O!!!
                valueRemapper.getValue().write(packetWrapper, object);
            }
            // If we had handlers we'd put them here
        } catch (InformativeException e) {
            e.addSource(this.getClass());
            throw e;
        }
    }
}
