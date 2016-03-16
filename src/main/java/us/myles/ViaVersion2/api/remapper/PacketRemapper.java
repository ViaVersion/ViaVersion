package us.myles.ViaVersion2.api.remapper;

import us.myles.ViaVersion2.api.PacketWrapper;
import us.myles.ViaVersion2.api.type.Type;
import us.myles.ViaVersion2.api.util.Pair;

import java.util.ArrayList;
import java.util.List;

public abstract class PacketRemapper {
    private List<Pair<ValueReader, ValueWriter>> valueRemappers = new ArrayList<>();

    public PacketRemapper() {
        registerMap();
    }

    public void map(Type type) {
        TypeRemapper remapper = new TypeRemapper(type);
        map(remapper, remapper);
    }

    public void map(Type oldType, Type newType) {
        map(new TypeRemapper(oldType), new TypeRemapper(newType));
    }

    public <T1, T2> void map(Type<T1> oldType, ValueTransformer<T1, T2> transformer) {
        map(new TypeRemapper(oldType), transformer);
    }

    public <T> void map(ValueReader<T> inputReader, ValueWriter<T> outputWriter) {
        valueRemappers.add(new Pair<ValueReader, ValueWriter>(inputReader, outputWriter));
    }

    public void create(ValueCreator creator) {
        map(new TypeRemapper(Type.NOTHING), creator);
    }

    public void handler(PacketHandler handler) {
        map(new TypeRemapper(Type.NOTHING), handler);
    }

    public abstract void registerMap();

    public void remap(PacketWrapper packetWrapper) throws Exception{
        // Read all the current values
        for (Pair<ValueReader, ValueWriter> valueRemapper : valueRemappers) {
            Object object = valueRemapper.getKey().read(packetWrapper);
            // Convert object to write type :O!!!
            valueRemapper.getValue().write(packetWrapper, object);
        }
        // If we had handlers we'd put them here
    }
}
