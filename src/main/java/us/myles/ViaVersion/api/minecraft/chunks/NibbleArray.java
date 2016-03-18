package us.myles.ViaVersion.api.minecraft.chunks;

import java.util.Arrays;

public class NibbleArray {
    private final byte[] handle;

    public NibbleArray(int length) {
        if (length == 0 || length % 2 != 0) {
            throw new IllegalArgumentException("Length of nibble array must be a positive number dividable by 2!");
        }

        this.handle = new byte[length / 2];
    }

    public NibbleArray(byte[] handle) {
        if (handle.length == 0 || handle.length % 2 != 0) {
            throw new IllegalArgumentException("Length of nibble array must be a positive number dividable by 2!");
        }

        this.handle = handle;
    }

    public byte get(int x, int y, int z) {
        return get(y << 8 | z << 4 | x);
    }

    public byte get(int index) {
        byte value = handle[index / 2];
        if (index % 2 == 0) {
            return (byte) (value & 0xF);
        } else {
            return (byte) ((value >> 4) & 0xF);
        }
    }

    public void set(int x, int y, int z, int value) {
        set(y << 8 | z << 4 | x, value);
    }

    public void set(int index, int value) {
        index /= 2;
        if (index % 2 == 0) {
            handle[index] = (byte) (handle[index] & 0xF0 | value & 0xF);
        } else {
            handle[index] = (byte) (handle[index] & 0xF | (value & 0xF) << 4);
        }
    }

    public int size() {
        return handle.length * 2;
    }

    public int actualSize() {
        return handle.length;
    }

    public void fill(byte value) {
        value &= 0xF; // Max nibble size (= 16)
        Arrays.fill(handle, (byte) ((value << 4) | value));
    }

    public byte[] getHandle() {
        return handle;
    }

    public void setHandle(byte[] handle) {
        if (handle.length != this.handle.length) {
            throw new IllegalArgumentException("Length of handle must equal to size of nibble array!");
        }

        System.arraycopy(handle, 0, this.handle, 0, handle.length);
    }
}
