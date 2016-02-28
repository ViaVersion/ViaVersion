package us.myles.ViaVersion.chunks;

import java.util.BitSet;

public class MagicBitSet extends BitSet{
    private final int initLength;

    public MagicBitSet(int nbits) {
        super(nbits);
        this.initLength = nbits;
    }

    public int getTrueLength() {
        return length() == 0 ? initLength : length();
    }
}
