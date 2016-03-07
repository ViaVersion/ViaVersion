package us.myles.ViaVersion.chunks;

import lombok.Getter;

@Getter
public class ByteWriter {

    private final byte[] output;
    private int byteIndex;
    private int bitIndex;

    public ByteWriter(int size) {
        this.output = new byte[size];
        this.byteIndex = 0;
        this.bitIndex = 0;
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
        output[byteIndex] = (byte) (current | (extractRange(byteB, 0, written) >> (bitIndex - 1)));
        System.out.println("output value: " + output[byteIndex]);
        this.bitIndex += length;
        if(this.bitIndex >= 8) {
            this.byteIndex += 1;
            this.bitIndex = bitIndex - 8;
            // write remaining into this
            System.out.println("Writing from " + written + " to " + (written + bitIndex));
            System.out.println("Value: " + extractRange(byteB, written, written + bitIndex));
            output[byteIndex] = (byte) (extractRange(byteB, written, written + bitIndex) << written);
        }
    }

    private byte extractRange(int in, int begin, int end){
        return (byte) ((in >> begin) & ((1 << (end - begin)) - 1));
    }
    public byte getCurrentByte() {
        if (byteIndex == output.length) throw new RuntimeException("ByteWriter overflow!");

        return output[byteIndex];
    }
}
