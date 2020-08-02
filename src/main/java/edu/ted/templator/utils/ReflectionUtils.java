package edu.ted.templator.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import edu.ted.templator.exception.*;

public class ReflectionUtils {

    public static Object getFieldValueByPass(String fieldPath, Object startObject) throws NoSuchFieldException {
        String[] pathElements = fieldPath.split("\\.");
        Object currentValue = startObject;
        for (int i = 0; i < pathElements.length; i++) {
            currentValue = getFieldValue(pathElements[i], currentValue);
        }
        return currentValue;
    }

    public static Object getFieldValue(String field, Object object) throws NoSuchFieldException {
        Class<?> objClass = object.getClass();
        Field foundField = objClass.getDeclaredField(field);
        Class<?> foundFieldClass = foundField.getType();
        return getFieldValueByGetter(foundField, object);
    }

    public static Object getFieldValueByGetter(Field field, Object object) {
        String getterMethodName = "get" + capitalize(field.getName());
        try {
            Method getterMethod = object.getClass().getMethod(getterMethodName);
            return getterMethod.invoke(object);
        } catch (Exception e) {
            e.printStackTrace();
            throw new NoValueCanBeObtainedException(e);
        } 
    }

    static String capitalize(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }
}
