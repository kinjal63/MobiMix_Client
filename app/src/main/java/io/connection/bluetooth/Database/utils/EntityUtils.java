package io.connection.bluetooth.Database.utils;

import java.lang.reflect.Field;
import java.util.List;

import io.connection.bluetooth.Database.annotations.Exclude;

/**
 * Created by KP49107 on 12-10-2017.
 */
public class EntityUtils {

    public static <T1 extends Object, T2 extends Object> void copyProperties(T1 srcEntity, T2 destEntity) {
        Field destFields[] = destEntity.getClass().getDeclaredFields();
        for(Field field : destFields) {
            try {
                if(!field.isAnnotationPresent(Exclude.class)) {
                    field.set(destEntity, field.get(srcEntity));
                }
            }
            catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
