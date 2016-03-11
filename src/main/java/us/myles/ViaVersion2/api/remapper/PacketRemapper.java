package us.myles.ViaVersion2.api.remapper;

import us.myles.ViaVersion2.api.PacketWrapper;
import us.myles.ViaVersion2.api.util.Pair;
import us.myles.ViaVersion2.api.type.Type;

import java.util.ArrayList;
import java.util.List;

public abstract class PacketRemapper {
    private List<Pair<ValueReader, ValueTransformer>> valueRemappers = new ArrayList<>();

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

    public <T> void map(ValueReader<T> inputRemapper, ValueTransformer<T> outputRemapper) {
        valueRemappers.add(new Pair<ValueReader, ValueTransformer>(inputRemapper, outputRemapper));
    }

    public void create(ValueCreator transformer) {
        map(new TypeRemapper(Type.NOTHING), transformer);
    }

    public abstract void registerMap();

    public void remap(PacketWrapper packetWrapper) {
        // Read all the current values
        for(Pair<ValueReader, ValueTransformer> valueRemapper : valueRemappers){
            Object object = valueRemapper.getKey().read(packetWrapper);
            // Convert object to write type :O!!!
            // TODO: Data converter lol
            valueRemapper.getValue().write(packetWrapper, object);
        }
        // If we had handlers we'd put them here
    }
}
