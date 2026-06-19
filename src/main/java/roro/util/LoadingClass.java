package roro.util;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

public class LoadingClass {
    public static boolean hasAnnotation(Class<?> clazzScanned, String monAnnotation) {
        try {
            Class<?> clazz = Class.forName(monAnnotation);
            Class<? extends Annotation> annotationClass = clazz.asSubclass(Annotation.class);

            Target target = annotationClass.getAnnotation(Target.class);

            if (target != null && Arrays.asList(target.value()).contains(ElementType.METHOD)) {
                return Arrays.stream(clazzScanned.getDeclaredMethods())
                        .anyMatch(m -> m.isAnnotationPresent(annotationClass));
            }

            return clazzScanned.isAnnotationPresent(annotationClass);

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Annotation non trouvée : " + monAnnotation, e);
            // return false;
        }
    }

    public static List<String> loadClassWithMyAnnotation(String packageName, List<String> mesAnnotations) {
        List<String> listeClasse = new ArrayList<>();
        try (ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages(packageName).scan()) {
            if (mesAnnotations.size() < 1) {
                return listeClasse;
            }
            ClassInfoList classesAvecAnnotation = scanResult.getAllClasses();
            for (ClassInfo kilassy : classesAvecAnnotation) {
                try {
                    Class<?> clazz = Class.forName(kilassy.getName());
                    boolean hasAllAnnotations = true;

                    for (String monAnnotation : mesAnnotations) {
                        if (!hasAnnotation(clazz, monAnnotation)) {
                            hasAllAnnotations = false;
                            break;
                        }
                    }
                    if (hasAllAnnotations) {
                        listeClasse.add(kilassy.getName());
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Erreur lors du chargement de la classe : " + kilassy.getName(), e);
                }
            }
        }
        return listeClasse;
    }

    public static List<String> loadClassWithMyAnnotation(String packageName, String monAnnotation) {
        List<String> listeClasse = new ArrayList<>();
        try (ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages(packageName).scan()) {
            ClassInfoList classesAvecAnnotation = scanResult.getClassesWithAnnotation(monAnnotation);
            for (ClassInfo classInfo : classesAvecAnnotation) {
                listeClasse.add(classInfo.getName());
            }
        }
        return listeClasse;
    }

    public static List<String> loadClassWithMyMethodeAnnotation(String packageName, String monAnnotation) {
        List<String> listeClasse = new ArrayList<>();
        try (ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages(packageName).scan()) {
            ClassInfoList classesAvecAnnotation = scanResult.getClassesWithMethodAnnotation(monAnnotation);
            for (ClassInfo classInfo : classesAvecAnnotation) {
                listeClasse.add(classInfo.getName());
            }
        }
        return listeClasse;
    }

    public static List<String> loadClassWithMyAnnotation(String monAnnotation) {
        List<String> listeClasse = new ArrayList<>();

        try (ScanResult scanResult = new ClassGraph()
                .enableAllInfo()
                .scan()) {

            ClassInfoList classesAvecAnnotation = scanResult.getClassesWithAnnotation(monAnnotation);
            for (ClassInfo classInfo : classesAvecAnnotation) {
                listeClasse.add(classInfo.getName());
            }
        }
        return listeClasse;
    }

    public static List<String> loadAllClasses() {
        List<String> listeClasse = new ArrayList<>();

        try (ScanResult scanResult = new ClassGraph()
                .enableAllInfo()
                .scan()) {

            ClassInfoList classesAvecAnnotation = scanResult.getAllClasses();
            for (ClassInfo classInfo : classesAvecAnnotation) {
                listeClasse.add(classInfo.getName());
            }
        }
        return listeClasse;
    }
}
