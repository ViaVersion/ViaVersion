package us.myles.ViaVersion.api.minecraft.metadata;

import java.util.Objects;

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

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Metadata metadata = (Metadata) o;
        if (id != metadata.id) return false;
        if (!Objects.equals(metaType, metadata.metaType)) return false;
        return Objects.equals(value, metadata.value);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (metaType != null ? metaType.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Metadata{" +
                "id=" + id +
                ", metaType=" + metaType +
                ", value=" + value +
                '}';
    }
}
