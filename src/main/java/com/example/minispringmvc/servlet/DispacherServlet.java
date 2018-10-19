package com.example.minispringmvc.servlet;

import com.example.minispringmvc.annotation.*;
import com.example.minispringmvc.controller.UserController;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "dispatcherServlet", urlPatterns = "/*", loadOnStartup = 1, initParams =
        {@WebInitParam(name = "base-package", value = "com.example.minispringmvc")})
public class DispacherServlet extends HttpServlet {
    private String basePackage = "";

    private List<String> packageNames = new ArrayList<String>();

    //注解上的名字：实例化对象
    private Map<String, Object> instanceMap = new HashMap<String, Object>();

    //带包路径的全限定名：注解上的名字
    private Map<String, String> nameMap = new HashMap<String, String>();

    //url路径名：方法
    private Map<String, Method> urlMethodMap = new HashMap<String, Method>();

    //Method:全限定类名
    private Map<Method, String> methodPackageMap = new HashMap<Method, String>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        basePackage = config.getInitParameter("base-package");


        try {
            //扫描基包，得到包路径下的全部全限名
            scanBasePackage(basePackage);

            instance(packageNames);

            springIOC();

            handlerUrlMethodMap();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }


    }

    private void handlerUrlMethodMap() throws ClassNotFoundException {
        if (packageNames.size() < 1) {
            return;
        }
        for (String string : packageNames) {

            Class c = Class.forName(string);
            if (c.isAnnotationPresent(Controller.class)) {

                Method[] methods = c.getMethods();
                StringBuffer baseUrl = new StringBuffer();
                if (c.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping requestMapping = (RequestMapping) c.getAnnotation(RequestMapping.class);
                    baseUrl.append(requestMapping.value());
                }

                for (Method method : methods) {
                    if (method.isAnnotationPresent(RequestMapping.class)) {
                        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                        baseUrl.append(requestMapping.value());
                        urlMethodMap.put(baseUrl.toString(), method);
                        methodPackageMap.put(method, string);
                    }
                }
            }
        }
    }

    private void springIOC() {
        instanceMap.forEach((packageName, object) -> {
            Field[] fields = object.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Qualifier.class)) {
                    String name = field.getAnnotation(Qualifier.class).value();
                    field.setAccessible(true);
                    try {
                        field.set(object, instanceMap.get(name));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    private void instance(List<String> packageNames) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (packageNames.size() < 1) {
            return;
        }

        for (String string : packageNames) {

            Class c = Class.forName(string);
            if (c.isAnnotationPresent(Controller.class)) {
                Controller controller = (Controller) c.getAnnotation(Controller.class);
                String controllerName = controller.value();
                instanceMap.put(controllerName, c.newInstance());
                nameMap.put(string, controllerName);
                System.out.println("Controller ：" + string + " , value：" + controller.value());

            } else if (c.isAnnotationPresent(Service.class)) {
                Service service = (Service) c.getAnnotation(Service.class);
                String serviceName = service.value();
                instanceMap.put(serviceName, c.newInstance());
                nameMap.put(string, serviceName);
                System.out.println("Service ：" + string + " , value：" + service.value());

            } else if (c.isAnnotationPresent(Repository.class)) {
                Repository repository = (Repository) c.getAnnotation(Repository.class);
                String repositoryName = repository.value();
                instanceMap.put(repositoryName, c.newInstance());
                nameMap.put(string, repositoryName);
                System.out.println("Repository ：" + string + " , value：" + repository.value());
            }
        }

    }

    private void scanBasePackage(String basePackage) {

        URL url = this.getClass().getClassLoader().getResource(basePackage.replaceAll("\\.", "/"));
        File basePackageFile = new File(url.getPath());
        System.out.println("scan: " + basePackageFile);
        File[] childFiles = basePackageFile.listFiles();
        for (File file : childFiles) {
            if (file.isDirectory()) {
                scanBasePackage(basePackage + "." + file.getName());
            } else if (file.isFile()) {
                packageNames.add(basePackage + "." + file.getName().split("\\.")[0]);
            }
        }
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = uri.replaceAll(contextPath, "");


        Method method = urlMethodMap.get(path);
        if (method != null) {
            String pakcageName = methodPackageMap.get(method);
            String controllerName = nameMap.get(pakcageName);
            UserController userController = (UserController) instanceMap.get(controllerName);

            method.setAccessible(true);
            try {
                method.invoke(userController);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }


    }
}
