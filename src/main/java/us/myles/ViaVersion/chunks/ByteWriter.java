package us.myles.ViaVersion.chunks;

public class ByteWriter {
    private final byte[] bytes;
    private int byteIndex;
    private int bitIndex;

    public ByteWriter(int size) {
        this.bytes = new byte[size];
        this.byteIndex = 0;
        this.bitIndex = 0;
    }

    public byte[] getOutput() {
        return this.bytes;
    }

    public void writeFullByte(int b){
        writeByte(b, 8);
    }

    public void writeByte(int b, int length) {
        byte current = getCurrentByte();
        byte byteB = (byte)(int)((b) & 0xff);
        System.out.println("Input: " + byteB);
        System.out.println(Integer.toBinaryString(byteB));

        int space = (8 - bitIndex);
        int written = space > length ? length : space;
        System.out.println("Written is " + written);
        bytes[byteIndex] = (byte) (current | (extractRange(byteB, 0, written) >> (bitIndex - 1)));
        System.out.println("output value: " + bytes[byteIndex]);
        this.bitIndex += length;
        if(this.bitIndex >= 8) {
            this.byteIndex += 1;
            this.bitIndex = bitIndex - 8;
            // write remaining into this
            System.out.println("Writing from " + written + " to " + (written + bitIndex));
            System.out.println("Value: " + extractRange(byteB, written, written + bitIndex));
            bytes[byteIndex] = (byte) (extractRange(byteB, written, written + bitIndex) << written);
        }
    }

    private byte extractRange(int in, int begin, int end){
        return (byte) ((in >> begin) & ((1 << (end - begin)) - 1));
    }
    public byte getCurrentByte() {
        if(byteIndex == bytes.length) throw new RuntimeException("ByteWriter overflow!");

        return bytes[byteIndex];
    }
}
