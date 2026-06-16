package roro.util;

import java.util.ArrayList;
import java.util.List;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import roro.annotation.MonController;

public class LoadingClass {
    public static boolean hasAnnotation(Class<?> clazzScanned) {
        return clazzScanned.isAnnotationPresent(MonController.class);
    }

    public static List<String> loadClass(String packageName) {
        List<String> listeClasse = new ArrayList<>();
        try (ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages(packageName).scan()) {
            ClassInfoList classes = scanResult.getAllClasses();
            for (ClassInfo classInfo : classes) {
                listeClasse.add(classInfo.getName());
            }
        }
        return listeClasse;
    }
}
