package com.useriq.sdk.capture;

import android.view.View;

import com.useriq.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by smylsamy on 06/12/16.
 */

public class Reflect {
    private static final Logger logger = Logger.init(Reflect.class.getSimpleName());

    public static View.AccessibilityDelegate getAccessibilityDelegate(View view) {
        Object[] args = {};
        String method = "getAccessibilityDelegate";
        return (View.AccessibilityDelegate) Reflect.invoke(view, method, args);
    }

    public static Object invoke(Object target, String method, Object[] args) {
        try {
            Class<?> klass = target.getClass();
            Method m = klass.getMethod(method);
            return m.invoke(target, args);
        } catch (NoSuchMethodException e) {
            // In this case, we just overwrite the original.
        } catch (IllegalAccessException e) {
            // In this case, we just overwrite the original.
        } catch (InvocationTargetException e) {
            logger.e(method + " threw an exception when called.", e);
        } catch (IllegalArgumentException e) {
            logger.e(method + " threw an exception when called.", e);
        }

        return null;
    }

    public static Object getFieldValue(String fieldName, Object target)
            throws ReflectiveOperationException {
        Field field = findField(fieldName, target.getClass());

        field.setAccessible(true);
        return field.get(target);
    }

    private static Field findField(String name, Class clazz) throws NoSuchFieldException {
        Class currentClass = clazz;
        while (currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (name.equals(field.getName())) {
                    return field;
                }
            }

            currentClass = currentClass.getSuperclass();
        }

        NoSuchFieldException err = new NoSuchFieldException("Field " + name + " not found for class " + clazz);
        logger.e("Field " + name + " not found for class " + clazz, err);
        throw err;
    }
}
