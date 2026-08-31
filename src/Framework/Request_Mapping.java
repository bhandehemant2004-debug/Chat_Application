package Framework;

import Framework.annotations.GetMapping;
import Framework.annotations.PostMapping;
import Framework.annotations.RestController;
import Framework.ioc.ApplicationContext;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Request_Mapping {

    public static Map<String, Method> map = new HashMap<>();
    public static Map<Method, Object> controllerInstanceMap = new HashMap<>();
    private static ApplicationContext context;

    public static void initializeIoC(ApplicationContext applicationContext) {
        context = applicationContext;
        map.clear();
        controllerInstanceMap.clear();

        for (Map.Entry<Class<?>, Object> entry : context.getAllBeans().entrySet()) {
            Class<?> clazz = entry.getKey();
            Object controllerInstance = entry.getValue();

            if (clazz.isAnnotationPresent(RestController.class)) {
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(PostMapping.class)) {
                        PostMapping post = method.getAnnotation(PostMapping.class);
                        String key = "POST|" + post.value();
                        map.put(key, method);
                        controllerInstanceMap.put(method, controllerInstance);
                        System.out.println("[RouteRegistry] Registered @PostMapping Route: " + key + " -> " + clazz.getSimpleName() + "." + method.getName() + "()");
                    }
                    if (method.isAnnotationPresent(GetMapping.class)) {
                        GetMapping get = method.getAnnotation(GetMapping.class);
                        String key = "GET|" + get.value();
                        map.put(key, method);
                        controllerInstanceMap.put(method, controllerInstance);
                        System.out.println("[RouteRegistry] Registered @GetMapping Route: " + key + " -> " + clazz.getSimpleName() + "." + method.getName() + "()");
                    }
                }
            }
        }
    }

    public static Method getMethod(String key) {
        return map.get(key);
    }

    public static Object getControllerInstance(Method method) {
        return controllerInstanceMap.get(method);
    }
}
