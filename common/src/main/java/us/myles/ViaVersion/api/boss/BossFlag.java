package us.myles.ViaVersion.api.boss;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum BossFlag {
    DARKEN_SKY(1),
    PLAY_BOSS_MUSIC(2);

    private final int id;
}
