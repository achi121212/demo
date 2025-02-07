package tw.com.demo.basic.handler;

import java.lang.reflect.Method;
import java.util.List;

import tw.com.demo.annotation.MyRequestMapping;
import tw.com.demo.annotation.MyRestController;
import tw.com.demo.annotation.MyTransaction;
import tw.com.demo.basic.MyApplicationContext;

public class TranscationHandler {
    
    private TranscationHandler() {}

    /**
     * scan所有Controller的function看有沒有Transaction控制
     * set裡面放url，方便filter尋找、避免method name重複
     */
    public static void handleTransaction(List<Class<?>> classList) {
        for (Class<?> clazz : classList) {
            System.out.println("handleTransaction start：" + clazz.getName());
            String classUrl = getClassUrl(clazz);
            if (clazz.isAnnotationPresent(MyTransaction.class)) {
                MyApplicationContext.getInstance().getTransactionalClass().add(classUrl);
            } else {
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(MyTransaction.class)) {
                        String methodUrl = getMethodUrl(method);
                        MyApplicationContext.getInstance().getTransactionalMethod().add(classUrl + methodUrl);
                    }
                }
            }
        }
    }
    
    
    private static String getClassUrl(Class<?> clazz) {
        MyRestController myRestController = clazz.getAnnotation(MyRestController.class);
        return myRestController.url();
    }
    
    private static String getMethodUrl(Method method) {
        MyRequestMapping myRequestMapping = method.getAnnotation(MyRequestMapping.class);
        return myRequestMapping.url();
    }
}
