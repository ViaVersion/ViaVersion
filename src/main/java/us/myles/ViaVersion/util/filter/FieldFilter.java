package us.myles.ViaVersion.util.filter;

import java.lang.reflect.Field;

/**
 * Created by Florian on 08.04.16 in us.myles.ViaVersion.util
 */
public abstract class FieldFilter extends IFilter<Field> {

    @Override
    public abstract boolean filter(Field field, Object in) throws Exception;

    @Override
    public String type() {
        return "Field";
    }

}
