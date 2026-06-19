package roro.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import roro.util.Mapping;

public class FrontControllerServlet extends HttpServlet {

    List<String> listeClasse = new ArrayList<>();

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

    Map<String, Mapping> routes;

    @Override
    public void init() throws ServletException {
        super.init();
        String packageName = "roro.app";
        String monAnnotation = "roro.annotation.MonController";
        String monAnnotation2 = "roro.annotation.UrlMapping";

        routes = roro.util.LoadingClass.loadUrlMappings(packageName, monAnnotation, monAnnotation2);
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

        String url = request.getRequestURL().toString();

        try (PrintWriter out = response.getWriter()) {
            out.println("URL interceptée : " + url);
            out.println("---Mon Framework Perso ---");

            // for (String classe : listeClasse) {
            // out.println(classe);
            // }

            String pathInfo = request.getRequestURI().substring(request.getContextPath().length());

            if (roro.util.LoadingClass.isARouteInsideMapping(pathInfo, routes)) {
                Mapping mapping = routes.get(pathInfo);
                out.println("Route trouvée : " + mapping);
            } else {
                out.println("Aucune route trouvée pour l'URL : " + pathInfo);
                for (Mapping mapping : routes.values()) {
                    out.println(mapping.getUrl() + " -> "
                            + mapping.getClassName() + "."
                            + mapping.getMethod().getName() + "()");
                }
            }

        }
    }
}