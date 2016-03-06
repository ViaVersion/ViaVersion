package us.myles.ViaVersion.api.boss;

public enum BossFlag {
    DARKEN_SKY(1),
    PLAY_BOSS_MUSIC(2);

    private final int id;

    BossFlag(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
