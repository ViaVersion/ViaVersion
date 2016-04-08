package us.myles.ViaVersion.util.filter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Florian on 08.04.16 in us.myles.ViaVersion.util
 */
public class Searcher {

    private Class<?> searchin;
    private Object searchinObj;
    private Object result;
    private ResultIterator resultIterator;



    public Searcher(Class<?> searchin, Object searchinObj) {
        this.searchin = searchin;
        this.searchinObj = searchinObj;
    }

    public Searcher search(IFilter filter) throws Exception {
        switch (filter.type()) {

            case "Field":
                for (Field field: searchin.getDeclaredFields()) {
                    field.setAccessible(true);
                    if (!filter.filter(field, searchinObj)) continue;

                    result = field.get(searchinObj);
                    if (resultIterator != null) resultIterator.iterate(field);
                }

                break;
            case "Method":
                for (Method method : searchin.getDeclaredMethods()) {
                    method.setAccessible(true);
                    if (!filter.filter(method, searchinObj)) continue;

                    result = method.invoke(searchinObj);
                    if (resultIterator != null) resultIterator.iterate(method);
                }

                break;
            default:
                throw new IllegalArgumentException("Invalid search type!");

        }

        return this;
    }

    public Searcher iterator(ResultIterator resultIterator) {
        this.resultIterator = resultIterator;
        return this;
    }

    public boolean checkNull() {
        return result == null;
    }

    public Searcher nextSearcher() {
        return new Searcher(result.getClass(), result);
    }

    public Searcher nextSearcher(Class<?> cl) {
        return new Searcher(cl, result);
    }

    public Object finalResult() {
        return result;
    }

}
