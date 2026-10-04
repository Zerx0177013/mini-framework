package roro.util;

import java.lang.reflect.Method;
import java.time.LocalDate;

import jakarta.servlet.http.HttpServletRequest;

public class Binder {
    public static Object bind(Class<?> clazz, HttpServletRequest request) throws Exception {
        Object obj = clazz.getDeclaredConstructor().newInstance();

        for (Method method : clazz.getMethods()) {
            if (!method.getName().startsWith("set") || method.getParameterCount() != 1)
                continue;

            String propertyName = Character.toLowerCase(method.getName().charAt(3)) + method.getName().substring(4);
            String propertyValue = request.getParameter(propertyName);
            if (propertyValue == null || propertyValue.isEmpty())
                continue;
            Object convertedValue = convert(propertyValue, method.getParameterTypes()[0]);
            method.invoke(obj, convertedValue);

        }

        return obj;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Object convert(String v, Class<?> t) {
        if (t == String.class)
            return v;
        if (t == int.class || t == Integer.class)
            return Integer.valueOf(v);
        if (t == byte.class || t == Byte.class)
            return Byte.valueOf(v);
        if (t == short.class || t == Short.class)
            return Short.valueOf(v);
        if (t == long.class || t == Long.class)
            return Long.valueOf(v);
        if (t == float.class || t == Float.class)
            return Float.valueOf(v);
        if (t == double.class || t == Double.class)
            return Double.valueOf(v);
        if (t == boolean.class || t == Boolean.class)
            return Boolean.valueOf(v);
        if (t == char.class || t == Character.class) {
            if (v.length() != 1)
                throw new IllegalArgumentException("Expected a single character: " + v);
            return v.charAt(0);
        }
        if (t == LocalDate.class)
            return LocalDate.parse(v);
        if (t.isEnum())
            return Enum.valueOf((Class<Enum>) t, v);
        throw new IllegalArgumentException("Unsupported type: " + t.getName());
    }
}
