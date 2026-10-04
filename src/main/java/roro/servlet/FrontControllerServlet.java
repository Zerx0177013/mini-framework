package roro.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import roro.util.Binder;
import roro.util.Mapping;
import roro.util.ModAndView;
import roro.util.UrlMethod;
public class FrontControllerServlet extends HttpServlet {

    Map<UrlMethod, Mapping> routesWithMethod;
    String viewPrefix;
    String viewSuffix;
    String annotationRest;
    ApplicationContext springContext;

    @SuppressWarnings("unchecked")
    @Override
    public void init() throws ServletException {
        super.init();
        routesWithMethod = (Map<UrlMethod, Mapping>) getServletContext().getAttribute("routesWithMethod");
        viewPrefix = (String) getServletContext().getAttribute("prefix");
        viewSuffix = (String) getServletContext().getAttribute("suffix");
        annotationRest = (String) getServletContext().getAttribute("annotationRest");
        springContext = (ApplicationContext) getServletContext().getAttribute("springContext");
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
            throws IOException, ServletException {
        String pathInfo = request.getRequestURI().substring(request.getContextPath().length());
        UrlMethod urlMethod = new UrlMethod(pathInfo, request.getMethod());

        if (roro.util.LoadingClass.isARouteInsideMappingWithMethod(urlMethod, routesWithMethod)) {
            Mapping mapping = routesWithMethod.get(urlMethod);
            System.out.println("Route trouvée : " + urlMethod + " -> " + mapping);

            try {
                Object controller = mapping.getControllerClass().getDeclaredConstructor().newInstance();
                Method controllerMethod = mapping.getMethod();
                Class<?>[] parameterTypes = controllerMethod.getParameterTypes();
                Object[] parameters = new Object[parameterTypes.length];
                for (int i = 0; i < parameterTypes.length; i++) {
                    Class<?> paramType = parameterTypes[i];

                    if (paramType.equals(ApplicationContext.class)) {
                        parameters[i] = springContext;
                    } else if (paramType.equals(HttpServletRequest.class)) {
                        parameters[i] = request;
                    } else if (paramType.equals(HttpServletResponse.class)) {
                        parameters[i] = response;
                    } else if (paramType.equals(String.class)) {
                        String paramName = controllerMethod.getParameters()[i].getName();
                        parameters[i] = request.getParameter(paramName);
                    } else if (paramType.equals(int.class) || paramType.equals(Integer.class)) {
                        String paramName = controllerMethod.getParameters()[i].getName();
                        String paramValue = request.getParameter(paramName);
                        int value = 0;
                        if (paramValue != null) {
                            value = Integer.parseInt(paramValue);
                        }
                        parameters[i] = value;
                    } else if (!paramType.isPrimitive() || !paramType.getName().startsWith("java.")) {
                        try {
                            parameters[i] = Binder.bind(paramType, request);
                        } catch (Exception e) {
                            throw new ServletException("Erreur lors de la liaison des paramètres pour " + urlMethod, e);
                        }
                    } else {
                        parameters[i] = null;
                    }
                }
                Object result = controllerMethod.invoke(controller, parameters);

                if (result instanceof ModAndView mav) {
                    if (mav.getValues() != null) {
                        for (Map.Entry<String, List<?>> en : mav.getValues().entrySet()) {
                            request.setAttribute(en.getKey(), en.getValue());
                        }
                    }
                    if (mav.getView() != null && !mav.getView().isBlank()) {
                        String viewPath = viewPrefix + mav.getView() + viewSuffix;
                        RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
                        dispatcher.forward(request, response);
                        return;
                    }

                    throw new ServletException("Aucune vue définie pour " + urlMethod);
                } else if (result instanceof String text) {
                    response.setContentType("text/plain;charset=UTF-8");
                    try {
                        Class<? extends Annotation> restAnnotationClass = Class.forName(annotationRest)
                                .asSubclass(Annotation.class);

                        if (mapping.getMethod().isAnnotationPresent(restAnnotationClass)) {
                            response.setContentType("application/json;charset=UTF-8");
                        }
                    } catch (ClassNotFoundException e) {
                        throw new ServletException("Annotation REST introuvable : " + annotationRest, e);
                    }
                    try (PrintWriter out = response.getWriter()) {
                        out.println(text);
                    }
                    return;
                } else {
                    ObjectMapper objectMapper = new ObjectMapper();
                    response.setContentType("application/json;charset=UTF-8");
                    try (PrintWriter out = response.getWriter()) {
                        String json = objectMapper.writeValueAsString(result);
                        out.println(json);
                    }
                }

                throw new ServletException(
                        "Type de retour non supporté pour " + urlMethod + " : " + result.getClass().getName());

            } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                    | NoSuchMethodException e) {
                throw new RuntimeException("Impossible d'exécuter la méthode liée à " + urlMethod, e);
            }
        } else {
            response.setContentType("text/plain;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.println("Aucune route trouvée pour l'URL : " + pathInfo);
                routesWithMethod.forEach((urlMethodKey, mapping) -> {
                    out.println(urlMethodKey + " -> " + mapping.getClassName() + "->" + mapping.getMethod().getName()
                            + "()");
                });
            }
        }
    }
}
