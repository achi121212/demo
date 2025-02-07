package tw.com.demo.basic;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import tw.com.demo.annotation.MyRestController;
import tw.com.demo.basic.handler.ControllerHandler;
import tw.com.demo.basic.handler.MappingHandler;
import tw.com.demo.basic.handler.TranscationHandler;

@WebListener
public class MyContextListener implements ServletContextListener  {
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext sc =  sce.getServletContext();
		System.out.println("contextInitialized start");
		// 建立自訂環境
		MyApplicationContext.getInstance();
		  
		String packageName = "tw.com.demo.controller";
		List<Class<?>> classList = null;
		try {
		    // 取得目標package classList
			classList = getAllClass(packageName);
			if (!classList.isEmpty()) {
			    refreshContext(sc, classList);
			}
		} catch (Exception e) {
            e.printStackTrace();
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

	private void refreshContext(ServletContext sc, List<Class<?>> classList) throws Exception {
	    System.out.println("refreshContext start");
        ControllerHandler.handleController(classList);
        MappingHandler.getInstance().handleMapping(sc);
        TranscationHandler.handleTransaction(classList);
		System.out.println("refreshContext end");
	}
	
}
