package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

public class PumpkinConnectionHandler extends AbstractStempConnectionHandler{

    static void init(){
        new PumpkinConnectionHandler("minecraft:pumpkin_stem[age=7]", "minecraft:carved_pumpkin", "minecraft:attached_pumpkin_stem");
    }

    public PumpkinConnectionHandler(String baseStateId, String blockId, String toKey) {
        super(baseStateId, blockId, toKey);
    }
}
