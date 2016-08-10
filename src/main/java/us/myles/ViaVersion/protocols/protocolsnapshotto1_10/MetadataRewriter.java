package us.myles.ViaVersion.protocols.protocolsnapshotto1_10;

import com.google.common.base.Optional;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;

import java.util.List;

public class MetadataRewriter {
    public static int rewriteEntityType(int currentType, List<Metadata> metadata) {
        if (currentType == 68) {
            // ElderGuardian - 4
            Optional<Metadata> options = getById(metadata, 12);
            if (options.isPresent()) {
                if ((((byte) options.get().getValue()) & 0x04) == 0x04) {
                    return 4;
                }
            }
        }
        if (currentType == 51) {
            // WitherSkeleton - 5
            // Stray - 6
            Optional<Metadata> options = getById(metadata, 12);
            if (options.isPresent()) {
                if (((int) options.get().getValue()) == 1) {
                    return 5;
                }
                if (((int) options.get().getValue()) == 2) {
                    return 6;
                }
            }
        }
        if (currentType == 54) {
            // ZombieVillager - 27
            // Husk - 23
        }
        if (currentType == 100) {
            // SkeletonHorse - 28
            // ZombieHorse - 29
            // Donkey - 31
            // Mule - 32
        }
        return currentType;
    }

    public static Optional<Metadata> getById(List<Metadata> metadatas, int id) {
        for (Metadata metadata : metadatas) {
            if (metadata.getId() == id) return Optional.of(metadata);
        }
        return Optional.absent();
    }
}
