package roro.util;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

public class Binder {
    public static Object bind(Class<?> clazz, HttpServletRequest request) throws Exception {
        return bind(clazz, request, "");
    }

    private static Object bind(Class<?> clazz, HttpServletRequest request, String prefix) throws Exception {
        Object obj = clazz.getDeclaredConstructor().newInstance();

        for (Method method : clazz.getMethods()) {
            if (!method.getName().startsWith("set") || method.getParameterCount() != 1)
                continue;

            String prop = Character.toLowerCase(method.getName().charAt(3)) + method.getName().substring(4);
            String fullName = prefix + prop;
            Class<?> type = method.getParameterTypes()[0];
            Type genericType = method.getGenericParameterTypes()[0];

            if (isMulti(type)) {
                if (request.getParameterValues(fullName) != null
                        || hasParamStartingWith(request, fullName + "[")
                        || (FileUpload.isMultipart(request) && request.getPart(fullName) != null))
                    method.invoke(obj, bindMulti(fullName, type, genericType, request));
            } else if (isSimple(type)) {
                String value = request.getParameter(fullName);
                if (value == null || value.isBlank())
                    continue;
                method.invoke(obj, convert(value, type));
            } else if (type == FileUpload.class) {
                if (!FileUpload.isMultipart(request))
                    continue;
                Part part = request.getPart(fullName);
                FileUpload file = part == null ? null : FileUpload.convertToFileUpload(part);
                if (file != null)
                    method.invoke(obj, file);
            } else if (type == FileUpload[].class) {
                if (!FileUpload.isMultipart(request))
                    continue;
                FileUpload[] files = FileUpload.fromAll(request, fullName);
                if (files.length > 0)
                    method.invoke(obj, (Object) files);
            } else if (hasParamStartingWith(request, fullName + ".")) {
                Object nested = bind(type, request, fullName + ".");
                method.invoke(obj, nested);
            }
        }
        return obj;
    }

    private static boolean hasParamStartingWith(HttpServletRequest request, String start) throws Exception {
        for (String key : request.getParameterMap().keySet()) {
            if (key.startsWith(start))
                return true;
        }
        if (FileUpload.isMultipart(request)) {
            for (Part part : request.getParts()) {
                if (part.getName().startsWith(start))
                    return true;
            }
        }
        return false;
    }

    private static boolean isSimple(Class<?> t) {
        return t.isPrimitive()
                || t == String.class
                || Number.class.isAssignableFrom(t)
                || t == Boolean.class
                || t == Character.class
                || t == LocalDate.class
                || t.isEnum();
    }

    public static boolean isMulti(Class<?> t) {
        if (t.isArray()) {
            Class<?> c = t.getComponentType();
            return c != FileUpload.class && !c.isArray();
        }
        return List.class == t || Set.class == t || Collection.class == t
                || ArrayList.class == t || LinkedHashSet.class == t;
    }


    //  Builds an array or a List/Set for the form field "name":
    //   simple elements (List<String>, int[]...): every value sent under "name"
    //  List<FileUpload>: every file sent under "name"
    //  objects (List<User>, User[]...): fields named name[0].prop, name[1].prop...
     
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Object bindMulti(String name, Class<?> type, Type genericType, HttpServletRequest request)
            throws Exception {
        Class<?> elem = elementType(type, genericType);

        if (isSimple(elem))
            return convertMulti(request.getParameterValues(name), type, genericType);

        List<Object> items = new ArrayList<>();
        if (elem == FileUpload.class) {
            if (FileUpload.isMultipart(request))
                items.addAll(Arrays.asList(FileUpload.fromAll(request, name)));
        } else {
            int max = maxIndex(request, name);
            for (int i = 0; i <= max; i++) {
                String itemPrefix = name + "[" + i + "].";
                if (hasParamStartingWith(request, itemPrefix))
                    items.add(bind(elem, request, itemPrefix));
            }
        }

        if (type.isArray()) {
            Object arr = Array.newInstance(elem, items.size());
            for (int i = 0; i < items.size(); i++)
                Array.set(arr, i, items.get(i));
            return arr;
        }
        Collection col = Set.class.isAssignableFrom(type) ? new LinkedHashSet<>() : new ArrayList<>();
        col.addAll(items);
        return col;
    }

    private static Class<?> elementType(Class<?> type, Type genericType) {
        if (type.isArray())
            return type.getComponentType();
        if (genericType instanceof ParameterizedType pt
                && pt.getActualTypeArguments()[0] instanceof Class<?> c)
            return c;
        return String.class;
    }

    private static int maxIndex(HttpServletRequest request, String name) throws Exception {
        List<String> keys = new ArrayList<>(request.getParameterMap().keySet());
        if (FileUpload.isMultipart(request))
            for (Part part : request.getParts())
                keys.add(part.getName());

        String start = name + "[";
        int max = -1;
        for (String key : keys) {
            if (!key.startsWith(start))
                continue;
            int end = key.indexOf(']', start.length());
            if (end < 0)
                continue;
            try {
                max = Math.max(max, Integer.parseInt(key.substring(start.length(), end)));
            } catch (NumberFormatException e) {
            }
        }
        return max;
    }

    
    //   Converts every value sent under the same name (checkboxes, select
    //   multiple...)
    //   to an array or a List/Set. The element type of a List/Set is read from the
    //  generic type (List<Integer> -> Integer), String when it cannot be found.
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Object convertMulti(String[] values, Class<?> type, Type genericType) {
        if (values == null)
            values = new String[0];

        if (type.isArray()) {
            Class<?> elem = type.getComponentType();
            List<Object> converted = new ArrayList<>();
            for (String v : values)
                if (v != null && !v.isBlank())
                    converted.add(convert(v, elem));
            Object arr = Array.newInstance(elem, converted.size());
            for (int i = 0; i < converted.size(); i++)
                Array.set(arr, i, converted.get(i));
            return arr;
        }

        Class<?> elem = String.class;
        if (genericType instanceof ParameterizedType pt
                && pt.getActualTypeArguments()[0] instanceof Class<?> c)
            elem = c;
        if (!isSimple(elem))
            throw new IllegalArgumentException("Unsupported list element type: " + elem.getName());

        Collection col = Set.class.isAssignableFrom(type) ? new LinkedHashSet<>() : new ArrayList<>();
        for (String v : values)
            if (v != null && !v.isBlank())
                col.add(convert(v, elem));
        return col;
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
