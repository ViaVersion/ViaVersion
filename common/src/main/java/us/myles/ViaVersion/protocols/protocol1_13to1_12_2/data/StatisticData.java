package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data;

public class StatisticData {

    private final int categoryId;
    private final int newId;
    private final int value;

    public StatisticData(int categoryId, int newId, int value) {
        this.categoryId = categoryId;
        this.newId = newId;
        this.value = value;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public int getNewId() {
        return newId;
    }

    public int getValue() {
        return value;
    }

}
