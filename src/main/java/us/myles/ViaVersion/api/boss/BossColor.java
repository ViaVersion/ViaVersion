package us.myles.ViaVersion.api.boss;

public enum BossColor {
    PINK(0),
    BLUE(1),
    RED(2),
    GREEN(3),
    YELLOW(4),
    PURPLE(5),
    WHITE(6);

    private final int id;

    BossColor(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
