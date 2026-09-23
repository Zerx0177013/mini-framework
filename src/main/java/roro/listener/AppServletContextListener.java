package roro.listener;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.context.ApplicationContext;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import roro.util.LoadingClass;
import roro.util.Mapping;
import roro.util.UrlMethod; 
public class AppServletContextListener implements ServletContextListener {

    String packageName;
    String viewPrefix;
    String viewSuffix;
    String annotationRest;
    Map<UrlMethod, Mapping> toutesLesRoutes;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("[INIT] Tomcat démarre l'application. Lancement du scan des routes...");

        try {
            ApplicationContext springContext = org.springframework.web.context.support.WebApplicationContextUtils
            .getRequiredWebApplicationContext(sce.getServletContext());

            toutesLesRoutes = new HashMap<>();
            Properties prop = new Properties();
            try (InputStream input = LoadingClass.class.getClassLoader().getResourceAsStream("config.properties")) {
                if (input == null) {
                    throw new RuntimeException("[ERREUR] Impossible de trouver le fichier config.properties.");
                }
                prop.load(input);

                packageName = prop.getProperty("app.package");

            } catch (IOException e) {
                throw new RuntimeException("Erreur lors de la lecture de config.properties", e);
            }

            LoadingClass.loadUrlMappingsWithMethod(packageName, toutesLesRoutes);
            viewPrefix = sce.getServletContext().getInitParameter("view.prefix");
            viewSuffix = sce.getServletContext().getInitParameter("view.suffix");

            annotationRest = prop.getProperty("annotation.rest");

            sce.getServletContext().setAttribute("routesWithMethod", toutesLesRoutes);
            sce.getServletContext().setAttribute("prefix", viewPrefix);
            sce.getServletContext().setAttribute("suffix", viewSuffix);
            sce.getServletContext().setAttribute("springContext", springContext);
            sce.getServletContext().setAttribute("annotationRest", annotationRest);

            System.out.println("[SUCCESS] Scan terminé avec succès. " + toutesLesRoutes.size() + " routes chargées.");

        } catch (IllegalStateException e) {
            System.err.println("[ERREUR CRITIQUE DÉMARRAGE] " + e.getMessage());
            throw new RuntimeException("Échec du déploiement de l'application à cause d'un conflit de routes.", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("[SHUTDOWN] L'application s'arrête.");
    }
}