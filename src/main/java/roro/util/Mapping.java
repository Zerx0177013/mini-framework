package roro.util;
import java.lang.reflect.Method;

public class Mapping {
    private final String url;
    private final Class<?> controllerClass;
    private final Method method;

    public Mapping(String url, Class<?> controllerClass, Method method) {
        this.url = url;
        this.controllerClass = controllerClass;
        this.method = method;
    }

    public String getUrl() { return url; }
    public String getClassName() { return controllerClass.getName(); }
    public Method getMethod() { return method; }
    public Class<?> getControllerClass() { return controllerClass; }

    @Override
    public String toString() {
        return url + " -> " + controllerClass.getName() + "." + method.getName() + "()";
    }
}