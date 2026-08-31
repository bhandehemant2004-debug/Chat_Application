package Framework.ioc;

import Framework.annotations.Autowired;
import Framework.annotations.Component;
import Framework.annotations.RestController;
import Framework.annotations.Service;

import java.lang.reflect.Field;

import java.util.HashMap;
import java.util.Map;

public class ApplicationContext {

    private final Map<Class<?>, Object> beanRegistry = new HashMap<>();

    public ApplicationContext(Class<?>... componentClasses) {
        System.out.println("====== [IoC Container] Initializing Core Java ApplicationContext ======");
        // Step 1: Register and instantiate singletons
        for (Class<?> clazz : componentClasses) {
            if (isComponent(clazz)) {
                registerBean(clazz);
            }
        }

        // Step 2: Perform Dependency Injection (@Autowired)
        injectDependencies();
        System.out.println("====== [IoC Container] Initialization Complete (" + beanRegistry.size() + " Beans loaded) ======");
    }

    private boolean isComponent(Class<?> clazz) {
        return clazz.isAnnotationPresent(Component.class) ||
               clazz.isAnnotationPresent(Service.class) ||
               clazz.isAnnotationPresent(RestController.class);
    }

    private void registerBean(Class<?> clazz) {
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            beanRegistry.put(clazz, instance);
            System.out.println("[IoC Registry] Created Singleton Bean: " + clazz.getName());
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate Bean: " + clazz.getName(), e);
        }
    }

    private void injectDependencies() {
        for (Map.Entry<Class<?>, Object> entry : beanRegistry.entrySet()) {
            Object beanInstance = entry.getValue();
            Class<?> clazz = entry.getKey();

            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    Class<?> dependencyType = field.getType();
                    Object dependencyInstance = beanRegistry.get(dependencyType);

                    if (dependencyInstance == null) {
                        throw new RuntimeException("Unsatisfied dependency of type " + dependencyType.getName() + " for " + clazz.getName());
                    }

                    try {
                        field.setAccessible(true);
                        field.set(beanInstance, dependencyInstance);
                        System.out.println("[IoC Injector] Injected @Autowired dependency " + dependencyType.getSimpleName() + " -> " + clazz.getSimpleName() + "." + field.getName());
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Could not inject field: " + field.getName(), e);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> clazz) {
        return (T) beanRegistry.get(clazz);
    }

    public Map<Class<?>, Object> getAllBeans() {
        return beanRegistry;
    }
}
