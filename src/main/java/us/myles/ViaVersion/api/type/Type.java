package us.myles.ViaVersion.api.type;


import lombok.Getter;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.spacehq.opennbt.tag.builtin.CompoundTag;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.type.types.*;
import us.myles.ViaVersion.api.type.types.minecraft.*;

import java.util.UUID;

@Getter
public abstract class Type<T> implements ByteBufReader<T>, ByteBufWriter<T> {
    /* Defined Types */
    public static final Type<Byte> BYTE = new ByteType();
    public static final Type<Byte[]> BYTE_ARRAY = new ArrayType<>(Type.BYTE);

    public static final Type<byte[]> REMAINING_BYTES = new RemainingBytesType();

    public static final Type<Short> UNSIGNED_BYTE = new UnsignedByteType();
    public static final Type<Short[]> UNSIGNED_BYTE_ARRAY = new ArrayType<>(Type.UNSIGNED_BYTE);

    public static final Type<Boolean> BOOLEAN = new BooleanType();
    public static final Type<Boolean[]> BOOLEAN_ARRAY = new ArrayType<>(Type.BOOLEAN);
    /* Number Types */
    public static final Type<Integer> INT = new IntType();
    public static final Type<Integer[]> INT_ARRAY = new ArrayType<>(Type.INT);

    public static final Type<Double> DOUBLE = new DoubleType();
    public static final Type<Double[]> DOUBLE_ARRAY = new ArrayType<>(Type.DOUBLE);

    public static final Type<Long> LONG = new LongType();
    public static final Type<Long[]> LONG_ARRAY = new ArrayType<>(Type.LONG);

    public static final Type<Float> FLOAT = new FloatType();
    public static final Type<Float[]> FLOAT_ARRAY = new ArrayType<>(Type.FLOAT);

    public static final Type<Short> SHORT = new ShortType();
    public static final Type<Short[]> SHORT_ARRAY = new ArrayType<>(Type.SHORT);

    public static final Type<Integer> UNSIGNED_SHORT = new UnsignedShortType();
    public static final Type<Integer[]> UNSIGNED_SHORT_ARRAY = new ArrayType<>(Type.UNSIGNED_SHORT);
    /* Other Types */
    public static final Type<String> STRING = new StringType();
    public static final Type<String[]> STRING_ARRAY = new ArrayType<>(Type.STRING);

    public static final Type<UUID> UUID = new UUIDType();
    public static final Type<UUID[]> UUID_ARRAY = new ArrayType<>(Type.UUID);
    /* Variable Types */
    public static final Type<Integer> VAR_INT = new VarIntType();
    public static final Type<Integer[]> VAR_INT_ARRAY = new ArrayType<>(Type.VAR_INT);
    /* Special Types */
    public static final Type<Void> NOTHING = new VoidType(); // This is purely used for remapping.
    /* MC Types */
    public static final Type<Position> POSITION = new PositionType();
    public static final Type<EulerAngle> ROTATION = new EulerAngleType();
    public static final Type<Vector> VECTOR = new VectorType();
    public static final Type<CompoundTag> NBT = new NBTType();
    public static final Type<CompoundTag[]> NBT_ARRAY = new ArrayType<>(Type.NBT);

    public static final Type<UUID> OPTIONAL_UUID = new OptUUIDType();
    public static final Type<Position> OPTIONAL_POSITION = new OptPositionType();

    public static final Type<Item> ITEM = new ItemType();
    public static final Type<Item[]> ITEM_ARRAY = new ItemArrayType();
    /* Actual Class */

    private final Class<? super T> outputClass;
    private final String typeName;

    public Type(Class<? super T> outputClass) {
        this(outputClass.getSimpleName(), outputClass);
    }

    public Type(String typeName, Class<? super T> outputClass) {
        this.outputClass = outputClass;
        this.typeName = typeName;
    }

    public Class<? extends Type> getBaseClass() {
        return this.getClass();
    }

    @Override
    public String toString() {
        return "Type|" + getTypeName();
    }
}
