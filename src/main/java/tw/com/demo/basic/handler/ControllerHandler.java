package tw.com.demo.basic.handler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import tw.com.demo.annotation.MyRequestMapping;
import tw.com.demo.annotation.MyRestController;
import tw.com.demo.basic.MyApplicationContext;
import tw.com.demo.basic.MyServlet;
import tw.com.demo.basic.RestServlet;

public class ControllerHandler {
    
    private ControllerHandler() {}

    /**
     * 處理annotation：MyRestController、MyRequestMapping
     * @param sc
     * @param clazz
     * @throws Exception
     */
    public static void handleController(List<Class<?>> classList) throws Exception {
        for (Class<?> clazz : classList) {
            if (clazz.isAnnotationPresent(MyRestController.class)) {
                System.out.println("handleController start：" + clazz.getName());
                MyRestController myRestController = clazz.getAnnotation(MyRestController.class);
                String classUrl = myRestController.url();
                for (Method method : clazz.getDeclaredMethods()) {
                    MappingHandler.getInstance().addUrl(method, classUrl);
                }

                MyApplicationContext.getInstance().addBean(clazz.getName(), clazz.getDeclaredConstructor().newInstance());
            }
        }
    }
}
