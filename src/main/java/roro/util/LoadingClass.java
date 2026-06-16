package roro.util;

import roro.annotation.MonController;

public class LoadingClass {
    public static boolean hasAnnotation(Class<?> clazzScanned){
        return clazzScanned.isAnnotationPresent(MonController.class);
    }

    
}
