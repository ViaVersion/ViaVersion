package org.spacehq.mc.protocol.data.game.chunk;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.Arrays;

public class NibbleArray3d {
    private byte[] data;

    public NibbleArray3d(int size) {
        this.data = new byte[size];
    }

    public NibbleArray3d(byte[] array) {
        this.data = array;
    }

    public NibbleArray3d(ByteBuf in, int size) throws IOException {
        this.data = new byte[size];
        in.readBytes(this.data);
    }

    public void write(ByteBuf out) throws IOException {
        out.writeBytes(this.data);
    }

    public byte[] getData() {
        return this.data;
    }

    public int get(int x, int y, int z) {
        int key = y << 8 | z << 4 | x;
        int index = key >> 1;
        int part = key & 1;
        return part == 0 ? this.data[index] & 15 : this.data[index] >> 4 & 15;
    }

    public void set(int x, int y, int z, int val) {
        int key = y << 8 | z << 4 | x;
        int index = key >> 1;
        int part = key & 1;
        if(part == 0) {
            this.data[index] = (byte) (this.data[index] & 240 | val & 15);
        } else {
            this.data[index] = (byte) (this.data[index] & 15 | (val & 15) << 4);
        }
    }

    public void fill(int val) {
        for(int index = 0; index < this.data.length << 1; index++) {
            int ind = index >> 1;
            int part = index & 1;
            if(part == 0) {
                this.data[ind] = (byte) (this.data[ind] & 240 | val & 15);
            } else {
                this.data[ind] = (byte) (this.data[ind] & 15 | (val & 15) << 4);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof NibbleArray3d && Arrays.equals(this.data, ((NibbleArray3d) o).data));
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.data);
    }
}