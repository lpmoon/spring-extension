package com.lpmoon.spring.util;


import java.lang.reflect.Field;

/**
 * Created by lpmoon on 17/11/25.
 */
public class ObjectUtil {

    public static Object copy(Object source, Class childClass) throws Exception {
        if (source == null) {
            return null;
        }

        Class parentClass = source.getClass();
        if (childClass.getSuperclass() != source.getClass()) {
            throw new Exception("ChildClass:" + childClass + "'s ParentClass is not " + source.getClass());
        }

        Object destination = childClass.newInstance();
        for (Field field : source.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            String fieldName = field.getName();
            Object value = field.get(source);

            Field destField = childClass.getSuperclass().getDeclaredField(fieldName);
            destField.setAccessible(true);
            destField.set(destination, value);
        }

        return destination;
    }
}
