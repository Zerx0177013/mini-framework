package roro.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontControllerServlet extends HttpServlet {

    List<String> listeClasse = new ArrayList<>();

    @Override
    public void init() throws ServletException {
        super.init();
        String packageName = "roro.app";
        String monAnnotation = "roro.annotation.MonController";
        listeClasse = roro.util.LoadingClass.loadClassWithMyAnnotation(packageName, monAnnotation);
        // listeClasse = roro.util.LoadingClass.loadAllClasses();
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

        // String url = request.getRequestURL().toString();

        try (PrintWriter out = response.getWriter()) {
            out.println("---Mon Framework Perso ---");
            // out.println("URL interceptée : " + url);

            for (String classe : listeClasse) {
                out.println(classe);
            }
        }
    }
}