package us.myles.ViaVersion.protocols.protocol1_9to1_8.chat;

public enum GameMode {
    SURVIVAL(0, "Survival Mode"),
    CREATIVE(1, "Creative Mode"),
    ADVENTURE(2, "Adventure Mode"),
    SPECTATOR(3, "Spectator Mode");

    private final int id;
    private final String text;

    GameMode(int id, String text) {
        this.id = id;
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public static GameMode getById(int id) {
        for (GameMode gm : GameMode.values())
            if (gm.getId() == id)
                return gm;
        return null;
    }
}
