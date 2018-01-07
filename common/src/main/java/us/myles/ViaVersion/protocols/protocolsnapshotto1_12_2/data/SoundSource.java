package us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.data;

import com.google.common.base.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SoundSource {
    MASTER("master", 0),
    MUSIC("music", 1),
    RECORD("record", 2),
    WEATHER("weather", 3),
    BLOCK("block", 4),
    HOSTILE("hostile", 5),
    NEUTRAL("neutral", 6),
    PLAYER("player", 7),
    AMBIENT("ambient", 8),
    VOICE("voice", 9);

    private final String name;
    private final int id;

    public static Optional<SoundSource> findBySource(String source) {
        for (SoundSource item : SoundSource.values())
            if (item.getName().equalsIgnoreCase(source))
                return Optional.of(item);
        return Optional.absent();
    }
}
