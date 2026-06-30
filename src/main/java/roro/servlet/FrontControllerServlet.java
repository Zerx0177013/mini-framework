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

    Map<UrlMethod, Mapping> routesWithMethod;

    @SuppressWarnings("unchecked")
    @Override
    public void init() throws ServletException {
        super.init();
        routesWithMethod =(Map<UrlMethod, Mapping>) getServletContext().getAttribute("routesWithMethod");
    }

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

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("text/plain;charset=UTF-8");

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
