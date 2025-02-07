package tw.com.demo.basic.handler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import tw.com.demo.annotation.MyRequestMapping;
import tw.com.demo.basic.MyServlet;
import tw.com.demo.basic.RestServlet;

public class MappingHandler {
    
    private static volatile MappingHandler instance;
    private Map<String, MyServlet> urlMap;
    
    private MappingHandler() {
        urlMap = new HashMap<>();
    }
    
    public static MappingHandler getInstance() {
        if (instance == null) {
            synchronized (MappingHandler.class) {
                if (instance == null) {
                    instance = new MappingHandler();
                }
            }
        }
        return instance;
    }

    /**
     * 將url加入 urlMap中
     * @param method
     * @param classUrl
     */
    public void addUrl(Method method, String classUrl) {
        if (method.isAnnotationPresent(MyRequestMapping.class)) {
            MyRequestMapping myRequestMapping = method.getAnnotation(MyRequestMapping.class);
            String url = classUrl + myRequestMapping.url();
            String type = myRequestMapping.type();
            
            if (urlMap.containsKey(url)) {
                urlMap.get(url).setMethodByType(type, method);                      
            } else {
                MyServlet myservlet = new MyServlet(url);
                myservlet.setMethodByType(type, method);
                urlMap.put(url, myservlet);
            } 
        }
    }
    
    /**
     * 處理Mapping
     * @param sc
     */
    public void handleMapping(ServletContext sc) {
        this.urlMap.forEach((key, value) -> {
            ServletRegistration.Dynamic servletDynamic = sc.addServlet(key, new RestServlet(value.getGet(), value.getPost(), value.getPut(), value.getDelete()));
            servletDynamic.addMapping(key);
        });
    }
    
}
