package tw.com.demo.basic;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.annotation.WebListener;
import tw.com.demo.annotation.MyRequestMapping;
import tw.com.demo.annotation.MyRestController;
import tw.com.demo.annotation.MyTransaction;

@WebListener
public class MyContextListener implements ServletContextListener  {
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext sc =  sce.getServletContext();
		System.out.println("contextInitialized start");
		// 建立自訂環境
		MyApplicationContext myctx = MyApplicationContext.getInstance();
		  
		String packageName = "tw.com.demo.controller";
		List<Class<?>> classList = null;
		try {
		    // 取得目標package classList
			classList = getAllClass(packageName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if (classList != null && !classList.isEmpty()) {
			for (Class<?> clazz : classList) {
				try {
					refreshContext(sc, myctx, clazz);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("contextInitialized end");
	}
	  
	private List<Class<?>> getAllClass(String packageName) throws ClassNotFoundException {
		System.out.println("getAllClass start");
		List<Class<?>> classList = new ArrayList<>();
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		String p = packageName.replace(".", "/");
		URL packageUrl = classLoader.getResource(p);
		File directory = new File(packageUrl.getFile());
		if (directory.exists() && directory.isDirectory()) {
			for (File file : directory.listFiles()) {
				String className = packageName + "." + file.getName().subSequence(0, file.getName().length() - 6);
				classList.add(Class.forName(className));
			}
		}
		System.out.println("getAllClass end");
		return classList;
	}

	private void refreshContext(ServletContext sc, MyApplicationContext mysc, Class<?> clazz) throws Exception {
	    System.out.println("refreshContext start");
		if (clazz.isAnnotationPresent(MyRestController.class)) {
			handleController(sc, clazz);
			handleTransaction(clazz);
		}
		mysc.addBean(clazz.getName(), clazz.getDeclaredConstructor().newInstance());
		System.out.println("refreshContext end");
	}

	/**
	 * 處理annotation：MyRestController、MyRequestMapping
	 * @param sc
	 * @param clazz
	 * @throws Exception
	 */
	private void handleController(ServletContext sc, Class<?> clazz) throws Exception {
		System.out.println("handleController start：" + clazz.getName());
		MyRestController myRestController = clazz.getAnnotation(MyRestController.class);
		String classUrl = myRestController.url();
		for (Method method : clazz.getDeclaredMethods()) {
			Map<String, MyServlet> servletMap = new HashMap<>();  // 因為要處理同url的情況，將所有url+method存到map後再addMapping
			if (method.isAnnotationPresent(MyRequestMapping.class)) {
				MyRequestMapping myRequestMapping = method.getAnnotation(MyRequestMapping.class);
				String url = classUrl + myRequestMapping.url();
				String type = myRequestMapping.type();
				if (servletMap.containsKey(url)) {
					servletMap.get(url).setMethodByType(type, method);						
				} else {
					MyServlet myservlet = new MyServlet(url);
					myservlet.setMethodByType(type, method);
					servletMap.put(url, myservlet);
				} 
			}
			servletMap.forEach((key, value) -> {
				ServletRegistration.Dynamic servletDynamic = sc.addServlet(key, new RestServlet(value.getGet(), value.getPost(), value.getPut(), value.getDelete()));
				servletDynamic.addMapping(key);
			});
		}
	}
	
	
	/**
	 * scan所有Controller的function看有沒有Transaction控制
	 * set裡面放url，方便filter尋找、避免method name重複
	 */
	private void handleTransaction(Class<?> clazz) {
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
	
	private String getClassUrl(Class<?> clazz) {
		MyRestController myRestController = clazz.getAnnotation(MyRestController.class);
		return myRestController.url();
	}
	
	private String getMethodUrl(Method method) {
		MyRequestMapping myRequestMapping = method.getAnnotation(MyRequestMapping.class);
		return myRequestMapping.url();
	}
	
}
