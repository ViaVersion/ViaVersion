package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage;

import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BlockStorage extends StoredObject {
    private static final Set<Integer> whitelist = new HashSet<>();
    private final Map<Position, ReplacementData> blocks = new ConcurrentHashMap<>();

    static {
        // Flower pots
        whitelist.add(5266);

        // Add those red beds
        for (int i = 0; i < 16; i++)
            whitelist.add(972 + i);

        // Add the white banners
        for (int i = 0; i < 20; i++)
            whitelist.add(6854 + i);

        // Add the white wall banners
        for (int i = 0; i < 4; i++) {
            whitelist.add(7110 + i);
        }

        // Skeleton skulls
        for (int i = 0; i < 5; i++)
            whitelist.add(5447 + i);
    }

    public BlockStorage(UserConnection user) {
        super(user);
    }

    public void store(Position position, int block) {
        store(position, block, -1);
    }

    public void store(Position position, int block, int replacementId) {
        if (!whitelist.contains(block))
            return;

        blocks.put(position, new ReplacementData(block, replacementId));
    }

    public boolean isWelcome(int block) {
        return whitelist.contains(block);
    }

    public boolean contains(Position position) {
        return blocks.containsKey(position);
    }

    public ReplacementData get(Position position) {
        return blocks.get(position);
    }

    public ReplacementData remove(Position position) {
        return blocks.remove(position);
    }

    public static class ReplacementData {
        private int original;
        private int replacement;

        public ReplacementData(int original, int replacement) {
            this.original = original;
            this.replacement = replacement;
        }

        public int getOriginal() {
            return original;
        }

        public void setOriginal(int original) {
            this.original = original;
        }

        public int getReplacement() {
            return replacement;
        }

        public void setReplacement(int replacement) {
            this.replacement = replacement;
        }
    }
}
