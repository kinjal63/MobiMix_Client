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
                    Field srcField = srcEntity.getClass().getDeclaredField(field.getName());
                    srcField.setAccessible(true);
                    field.setAccessible(true);

                    Object srcValue = srcField.get(srcEntity);

                    field.set(destEntity, srcField.get(srcEntity));
                }
            }
            catch (IllegalAccessException | NoSuchFieldException e) {
//                e.printStackTrace();
            }
        }
    }
}
