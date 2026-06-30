package roro.listener;

import java.util.Map;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import roro.util.Mapping;
import roro.util.UrlMethod;

@WebListener
public class AppServletContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("[INIT] Tomcat démarre l'application. Lancement du scan des routes...");

        try {
            String packageName = "roro.app";
            String monAnnotation = "roro.annotation.MonController";
            String monAnnotation2 = "roro.annotation.UrlMapping";
            Map<UrlMethod, Mapping> toutesLesRoutes= roro.util.LoadingClass.loadUrlMappingsWithMethod(packageName, monAnnotation,
                    monAnnotation2);

            sce.getServletContext().setAttribute("routesWithMethod", toutesLesRoutes);

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