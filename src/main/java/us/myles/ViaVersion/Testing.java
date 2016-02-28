package us.myles.ViaVersion;

import us.myles.ViaVersion.chunks.ByteWriter;

public class Testing {
    public static void main(String[] args) {
//        Map<NewType, Set<Type>> conversions = new HashMap<>();
//        for (MetaIndex metaIndex : MetaIndex.values()) {
//            if (!conversions.containsKey(metaIndex.getNewType())) {
//                conversions.put(metaIndex.getNewType(), new HashSet<Type>());
//            }
//            conversions.get(metaIndex.getNewType()).add(metaIndex.getOldType());
//        }
//        for (Map.Entry<NewType, Set<Type>> conv : conversions.entrySet()) {
//            System.out.println("Type: " + conv.getKey().name());
//            for (Type t : conv.getValue()) {
//                System.out.println(" -> " + t.name());
//            }
//        }
        ByteWriter bytes = new ByteWriter(2);
        int id = 255;
        int data = 4;
        bytes.writeByte(0, 1);
        bytes.writeFullByte(id);
        bytes.writeByte(data, 4);

        System.out.println("output bytes: ");
        for(byte b:bytes.getOutput()){
            System.out.println("B: " + b + " I: " + ((int)b & 0xff));
        }
    }



    public static byte writeByte(byte a, byte b, int index){
        int intA = (int) a & 0xff;
        int intB = (int) b & 0xff;
        System.out.println("As int: " + (intA | (intB >> index)));
        return (byte) (intA | (intB >> index));
    }
}
