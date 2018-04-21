package us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.storage;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Data;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BlockStorage extends StoredObject {
    // This BlockStorage is very exclusive (;
    private static final Set<Integer> whitelist = Sets.newConcurrentHashSet();

    static {
        // Flower pots
        whitelist.add(5175);

        // Add those red beds
        for (int i = 0; i < 16; i++)
            whitelist.add(882 + i);

        // Add the white banners
        for (int i = 0; i < 20; i++)
            whitelist.add(6764 + i);

        // Add the white wall banners
        for (int i = 0; i < 4; i++) {
            whitelist.add(7020 + i);
        }

        // Skeleton skulls
        for (int i = 0; i < 5; i++)
            whitelist.add(5357 + i);
    }

    private Map<Position, ReplacementData> blocks = new ConcurrentHashMap<>();

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

    @Data
    @AllArgsConstructor
    public class ReplacementData {
        private int original;
        private int replacement;
    }

}
