package io.connection.bluetooth.Database.utils;

import android.content.ContentValues;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import io.connection.bluetooth.Database.annotations.Column;
import io.connection.bluetooth.Database.annotations.Table;

/**
 * Created by Kinjal on 10/7/2017.
 */

public class DatabaseUtils {
    public static String getTableName(Class<?> c) {
        Table annotationTable = c.getAnnotation(Table.class);
        if(annotationTable != null) {
            if(!annotationTable.name().equals("")) {
                return annotationTable.name();
            }
        }
        return null;
    }

    public static String[] getColumnNames(Class<?> c) {
        List<Column> columns = new ArrayList<>();
        Field fields[] = c.getDeclaredFields();
        for(Field field : fields) {
            Column column = field.getAnnotation(Column.class);
            if(column != null) {
                columns.add(column);
            }
        }
        String[] columnsArray = new String[columns.size()];
        return columns.toArray(columnsArray);
    }

    public static String getColumnName(Field field) {
        Column column = field.getAnnotation(Column.class);
        if(column != null) {
            column.name();
        }
        return null;
    }

    public static String getColumnType(Field field) {
        Column column = field.getAnnotation(Column.class);
        if(column != null) {
            column.type();
        }
        return null;
    }

    public static boolean isPrimaryKeyColumn(Field field) {
        Column column = field.getAnnotation(Column.class);
        if(column != null) {
            column.isPrimaryKey();
        }
        return false;
    }

    public static Field[] getFields(Class<?> c) {
        Field[] field = null;
        if(c != null) {
            field = c.getDeclaredFields();
        }
        return field;
    }

    private static void fillContentValues(ContentValues contentValues, Field field, Object object) throws IllegalAccessException {
        String columnName = getColumnName(field);
        Object fieldValue = field.get(object);
        if(fieldValue instanceof String) {
            contentValues.put(columnName, fieldValue.toString());
        }
        else if(fieldValue instanceof Integer) {
            contentValues.put(columnName, Integer.valueOf(fieldValue.toString()));
        }
        else if(fieldValue instanceof Long) {
            contentValues.put(columnName, Long.valueOf(fieldValue.toString()));
        }
        else if(fieldValue instanceof Double) {
            contentValues.put(columnName, Double.valueOf(fieldValue.toString()));
        }
        else if(fieldValue instanceof Float) {
            contentValues.put(columnName, Float.valueOf(fieldValue.toString()));
        }
        else if(fieldValue instanceof Byte) {
            contentValues.put(columnName, Byte.valueOf(fieldValue.toString()));
        }
        else if(fieldValue instanceof Boolean) {
            contentValues.put(columnName, Boolean.valueOf(fieldValue.toString()));
        }
        else if(fieldValue instanceof Short) {
            contentValues.put(columnName, Short.valueOf(fieldValue.toString()));
        }
        else if(fieldValue instanceof Byte[] || fieldValue instanceof byte[]) {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                        outputStream);
                objectOutputStream.writeObject(fieldValue);
                contentValues.put(columnName, outputStream.toByteArray());
                objectOutputStream.flush();
                objectOutputStream.close();
                outputStream.flush();
                outputStream.close();
            } catch (Exception e) {
            }
        }

    }

    public static ContentValues getFilledContentValues(Object object) {
        ContentValues contentValues = new ContentValues();
        if(object.getClass() != null) {
            Field fields[] = object.getClass().getDeclaredFields();
            for( Field field : fields) {
                try {
                    fillContentValues(contentValues, field, object);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return contentValues;
    }
}
