package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

public class MelonConnectionHandler extends AbstractStempConnectionHandler{

    public MelonConnectionHandler(String baseStateId, String blockId, String toKey) {
        super(baseStateId, blockId, toKey);
    }

    static void init() {
        new MelonConnectionHandler("minecraft:melon_stem[age=7]", "minecraft:melon", "minecraft:attached_melon_stem");
    }
}
