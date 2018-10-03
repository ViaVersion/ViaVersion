package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers;

import com.google.common.base.Optional;
import us.myles.ViaVersion.api.platform.providers.Provider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PaintingProvider implements Provider {
    private Map<String, Integer> paintings = new ConcurrentHashMap<>();

    public PaintingProvider() {
        add("kebab");
        add("aztec");
        add("alban");
        add("aztec2");
        add("bomb");
        add("plant");
        add("wasteland");
        add("pool");
        add("courbet");
        add("sea");
        add("sunset");
        add("creebet");
        add("wanderer");
        add("graham");
        add("match");
        add("bust");
        add("stage");
        add("void");
        add("skullandroses");
        add("wither");
        add("fighters");
        add("pointer");
        add("pigscene");
        add("burningskull");
        add("skeleton");
        add("donkeykong");
    }

    private void add(String motive) {
        paintings.put("minecraft:" + motive, paintings.size());
    }

    public Optional<Integer> getIntByIdentifier(String motive) {
        // Handle older versions
        if (!motive.startsWith("minecraft:"))
            motive = "minecraft:" + motive.toLowerCase();
        return Optional.fromNullable(paintings.get(motive));
    }
}