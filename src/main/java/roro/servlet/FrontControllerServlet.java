package roro.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import roro.util.Mapping;
import roro.util.UrlMethod;

public class FrontControllerServlet extends HttpServlet {

    // List<String> listeClasse = new ArrayList<>();

    // @Override
    // public void init() throws ServletException {
    // super.init();
    // String packageName = "roro.app";
    // String monAnnotation = "roro.annotation.MonController";
    // String monAnnotation2 = "roro.annotation.UrlMapping";
    // List<String> mesAnnotations = new ArrayList<>();
    // mesAnnotations.add(monAnnotation);
    // mesAnnotations.add(monAnnotation2);
    // listeClasse = roro.util.LoadingClass.loadClassWithMyAnnotation(packageName,
    // mesAnnotations);
    // }

    // Map<String, Mapping> routes;
    // Map<UrlMethod, Mapping> routesWithMethod;

    // @Override
    // public void init() throws ServletException {
    //     super.init();
    //     String packageName = "roro.app";
    //     String monAnnotation = "roro.annotation.MonController";
    //     String monAnnotation2 = "roro.annotation.UrlMapping";

    //     // routes = roro.util.LoadingClass.loadUrlMappings(packageName, monAnnotation, monAnnotation2);
    //     routesWithMethod = roro.util.LoadingClass.loadUrlMappingsWithMethod(packageName, monAnnotation, monAnnotation2);
    // }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @SuppressWarnings("unchecked")
    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("text/plain;charset=UTF-8");

        // String url = request.getRequestURL().toString();
        Map<UrlMethod, Mapping> routesWithMethod = (Map<UrlMethod, Mapping>) getServletContext().getAttribute("routesWithMethod");

        try (PrintWriter out = response.getWriter()) {
            out.println("---Mon Framework Perso ---");


            String pathInfo = request.getRequestURI().substring(request.getContextPath().length());
            UrlMethod urlMethod = new UrlMethod(pathInfo, request.getMethod());
            if (roro.util.LoadingClass.isARouteInsideMappingWithMethod(urlMethod, routesWithMethod)) {
                Mapping mapping = routesWithMethod.get(urlMethod);
                out.println("Route trouvée : " + urlMethod + " -> " + mapping);
                System.out.println("Route trouvée : " + urlMethod + " -> " + mapping);
            
                try {
                    Object controller = mapping.getControllerClass().getDeclaredConstructor().newInstance();
                    Method controllerMethod = mapping.getMethod();
                    Object result = controllerMethod.invoke(controller);

                    if (result != null) {
                        out.println("Resultat de la methode:\n");
                        out.println(result);
                        System.out.println(result);
                    }
                    
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                        | NoSuchMethodException e) {
                    throw new RuntimeException("Impossible d'exécuter la méthode liée à " + urlMethod, e);
                }
            } else {
                out.println("Aucune route trouvée pour l'URL : " + pathInfo);
                routesWithMethod.forEach((urlMethodKey, mapping) -> {
                    out.println(urlMethodKey + " -> " + mapping.getClassName() + "->" + mapping.getMethod().getName() + "()");
                });
            }

        }
    }
}
