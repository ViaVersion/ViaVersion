package us.myles.ViaVersion.api.minecraft.metadata;

public class Metadata {
    private int id;
    private MetaType metaType;
    private Object value;

    public Metadata(int id, MetaType metaType, Object value) {
        this.id = id;
        this.metaType = metaType;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public MetaType getMetaType() {
        return metaType;
    }

    public void setMetaType(MetaType metaType) {
        this.metaType = metaType;
    }

    public Object getValue() {
        return value;
    }

    public <T> T getCastedValue() {
        return (T) value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
