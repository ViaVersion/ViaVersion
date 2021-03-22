package us.myles.ViaVersion.api.minecraft;

public class VillagerData {
    private final int type;
    private final int profession;
    private final int level;

    public VillagerData(final int type, final int profession, final int level) {
        this.type = type;
        this.profession = profession;
        this.level = level;
    }

    public int getType() {
        return type;
    }

    public int getProfession() {
        return profession;
    }

    public int getLevel() {
        return level;
    }
}
